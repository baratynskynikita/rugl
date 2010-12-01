
package com.ryanm.droid.config.imp;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.method.DigitsKeyListener;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.VariableType;

/**
 * @author ryanm
 */
public class IntType extends VariableType<Number>
{
	/***/
	public IntType()
	{
		super( int.class );
	}

	@Override
	public String encode( Number value )
	{
		return value.toString();
	}

	@Override
	public Number decode( String encoded, Class type ) throws ParseException
	{
		try
		{
			return new Integer( encoded );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}

	@Override
	protected Preference buildPreference( Context context, Class type, String value )
	{
		EditTextPreference pref = new EditTextPreference( context );
		pref.setText( value );
		pref.getEditText().setKeyListener( new DigitsKeyListener( true, false ) );
		pref.getEditText().setSelectAllOnFocus( true );
		return pref;
	}

	@Override
	protected String formatInput( Object input )
	{
		try
		{
			return String.valueOf( Integer.parseInt( ( String ) input ) );
		}
		catch( NumberFormatException nfe )
		{
			throw new NumberFormatException( "Could not parse \"" + input
					+ "\" as an integer" );
		}
	}
}
