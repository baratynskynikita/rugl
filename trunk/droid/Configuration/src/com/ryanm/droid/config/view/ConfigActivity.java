
package com.ryanm.droid.config.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.ryanm.droid.config.Configuration;

/**
 * @author ryanm
 */
public class ConfigActivity extends PreferenceActivity
{
	private JSONObject json;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		String input = ( String ) getIntent().getSerializableExtra( "conf" );

		PreferenceManager manager = getPreferenceManager();

		try
		{
			json = new JSONObject( input );

			setPreferenceScreen( buildScreen( json, manager ) );
		}
		catch( JSONException e )
		{
			Toast t = Toast.makeText( this, "Problem parsing JSON", Toast.LENGTH_LONG );
			t.show();
			Log.e( Configuration.LOG_TAG, "oops!", e );
			finish();
		}
	}

	@Override
	public void onBackPressed()
	{
		setResult( RESULT_OK, new Intent().putExtra( "conf", json.toString() ) );

		super.onBackPressed();
	}

	private PreferenceScreen buildScreen( JSONObject json, PreferenceManager manager )
	{
		JSONArray n = json.names();

		PreferenceScreen screen = manager.createPreferenceScreen( this );
		screen.setSummary( json.optString( "desc" ) );
		screen.setOrder( json.optInt( "order", Preference.DEFAULT_ORDER ) );

		Map<String, List<Preference>> categories = new HashMap<String, List<Preference>>();

		screen.setOrderingAsAdded( false );

		for( int i = 0; i < n.length(); i++ )
		{
			try
			{
				String name = n.getString( i );

				Object v = json.get( name );

				if( v instanceof JSONObject )
				{
					JSONObject jv = ( JSONObject ) v;
					Variable var = new Variable( name, jv );

					if( !categories.containsKey( var.category ) )
					{
						categories.put( var.category, new LinkedList<Preference>() );
					}

					if( var.type == null )
					{
						PreferenceScreen sub = buildScreen( var.json, manager );
						sub.setTitle( name );

						categories.get( var.category ).add( sub );
					}
					else
					{
						PreferenceFactory pf = PreferenceFactory.getFactory( var.type );
						if( pf != null )
						{
							Preference pref = pf.getPreference( this, var );

							categories.get( var.category ).add( pref );
						}
					}
				}
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}

		for( Map.Entry<String, List<Preference>> entry : categories.entrySet() )
		{
			PreferenceCategory cat = new PreferenceCategory( this );
			cat.setOrderingAsAdded( false );
			cat.setTitle( entry.getKey() == null ? screen.getSummary() : entry.getKey() );
			screen.addPreference( cat );
			for( Preference p : entry.getValue() )
			{
				cat.addPreference( p );
			}
		}

		return screen;
	}

	/**
	 * Encapsulates data about a variable
	 * 
	 * @author ryanm
	 */
	public static class Variable
	{
		/**
		 * Variable name
		 */
		public final String name;

		/**
		 * Variable description. Might be empty, but will not be
		 * <code>null</code>
		 */
		public final String description;

		/**
		 * Variable category, or <code>null</code> if absent.
		 */
		public final String category;

		/**
		 * Variable type, or null for a configurable object
		 */
		public final Class type;

		/**
		 * Preference order
		 */
		public final int order;

		/**
		 * Variable description json
		 */
		public final JSONObject json;

		private Variable( String name, JSONObject json )
		{
			this.name = name;
			this.json = json;

			description = json.optString( "desc" );

			category = json.has( "cat" ) ? json.optString( "cat" ) : null;

			String t = json.optString( "type" );

			if( "".equals( t ) )
			{ // configurable type
				type = null;
			}
			else
			{ // variable
				type = Configuration.getType( t );
			}

			order = json.optInt( "order", Preference.DEFAULT_ORDER );
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
