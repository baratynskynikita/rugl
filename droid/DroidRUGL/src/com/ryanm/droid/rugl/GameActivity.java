
package com.ryanm.droid.rugl;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ryanm.droid.rugl.res.ResourceLoader;

/**
 * Handy activity that can be simply subclassed. Just remember to call
 * {@link #start(Game)} in your {@link #onCreate(android.os.Bundle)}
 * or nothing will happen. Handles starting the {@link ResourceLoader}
 * and key input
 * 
 * @author ryanm
 */
public class GameActivity extends Activity
{
	/***/
	private GameView gameView;

	/**
	 * Call this in your {@link #onCreate(android.os.Bundle)}
	 * implementation
	 * 
	 * @param game
	 */
	protected void start( Game game )
	{
		ResourceLoader.start( getResources() );

		try
		{
			PackageInfo pi = getPackageManager().getPackageInfo( getPackageName(), 0 );
			Log.i( Game.RUGL_TAG, "Version number = " + pi.versionCode );
			Log.i( Game.RUGL_TAG, "Version name = " + pi.versionName );
			Log.i( Game.RUGL_TAG, "SDK version = " + Build.VERSION.SDK );
		}
		catch( NameNotFoundException e )
		{
			Log.e( Game.RUGL_TAG, "could not find package info", e );
		}

		gameView = new GameView( this, game );

		setContentView( gameView );
	}

	/**
	 * Displays a short message
	 * 
	 * @param message
	 */
	public void showToast( final String message )
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				Toast t =
						Toast.makeText( getApplicationContext(), message, Toast.LENGTH_SHORT );
				t.show();
			}
		} );
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		gameView.onPause();
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		// if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG &&
		// resultCode == RESULT_OK )
		// {
		// Configuration.applyConfiguration( data, null );
		// }
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		gameView.onResume();
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if( event.getRepeatCount() == 0 )
		{
			gameView.game.currentPhase().onKeyDown( keyCode, event );
		}

		return true;
	}

	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event )
	{
		gameView.game.currentPhase().onKeyUp( keyCode, event );
		return true;
	}
}
