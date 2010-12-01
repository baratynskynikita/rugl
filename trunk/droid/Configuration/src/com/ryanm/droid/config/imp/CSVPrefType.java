
package com.ryanm.droid.config.imp;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;
import android.text.method.NumberKeyListener;

import com.ryanm.droid.config.VariableType;

/**
 * For inputting comma-separated number lists
 * 
 * @author ryanm
 * @param <T>
 */
public abstract class CSVPrefType<T> extends VariableType<T>
{
	private final char[] accepted;

	private final String format;

	/**
	 * The number of values needed. Inferred from the format
	 */
	private final int valueCount;

	private int inputType = InputType.TYPE_CLASS_NUMBER;

	private final boolean fractions;

	/**
	 * @param type
	 * @param negative
	 *           <code>true</code> to allow negative numbers
	 * @param fractions
	 *           <code>true</code> to allow fractional numbers
	 * @param format
	 *           expected input format e.g.: "x,y,z"
	 */
	protected CSVPrefType( Class<? extends T> type, boolean negative, boolean fractions,
			String format )
	{
		super( type );
		this.format = format;
		this.fractions = fractions;

		int commas = 0;
		for( int i = 0; i < format.length(); i++ )
		{
			if( format.charAt( i ) == ',' )
			{
				commas++;
			}
		}
		valueCount = commas + 1;

		StringBuffer buff = new StringBuffer( "01234567890, " );
		if( negative )
		{
			inputType |= InputType.TYPE_NUMBER_FLAG_SIGNED;
			buff.append( "-" );
		}
		if( fractions )
		{
			inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
			buff.append( "." );
		}
		accepted = buff.toString().toCharArray();
	}

	@Override
	public Preference buildPreference( Context context, Class type, String value )
	{
		EditTextPreference pref = new EditTextPreference( context );
		pref.setText( value );

		NumberKeyListener dkl = new NumberKeyListener() {
			@Override
			public int getInputType()
			{
				return inputType;
			}

			@Override
			protected char[] getAcceptedChars()
			{
				return accepted;
			}
		};
		pref.getEditText().setKeyListener( dkl );

		pref.getEditText().setHint( format );

		return pref;
	}

	@Override
	protected String formatInput( Object input )
	{
		String sv = ( String ) input;

		String[] va = sv.trim().split( "," );

		if( va.length != valueCount )
		{
			throw new NumberFormatException( "Expected " + valueCount + " values in "
					+ format + " order, found " + va.length );
		}

		float[] fa = new float[ va.length ];

		for( int i = 0; i < fa.length; i++ )
		{
			va[ i ] = va[ i ].trim();
			try
			{
				fa[ i ] = Float.parseFloat( va[ i ] );
			}
			catch( NumberFormatException nfe )
			{
				throw new NumberFormatException( "Could not parse \"" + va[ i ]
						+ "\" as a decimal" );
			}
		}

		StringBuilder buff =
				new StringBuilder( fractions ? Float.toString( fa[ 0 ] )
						: Integer.toString( ( int ) fa[ 0 ] ) );
		for( int i = 1; i < fa.length; i++ )
		{
			buff.append( ", " )
					.append(
							fractions ? Float.toString( fa[ i ] ) : Integer
									.toString( ( int ) fa[ i ] ) );
		}

		return buff.toString();
	}
}
