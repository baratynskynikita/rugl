
package com.ryanm.droid.config.serial;

import com.ryanm.droid.rugl.util.geom.Vector2f;

/**
 * @author ryanm
 */
public class Vector2fCodec extends Codec<Vector2f>
{
	@Override
	public String encode( Vector2f value )
	{
		return value.x + "," + value.y;
	}

	@Override
	public Vector2f decode( String encoded, Class runtimeType ) throws ParseException
	{
		String[] s = encoded.split( "," );
		if( s.length < 2 )
		{
			throw new ParseException( "Only found " + s.length + " elements in " + encoded );
		}

		try
		{
			return new Vector2f( Float.parseFloat( s[ 0 ] ), Float.parseFloat( s[ 1 ] ) );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( "Could not parse at least one of \"" + s[ 0 ]
					+ "\" or \"" + s[ 1 ] + "\" from " + encoded );
		}
	}

	@Override
	public Class getType()
	{
		return Vector2f.class;
	}
}
