
package com.ryanm.minedroid;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.gl.MutableState;
import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.util.QuickSort;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Frustum.Result;
import com.ryanm.droid.rugl.util.geom.ReadableVector3f;
import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * Manages loading chunks, decides which chunklets to render
 * 
 * @author ryanm
 */
@Variable( "World" )
@Summary( "Controls chunk loading and render state" )
public class World
{
	/***/
	@Variable( "Render state" )
	public MutableState muState;

	/***/
	@Variable( "Draw chunklets" )
	public boolean drawChunklets = true;

	/***/
	@Variable( "Outline chunklets" )
	public boolean drawOutlines = false;

	/**
	 * For drawing the wireframes
	 */
	private Renderer renderer = new Renderer();

	/**
	 * The world save directory
	 */
	public final File dir;

	private int loadradius = 4;

	/**
	 * 1st index = x, 2nd = z
	 */
	private Chunk[][] chunks =
			new Chunk[ 2 * getLoadRadius() + 1 ][ 2 * getLoadRadius() + 1 ];

	/**
	 * Coordinates of the currently-occupied chunk
	 */
	private int chunkPosX, chunkPosZ;

	private Queue<Chunklet> floodQueue = new ArrayBlockingQueue<Chunklet>( 50 );

	// we're not using ArrayList here because Collections.sort creates
	// garbage. Hopefully Arrays.sort does not
	private Chunklet[] renderList = new Chunklet[ 64 ];

	private int renderListSize = 0;

	/**
	 * Number of chunklets rendered in the last draw call
	 */
	public static int renderedChunklets = 0;

	private int drawFlag = Integer.MIN_VALUE;

	/**
	 * The player position, as read from level.dat
	 */
	public final ReadableVector3f startPosition;

	private static final ChunkSorter cs = new ChunkSorter();

	/**
	 * @param dir
	 * @param startPosition
	 */
	public World( File dir, Vector3f startPosition )
	{
		this.dir = dir;
		this.startPosition = startPosition;

		chunkPosX = ( int ) startPosition.getX() / 16;
		chunkPosZ = ( int ) startPosition.getZ() / 16;

		fillChunks();
	}

	/**
	 * Tries to ensure we have the right chunks loaded
	 * 
	 * @param posX
	 *           player position
	 * @param posZ
	 *           player position
	 */
	public void advance( float posX, float posZ )
	{
		boolean chunksDirty = false;

		int cx = ( int ) Math.floor( posX / 16 );
		if( cx < chunkPosX )
		{
			shiftUpX();
			chunkPosX--;
			chunksDirty = true;
		}
		else if( cx > chunkPosX )
		{
			shiftDownX();
			chunkPosX++;
			chunksDirty = true;
		}

		int cz = ( int ) Math.floor( posZ / 16 );
		if( cz < chunkPosZ )
		{
			shiftUpZ();
			chunkPosZ--;
			chunksDirty = true;
		}
		else if( cz > chunkPosZ )
		{
			shiftDownZ();
			chunkPosZ++;
			chunksDirty = true;
		}

		if( chunksDirty )
		{ // load new chunks
			fillChunks();
		}
	}

	private void shiftDownX()
	{
		// free bottom x
		Chunk[] swap = chunks[ 0 ];
		for( int i = 0; i < swap.length; i++ )
		{
			if( swap[ i ] != null )
			{
				swap[ i ].unload();
				swap[ i ] = null;
			}
		}

		// shift down
		for( int i = 0; i < chunks.length - 1; i++ )
		{
			chunks[ i ] = chunks[ i + 1 ];
		}

		// add swap
		chunks[ chunks.length - 1 ] = swap;
	}

	private void shiftUpX()
	{
		// free top x
		Chunk[] swap = chunks[ chunks.length - 1 ];
		for( int i = 0; i < swap.length; i++ )
		{
			if( swap[ i ] != null )
			{
				swap[ i ].unload();
				swap[ i ] = null;
			}
		}

		// shift up
		for( int i = chunks.length - 1; i > 0; i-- )
		{
			chunks[ i ] = chunks[ i - 1 ];
		}

		// add swap
		chunks[ 0 ] = swap;
	}

