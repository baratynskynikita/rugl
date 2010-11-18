
package com.ryanm.droid.configtest;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ryanm.droid.config.Configuration;

/**
 * @author ryanm
 */
public class ConfigTestActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		ScrollView sc = new ScrollView( this );
		TextView tv = new TextView( this );

		try
		{
			ConfTest ct = new ConfTest();

			JSONObject json = Configuration.extract( ct );

			tv.setText( json.toString( 3 ) );
		}
		catch( Exception e )
		{
			tv.setText( e.getMessage() );
		}

		sc.addView( tv );

		setContentView( sc );
	}
}