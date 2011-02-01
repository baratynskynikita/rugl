
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.preflect.ParseException;
import com.ryanm.preflect.imp.CSVPrefType;

/**
 * @author ryanm
 */
public class Vector3fVarType extends CSVPrefType<Vector3f>
{
	/***/
	public Vector3fVarType()
	{
		super( Vector3f.class, true, true, "x", "y", "z" );
	}

	@Override
	public String encode( Vector3f value )
	{
		return value.x + ", " + value.y + ", " + value.z;
	}

	@Override
	public Vector3f decode( String encoded, Class runtimeType ) throws ParseException
	{
		try
		{
			float[] v = parse( encoded );
			return new Vector3f( v[ 0 ], v[ 1 ], v[ 2 ] );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}
}
