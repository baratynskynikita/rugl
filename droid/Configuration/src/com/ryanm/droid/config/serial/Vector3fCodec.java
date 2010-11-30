
package com.ryanm.droid.config.serial;

import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * @author ryanm
 */
public class Vector3fCodec extends Codec<Vector3f>
{
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

	@Override
	public Class getType()
	{
		return Vector3f.class;
	}
}
