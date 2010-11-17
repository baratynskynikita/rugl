
package com.ryanm.minedroid;

import android.util.Log;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.geom.ShapeBuilder;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.gl.enums.MagFilter;
import com.ryanm.droid.rugl.gl.enums.MinFilter;
import com.ryanm.droid.rugl.res.BitmapLoader;
import com.ryanm.droid.rugl.texture.Texture;
import com.ryanm.droid.rugl.texture.TextureFactory;

/**
 * Defines block data, can add face geometry to {@link ShapeBuilder}s
 * 
 * @author ryanm
 */
public class BlockFactory
{
	private static final float[] nbl = new float[] { 0, 0, 0 };

	private static final float[] ntl = new float[] { 0, 1, 0 };

	private static final float[] nbr = new float[] { 1, 0, 0 };

	private static final float[] ntr = new float[] { 1, 1, 0 };

	private static final float[] fbl = new float[] { 0, 0, 1 };

	private static final float[] ftl = new float[] { 0, 1, 1 };

	private static final float[] fbr = new float[] { 1, 0, 1 };

	private static final float[] ftr = new float[] { 1, 1, 1 };

	private static final float sxtn = 1.0f / 16;

	/**
	 * The terrain.png texture
	 */
	public static Texture texture;

	/**
	 * Rendering state for blocks. Texture filtering is for wimps
	 */
	public static State state = GLUtil.typicalState.with( MinFilter.NEAREST,
			MagFilter.NEAREST );

	/**
	 * Synchronously loads the terrain texture
	 */
	public static void loadTexture()
	{
		BitmapLoader l = new BitmapLoader( R.drawable.terrain ) {
			@Override
			public void complete()
			{
				Texture terrain = TextureFactory.buildTexture( resource, true, false );
				BlockFactory.texture = terrain;

				if( texture != null )
				{
					state = texture.applyTo( state );
				}
			}
		};

		l.load();
		l.loaded();

		Log.i( Game.RUGL_TAG, "terrain texture loaded: " );
		Log.i( Game.RUGL_TAG, l.toString() );

		l.complete();
	}

	/**
	 * A map of block id values to {@link Block}s
	 */
	private static final Block[] blocks;
	static
	{
		int maxID = 0;

		for( Block b : Block.values() )
		{
			maxID = Math.max( maxID, b.id );
		}

		blocks = new Block[ maxID + 1 ];

		for( Block b : Block.values() )
		{
			blocks[ b.id ] = b;
		}
	}

	/**
	 * @param id
	 * @return The so-typed block
	 */
	public static Block getBlock( byte id )
	{
		if( id >= blocks.length )
		{
			return null;
		}

		return blocks[ id ];
	}

	/**
	 * @param id
	 * @return <code>true</code> if the block is opaque,
	 *         <code>false</code> if transparent
	 */
	public static boolean opaque( byte id )
	{
		if( id >= blocks.length || blocks[ id ] == null )
		{
			return false;
		}

		return blocks[ id ].opaque;
	}

	/**
	 * Holds vertex positions for each face of a unit cube
	 * 
	 * @author ryanm
	 */
	public static enum Face
	{
		/**
		 * -ve x direction
		 */
		North( fbl, ftl, nbl, ntl ),
		/**
		 * +ve x direction
		 */
		South( nbr, ntr, fbr, ftr ),
		/**
		 * -ve z direction
		 */
		East( nbl, ntl, nbr, ntr ),
		/**
		 * +ve z direction
		 */
		West( fbr, ftr, fbl, ftl ),
		/**
		 * +ve y direction
		 */
		Top( ntl, ftl, ntr, ftr ),
		/**
		 * -ve y direction
		 */
		Bottom( nbl, fbl, nbr, fbr );

		private final float[] verts = new float[ 12 ];

		private Face( float[]... verts )
		{
			for( int i = 0; i < verts.length; i++ )
			{
				System.arraycopy( verts[ i ], 0, this.verts, i * 3, 3 );
			}
		}
	}

