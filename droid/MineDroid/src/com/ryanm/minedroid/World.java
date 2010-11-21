
package com.ryanm.minedroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.os.Environment;

import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Frustum.Result;
import com.ryanm.droid.rugl.util.geom.ReadableVector3f;
import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * Manages loading chunks, decides which chunklets to render
 * 
 * @author ryanm
 */
public class World
{
	/**
	 * The world save directory
	 */
	public final File dir;

	/**
	 * The size of the square of chunks around the player that we try
	 * to load
	 */
	private int width = 9, depth = 9;

	private List<Chunk> chunks = new ArrayList<Chunk>();

	private int chunkPosX, chunkPosZ;

	private Queue<Chunklet> floodQueue = new ArrayBlockingQueue<Chunklet>( 50 );

	private ArrayList<Chunklet> renderList = new ArrayList<Chunklet>();

	private int drawFlag = Integer.MIN_VALUE;

	/**
	 * The player position, as read from level.dat
	 */
	public final ReadableVector3f startPosition;

	private static final ChunkSorter cs = new ChunkSorter();

	private Renderer renderer = new Renderer();

	/**
	 * @param n
	 * @throws IOException
	 *            If something bad happens when we read level.dat
	 */
	public World( int n ) throws IOException
	{
		dir =
				new File( Environment.getExternalStorageDirectory(), ".minecraft/saves/World"
						+ n );

		Tag level = Tag.readFrom( new FileInputStream( new File( dir, "level.dat" ) ) );
		Tag player = level.findTagByName( "Player" );
		Tag pos = player.findTagByName( "Pos" );

		Tag[] tl = ( Tag[] ) pos.getValue();
		Vector3f p = new Vector3f();
		p.x = ( ( Double ) tl[ 0 ].getValue() ).floatValue() / 16.0f;
		p.y = ( ( Double ) tl[ 1 ].getValue() ).floatValue() / 16.0f;
		p.z = ( ( Double ) tl[ 2 ].getValue() ).floatValue() / 16.0f;

		startPosition = p;

		chunkPosX = ( int ) startPosition.getX();
		chunkPosZ = ( int ) startPosition.getZ();

		fillChunks();

		// it will often be the case that we want to render the same
		// chunklets frame after frame, so no sense repeatedly filling
		// the buffers with the same data
		renderer.automaticallyClear = false;
	}

	/**
	 * Tries to ensure we have the right chunks loaded
	 * 
	 * @param posX
	 * @param posZ
	 */
	public void advance( float posX, float posZ )
	{
		boolean chunksDirty = false;

		int cx = ( int ) Math.floor( posX );
		if( cx != chunkPosX )
		{
			chunkPosX = cx;
			chunksDirty = true;
		}

		int cz = ( int ) Math.floor( posZ );
		if( cz != chunkPosZ )
		{
			chunkPosZ = cz;
			chunksDirty = true;
		}

		if( chunksDirty )
		{ // load new chunks
			fillChunks();
		}

		// free extraneous, far-away chunks for GC
		while( chunks.size() > 100 )
		{
			int furthest = -1;
			float distance = -1;
			for( int i = 0; i < chunks.size(); i++ )
			{
				Chunk c = chunks.get( i );
				float dx = c.x - posX;
				float dz = c.z - posZ;
				float d = dx * dx + dz * dz;

				if( d > distance )
				{
					distance = d;
					furthest = i;
				}
			}

			Chunk chunk = chunks.remove( furthest );
			chunk.unload();
		}
	}

