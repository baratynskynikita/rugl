
package com.ryanm.droid.config.view;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.ryanm.droid.config.view.ConfigActivity.Variable;

/**
 * @author ryanm
 */
public class BooleanPrefFactory extends PreferenceFactory
{
	/**
	 * @param type
	 */
	public BooleanPrefFactory( Class type )
	{
		super( type );
	}

	@Override
	public Preference buildPreference( Context context, final Variable var )
	{
		final CheckBoxPreference pref = new CheckBoxPreference( context );

		pref.setChecked( var.json.optBoolean( "value", false ) );

		return pref;
	}
}
