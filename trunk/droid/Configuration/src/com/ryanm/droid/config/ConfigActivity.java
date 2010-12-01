
package com.ryanm.droid.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author ryanm
 */
public class ConfigActivity extends PreferenceActivity
{
	private JSONObject json;

	private MenuItem apply, cancel, save, load, delete;

	private static final int newNameDialogID = 1;

	/**
	 * ClassName of requester
	 */
	private String returnTo;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		String input = ( String ) getIntent().getSerializableExtra( "conf" );
		returnTo = ( String ) getIntent().getSerializableExtra( "returnTo" );

		try
		{
			json = new JSONObject( input );

			setPreferenceScreen( buildScreen( json ) );
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
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		apply = menu.add( "Apply" );
		apply.setIcon( android.R.drawable.ic_menu_set_as );

		SubMenu persist = menu.addSubMenu( "Persist" );
		persist.setIcon( android.R.drawable.ic_menu_save );

		save = persist.add( "Save" );
		load = persist.add( "Load" );
		delete = persist.add( "Delete" );

		cancel = menu.add( "Cancel" );
		cancel.setIcon( android.R.drawable.ic_menu_delete );

		return true;
	}

	@Override
	public boolean onMenuItemSelected( int featureId, MenuItem item )
	{
		if( item == apply )
		{
			setResult( RESULT_OK, new Intent().putExtra( "conf", json.toString() ) );
			finish();
			return true;
		}
		else if( item == cancel )
		{
			setResult( RESULT_CANCELED );
			finish();
			return true;
		}
		else if( item == save )
		{
			buildSaveDialog().show();
		}
		else if( item == load )
		{
			buildLoadDialog().show();
		}
		else if( item == delete )
		{
			buildDeleteDialog().show();
		}

		return false;
	}

	@Override
	protected Dialog onCreateDialog( int id )
	{
		switch( id )
		{
			case newNameDialogID:
				return buildNewNameDialog();

			default:
				return super.onCreateDialog( id );
		}
	}

	private PreferenceScreen buildScreen( JSONObject json )
	{
		JSONArray names = json.names();

		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen( this );
		screen.setSummary( json.optString( "desc" ) );
		screen.setOrder( json.optInt( "order", Preference.DEFAULT_ORDER ) );

		Map<String, List<Preference>> categories = new HashMap<String, List<Preference>>();
		Map<String, Integer> minCatOrders = new HashMap<String, Integer>();

		screen.setOrderingAsAdded( false );

		for( int i = 0; i < names.length(); i++ )
		{
			try
			{
				String name = names.getString( i );

				Object v = json.get( name );

				if( v instanceof JSONObject )
				{
					JSONObject jv = ( JSONObject ) v;
					Variable var = new Variable( name, jv );

					if( !categories.containsKey( var.category ) )
					{
						categories.put( var.category, new LinkedList<Preference>() );
						minCatOrders.put( var.category, new Integer( 0 ) );
					}

					Preference pref = null;
					if( var.type == null )
					{
						pref = buildScreen( var.json );
						pref.setTitle( name );
					}
					else
					{
						VariableType pf = VariableType.get( var.type );
						if( pf != null )
						{
							pref = pf.getPreference( this, var );
						}
					}

					if( pref != null )
					{
						categories.get( var.category ).add( pref );
						minCatOrders.put(
								var.category,
								new Integer( Math.min( minCatOrders.get( var.category )
										.intValue(), var.order ) ) );
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
			cat.setTitle( entry.getKey() == null ? json.optString( "desc" ) : entry.getKey() );
			cat.setOrder( minCatOrders.get( entry.getKey() ).intValue() );
			screen.addPreference( cat );
			for( Preference p : entry.getValue() )
			{
				cat.addPreference( p );
			}
		}

		return screen;
	}

	private Dialog buildSaveDialog()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder( this );
		adb.setTitle( "Save" );

		final String[] saves = Persist.listSaves( ConfigActivity.this, returnTo );
		adb.setItems( saves, new OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which )
			{
				Persist.save( ConfigActivity.this, returnTo, saves[ which ], json );
			}
		} );

		adb.setPositiveButton( "New", new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int whichButton )
			{
				showDialog( newNameDialogID );
			}
		} );

		adb.setNegativeButton( "Cancel", null );
		return adb.create();
	}

	private Dialog buildNewNameDialog()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder( this );
		adb.setTitle( "New save" );

		final EditText input = new EditText( this );
		input.setSelectAllOnFocus( true );
		input.setHint( "Save name" );
		adb.setView( input );
		adb.setPositiveButton( "Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int whichButton )
			{
				String savename = input.getText().toString().trim();
				Persist.save( ConfigActivity.this, returnTo, savename, json );
			}
		} );

		adb.setNegativeButton( "Cancel", null );
		return adb.create();
	}

	private Dialog buildLoadDialog()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder( this );
		adb.setTitle( "Load" );

		final String[] saves = Persist.listSaves( ConfigActivity.this, returnTo );
		adb.setItems( saves, new OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which )
			{
				JSONObject loaded =
						Persist.load( ConfigActivity.this, returnTo, saves[ which ] );
				if( loaded != null )
				{
					setPreferenceScreen( buildScreen( loaded ) );
					json = loaded;
				}
				else
				{
					Toast.makeText( ConfigActivity.this,
							"Deleting corrupted save\n" + saves[ which ], Toast.LENGTH_LONG )
							.show();
				}
			}
		} );

		adb.setNegativeButton( "Cancel", null );
		return adb.create();
	}

	private Dialog buildDeleteDialog()
	{
		AlertDialog.Builder adb = new AlertDialog.Builder( this );
		adb.setTitle( "Delete" );

		final String[] saves = Persist.listSaves( ConfigActivity.this, returnTo );
		final boolean[] delete = new boolean[ saves.length ];
		Arrays.fill( delete, false );

		adb.setMultiChoiceItems( saves, delete, new OnMultiChoiceClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which, boolean isChecked )
			{
				delete[ which ] = isChecked;
			}
		} );

		adb.setPositiveButton( "Delete", new OnClickListener() {
			@Override
			public void onClick( DialogInterface dialog, int which )
			{
				for( int i = 0; i < saves.length; i++ )
				{
					if( delete[ i ] )
					{
						Persist.deleteSave( ConfigActivity.this, returnTo, saves[ i ] );
					}
				}
			}
		} );

		adb.setNegativeButton( "Cancel", null );
		return adb.create();
	}

	/**
	 * Encapsulates data about a variable
	 * 
	 * @author ryanm
	 */
	static class Variable
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
		 * Variable type, or <code>null</code> for a configurable object
		 */
		public final Class type;

		/**
		 * Preference order
		 */
		public final int order;

		/**
		 * Variable description json. Store the value back in here.
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
				type = Util.getType( t );
			}

			order = json.optInt( "order", Preference.DEFAULT_ORDER );
		}
	}
}
