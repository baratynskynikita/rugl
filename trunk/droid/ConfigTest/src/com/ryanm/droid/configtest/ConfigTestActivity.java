
package com.ryanm.droid.configtest;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ryanm.droid.config.Configuration;

/**
 * @author ryanm
 */
public class ConfigTestActivity extends Activity
{
	private ConfTest testy = new ConfTest();

	private TextView tv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		ScrollView sc = new ScrollView( this );
		tv = new TextView( this );
		sc.addView( tv );

		try
		{
			tv.setText( Configuration.extract( testy ).toString( 3 ) );
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}

		setContentView( sc );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		Configuration.onActivityResult( requestCode, resultCode, data, testy );

		tv.setText( testy.toString() );
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_MENU )
		{
			Configuration.launchConfig( this, testy );

			return true;
		}

		return super.onKeyDown( keyCode, event );
	}
}