	private void shiftDownZ()
	{
		for( int i = 0; i < chunks.length; i++ )
		{
			if( chunks[ i ][ 0 ] != null )
			{
				chunks[ i ][ 0 ].unload();
			}

			for( int j = 0; j < chunks[ i ].length - 1; j++ )
			{
				chunks[ i ][ j ] = chunks[ i ][ j + 1 ];
			}

			chunks[ i ][ chunks[ i ].length - 1 ] = null;
		}
	}

	private void shiftUpZ()
	{
		for( int i = 0; i < chunks.length; i++ )
		{
			if( chunks[ i ][ chunks[ i ].length - 1 ] != null )
			{
				chunks[ i ][ chunks[ i ].length - 1 ].unload();
			}

			for( int j = chunks[ i ].length - 1; j > 0; j-- )
			{
				chunks[ i ][ j ] = chunks[ i ][ j - 1 ];
			}

			chunks[ i ][ 0 ] = null;
		}
	}

	/**
	 * @param eye
	 * @param frustum
	 */
	public void draw( Vector3f eye, Frustum frustum )
	{
		if( muState == null )
		{
			muState = new MutableState( BlockFactory.state );
		}

		if( muState.dirty )
		{ // the rendering state has been changed by configuration
			BlockFactory.state = muState.compile();
			muState.dirty = false;
		}

		Chunklet c = getChunklet( eye.x, eye.y, eye.z );

		if( c != null )
		{
			Chunklet origin = c;
			c.drawFlag = drawFlag;
			floodQueue.offer( c );

			while( !floodQueue.isEmpty() )
			{
				c = floodQueue.poll();

				renderList[ renderListSize++ ] = c;

				if( renderListSize >= renderList.length )
				{ // grow
					Chunklet[] nrl = new Chunklet[ renderList.length * 2 ];
					System.arraycopy( renderList, 0, nrl, 0, renderList.length );
					renderList = nrl;
				}

				// floodfill - for each neighbouring chunklet...
				if( c.x <= origin.x && !c.northSheet )
				// we are not reversing flood direction and we can see
				// through that face of this chunk
				{
					Chunklet north = getChunklet( c.x - 16, c.y, c.z );
					if( north != null
					// neighbouring chunk exists
							&& !north.southSheet
							// we can see through the traversal face
							&& north.drawFlag != drawFlag
							// we haven't already visited it in this frame
							&& north.intersection( frustum ) != Result.Miss )
					// it intersects the frustum
					{
						north.drawFlag = drawFlag;
						floodQueue.offer( north );
					}
				}
				if( c.x >= origin.x && !c.southSheet )
				{
					Chunklet south = getChunklet( c.x + 16, c.y, c.z );
					if( south != null && !south.northSheet && south.drawFlag != drawFlag
							&& south.intersection( frustum ) != Result.Miss )
					{
						south.drawFlag = drawFlag;
						floodQueue.offer( south );
					}
				}
				if( c.z <= origin.z && !c.eastSheet )
				{
					Chunklet east = getChunklet( c.x, c.y, c.z - 16 );
					if( east != null && !east.westSheet && east.drawFlag != drawFlag
							&& east.intersection( frustum ) != Result.Miss )
					{
						east.drawFlag = drawFlag;
						floodQueue.offer( east );
					}
				}
				if( c.z >= origin.z && !c.westSheet )
				{
					Chunklet west = getChunklet( c.x, c.y, c.z + 16 );
					if( west != null && !west.eastSheet && west.drawFlag != drawFlag
							&& west.intersection( frustum ) != Result.Miss )
					{
						west.drawFlag = drawFlag;
						floodQueue.offer( west );
					}
				}
				if( c.y <= origin.y && !c.bottomSheet )
				{
					Chunklet bottom = getChunklet( c.x, c.y - 16, c.z );
					if( bottom != null && !bottom.topSheet && bottom.drawFlag != drawFlag
							&& bottom.intersection( frustum ) != Result.Miss )
					{
						bottom.drawFlag = drawFlag;
						floodQueue.offer( bottom );
					}
				}
				if( c.y >= origin.y && !c.topSheet )
				{
					Chunklet top = getChunklet( c.x, c.y + 16, c.z );
					if( top != null && !top.bottomSheet && top.drawFlag != drawFlag
							&& top.intersection( frustum ) != Result.Miss )
					{
						top.drawFlag = drawFlag;
						floodQueue.offer( top );
					}
				}
			}
		}

		renderedChunklets = 0;

		// sort chunklets into ascending order of distance from the eye
		cs.eye.set( eye );
		QuickSort.sort( renderList, cs, 0, renderListSize - 1 );

		// vbo
		// solid stuff from near to far
		for( int i = 0; i < renderListSize; i++ )
		{
			c = renderList[ i ];
			c.generateGeometry();

			if( c.solidVBO != null && drawChunklets )
			{
				c.solidVBO.state = BlockFactory.state;
				c.solidVBO.draw();

				renderedChunklets++;
			}
		}

		// translucent stuff from far to near
		for( int i = renderListSize - 1; i >= 0; i-- )
		{
			c = renderList[ i ];
			if( c.transparentVBO != null && drawChunklets )
			{
				c.transparentVBO.state = BlockFactory.state;
				c.transparentVBO.draw();
			}
		}

		if( drawOutlines )
		{
			for( int i = 0; i < renderListSize; i++ )
			{
				renderList[ i ].drawOutline( renderer );
			}
			renderer.render();
		}

		Arrays.fill( renderList, null );
		renderListSize = 0;
		drawFlag++;
	}

