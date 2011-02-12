
package com.ryanm.minedroid.chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.ryanm.minedroid.World;
import com.ryanm.minedroid.nbt.Tag;

/**
 * A 16x16x128 chunk of blocks
 * 
 * @author ryanm
 */
public class Chunk
{
	/**
	 * World chunk x coordinate
	 */
	public int chunkX;

	/**
	 * World chunk z coordinate
	 */
	public int chunkZ;

	/**
	 * Block data
	 */
	public final byte[] blockData;

	/**
	 * Skylight data, remember it's only 4 bits per block
	 */
	public final byte[] skylight;

	/**
	 * Blocklight data, remember it's only 4 bits per block
	 */
	public final byte[] blocklight;

	/**
	 * The parent world
	 */
	public final World world;

	/**
	 * The child chunklets
	 */
	public final Chunklet[] chunklets;

	/**
	 * @param world
	 * @param f
	 * @throws IOException
	 */
	public Chunk( World world, File f ) throws IOException
	{
		this.world = world;
		Tag ct = Tag.readFrom( new FileInputStream( f ) );
		chunkX = ( ( Integer ) ct.findTagByName( "xPos" ).getValue() ).intValue();
		chunkZ = ( ( Integer ) ct.findTagByName( "zPos" ).getValue() ).intValue();
		blockData = ( byte[] ) ct.findTagByName( "Blocks" ).getValue();
		skylight = ( byte[] ) ct.findTagByName( "SkyLight" ).getValue();
		blocklight = ( byte[] ) ct.findTagByName( "BlockLight" ).getValue();

		chunklets = new Chunklet[ 8 ];
		for( int i = 0; i < chunklets.length; i++ )
		{
			chunklets[ i ] = new Chunklet( this, i );
		}
	}

	/**
	 * @param bx
	 * @param by
	 * @param bz
	 * @return the type of the so-indexed block in this chunk
	 */
	public byte blockType( int bx, int by, int bz )
	{
		if( bx < 0 )
		{
			Chunk north = world.getChunk( chunkX - 1, chunkZ );
			// north = null;
			return north == null ? 0 : north.blockType( bx + 16, by, bz );
		}
		else if( bx >= 16 )
		{
			Chunk south = world.getChunk( chunkX + 1, chunkZ );
			// south = null;
			return south == null ? 0 : south.blockType( bx - 16, by, bz );
		}
		else if( bz < 0 )
		{
			Chunk east = world.getChunk( chunkX, chunkZ - 1 );
			// east = null;
			return east == null ? 0 : east.blockType( bx, by, bz + 16 );
		}
		else if( bz >= 16 )
		{
			Chunk west = world.getChunk( chunkX, chunkZ + 1 );
			// west = null;
			return west == null ? 0 : west.blockType( bx, by, bz - 16 );
		}
		else if( by < 0 || by >= 128 )
		{
			return 0;
		}

		return blockData[ by + bz * 128 + bx * 2048 ];
	}

