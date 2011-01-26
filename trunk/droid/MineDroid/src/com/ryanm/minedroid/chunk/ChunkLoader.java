
package com.ryanm.minedroid.chunk;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.res.ResourceLoader.Loader;
import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.minedroid.World;

/**
 * This is packaged up like this so it can happen on the resource
 * loading thread, rather than the main render thread
 * 
 * @author ryanm
 */
public abstract class ChunkLoader extends Loader<Chunk>
{
	private final World world;

	private final int x;

	private final int z;

	/**
	 * @param w
	 * @param x
	 * @param z
	 */
	public ChunkLoader( World w, int x, int z )
	{
		world = w;
		this.x = x;
		this.z = z;
	}

	@Override
	public void load()
	{
		String dir1 = Integer.toString( ( int ) Range.wrap( x, 0, 64 ), 36 );
		String dir2 = Integer.toString( ( int ) Range.wrap( z, 0, 64 ), 36 );

		File chunkFile =
				new File( world.dir, dir1 + "/" + dir2 + "/c." + Integer.toString( x, 36 )
						+ "." + Integer.toString( z, 36 ) + ".dat" );

		try
		{
			resource = new Chunk( world, chunkFile );
		}
		catch( IOException ioe )
		{
			Log.e( Game.RUGL_TAG, "Problem loading chunk (" + x + "," + z + ") from "
					+ chunkFile, ioe );

			resource = null;
		}
	}

	@Override
	public String toString()
	{
		return "chunk " + x + ", " + z;
	}
}