	private void fillChunks()
	{
		for( int i = 0; i < chunks.length; i++ )
		{
			for( int j = 0; j < chunks[ i ].length; j++ )
			{
				final int caix = i, caiz = j;
				final int x = chunkPosX + i - getLoadRadius();
				final int z = chunkPosZ + j - getLoadRadius();

				if( getChunk( x, z ) == null )
				{
					ResourceLoader.load( new ChunkLoader( this, x, z ) {
						@Override
						public void complete()
						{
							if( resource != null )
							{
								chunks[ caix ][ caiz ] = resource;

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
		int dx = x - chunkPosX;
		int dz = z - chunkPosZ;
		int caix = getLoadRadius() + dx;
		int caiz = getLoadRadius() + dz;

		if( caix < 0 || caix >= chunks.length || caiz < 0 || caiz >= chunks[ caix ].length )
		{
			return null;
		}
		else
		{
			return chunks[ caix ][ caiz ];
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The chunklet that contains that point
	 */
	public Chunklet getChunklet( float x, float y, float z )
	{
		Chunk chunk = getChunk( ( int ) Math.floor( x / 16 ), ( int ) Math.floor( z / 16 ) );

		if( chunk != null && y >= 0 && y < 128 )
		{
			int yi = ( int ) Math.floor( y / 16 );

			return chunk.chunklets[ yi ];
		}

		return null;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The type of the block that contains the point
	 */
	public byte blockType( float x, float y, float z )
	{
		Chunklet c = getChunklet( x, y, z );

		if( c != null )
		{
			return c.parent.blockTypeForPosition( x, y, z );
		}

		return 0;
	}

	/**
	 * @param chunkRadius
	 */
	@Variable( "Chunk load range" )
	@Summary( "The distance (in chunk units) at which to load chunks" )
	public void setLoadRadius( int chunkRadius )
	{
		loadradius = chunkRadius;

		// I can't be bothered to work out the indices to do this
		// properly, so brace yourself for the Madagascan strategy:

		// RELOAD. EVERYTHING.

		for( int i = 0; i < chunks.length; i++ )
		{
			for( int j = 0; j < chunks[ i ].length; j++ )
			{
				chunks[ i ][ j ].unload();
			}
		}

		chunks = new Chunk[ 2 * chunkRadius + 1 ][ 2 * chunkRadius + 1 ];
		fillChunks();
	}

	/**
	 * @return chunk load radius
	 */
	@Variable( "Chunk load range" )
	public int getLoadRadius()
	{
		return loadradius;
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
