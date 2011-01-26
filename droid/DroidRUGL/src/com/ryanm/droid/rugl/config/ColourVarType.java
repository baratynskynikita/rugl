
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
		super( Colour.class, false, false, "r", "g", "b", "a" );
	}

	@Override
	public Preference buildPreference( Context context, Class type, String value )
	{
		EditTextPreference pref =
				( EditTextPreference ) super.buildPreference( context, type, value );

		// the values need unpacked on the way in...
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
		float[] fv = parse( ( String ) input );

		// and packed on the way out
		return String.valueOf( Colour.packInt( ( int ) fv[ 0 ], ( int ) fv[ 1 ],
				( int ) fv[ 2 ], ( int ) fv[ 3 ] ) );
	}

	@Override
	public String encode( Colour value )
	{
		Log.e( Configuration.LOG_TAG,
				"ColourVarType.encode() - This should never be called!" );
		return "";
	}

	@Override
	public Colour decode( String encoded, Class runtimeType ) throws ParseException
	{
		Log.e( Configuration.LOG_TAG,
				"ColourVarType.decode() - This should never be called!" );
		return null;
	}
}
