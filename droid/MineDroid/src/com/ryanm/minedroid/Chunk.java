
package com.ryanm.minedroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A 16x16x128 chunk of blocks
 * 
 * @author ryanm
 */
public class Chunk
{
	/**
	 * World x coordinate
	 */
	public int x;

	/**
	 * World z coordinate
	 */
	public int z;

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
		x = ( ( Integer ) ct.findTagByName( "xPos" ).getValue() ).intValue();
		z = ( ( Integer ) ct.findTagByName( "zPos" ).getValue() ).intValue();
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
	 * @return the type of the block at that point in the chunk
	 */
	public byte blockType( int bx, int by, int bz )
	{
		if( bx < 0 )
		{
			Chunk north = world.getChunk( x - 1, z );
			return north == null ? 0 : north.blockType( bx + 16, by, bz );
		}
		else if( bx >= 16 )
		{
			Chunk south = world.getChunk( x + 1, z );
			return south == null ? 0 : south.blockType( bx - 16, by, bz );
		}
		else if( bz < 0 )
		{
			Chunk east = world.getChunk( x, z - 1 );
			return east == null ? 0 : east.blockType( bx, by, bz + 16 );
		}
		else if( bz >= 16 )
		{
			Chunk west = world.getChunk( x, z + 1 );
			return west == null ? 0 : west.blockType( bx, by, bz - 16 );
		}
		else if( by < 0 || by >= 128 )
		{
			return 0;
		}

		return blockData[ by + bz * 128 + bx * 2048 ];
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
}
