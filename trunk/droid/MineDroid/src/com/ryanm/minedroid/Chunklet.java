
package com.ryanm.minedroid;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ryanm.droid.rugl.geom.CompiledShape;
import com.ryanm.droid.rugl.geom.ShapeBuilder;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.gl.Renderer;
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
	 * in n, s, e, w, t, b order
	 */
	private final CompiledShape[] solids = new CompiledShape[ 6 ];

	/**
	 * in n, s, e, w, t, b order
	 */
	private final CompiledShape[] transparents = new CompiledShape[ 6 ];

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

	/**
	 * Stops us revisiting this chunklet when we flood-fill the view
	 * frustum to find which chunklets to render
	 */
	public int drawFlag = 0;

	/**
	 * Used to generate the solid geometry
	 */
	private static ShapeBuilder[] opaque = new ShapeBuilder[ 6 ];

	/**
	 * Used to generate the transparent geometry
	 */
	private static ShapeBuilder[] transparent = new ShapeBuilder[ 6 ];
	static
	{
		for( int i = 0; i < 6; i++ )
		{
			opaque[ i ] = new ShapeBuilder();
			transparent[ i ] = new ShapeBuilder();
		}
	}

	/**
	 * The service where we generate geometry. Note that there's only
	 * one worker thread: if you want more, you're going to have to use
	 * separate {@link ShapeBuilder}s for each rather than the static
	 * ones above in {@link #opaque} and {@link #transparent}
	 */
	private static ExecutorService geomGenService = Executors.newSingleThreadExecutor();

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

	private void generateGeometry()
	{
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
									addFace( b, x - 1, y, z, Face.South, colour, opaque[ 0 ],
											transparent[ 0 ] );
									addFace( b, x + 1, y, z, Face.North, colour, opaque[ 1 ],
											transparent[ 1 ] );
									addFace( b, x, y, z - 1, Face.West, colour, opaque[ 2 ],
											transparent[ 2 ] );
									addFace( b, x, y, z + 1, Face.East, colour, opaque[ 3 ],
											transparent[ 3 ] );
									addFace( b, x, y + 1, z, Face.Bottom, colour, opaque[ 4 ],
											transparent[ 4 ] );
									addFace( b, x, y - 1, z, Face.Top, colour, opaque[ 5 ],
											transparent[ 5 ] );
								}
							}
						}
					}

					for( int i = 0; i < opaque.length; i++ )
					{
						TexturedShape pts = opaque[ i ].compile();

						if( pts != null )
						{
							pts.scale( SIXTEENTH, SIXTEENTH, SIXTEENTH );
							pts.translate( parent.x, 0, parent.z );
							solids[ i ] = new CompiledShape( pts );
							solids[ i ].state = BlockFactory.state;
						}
						else
						{
							solids[ i ] = null;
						}

						pts = transparent[ i ].compile();

						if( pts != null )
						{
							pts.scale( SIXTEENTH, SIXTEENTH, SIXTEENTH );
							pts.translate( parent.x, 0, parent.z );
							transparents[ i ] = new CompiledShape( pts );
							transparents[ i ].state = BlockFactory.state;
						}
						else
						{
							transparents[ i ] = null;
						}
					}

					geomDirty = false;
					geomPending = false;
				}
			};

			geomPending = true;
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

	/**
	 * @param eyeX
	 * @param eyeY
	 * @param eyeZ
	 * @param solids
	 *           <code>true</code> to render the solids,
	 *           <code>false</code> for the transparent stuff
	 * @param r
	 */
	public void render( double eyeX, double eyeY, double eyeZ, boolean solids, Renderer r )
	{
		generateGeometry();

		CompiledShape[] toDraw = solids ? this.solids : transparents;

		if( toDraw != null )
		{
			if( toDraw[ 0 ] != null && eyeX > x )
			{
				toDraw[ 0 ].render( r );
			}
			if( toDraw[ 1 ] != null && eyeX < x + 1 )
			{
				toDraw[ 1 ].render( r );
			}
			if( toDraw[ 2 ] != null && eyeZ > z )
			{
				toDraw[ 2 ].render( r );
			}
			if( toDraw[ 3 ] != null && eyeZ < z + 1 )
			{
				toDraw[ 3 ].render( r );
			}
			if( toDraw[ 4 ] != null && eyeY < y + 1 )
			{
				toDraw[ 4 ].render( r );
			}
			if( toDraw[ 5 ] != null && eyeY > y )
			{
				toDraw[ 5 ].render( r );
			}
		}
	}

	@Override
	public String toString()
	{
		return "Chunklet @ " + x + ", " + y + ", " + z + "\nsheets n " + northSheet + " s "
				+ southSheet + "\n e " + eastSheet + " w " + westSheet + "\n t " + topSheet
				+ " b " + bottomSheet;
	}
}
