
package com.ryanm.droid.config.serial;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.ryanm.droid.config.Configuration;

/**
 * Defines an object that can encode and decode variables of a given
 * type
 * 
 * @author ryanm
 * @param <P>
 *           The type of object handled by the codec
 */
public abstract class Codec<P>
{
	/**
	 * Encodes the value of a given object
	 * 
	 * @param value
	 *           The value to encode
	 * @return The String encoding for the value, or null if encoding
	 *         was not possible
	 */
	public abstract String encode( P value );

	/**
	 * Decodes the encoded string into a value object
	 * 
	 * @param encoded
	 *           The encoded string
	 * @param runtimeType
	 *           The desired type of the object
	 * @return The value of the encoded string
	 * @throws ParseException
	 */
	public abstract P decode( String encoded, Class runtimeType ) throws ParseException;

	/**
	 * Gets the type identifier that this codec object can handle
	 * 
	 * @return The type identifier
	 */
	public abstract Class getType();

	/**
	 * Maps from a type identifier to a codec that reports that it can
	 * handle that type
	 */
	private static Map<Class, Codec<? extends Object>> codecs =
			new HashMap<Class, Codec<? extends Object>>();
	static
	{
		registerCodec( new VoidCodec() );
		registerCodec( new BooleanCodec() );
		registerCodec( new IntCodec() );
		registerCodec( new FloatCodec() );
		registerCodec( new StringCodec() );
		registerCodec( new EnumCodec() );
		registerCodec( new RangeCodec() );
		registerCodec( new Vector2fCodec() );
		registerCodec( new Vector3fCodec() );
	}

	/**
	 * Registers a codec for use
	 * 
	 * @param codec
	 *           The codec to register
	 * @return true if the codec is registered successfully, false
	 *         otherwise
	 */
	public static boolean registerCodec( Codec<?> codec )
	{
		if( !codecs.containsKey( codec.getType() ) )
		{
			codecs.put( codec.getType(), codec );
			return true;
		}
		else
		{
			System.err.println( "Attempted to register duplicate codec for type \""
					+ codec.getType() + "\"" );
			return false;
		}
	}

	/**
	 * Finds the most suitable codec for a type. Searches through the
	 * type's superclass hierarchy for a matching codec
	 * 
	 * @param type
	 * @return The most specific codec possible, or null if none is
	 *         found
	 */
	public static Codec getCodec( Class type )
	{
		Log.i( Configuration.LOG_TAG, "looking for type " + type );

		Codec codec = codecs.get( type );

		while( codec == null && type != null )
		{
			type = type.getSuperclass();

			codec = codecs.get( type );
		}

		Log.i( Configuration.LOG_TAG, "found " + codec + " for type " + type );

		return codec;
	}
}
