
package com.ryanm.droid.rugl;

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

		setRenderer( game );

		this.game = game;
	}

	@Override
	public boolean onTouchEvent( final MotionEvent event )
	{
		Touch.onTouchEvent( event );

		return true;
	}
}
