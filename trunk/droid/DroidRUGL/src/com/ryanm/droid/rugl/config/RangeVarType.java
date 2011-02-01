
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.preflect.ParseException;
import com.ryanm.preflect.imp.CSVPrefType;

/**
 * @author ryanm
 */
public class RangeVarType extends CSVPrefType<Range>
{
	/***/
	public RangeVarType()
	{
		super( Range.class, true, true, "min", "max" );
	}

	@Override
	public String encode( Range value )
	{
		return value.getMin() + ", " + value.getMax();
	}

	@Override
	public Range decode( String encoded, Class runtimeType ) throws ParseException
	{
		try
		{
			float[] v = parse( encoded );

			return new Range( v[ 0 ], v[ 1 ] );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}
}
