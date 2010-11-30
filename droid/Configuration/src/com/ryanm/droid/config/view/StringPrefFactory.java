
package com.ryanm.droid.config.view;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;

import com.ryanm.droid.config.view.ConfigActivity.Variable;

/**
 * @author ryanm
 */
public class StringPrefFactory extends PreferenceFactory
{
	/***/
	public StringPrefFactory()
	{
		super( String.class );
	}

	@Override
	public Preference buildPreference( Context context, final Variable var )
	{
		EditTextPreference pref = new EditTextPreference( context );

		pref.setText( var.json.optString( "value" ) );

		pref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE );

		pref.getEditText().setSelectAllOnFocus( true );

		return pref;
	}
}
