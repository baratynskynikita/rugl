
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;
import com.ryanm.util.math.Range;

/**
 * @author ryanm
 */
public class RangeCodec implements ConfiguratorCodec<Range>
{

	@Override
	public Range decode( String encoded, Class runtimeType ) throws ParseException
	{
		String[] s = encoded.trim().split( ":" );

		try
		{
			if( s.length >= 2 )
			{
				float min = Float.parseFloat( s[ 0 ] );
				float max = Float.parseFloat( s[ 1 ] );

				return new Range( min, max );
			}
			else if( s.length == 1 )
			{
				// probably transitioning from an old config file
				return new Range( 0, Float.parseFloat( s[ 0 ] ) );
			}
		}
		catch( NumberFormatException e )
		{
		}

		return null;
	}

	@Override
	public String encode( Range value )
	{
		return value.getMin() + ":" + value.getMax();
	}

	@Override
	public Class getType()
	{
		return Range.class;
	}

	@Override
	public String getDescription()
	{
		return "<min>:<max>";
	}

}
