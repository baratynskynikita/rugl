
package com.ryanm.droid.config.imp;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.ryanm.droid.config.VariableType;

/**
 * @author ryanm
 */
public class BooleanType extends VariableType<Boolean>
{
	/***/
	public BooleanType()
	{
		super( boolean.class );
	}

	@Override
	public String encode( Boolean value )
	{
		return value.toString();
	}

	@Override
	public Boolean decode( String encoded, Class type )
	{
		return new Boolean( encoded );
	}

	@Override
	protected Preference buildPreference( Context context, Class type, String value )
	{
		CheckBoxPreference pref = new CheckBoxPreference( context );
		pref.setChecked( Boolean.parseBoolean( value ) );
		return pref;
	}
}
