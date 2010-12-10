
package com.ryanm.droid.rugl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.opengl.GLES10;

/**
 * OpenGL screenshot facility
 * 
 * @author ryanm
 */
public class ScreenShot
{
	private static Bitmap savePixels( int x, int y, int w, int h )
	{
		int b[] = new int[ w * h ];
		int bt[] = new int[ w * h ];
		IntBuffer ib = IntBuffer.wrap( b );
		ib.position( 0 );
		GLES10.glReadPixels( x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib );
		for( int i = 0; i < h; i++ )
		{// remember that OpenGL bitmap is incompatible with Android
			// bitmap and so some correction need.
			for( int j = 0; j < w; j++ )
			{
				int pix = b[ i * w + j ];
				int pb = pix >> 16 & 0xff;
				int pr = pix << 16 & 0x00ff0000;
				int pix1 = pix & 0xff00ff00 | pr | pb;
				bt[ ( h - i - 1 ) * w + j ] = pix1;
			}
		}

		Bitmap sb = Bitmap.createBitmap( bt, w, h, Config.ARGB_8888 );
		return sb;
	}

	/**
	 * Takes a screenshot
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param output
	 */
	public static void savePNG( int x, int y, int w, int h, File output )
	{
		Bitmap bmp = savePixels( x, y, w, h );
		try
		{
			FileOutputStream fos = new FileOutputStream( output );
			bmp.compress( CompressFormat.PNG, 100, fos );
			fos.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