	private void setBlockType( final int bx, final int by, final int bz,
			final byte blockType )
	{
		if( bx < 0 )
		{
			Chunk north = world.getChunk( chunkX - 1, chunkZ );
			if( north != null )
			{
				north.setBlockType( bx + 16, by, bz, blockType );
			}
		}
		else if( bx >= 16 )
		{
			Chunk south = world.getChunk( chunkX + 1, chunkZ );
			if( south != null )
			{
				south.setBlockType( bx - 16, by, bz, blockType );
			}
		}
		else if( bz < 0 )
		{
			Chunk east = world.getChunk( chunkX, chunkZ - 1 );
			if( east != null )
			{
				east.setBlockType( bx, by, bz + 16, blockType );
			}
		}
		else if( bz >= 16 )
		{
			Chunk west = world.getChunk( chunkX, chunkZ + 1 );
			if( west != null )
			{
				west.setBlockType( bx, by, bz - 16, blockType );
			}
		}
		else if( by < 0 || by >= 128 )
		{
			return;
		}
		else
		{
			final int index = by + bz * 128 + bx * 2048;

			blockData[ index ] = blockType;

			if( blockType == 0 )
			{
				// removed a block, lighting propagation needed
				byte ol =
						( byte ) Math.max( skyLight( bx, by, bz ), blockLight( bx, by, bz ) );

				byte nl =
						( byte ) Math.max( skyLight( bx + 1, by, bz ),
								blockLight( bx + 1, by, bz ) );
				nl =
						( byte ) Math.max( nl, Math.max( skyLight( bx, by + 1, bz ),
								blockLight( bx, by + 1, bz ) ) );
				nl =
						( byte ) Math.max( nl, Math.max( skyLight( bx, by, bz + 1 ),
								blockLight( bx, by, bz + 1 ) ) );
				nl =
						( byte ) Math.max( nl, Math.max( skyLight( bx - 1, by, bz ),
								blockLight( bx - 1, by, bz ) ) );
				nl =
						( byte ) Math.max( nl, Math.max( skyLight( bx, by - 1, bz ),
								blockLight( bx, by - 1, bz ) ) );
				nl =
						( byte ) Math.max( nl, Math.max( skyLight( bx, by, bz - 1 ),
								blockLight( bx, by, bz - 1 ) ) );

				nl--;
				if( nl > ol )
				{
					// set
					int hi = index / 2;
					boolean odd = ( index & 1 ) != 0;
					if( odd )
					{
						blocklight[ hi ] &= 0xf;
						blocklight[ hi ] |= nl << 4;
					}
					else
					{
						blocklight[ hi ] &= 0xf0;
						blocklight[ hi ] |= nl;
					}
				}
			}

			int cyi = by / 16;
			chunklets[ cyi ].geomDirty();
			chunklets[ cyi ].generateGeometry( true );

			// neighbours also dirty?
			if( bx == 0 )
			{
				Chunk north = world.getChunk( chunkX - 1, chunkZ );
				north.chunklets[ cyi ].geomDirty();
				north.chunklets[ cyi ].generateGeometry( true );
			}
			else if( bx == 15 )
			{
				Chunk south = world.getChunk( chunkX + 1, chunkZ );
				south.chunklets[ cyi ].geomDirty();
				south.chunklets[ cyi ].generateGeometry( true );
			}

			if( bz == 0 )
			{
				Chunk east = world.getChunk( chunkX, chunkZ - 1 );
				east.chunklets[ cyi ].geomDirty();
				east.chunklets[ cyi ].generateGeometry( true );
			}
			else if( bz == 15 )
			{
				Chunk west = world.getChunk( chunkX, chunkZ + 1 );
				west.chunklets[ cyi ].geomDirty();
				west.chunklets[ cyi ].generateGeometry( true );
			}

			if( by % 16 == 0 && cyi >= 1 )
			{
				Chunklet below = chunklets[ cyi - 1 ];
				below.geomDirty();
				below.generateGeometry( true );
			}

			if( by % 16 == 15 && cyi < 6 )
			{
				Chunklet above = chunklets[ cyi + 1 ];
				above.geomDirty();
				above.generateGeometry( true );
			}
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The type of the block that contains the specified point
	 */
	public byte blockTypeForPosition( float x, float y, float z )
	{
		return blockType( ( int ) Math.floor( x - chunkX * 16 ), ( int ) Math.floor( y ),
				( int ) Math.floor( z - chunkZ * 16 ) );
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param blockType
	 */
	public void setBlockTypeForPosition( float x, float y, float z, byte blockType )
	{
		setBlockType( ( int ) Math.floor( x - chunkX * 16 ), ( int ) Math.floor( y ),
				( int ) Math.floor( z - chunkZ * 16 ), blockType );
	}

	/**
	 * @param bx
	 * @param by
	 * @param bz
	 * @return The light contribution from torches, lava etc, in range
	 *         0-15
	 */
	public int blockLight( int bx, int by, int bz )
	{
		if( bx < 0 )
		{
			Chunk north = world.getChunk( chunkX - 1, chunkZ );
			return north == null ? 0 : north.blockLight( bx + 16, by, bz );
		}
		else if( bx >= 16 )
		{
			Chunk south = world.getChunk( chunkX + 1, chunkZ );
			return south == null ? 0 : south.blockLight( bx - 16, by, bz );
		}
		else if( bz < 0 )
		{
			Chunk east = world.getChunk( chunkX, chunkZ - 1 );
			return east == null ? 0 : east.blockLight( bx, by, bz + 16 );
		}
		else if( bz >= 16 )
		{
			Chunk west = world.getChunk( chunkX, chunkZ + 1 );
			return west == null ? 0 : west.blockLight( bx, by, bz - 16 );
		}
		else if( by < 0 || by >= 128 )
		{
			return 0;
		}

		int index = by + bz * 128 + bx * 2048;
		int hi = index / 2;
		boolean odd = ( index & 1 ) != 0;
		if( odd )
		{
			return ( blocklight[ hi ] & 0xf0 ) >> 4;
		}
		else
		{
			return blocklight[ hi ] & 0xf;
		}
	}

	/**
	 * @param bx
	 * @param by
	 * @param bz
	 * @return The light contribution from the sky, in range 0-15
	 */
	public int skyLight( int bx, int by, int bz )
	{
		if( bx < 0 )
		{
			Chunk north = world.getChunk( chunkX - 1, chunkZ );
			return north == null ? 0 : north.skyLight( bx + 16, by, bz );
		}
		else if( bx >= 16 )
		{
			Chunk south = world.getChunk( chunkX + 1, chunkZ );
			return south == null ? 0 : south.skyLight( bx - 16, by, bz );
		}
		else if( bz < 0 )
		{
			Chunk east = world.getChunk( chunkX, chunkZ - 1 );
			return east == null ? 0 : east.skyLight( bx, by, bz + 16 );
		}
		else if( bz >= 16 )
		{
			Chunk west = world.getChunk( chunkX, chunkZ + 1 );
			return west == null ? 0 : west.skyLight( bx, by, bz - 16 );
		}
		else if( by < 0 || by >= 128 )
		{
			return 0;
		}

		int index = by + bz * 128 + bx * 2048;
		int hi = index / 2;
		boolean odd = ( index & 1 ) != 0;

		if( odd )
		{
			return ( skylight[ hi ] & 0xf0 ) >> 4;
		}
		else
		{
			return skylight[ hi ] & 0xf;
		}
	}

	/**
	 * Call this to refresh the geometry of the chunk
	 */
	public void geomDirty()
	{
		for( int i = 0; i < chunklets.length; i++ )
		{
			chunklets[ i ].geomDirty();
		}
	}

	/**
	 * Destroys VBO handles
	 */
	public void unload()
	{
		for( int i = 0; i < chunklets.length; i++ )
		{
			chunklets[ i ].unload();
		}
	}
}
