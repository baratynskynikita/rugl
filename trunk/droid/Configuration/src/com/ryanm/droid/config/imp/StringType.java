
package com.ryanm.droid.config.imp;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;

import com.ryanm.droid.config.VariableType;

/**
 * @author ryanm
 */
public class StringType extends VariableType<String>
{
	/***/
	public StringType()
	{
		super( String.class );
	}

	@Override
	public String encode( String value )
	{
		return value;
	}

	@Override
	public String decode( String encoded, Class type )
	{
		return encoded;
	}

	@Override
	protected Preference buildPreference( Context context, Class type, String value )
	{
		EditTextPreference pref = new EditTextPreference( context );
		pref.setText( value );
		pref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE );
		pref.getEditText().setSelectAllOnFocus( true );

		return pref;
	}

}