	/**
	 * Block types
	 * 
	 * @author ryanm
	 */
	public static enum Block
	{
		/***/
		Grass( ( byte ) 2, true, 3, 0, 0, 0, 2, 0 ),
		/***/
		Stone( ( byte ) 1, true, 1, 0 ),
		/***/
		Dirt( ( byte ) 3, true, 2, 0 ),
		/***/
		Cobble( ( byte ) 4, true, 0, 1 ),
		/***/
		Wood( ( byte ) 5, true, 4, 0 ),
		/***/
		Bedrock( ( byte ) 7, true, 1, 1 ),
		/***/
		Sand( ( byte ) 12, true, 2, 1 ),
		/***/
		Gravel( ( byte ) 13, true, 3, 1 ),
		/***/
		GoldOre( ( byte ) 14, true, 0, 2 ),
		/***/
		IronOre( ( byte ) 15, true, 1, 2 ),
		/***/
		CoalOre( ( byte ) 16, true, 2, 2 ),
		/***/
		Log( ( byte ) 17, true, 4, 1, 5, 1 ),
		/***/
		Leaves( ( byte ) 18, false, 4, 3 ),
		/***/
		Glass( ( byte ) 20, false, 1, 3 ),
		/***/
		RedstoneOre( ( byte ) 73, true, 3, 3 ),
		/***/
		DiamondOre( ( byte ) 56, true, 2, 3 ),
		/***/
		Water( ( byte ) 8, false, 15, 12 ),
		/***/
		StillWater( ( byte ) 9, false, 15, 12 ),
		/***/
		Lava( ( byte ) 10, true, 15, 15 ),
		/***/
		StillLava( ( byte ) 11, true, 15, 15 ),
		/***/
		Obsidian( ( byte ) 49, true, 5, 2 ),
		/***/
		TilledEarth( ( byte ) 60, true, 2, 0, 7, 5, 2, 0 ),
		/***/
		Chest( ( byte ) 54, true, 10, 1, 10, 1, 10, 1, 11, 1, 9, 1, 9, 1 ),
		/***/
		Oven( ( byte ) 61, true, 13, 2, 13, 2, 13, 2, 12, 2, 1, 0, 1, 0 ),
		/***/
		WorkBench( ( byte ) 58, true, 11, 3, 12, 3, 11, 3, 12, 3, 11, 2, 11, 2 );

		/***/
		public final byte id;

		/***/
		public final boolean opaque;

		/***/
		public final int[] texCoords;

		/**
		 * @param id
		 *           block type identifier
		 * @param opaque
		 *           <code>true</code> if you can't see through the
		 *           block
		 * @param tc
		 *           coordinates of the face textures in terrain.png.
		 *           e.g.: grass is (0,0), stone is (1,0), mossy
		 *           cobblestone is (4,2)
		 */
		private Block( byte id, boolean opaque, int... tc )
		{
			this.id = id;
			this.opaque = opaque;
			if( tc.length == 6 )
			{ // similar sides, distinct top, distinct bottom
				// (ooh matron!)
				texCoords =
						new int[] { tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ],
								tc[ 0 ], tc[ 1 ], tc[ 2 ], tc[ 3 ], tc[ 4 ], tc[ 5 ] };
			}
			else if( tc.length == 4 )
			{ // similar sides, similar top and bottom
				// (don't fancy yours much)
				texCoords =
						new int[] { tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ],
								tc[ 0 ], tc[ 1 ], tc[ 2 ], tc[ 3 ], tc[ 2 ], tc[ 3 ] };
			}
			else if( tc.length == 2 )
			{ // all sides similar
				texCoords =
						new int[] { tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ],
								tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ], tc[ 0 ], tc[ 1 ] };
			}
			else
			{
				// all sides distinct
				texCoords = tc;
			}
		}

		/**
		 * Adds a face to the {@link ShapeBuilder}
		 * 
		 * @param f
		 *           which side
		 * @param bx
		 *           block coordinate
		 * @param by
		 *           block coordinate
		 * @param bz
		 *           block coordinate
		 * @param colour
		 *           Vertex colour
		 * @param sb
		 */
		public void face( Face f, float bx, float by, float bz, int colour, ShapeBuilder sb )
		{
			sb.ensureCapacity( 4, 2 );

			// add vertices
			System.arraycopy( f.verts, 0, sb.vertices, sb.vertexOffset, f.verts.length );

			for( int i = 0; i < 4; i++ )
			{
				// translation
				sb.vertices[ sb.vertexOffset++ ] += bx;
				sb.vertices[ sb.vertexOffset++ ] += by;
				sb.vertices[ sb.vertexOffset++ ] += bz;

				// colour
				sb.colours[ sb.colourOffset++ ] = colour;
			}

			// texcoords
			int txco = 2 * f.ordinal();
			float bu = sxtn * texCoords[ txco ];
			float bv = sxtn * ( texCoords[ txco + 1 ] + 1 );
			float tu = sxtn * ( texCoords[ txco ] + 1 );
			float tv = sxtn * texCoords[ txco + 1 ];

			sb.texCoords[ sb.texCoordOffset++ ] = bu;
			sb.texCoords[ sb.texCoordOffset++ ] = bv;
			sb.texCoords[ sb.texCoordOffset++ ] = bu;
			sb.texCoords[ sb.texCoordOffset++ ] = tv;
			sb.texCoords[ sb.texCoordOffset++ ] = tu;
			sb.texCoords[ sb.texCoordOffset++ ] = bv;
			sb.texCoords[ sb.texCoordOffset++ ] = tu;
			sb.texCoords[ sb.texCoordOffset++ ] = tv;

			sb.relTriangle( 0, 2, 1 );
			sb.relTriangle( 2, 3, 1 );

			sb.vertexCount += 4;
		}
	}
}
