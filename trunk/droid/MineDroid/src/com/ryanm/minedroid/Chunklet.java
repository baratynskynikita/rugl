
package com.ryanm.minedroid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.ryanm.droid.rugl.geom.ShapeBuilder;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.gl.VBOShape;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Frustum.Result;
import com.ryanm.minedroid.BlockFactory.Block;
import com.ryanm.minedroid.BlockFactory.Face;

/**
 * A 16 * 16 * 16 cube of a {@link Chunk}
 * 
 * @author ryanm
 */
public class Chunklet
{
	private static final float SIXTEENTH = 1.0f / 16;

	/**
	 * Parent chunk
	 */
	public final Chunk parent;

	/**
	 * world coordinate
	 */
	public final int x;

	/**
	 * world coordinate
	 */
	public final int y;

	/**
	 * world coordinate
	 */
	public final int z;

	/**
	 * Lowest level of blocks in this chunklet in block index
	 * coordinates
	 */
	public final int yMin;

	private final int yMax;

	private boolean geomDirty = true;

	/**
	 * Solid geometry
	 */
	public VBOShape solidVBO;

	/**
	 * Transparent geometry
	 */
	public VBOShape transparentVBO;

	/**
	 * <code>true</code> if we're waiting on being processed by the
	 * geometry-generating thread
	 */
	private boolean geomPending = false;

	/**
	 * <code>true</code> if the north side of this chunklet is
	 * completely opaque
	 */
	public boolean northSheet = true;

	/**
	 * <code>true</code> if the bottom side of this chunklet is
	 * completely opaque
	 */
	public boolean bottomSheet = true;

	/**
	 * <code>true</code> if the east side of this chunklet is
	 * completely opaque
	 */
	public boolean eastSheet = true;

	/**
	 * <code>true</code> if the south side of this chunklet is
	 * completely opaque
	 */
	public boolean southSheet = true;

	/**
	 * <code>true</code> if the top side of this chunklet is completely
	 * opaque
	 */
	public boolean topSheet = true;

	/**
	 * <code>true</code> if the west side of this chunklet is
	 * completely opaque
	 */
	public boolean westSheet = true;

	private boolean empty = true;

	/**
	 * Stops us revisiting this chunklet when we flood-fill the view
	 * frustum to find which chunklets to render
	 */
	public int drawFlag = 0;

	private static ShapeBuilder opaqueVBOBuilder = new ShapeBuilder();

	private static ShapeBuilder transVBOBuilder = new ShapeBuilder();

	/**
	 * The service where we generate geometry. Note that there's only
	 * one worker thread: if you want more, you're going to have to use
	 * separate {@link ShapeBuilder}s for each rather than the static
	 * ones above in {@link #opaqueVBOBuilder} and
	 * {@link #transVBOBuilder}
	 */
	private static ExecutorService geomGenService = Executors.newSingleThreadExecutor();

	private static AtomicInteger queueSize = new AtomicInteger( 0 );

	/**
	 * @return The number of chunklets awaiting geometry generation
	 */
	public static int getChunkletQueueSize()
	{
		return queueSize.get();
	}

