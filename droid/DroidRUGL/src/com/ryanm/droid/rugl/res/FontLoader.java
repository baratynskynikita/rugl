
package com.ryanm.droid.rugl.res;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.res.ResourceLoader.Loader;
import com.ryanm.droid.rugl.text.Font;

/**
 * Loads a font
 * 
 * @author ryanm
 */
public abstract class FontLoader extends Loader<Font>
{
	private final int resourceID;

	private final boolean mipmap;

	/**
	 * @param resourceID
	 * @param mipmap
	 */
	public FontLoader( int resourceID, boolean mipmap )
	{
		this.resourceID = resourceID;
		this.mipmap = mipmap;
	}

	@Override
	public void load()
	{
		InputStream is = ResourceLoader.resources.openRawResource( resourceID );
		try
		{
			resource = new Font( is );
		}
		catch( IOException e )
		{
			exception = e;
			Log.e( Game.RUGL_TAG, "Problem loading font " + resourceID, e );
		}
	}

	@Override
	public final void complete()
	{
		if( resource != null )
		{
			resource.init( mipmap );

			fontLoaded();
		}
	}

	/**
	 * Called once the font has been constructed from the file and
	 * loaded into opengl
	 */
	public abstract void fontLoaded();

	@Override
	public String toString()
	{
		return "Font loader " + resourceID + " mipmap = " + mipmap;
	}
}
