
package com.ryanm.droid.config;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ryanm.droid.config.annote.Variable;

/**
 * Handy utility for manipulating object trees that have been marked
 * up with {@link Variable} annotations
 * 
 * @author ryanm
 */
public class Configuration
{
	/**
	 * Logging tag
	 */
	public static final String LOG_TAG = "Configuration";

	/**
	 * The activity request flag used in
	 * {@link Activity#startActivityForResult(Intent, int)} when
	 * launching the configuration activity (value = 266344)
	 */
	public static final int ACTIVITY_REQUEST_FLAG = 266344;

	private Configuration()
	{
		// no instances
	}

	/**
	 * Starts a configuration activity
	 * 
	 * @param returnTo
	 *           The activity to return to when we are done configuring
	 * @param roots
	 *           The (annotated) objects to configure
	 */
	public static void configure( Activity returnTo, Object... roots )
	{
		// launch config
		Intent i = new Intent( returnTo, ConfigActivity.class );
		i.putExtra( "conf", Extract.extract( roots ).toString() );
		i.putExtra( "returnto", returnTo.getClass().getName() );
		returnTo.startActivityForResult( i, ACTIVITY_REQUEST_FLAG );
	}

	/**
	 * Call this from your activity to apply a configuration
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @param roots
	 */
	public static void onActivityResult( int requestCode, int resultCode, Intent data,
			Object... roots )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			try
			{
				Apply.apply( new JSONObject( data.getStringExtra( "conf" ) ), roots );
			}
			catch( JSONException e )
			{
				Log.e( LOG_TAG,
						"Problem parsing json data : " + data.getStringExtra( "conf" ), e );
			}
		}
	}
}
