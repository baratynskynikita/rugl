
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.imp.CSVPrefType;
import com.ryanm.droid.rugl.util.math.Range;

/**
 * @author ryanm
 */
public class RangeVarType extends CSVPrefType<Range>
{
	/***/
	public RangeVarType()
	{
		super( Range.class, true, true, "min,max" );
	}

	@Override
	public String encode( Range value )
	{
		return value.getMin() + ", " + value.getMax();
	}

	@Override
	public Range decode( String encoded, Class runtimeType ) throws ParseException
	{
		String[] s = encoded.split( "," );
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
}
