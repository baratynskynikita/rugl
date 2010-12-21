
package com.ryanm.minedroid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.Shape;
import com.ryanm.droid.rugl.geom.ShapeBuilder;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.geom.WireUtil;
import com.ryanm.droid.rugl.gl.Renderer;
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
	/**
	 * Parent chunk
	 */
	public final Chunk parent;

	/**
	 * World coordinate
	 */
	public final int x;

	/**
	 * World coordinate
	 */
	public final int y;

	/**
	 * World coordinate
	 */
	public final int z;

	private ColouredShape outline = null;

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
		x = parent.chunkX * 16;
		this.y = y * 16;
		z = parent.chunkZ * 16;

		// find sheets
		for( int i = 0; i < 16; i++ )
		{
			for( int j = 0; j < 16; j++ )
			{
				byte bt = blockType( 0, i, j );
				northSheet &= BlockFactory.opaque( bt );

				bt = blockType( 15, i, j );
				southSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, j, 0 );
				eastSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, j, 15 );
				westSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, 15, j );
				topSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, 0, j );
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
					empty &= blockType( x, k, z ) == 0;
				}
			}
		}
		// need to check the sides of neighbouring blocks too
		for( int i = 0; i < 16 && empty; i++ )
		{
			for( int j = 0; j < 16 && empty; j++ )
			{
				empty &= blockType( -1, i, j ) == 0;
				empty &= blockType( 16, i, j ) == 0;

				empty &= blockType( i, j, -1 ) == 0;
				empty &= blockType( i, j, 16 ) == 0;

				empty &= blockType( i, -1, j ) == 0;
				empty &= blockType( i, 16, j ) == 0;
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
		float dx = this.x + 8f - x;
		float dy = this.y + 8f - y;
		float dz = this.z + 8f - z;

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
					for( int xi = 0; xi < 16; xi++ )
					{
						for( int yi = 0; yi < 16; yi++ )
						{
							for( int zi = 0; zi < 16; zi++ )
							{
								Block b = BlockFactory.getBlock( blockType( xi, yi, zi ) );
								// lighting
								int sl = parent.skyLight( xi, y + yi, zi );
								int bl = parent.blockLight( xi, y + yi, zi );
								int l = Math.max( sl, bl );

								float light = ( float ) Math.pow( 0.8, 15 - l );

								int colour = Colour.packFloat( light, light, light, 1 );

								if( b == null || !b.opaque )
								{
									addFace( b, xi - 1, yi, zi, Face.South, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, xi + 1, yi, zi, Face.North, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, xi, yi, zi - 1, Face.West, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, xi, yi, zi + 1, Face.East, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, xi, yi + 1, zi, Face.Bottom, colour,
											opaqueVBOBuilder, transVBOBuilder );
									addFace( b, xi, yi - 1, zi, Face.Top, colour,
											opaqueVBOBuilder, transVBOBuilder );
								}
							}
						}
					}

					TexturedShape ts = opaqueVBOBuilder.compile();

					if( ts != null )
					{
						ts.state = BlockFactory.state;
						ts.translate( x, y, z );

						solidVBO = new VBOShape( ts );
					}

					ts = transVBOBuilder.compile();
					if( ts != null )
					{
						ts.state = BlockFactory.state;
						ts.translate( x, y, z );

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
		Block b = BlockFactory.getBlock( blockType( x, y, z ) );

		if( b != null && b != facing )
		{
			b.face( f, x, y, z, colour, b.opaque ? opaque : transparent );
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return the so-indexed point
	 */
	private byte blockType( int x, int y, int z )
	{
		return parent.blockType( x, this.y + y, z );
	}

	/**
	 * @param frustum
	 * @return The intersection status of the chunk and frustum
	 */
	public Result intersection( Frustum frustum )
	{
		return frustum.cuboidIntersects( x, y, z, x + 16, y + 16, z + 16 );
	}

	@Override
	public String toString()
	{
		return "Chunklet @ " + x + ", " + y + ", " + z + "\n = " + x * 16 + ", " + y * 16
				+ ", " + z * 16 + "\nsheets n " + northSheet + " s " + southSheet + "\n e "
				+ eastSheet + " w " + westSheet + "\n t " + topSheet + " b " + bottomSheet;
	}

	/**
	 * Draws wireframe outline
	 * 
	 * @param r
	 */
	public void drawOutline( Renderer r )
	{
		if( solidVBO != null || transparentVBO != null || geomPending )
		{
			if( outline == null )
			{
				Shape s = WireUtil.unitCube();
				s.scale( 16, 16, 16 );
				s.translate( x, y, z );

				outline = new ColouredShape( s, Colour.black, WireUtil.state );
			}

			outline.render( r );
		}
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
