
package com.ryanm.config.serial;

/**
 * Defines an object that can encode and decode variables of a given
 * type
 * 
 * @author s9902505
 * @param <P>
 *           The type of object handled by the codec
 */
public interface ConfiguratorCodec<P>
{
	/**
	 * Encodes the value of a given object
	 * 
	 * @param value
	 *           The value to encode
	 * @return The String encoding for the value, or null if encoding
	 *         was not possible
	 */
	public String encode( P value );

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
	public P decode( String encoded, Class runtimeType ) throws ParseException;

	/**
	 * Gets the type identifier that this codec object can handle
	 * 
	 * @return The type identifier
	 */
	public Class getType();

	/**
	 * Gets a description of the saved format
	 * 
	 * @return a descriptive string
	 */
	public String getDescription();
}
