
package com.ryanm.droid.config.view;

import org.json.JSONException;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.view.ConfigActivity.Variable;

/**
 * @author ryanm
 */
public class EnumPrefFactory extends PreferenceFactory
{
	/***/
	public EnumPrefFactory()
	{
		super( Enum.class );
	}

	@Override
	public Preference buildPreference( Context context, final Variable var )
	{
		ListPreference pref = new ListPreference( context );

		Object[] enums = var.type.getEnumConstants();
		String[] names = new String[ enums.length ];
		for( int i = 0; i < names.length; i++ )
		{
			names[ i ] = enums[ i ].toString();
		}

		pref.setEntries( names );
		pref.setEntryValues( names );
		pref.setValue( var.json.optString( "value" ) );

		pref.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange( Preference preference, Object newValue )
			{
				try
				{
					var.json.put( "value", newValue );
				}
				catch( JSONException e )
				{
					Log.e( Configuration.LOG_TAG, "shouldn't happen", e );
				}
				return true;
			}
		} );

		return pref;
	}
}