	/**
	 * @param parent
	 * @param y
	 *           in world coordinates
	 */
	public Chunklet( Chunk parent, int y )
	{
		this.parent = parent;
		x = parent.x;
		this.y = y;
		z = parent.z;

		yMin = y * 16;
		yMax = yMin + 16;

		// find sheets
		for( int i = 0; i < 16; i++ )
		{
			for( int j = 0; j < 16; j++ )
			{
				byte bt = parent.blockType( 0, i + yMin, j );
				northSheet &= BlockFactory.opaque( bt );

				bt = parent.blockType( 15, i + yMin, j );
				southSheet &= BlockFactory.opaque( bt );

				bt = parent.blockType( i, j + yMin, 0 );
				eastSheet &= BlockFactory.opaque( bt );

				bt = parent.blockType( i, j + yMin, 15 );
				westSheet &= BlockFactory.opaque( bt );

				bt = parent.blockType( i, 15 + yMin, j );
				topSheet &= BlockFactory.opaque( bt );

				bt = parent.blockType( i, 0 + yMin, j );
				bottomSheet &= BlockFactory.opaque( bt );
			}
		}

		// empty check
		empty =
				!( northSheet || southSheet || eastSheet || westSheet || topSheet || bottomSheet );
		for( int x = 0; x < 16 && empty; x++ )
		{
			for( int z = 0; z < 16 && empty; z++ )
			{
				for( int k = 0; k < 16 && empty; k++ )
				{
					empty &= parent.blockType( x, yMin + k, z ) == 0;
				}
			}
		}
		// need to check the sides of neighbouring blocks too
		for( int i = 0; i < 16 && empty; i++ )
		{
			for( int j = 0; j < 16 && empty; j++ )
			{
				empty &= parent.blockType( -1, i + yMin, j ) == 0;
				empty &= parent.blockType( 16, i + yMin, j ) == 0;

				empty &= parent.blockType( i, j + yMin, -1 ) == 0;
				empty &= parent.blockType( i, j + yMin, 16 ) == 0;

				empty &= parent.blockType( i, -1 + yMin, j ) == 0;
				empty &= parent.blockType( i, 16 + yMin, j ) == 0;
			}
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The distance from the center of this chunklet to the
	 *         point
	 */
	public float distanceSq( float x, float y, float z )
	{
		float dx = this.x + 0.5f - x;
		float dy = this.y + 0.5f - y;
		float dz = this.z + 0.5f - z;

		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Call this to refresh the chunklet's geometry the next time it is
	 * rendered
	 */
	public void geomDirty()
	{
		geomDirty = true;
	}

	/**
	 * 
	 */
	public void generateGeometry()
	{
		if( empty )
		{
			return;
		}

		if( geomDirty && !geomPending )
		{
			Runnable r = new Runnable() {
				@Override
				public void run()
				{
					for( int x = 0; x < 16; x++ )
					{
						for( int y = yMin; y < yMax; y++ )
						{
							for( int z = 0; z < 16; z++ )
							{
								Block b = BlockFactory.getBlock( parent.blockType( x, y, z ) );
								// lighting
								int sl = parent.skyLight( x, y, z );
								int bl = parent.blockLight( x, y, z );
								int l = Math.max( sl, bl );

								float light = ( float ) Math.pow( 0.8, 15 - l );

								int colour = Colour.packFloat( light, light, light, 1 );

								if( b == null || !b.opaque )
								{
									addFace( b, x - 1, y, z, Face.South, colour, opaqueVBOBuilder,
											transVBOBuilder );
									addFace( b, x + 1, y, z, Face.North, colour, opaqueVBOBuilder,
											transVBOBuilder );
									addFace( b, x, y, z - 1, Face.West, colour, opaqueVBOBuilder,
											transVBOBuilder );
									addFace( b, x, y, z + 1, Face.East, colour, opaqueVBOBuilder,
											transVBOBuilder );
									addFace( b, x, y + 1, z, Face.Bottom, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, x, y - 1, z, Face.Top, colour, opaqueVBOBuilder,
											transVBOBuilder );
								}
							}
						}
					}

					TexturedShape ts = opaqueVBOBuilder.compile();

					if( ts != null )
					{
						ts.state = BlockFactory.state;
						ts.scale( SIXTEENTH, SIXTEENTH, SIXTEENTH );
						ts.translate( parent.x, 0, parent.z );

						solidVBO = new VBOShape( ts );
					}

					ts = transVBOBuilder.compile();
					if( ts != null )
					{
						ts.state = BlockFactory.state;
						ts.scale( SIXTEENTH, SIXTEENTH, SIXTEENTH );
						ts.translate( parent.x, 0, parent.z );

						transparentVBO = new VBOShape( ts );
					}

					geomDirty = false;
					geomPending = false;
					queueSize.decrementAndGet();
				}
			};

			geomPending = true;
			queueSize.incrementAndGet();
			geomGenService.submit( r );
		}
	}

	private void addFace( Block facing, int x, int y, int z, Face f, int colour,
			ShapeBuilder opaque, ShapeBuilder transparent )
	{
		Block b = BlockFactory.getBlock( parent.blockType( x, y, z ) );

		if( b != null && b != facing )
		{
			b.face( f, x, y, z, colour, b.opaque ? opaque : transparent );
		}
	}

	/**
	 * @param frustum
	 * @return The intersection status of the chunk and frustum
	 */
	public Result intersection( Frustum frustum )
	{
		return frustum.cuboidIntersects( x, y, z, x + 1, y + 1, z + 1 );
	}

	@Override
	public String toString()
	{
		return "Chunklet @ " + x + ", " + y + ", " + z + "\nsheets n " + northSheet + " s "
				+ southSheet + "\n e " + eastSheet + " w " + westSheet + "\n t " + topSheet
				+ " b " + bottomSheet;
	}

	/**
	 * Deletes VBOs
	 */
	public void unload()
	{
		if( solidVBO != null )
		{
			solidVBO.delete();
		}

		if( transparentVBO != null )
		{
			transparentVBO.delete();
		}
	}
}
