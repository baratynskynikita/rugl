
package com.ryanm.config.serial.imp;

import com.ryanm.config.serial.ConfiguratorCodec;

/**
 * @author s9902505
 */
public class BooleanCodec implements ConfiguratorCodec<Boolean>
{
	private static final String[] yes = new String[] { "true", "1", "yes", "y", "t" };

	private static String description = null;

	@Override
	public String encode( Boolean value )
	{
		return value.toString();
	}

	@Override
	public Boolean decode( String encoded, Class type )
	{
		assert type.equals( getType() );

		encoded = encoded.toLowerCase();

		for( int i = 0; i < yes.length; i++ )
		{
			if( encoded.equals( yes[ i ] ) )
			{
				return new Boolean( true );
			}
		}

		return new Boolean( false );
	}

	@Override
	public Class getType()
	{
		return boolean.class;
	}

	@Override
	public String getDescription()
	{
		if( description == null )
		{
			StringBuilder buff = new StringBuilder( "\"" );
			buff.append( yes[ 0 ] );

			for( int i = 0; i < yes.length; i++ )
			{
				buff.append( "\", \"" );
				buff.append( yes[ i ] );
			}

			buff.append( "\" for true, or anything else for false" );
		}

		return description;
	}
}
