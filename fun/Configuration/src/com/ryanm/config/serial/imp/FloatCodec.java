
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;

/**
 * @author s9902505
 */
public class FloatCodec implements ConfiguratorCodec<Number>
{
	@Override
	public String encode( Number value )
	{
		return value.toString();
	}

	@Override
	public Number decode( String encoded, Class type ) throws ParseException
	{
		assert type.equals( getType() );

		try
		{
			return new Float( encoded );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}

	@Override
	public Class getType()
	{
		return float.class;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

}
