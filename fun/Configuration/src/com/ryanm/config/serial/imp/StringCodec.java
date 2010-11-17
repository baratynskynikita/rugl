
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;

/**
 * @author s9902505
 */
public class StringCodec implements ConfiguratorCodec<String>
{

	@Override
	public String encode( String value )
	{
		return value;
	}

	@Override
	public String decode( String encoded, Class type )
	{
		assert type.equals( getType() );

		return encoded;
	}

	@Override
	public Class getType()
	{
		return String.class;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

}
