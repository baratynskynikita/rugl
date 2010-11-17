
package com.ryanm.minedroid;

import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.GameActivity;

/**
 * Entry point for application. Not much happens here, look to
 * {@link BlockView} for actual behaviour
 * 
 * @author ryanm
 */
public class MineDroidActivity extends GameActivity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		try
		{
			// change the 1 to whatever world number you want
			World w = new World( 1 );

			Game game = new Game( this, new BlockView( w ) );

			start( game );
		}
		catch( IOException e )
		{
			Toast t =
					Toast.makeText( getApplicationContext(),
							"Could not load world level.dat, check logcat", Toast.LENGTH_LONG );
			t.show();

			Log.e( Game.RUGL_TAG, "Problem loading level.dat", e );

			finish();
		}
	}
}