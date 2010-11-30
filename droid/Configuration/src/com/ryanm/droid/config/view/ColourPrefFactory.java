
package com.ryanm.droid.config.view;

import org.json.JSONException;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;

import com.ryanm.droid.config.view.ConfigActivity.Variable;
import com.ryanm.droid.rugl.util.Colour;

/**
 * @author ryanm
 */
public class ColourPrefFactory extends CSVPrefFactory
{
	/***/
	public ColourPrefFactory()
	{
		super( Colour.class, false, false, "r,g,b,a" );
	}

	@Override
	public Preference buildPreference( Context context, Variable var )
	{
		EditTextPreference pref =
				( EditTextPreference ) super.buildPreference( context, var );
		int v = Integer.parseInt( var.json.optString( "value" ) );

		String colourString =
				Colour.redi( v ) + ", " + Colour.greeni( v ) + ", " + Colour.bluei( v )
						+ ", " + Colour.alphai( v );

		pref.setText( colourString );

		return pref;
	}

	@Override
	protected void setValue( Variable var, Object value ) throws NumberFormatException,
			JSONException
	{ // use super to check formatting
		super.setValue( var, value );

		String[] s = var.json.getString( "value" ).split( "," );
		int[] c = new int[ 4 ];
		for( int i = 0; i < c.length; i++ )
		{
			s[ i ] = s[ i ].trim();
			c[ i ] = Integer.parseInt( s[ i ] );
		}

		var.json.put( "value",
				String.valueOf( Colour.packInt( c[ 0 ], c[ 1 ], c[ 2 ], c[ 3 ] ) ) );
	}
}
