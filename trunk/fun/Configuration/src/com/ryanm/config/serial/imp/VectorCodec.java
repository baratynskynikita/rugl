
package com.ryanm.config.serial.imp;

import org.lwjgl.util.vector.Vector3f;

import com.ryanm.config.serial.ConfiguratorCodec;
import com.ryanm.config.serial.ParseException;

/**
 * @author s9902505
 */
public class VectorCodec implements ConfiguratorCodec<Vector3f>
{

	@Override
	public String encode( Vector3f value )
	{
		StringBuilder buff = new StringBuilder();
		buff.append( value.x );
		buff.append( ":" );
		buff.append( value.y );
		buff.append( ":" );
		buff.append( value.z );

		return buff.toString();
	}

	@Override
	public Vector3f decode( String encoded, Class type ) throws ParseException
	{
		assert type.equals( getType() );

		try
		{
			String[] s = encoded.split( ":" );

			if( s.length < 3 )
			{
				throw new ParseException( "Vectors need 3 components" );
			}

			float[] array = new float[ 3 ];

			for( int i = 0; i < array.length; i++ )
			{
				array[ i ] = Float.valueOf( s[ i ] ).floatValue();
			}

			Vector3f vector = new Vector3f( array[ 0 ], array[ 1 ], array[ 2 ] );

			return vector;
		}
		catch( NumberFormatException nfe )
		{
			throw new ParseException( nfe );
		}
	}

	@Override
	public Class getType()
	{
		return Vector3f.class;
	}

	@Override
	public String getDescription()
	{
		return "An x:y:z triple";
	}

}
