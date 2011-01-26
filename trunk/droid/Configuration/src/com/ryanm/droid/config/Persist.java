
package com.ryanm.droid.config;

import java.util.Arrays;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Allows storage of configurations. Configurations are JSON-encoded
 * and placed in a {@link SharedPreferences}
 * 
 * @author ryanm
 */
public class Persist
{
	private Persist()
	{
	}

	private static SharedPreferences getPrefs( Activity act, String owner )
	{
		return act.getSharedPreferences( owner + ".config", Context.MODE_WORLD_WRITEABLE );
	}

	/**
	 * Saves the configuration of an object tree
	 * 
	 * @param owner
	 *           The activity that owns the data
	 * @param savename
	 *           The name of the saved data
	 * @param roots
	 *           The objects whose configuration to save
	 */
	public static void save( Activity owner, String savename, Object... roots )
	{
		JSONObject json = Extract.extract( roots );
		save( owner, owner.getClass().getName(), savename, json );
	}

	/**
	 * Loads and applies a saved configuration. If there's a problem in
	 * loading or parsing the save it is deleted.
	 * 
	 * @param owner
	 *           The activity that owns the data
	 * @param savename
	 *           The name of the saved data
	 * @param roots
	 *           The objects to apply the configuration to
	 */
	public static void load( Activity owner, String savename, Object... roots )
	{
		JSONObject json = load( owner, owner.getClass().getName(), savename );

		if( json != null )
		{
			Apply.apply( json, roots );
		}
	}

	/**
	 * Deletes a saved configuration
	 * 
	 * @param owner
	 *           The activity that owns the data
	 * @param savename
	 *           The name of the save to delete
	 */
	public static void deleteSave( Activity owner, String savename )
	{
		deleteSave( owner, owner.getClass().getName(), savename );
	}

	/**
	 * Lists saved configurations
	 * 
	 * @param owner
	 *           The activity that owns the saves
	 * @return A sorted list of save names
	 */
	public static String[] listSaves( Activity owner )
	{
		return listSaves( owner, owner.getClass().getName() );
	}

	static void save( Activity act, String owner, String savename, JSONObject json )
	{
		SharedPreferences.Editor prefs = getPrefs( act, owner ).edit();
		prefs.putString( savename, json.toString() );
		prefs.commit();
	}

	static JSONObject load( Activity act, String owner, String savename )
	{
		SharedPreferences prefs = getPrefs( act, owner );
		String json = prefs.getString( savename, null );

		if( json != null )
		{
			try
			{
				return new JSONObject( json );
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Problem parsing save \"" + savename
						+ "\". I'll delete it", e );
				deleteSave( act, owner, savename );
				return null;
			}
		}
		else
		{
			Log.e( Configuration.LOG_TAG, "Requested save \"" + savename + "\" not found" );
			return null;
		}
	}

	static void deleteSave( Activity act, String owner, String savename )
	{
		SharedPreferences.Editor prefs = getPrefs( act, owner ).edit();
		prefs.remove( savename );
		prefs.commit();
	}

	static String[] listSaves( Activity act, String owner )
	{
		SharedPreferences prefs = getPrefs( act, owner );
		Set<String> saves = prefs.getAll().keySet();
		String[] array = saves.toArray( new String[ saves.size() ] );
		Arrays.sort( array );

		return array;
	}
}
