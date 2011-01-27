
package com.ryanm.droid.rugl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.ryanm.droid.rugl.input.Touch;

/**
 * @author ryanm
 */
final class GameView extends GLSurfaceView
{
	/**
	 * The game
	 */
	public final Game game;

	/**
	 * @param context
	 * @param game
	 */
	public GameView( Context context, Game game )
	{
		super( context );

		setDebugFlags( DEBUG_CHECK_GL_ERROR );

		this.game = game;

		setEGLConfigChooser( new GLSurfaceView.EGLConfigChooser() {

			@Override
			public EGLConfig chooseConfig( EGL10 egl, EGLDisplay display )
			{
				// Ensure that we get a 16bit framebuffer. Otherwise,
				// we'll fall back to Pixelflinger on some device (read:
				// Samsung I7500)

				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };

				EGLConfig[] configs = new EGLConfig[ 1 ];

				int[] result = new int[ 1 ];

				egl.eglChooseConfig( display, attributes, configs, 1, result );

				return configs[ 0 ];
			}
		} );

		setRenderer( game );
	}

	@Override
	public boolean onTouchEvent( final MotionEvent event )
	{
		Touch.onTouchEvent( event );

		return true;
	}
}
