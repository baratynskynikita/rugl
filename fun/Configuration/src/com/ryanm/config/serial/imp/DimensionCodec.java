
package com.ryanm.config.serial.imp;

import org.lwjgl.util.Dimension;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;

/**
 * @author ryanm
 */
public class DimensionCodec implements ConfiguratorCodec<Dimension>
{

	@Override
	public Dimension decode( String encoded, Class type ) throws ParseException
	{
		assert type.equals( getType() );
		String[] s = encoded.split( "x" );

		if( s.length >= 2 )
		{
			int w = Integer.parseInt( s[ 0 ] );
			int h = Integer.parseInt( s[ 1 ] );

			return new Dimension( w, h );
		}

		throw new ParseException( "Could not parse \"" + encoded + "\" into a dimension" );
	}

	@Override
	public String encode( Dimension value )
	{
		assert value != null;

		return value.getWidth() + "x" + value.getHeight();
	}

	@Override
	public String getDescription()
	{
		return "<width>x<height>";
	}

	@Override
	public Class getType()
	{
		return Dimension.class;
	}

}
