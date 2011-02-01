
package com.ryanm.droid.rugl.texture;

import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLUtils;

import com.ryanm.droid.rugl.gl.GLUtil;

/**
 * An image based on a {@link Bitmap}
 * 
 * @author ryanm
 */
public class BitmapImage extends Image
{
	/**
	 * The source bitmap
	 */
	public final Bitmap bitmap;

	/**
	 * @param b
	 */
	public BitmapImage( Bitmap b )
	{
		super( b.getWidth(), b.getHeight(), Format.RGBA );
		bitmap = b;
	}

	@Override
	public void writeToTexture( int x, int y )
	{
		GLES10.glPixelStorei( GLES10.GL_UNPACK_ALIGNMENT, format.bytes );

		GLUtils.texSubImage2D( GLES10.GL_TEXTURE_2D, 0, x, y, bitmap );

		GLUtil.checkGLError();
	}
}
