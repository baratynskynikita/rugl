
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;

/**
 * @author ryanm
 */
public class EnumCodec implements ConfiguratorCodec<Enum>
{
	@SuppressWarnings( "unchecked" )
	@Override
	public Enum decode( String encoded, Class type ) throws ParseException
	{
		try
		{
			return Enum.valueOf( type, encoded );
		}
		catch( IllegalArgumentException iae )
		{
			throw new ParseException( "No value \"" + encoded + "\" exists in enum \""
					+ type.getName() + "\"" );
		}
	}

	@Override
	public String encode( Enum value )
	{
		return value.name();
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public Class getType()
	{
		return Enum.class;
	}

}
