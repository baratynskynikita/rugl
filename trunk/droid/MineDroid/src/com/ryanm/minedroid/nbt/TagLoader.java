
package com.ryanm.minedroid.nbt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.util.Log;

import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.res.ResourceLoader.Loader;

/**
 * Loads a {@link Tag} file
 * 
 * @author ryanm
 */
public abstract class TagLoader extends Loader<Tag>
{
	private final File f;

	/**
	 * @param file
	 */
	public TagLoader( File file )
	{
		f = file;
	}

	@Override
	public void load()
	{
		try
		{
			resource = Tag.readFrom( new FileInputStream( f ) );
		}
		catch( IOException e )
		{
			Log.e( ResourceLoader.LOG_TAG, "Problem loading tag file", e );
		}
	}
}
