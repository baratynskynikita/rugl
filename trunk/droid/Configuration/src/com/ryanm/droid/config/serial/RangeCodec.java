
package com.ryanm.droid.config.serial;

import com.ryanm.droid.rugl.util.math.Range;

/**
 * @author ryanm
 */
public class RangeCodec extends Codec<Range>
{
	@Override
	public String encode( Range value )
	{
		return value.getMin() + "-" + value.getMax();
	}

	@Override
	public Range decode( String encoded, Class runtimeType ) throws ParseException
	{
		String[] s = encoded.split( "-" );
		if( s.length < 2 )
		{
			throw new ParseException( "Only found " + s.length + " elements in " + encoded );
		}

		try
		{
			return new Range( Float.parseFloat( s[ 0 ] ), Float.parseFloat( s[ 1 ] ) );
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
		return Range.class;
	}
}
