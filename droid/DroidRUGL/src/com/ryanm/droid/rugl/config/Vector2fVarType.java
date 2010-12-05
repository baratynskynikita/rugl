
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.imp.CSVPrefType;
import com.ryanm.droid.rugl.util.geom.Vector2f;

/**
 * @author ryanm
 */
public class Vector2fVarType extends CSVPrefType<Vector2f>
{
	/***/
	public Vector2fVarType()
	{
		super( Vector2f.class, true, true, "x,y" );
	}

	@Override
	public String encode( Vector2f value )
	{
		return value.x + ", " + value.y;
	}

	@Override
	public Vector2f decode( String encoded, Class runtimeType ) throws ParseException
	{
		try
		{
			float[] v = parse( encoded );
			return new Vector2f( v[ 0 ], v[ 1 ] );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}
}
