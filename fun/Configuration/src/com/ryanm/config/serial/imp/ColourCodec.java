
package com.ryanm.config.serial.imp;

import org.lwjgl.util.Color;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;

/**
 * @author s9902505
 */
public class ColourCodec implements ConfiguratorCodec<Color>
{

	@Override
	public String encode( Color value )
	{
		StringBuilder buff = new StringBuilder();
		int[] array =
				new int[] { value.getRed(), value.getGreen(), value.getBlue(),
						value.getAlpha() };

		for( int i = 0; i < array.length; i++ )
		{
			buff.append( array[ i ] );

			if( i != array.length - 1 )
			{
				buff.append( ":" );
			}
		}

		return buff.toString();
	}

	@Override
	public Color decode( String encoded, Class type ) throws ParseException
	{
		assert type.equals( getType() );

		try
		{
			String[] s = encoded.split( ":" );

			if( s.length < 4 )
			{
				throw new ParseException( "Colours need 4 components" );
			}

			int[] array = new int[ 4 ];

			for( int i = 0; i < array.length; i++ )
			{
				array[ i ] = Integer.valueOf( s[ i ] ).intValue();

				if( array[ i ] < 0 || array[ i ] > 255 )
				{
					throw new ParseException( "Components must be in range 0-255" );
				}
			}

			return new Color( array[ 0 ], array[ 1 ], array[ 2 ], array[ 3 ] );
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}

	@Override
	public Class getType()
	{
		return Color.class;
	}

	@Override
	public String getDescription()
	{
		return "R:G:B:A, each in the range 0-255";
	}

}
