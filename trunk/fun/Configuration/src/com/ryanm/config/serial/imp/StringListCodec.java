
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;

/**
 * @author s9902505
 */
public class StringListCodec implements ConfiguratorCodec<String[]>
{

	@Override
	public String encode( String[] value )
	{
		StringBuilder buffy = new StringBuilder();

		for( String element : value )
		{
			assert element.indexOf( "|" ) == -1;

			buffy.append( element );
			buffy.append( "|" );
		}

		return buffy.toString();
	}

	@Override
	public String[] decode( String encoded, Class type )
	{
		assert type.equals( getType() );

		String[] sa = encoded.split( "|" );
		return sa;
	}

	@Override
	public Class getType()
	{
		return String[].class;
	}

	@Override
	public String getDescription()
	{
		return "The elements of the list, separated by \"|\" characters";
	}
}
