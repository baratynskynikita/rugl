
package com.ryanm.droid.rugl;

import com.ryanm.droid.rugl.input.Touch;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

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
