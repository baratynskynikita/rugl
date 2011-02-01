
package com.ryanm.droid.rugl;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ryanm.droid.rugl.config.BoundingRectVarType;
import com.ryanm.droid.rugl.config.ColourVarType;
import com.ryanm.droid.rugl.config.RangeVarType;
import com.ryanm.droid.rugl.config.Vector2fVarType;
import com.ryanm.droid.rugl.config.Vector3fVarType;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.util.ExceptionHandler;
import com.ryanm.preflect.Preflect;
import com.ryanm.preflect.VariableType;

/**
 * Handy activity that can be simply subclassed. Just remember to call
 * {@link #start(Game, String)} in your
 * {@link #onCreate(android.os.Bundle)} or nothing will happen.
 * Handles starting the {@link ResourceLoader} and key input
 * 
 * @author ryanm
 */
public abstract class GameActivity extends Activity
{
	/***/
	private GameView gameView;

	/**
	 * The {@link Game}
	 */
	protected Game game;

	/**
	 * Call this in your {@link #onCreate(android.os.Bundle)}
	 * implementation
	 * 
	 * @param game
	 * @param supportEmail
	 *           The email address where uncaught exception reports
	 *           should be sent to, or <code>null</code> not to bother
	 */
	public void start( Game game, String supportEmail )
	{
		if( supportEmail != null )
		{
			ExceptionHandler.register( this, supportEmail );
		}

		this.game = game;

		// additional configuration types
		VariableType.register( new ColourVarType() );
		VariableType.register( new RangeVarType() );
		VariableType.register( new Vector2fVarType() );
		VariableType.register( new Vector3fVarType() );
		VariableType.register( new BoundingRectVarType() );

		ResourceLoader.start( getResources() );

		gameView = new GameView( this, game );

		setContentView( gameView );
	}

	/**
	 * Displays a short message to the user
	 * 
	 * @param message
	 * @param longShow
	 *           <code>true</code> for {@link Toast#LENGTH_LONG},
	 *           <code>false</code> for {@link Toast#LENGTH_SHORT}
	 */
	public void showToast( final String message, final boolean longShow )
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				Toast t =
						Toast.makeText( getApplicationContext(), message,
								longShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT );
				t.show();
			}
		} );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if( gameView != null )
		{
			gameView.onPause();
		}
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		// launched from Game.launchConfiguration(). We need to defer
		// application till we're on the OpenGL thread
		Preflect.deferActivityResult( requestCode, resultCode, data );
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if( gameView != null )
		{
			gameView.onResume();
		}
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if( event.getRepeatCount() == 0 )
		{
			if( gameView != null && gameView.game != null )
			{
				Phase p = gameView.game.currentPhase();

				if( p != null )
				{
					p.onKeyDown( keyCode, event );
				}
			}
		}

		return true;
	}

	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event )
	{
		if( gameView != null && gameView.game != null )
		{
			Phase p = gameView.game.currentPhase();

			if( p != null )
			{
				p.onKeyUp( keyCode, event );
			}
		}

		return true;
	}
}
