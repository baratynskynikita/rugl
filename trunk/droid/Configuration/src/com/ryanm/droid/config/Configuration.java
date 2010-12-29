
package com.ryanm.droid.config;

import java.util.LinkedList;
import java.util.Queue;

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

	private static final String CONF_TAG = "conf";

	private static final String RETURNTO_TAG = "returnto";

	/**
	 * The activity request flag used in
	 * {@link Activity#startActivityForResult(Intent, int)} when
	 * launching the configuration activity (value = 266344)
	 */
	public static final int ACTIVITY_REQUEST_FLAG = 266344;

	private static Queue<ConfigResult> deferredResults =
			new LinkedList<Configuration.ConfigResult>();

	private Configuration()
	{
	}

	/**
	 * Starts a configuration activity
	 * 
	 * @param returnTo
	 *           The activity to return to when we are done configuring
	 * @param roots
	 *           The (annotated) objects to configure.
	 */
	public static void configure( Activity returnTo, Object... roots )
	{
		if( roots == null )
		{
			Log.e( LOG_TAG,
					"Attempt made to configure null roots. I don't think we'll be doing that" );
		}
		else
		{
			Log.i( LOG_TAG, "Launching configuration activity" );
			// launch config
			Intent i = new Intent( returnTo, ConfigActivity.class );
			i.putExtra( CONF_TAG, Extract.extract( roots ).toString() );
			i.putExtra( RETURNTO_TAG, returnTo.getClass().getName() );
			returnTo.startActivityForResult( i, ACTIVITY_REQUEST_FLAG );
		}
	}

	/**
	 * Call this from your activity in {@link Activity}
	 * .onActivityResult() to apply a configuration
	 * 
	 * @param requestCode
	 *           from {@link Activity}.onActivityResult()
	 * @param resultCode
	 *           from {@link Activity}.onActivityResult()
	 * @param data
	 *           from {@link Activity}.onActivityResult()
	 * @param configTargets
	 *           roots of the object trees to apply configurations to
	 */
	public static void onActivityResult( int requestCode, int resultCode, Intent data,
			Object... configTargets )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			Log.i( LOG_TAG, "Applying configuration" );
			try
			{
				Apply.apply( new JSONObject( data.getStringExtra( CONF_TAG ) ), configTargets );
			}
			catch( JSONException e )
			{
				Log.e( LOG_TAG,
						"Problem parsing json data : " + data.getStringExtra( "conf" ), e );
			}
		}
	}

	/**
	 * Call this from your {@link Activity}.onActivityResult() instead
	 * of {@link #onActivityResult(int, int, Intent, Object...)} to
	 * save a configuration for later application. This is useful if
	 * your configuration changes need to be done in a particular
	 * thread like, for example, OpenGL stuff
	 * 
	 * @param requestCode
	 *           from {@link Activity}.onActivityResult()
	 * @param resultCode
	 *           from {@link Activity}.onActivityResult()
	 * @param data
	 *           from {@link Activity}.onActivityResult()
	 */
	public static void deferActivityResult( int requestCode, int resultCode, Intent data )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			synchronized( deferredResults )
			{
				Log.i( LOG_TAG, "Deferring configuration" );
				try
				{
					JSONObject json = new JSONObject( data.getStringExtra( CONF_TAG ) );
					deferredResults.add( new ConfigResult( json ) );
				}
				catch( JSONException e )
				{
					Log.e( LOG_TAG,
							"Problem parsing json data : " + data.getStringExtra( CONF_TAG ), e );
				}
			}
		}
	}

	/**
	 * Call this to apply configurations previously deferred in
	 * {@link #deferActivityResult(int, int, Intent)}
	 * 
	 * @param configTargets
	 *           roots of the object trees to apply configurations to
	 */
	public static void applyDeferredConfigurations( Object... configTargets )
	{
		synchronized( deferredResults )
		{
			while( !deferredResults.isEmpty() )
			{
				Log.i( LOG_TAG, "Applying deferred configuration" );

				ConfigResult cr = deferredResults.poll();
				Apply.apply( cr.config, configTargets );
			}
		}
	}

	/**
	 * For deferring configuration application
	 * 
	 * @author ryanm
	 */
	private static class ConfigResult
	{
		private final JSONObject config;

		private ConfigResult( JSONObject config )
		{
			this.config = config;
		}
	}
}
