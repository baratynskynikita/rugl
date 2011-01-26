
package com.ryanm.droid.rugl.config;

import com.ryanm.droid.config.ParseException;
import com.ryanm.droid.config.imp.CSVPrefType;
import com.ryanm.droid.rugl.util.geom.BoundingRectangle;

/**
 * @author ryanm
 */
public class BoundingRectVarType extends CSVPrefType<BoundingRectangle>
{
	/***/
	public BoundingRectVarType()
	{
		super( BoundingRectangle.class, true, true, "x", "y", "width", "height" );
	}

	@Override
	public String encode( BoundingRectangle value )
	{
		return value.x.getMin() + ", " + value.y.getMin() + ", " + value.x.getSpan() + ", "
				+ value.y.getSpan();
	}

	@Override
	public BoundingRectangle decode( String encoded, Class runtimeType )
			throws ParseException
	{
		try
		{
			float[] fa = parse( encoded );
			BoundingRectangle br =
					new BoundingRectangle( fa[ 0 ], fa[ 1 ], fa[ 2 ], fa[ 3 ] );
			return br;
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}
}
