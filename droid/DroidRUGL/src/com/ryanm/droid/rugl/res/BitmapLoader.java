
package com.ryanm.droid.rugl.res;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ryanm.droid.rugl.res.ResourceLoader.Loader;
import com.ryanm.droid.rugl.texture.BitmapImage;

/**
 * Loads a {@link BitmapImage}
 * 
 * @author ryanm
 */
public abstract class BitmapLoader extends Loader<BitmapImage>
{
	private final int id;

	/**
	 * @param id
	 */
	public BitmapLoader( int id )
	{
		this.id = id;
	}

	@Override
	public void load()
	{
		Bitmap b = BitmapFactory.decodeResource( ResourceLoader.resources, id );
		resource = new BitmapImage( b );
	}

	@Override
	public String toString()
	{
		return "Bitmap id = " + id
				+ ( resource != null ? " " + resource.width + "x" + resource.height : "" );
	}
}