	/**
	 * @param eye
	 * @param frustum
	 */
	public void draw( Vector3f eye, Frustum frustum )
	{
		Chunklet c = getChunklet( eye.x, eye.y, eye.z );
		float distlimit = 25;

		if( c != null )
		{
			Chunklet origin = c;
			c.drawFlag = drawFlag;
			floodQueue.offer( c );

			while( !floodQueue.isEmpty() )
			{
				c = floodQueue.poll();

				renderList.add( c );

				// floodfill - for each neighbouring chunklet...
				if( c.x <= origin.x && !c.northSheet )
				// we are not reversing flood direction and we can see
				// through that face of this chunk
				{
					Chunklet north = getChunklet( c.x - 1, c.y, c.z );
					if( north != null
					// neighbouring chunk exists
							&& !north.southSheet
							// we can see through the traversal face
							&& north.drawFlag != drawFlag
							// we haven't already visited it in this frame
							&& north.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							// it is within the distance limit
							&& north.intersection( frustum ) != Result.Miss )
					// it intersects the frustum
					{
						north.drawFlag = drawFlag;
						floodQueue.offer( north );
					}
				}
				if( c.x >= origin.x && !c.southSheet )
				{
					Chunklet south = getChunklet( c.x + 1, c.y, c.z );
					if( south != null && !south.northSheet && south.drawFlag != drawFlag
							&& south.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							&& south.intersection( frustum ) != Result.Miss )
					{
						south.drawFlag = drawFlag;
						floodQueue.offer( south );
					}
				}
				if( c.z <= origin.z && !c.eastSheet )
				{
					Chunklet east = getChunklet( c.x, c.y, c.z - 1 );
					if( east != null && !east.westSheet && east.drawFlag != drawFlag
							&& east.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							&& east.intersection( frustum ) != Result.Miss )
					{
						east.drawFlag = drawFlag;
						floodQueue.offer( east );
					}
				}
				if( c.z >= origin.z && !c.westSheet )
				{
					Chunklet west = getChunklet( c.x, c.y, c.z + 1 );
					if( west != null && !west.eastSheet && west.drawFlag != drawFlag
							&& west.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							&& west.intersection( frustum ) != Result.Miss )
					{
						west.drawFlag = drawFlag;
						floodQueue.offer( west );
					}
				}
				if( c.y <= origin.y && !c.bottomSheet )
				{
					Chunklet bottom = getChunklet( c.x, c.y - 1, c.z );
					if( bottom != null && !bottom.topSheet && bottom.drawFlag != drawFlag
							&& bottom.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							&& bottom.intersection( frustum ) != Result.Miss )
					{
						bottom.drawFlag = drawFlag;
						floodQueue.offer( bottom );
					}
				}
				if( c.y >= origin.y && !c.topSheet )
				{
					Chunklet top = getChunklet( c.x, c.y + 1, c.z );
					if( top != null && !top.bottomSheet && top.drawFlag != drawFlag
							&& top.distanceSq( eye.x, eye.y, eye.z ) < distlimit
							&& top.intersection( frustum ) != Result.Miss )
					{
						top.drawFlag = drawFlag;
						floodQueue.offer( top );
					}
				}
			}
		}

		// sort chunklets into ascending order of distance from the eye
		cs.eye.set( eye );
		Collections.sort( renderList, cs );

		// vbo
		// solid stuff from near to far
		for( int i = 0; i < renderList.size(); i++ )
		{
			c = renderList.get( i );
			c.generateGeometry();

			if( c.solidVBO != null )
			{
				c.solidVBO.draw();
			}
		}

		// translucent stuff from far to near
		for( int i = renderList.size() - 1; i >= 0; i-- )
		{
			c = renderList.get( i );
			if( c.transparentVBO != null )
			{
				c.transparentVBO.draw();
			}
		}

		renderList.clear();
		drawFlag++;
	}

	private void fillChunks()
	{
		for( int i = 0; i < width; i++ )
		{
			for( int j = 0; j < depth; j++ )
			{
				final int x = chunkPosX + i - ( int ) ( width / 2.0f );
				final int z = chunkPosZ + j - ( int ) ( depth / 2.0f );

				if( getChunk( x, z ) == null )
				{
					ResourceLoader.load( new ChunkLoader( this, x, z ) {
						@Override
						public void complete()
						{
							if( resource != null )
							{
								chunks.add( resource );

								// need to re-evaluate the geometry of
								// neighbouring chunks
								Chunk c;
								if( ( c = getChunk( x - 1, z ) ) != null )
								{
									c.geomDirty();
								}
								if( ( c = getChunk( x + 1, z ) ) != null )
								{
									c.geomDirty();
								}
								if( ( c = getChunk( x, z - 1 ) ) != null )
								{
									c.geomDirty();
								}
								if( ( c = getChunk( x, z + 1 ) ) != null )
								{
									c.geomDirty();
								}
							}
						}
					} );
				}
			}
		}
	}

	/**
	 * Gets a loaded chunk
	 * 
	 * @param x
	 * @param z
	 * @return the so-indexed chunk, or <code>null</code> if it does
	 *         not exist
	 */
	public Chunk getChunk( int x, int z )
	{
		// this is just awful, change to some spatial data structure
		// ASAP

		for( int i = 0; i < chunks.size(); i++ )
		{
			Chunk c = chunks.get( i );

			if( c != null && c.x == x && c.z == z )
			{
				return c;
			}
		}

		return null;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The chunklet that contains that point
	 */
	public Chunklet getChunklet( float x, float y, float z )
	{
		Chunk chunk = getChunk( ( int ) Math.floor( x ), ( int ) Math.floor( z ) );

		if( chunk != null && y >= 0 && y < 8 )
		{
			int yi = ( int ) Math.floor( y );

			return chunk.chunklets[ yi ];
		}

		return null;
	}

	private static class ChunkSorter implements Comparator<Chunklet>
	{
		private final Vector3f eye = new Vector3f();

		@Override
		public int compare( Chunklet a, Chunklet b )
		{
			float ad = a.distanceSq( eye.x, eye.y, eye.z );
			float bd = b.distanceSq( eye.x, eye.y, eye.z );
			return ( int ) Math.signum( ad - bd );
		}
	}

}
