
package com.ryanm.droid.config.view;

import org.json.JSONException;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.method.DigitsKeyListener;

import com.ryanm.droid.config.view.ConfigActivity.Variable;

/**
 * @author ryanm
 */
public class IntPrefFactory extends PreferenceFactory
{
	/***/
	public IntPrefFactory()
	{
		super( int.class );
	}

	@Override
	protected Preference buildPreference( Context context, Variable var )
	{
		EditTextPreference pref = new EditTextPreference( context );
		pref.setText( var.json.optString( "value" ) );
		pref.getEditText().setKeyListener( new DigitsKeyListener( true, false ) );
		pref.getEditText().setSelectAllOnFocus( true );
		return pref;
	}

	@Override
	protected void setValue( Variable var, Object value ) throws JSONException
	{
		try
		{
			var.json.put( "value", Integer.parseInt( ( String ) value ) );
		}
		catch( NumberFormatException nfe )
		{
			throw new NumberFormatException( "Could not parse \"" + value
					+ "\" as an integer" );
		}
	}
}
