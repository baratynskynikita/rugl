
package com.ryanm.droid.rugl.config;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.imp.CSVPrefType;
import com.ryanm.droid.rugl.util.Colour;

/**
 * @author ryanm
 */
public class ColourVarType extends CSVPrefType<Colour>
{
	/***/
	public ColourVarType()
	{
		super( Colour.class, false, false, "r,g,b,a" );
	}

	@Override
	public Preference buildPreference( Context context, Class type, String value )
	{
		EditTextPreference pref =
				( EditTextPreference ) super.buildPreference( context, type, value );
		int v = Integer.parseInt( value );

		String colourString =
				Colour.redi( v ) + ", " + Colour.greeni( v ) + ", " + Colour.bluei( v )
						+ ", " + Colour.alphai( v );

		pref.setText( colourString );

		return pref;
	}

	@Override
	protected String formatInput( Object input )
	{
		String formatted = super.formatInput( input );

		String[] s = formatted.split( "," );
		int[] c = new int[ 4 ];
		for( int i = 0; i < c.length; i++ )
		{
			s[ i ] = s[ i ].trim();
			c[ i ] = Integer.parseInt( s[ i ] );
		}

		return String.valueOf( Colour.packInt( c[ 0 ], c[ 1 ], c[ 2 ], c[ 3 ] ) );
	}

	@Override
	public String encode( Colour value )
	{
		Log.e( Configuration.LOG_TAG, "This should never be called!" );
		return "";
	}

	@Override
	public Colour decode( String encoded, Class runtimeType ) throws ParseException
	{
		Log.e( Configuration.LOG_TAG, "This should never be called!" );
		return null;
	}
}
