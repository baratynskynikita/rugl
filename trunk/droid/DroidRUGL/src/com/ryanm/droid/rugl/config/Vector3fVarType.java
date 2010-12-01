
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.imp.CSVPrefType;
import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * @author ryanm
 */
public class Vector3fVarType extends CSVPrefType<Vector3f>
{
	/***/
	public Vector3fVarType()
	{
		super( Vector3f.class, true, true, "x,y,z" );
	}

	@Override
	public String encode( Vector3f value )
	{
		return value.x + ", " + value.y + ", " + value.z;
	}

	@Override
	public Vector3f decode( String encoded, Class runtimeType ) throws ParseException
	{
		String[] s = encoded.split( "," );
		if( s.length < 3 )
		{
			throw new ParseException( "Only found " + s.length + " elements in " + encoded );
		}

		try
		{
			return new Vector3f( Float.parseFloat( s[ 0 ] ), Float.parseFloat( s[ 1 ] ),
					Float.parseFloat( s[ 2 ] ) );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( "Could not parse at least one of \"" + s[ 0 ]
					+ "\", \"" + s[ 1 ] + "\" or \"" + s[ 2 ] + "\" from " + encoded );
		}
	}
}
