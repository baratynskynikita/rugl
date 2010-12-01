
package com.ryanm.droid.config.imp;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.VariableType;

/**
 * @author ryanm
 */
public class EnumType extends VariableType<Enum>
{
	/***/
	public EnumType()
	{
		super( Enum.class );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Enum decode( String encoded, Class type ) throws ParseException
	{
		try
		{
			return Enum.valueOf( type, encoded );
		}
		catch( IllegalArgumentException iae )
		{
			throw new ParseException( "No value \"" + encoded + "\" exists in enum \""
					+ type.getName() + "\"" );
		}
	}

	@Override
	public String encode( Enum value )
	{
		return value.name();
	}

	@Override
	protected Preference buildPreference( Context context, Class type, String value )
	{
		ListPreference pref = new ListPreference( context );

		Object[] enums = type.getEnumConstants();
		String[] names = new String[ enums.length ];
		for( int i = 0; i < names.length; i++ )
		{
			names[ i ] = enums[ i ].toString();
		}

		pref.setEntries( names );
		pref.setEntryValues( names );
		pref.setValue( value );

		return pref;
	}
}
