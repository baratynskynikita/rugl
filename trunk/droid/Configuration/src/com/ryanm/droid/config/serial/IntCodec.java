/**
 * 
 */

package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class IntCodec extends Codec<Number> {
	@Override
	public String encode(Number value) {
		return value.toString();
	}

	@Override
	public Number decode(String encoded, Class type) throws ParseException {
		try {
			return new Integer(encoded);
		} catch (NumberFormatException nfe) {
			throw new ParseException(nfe);
		}
	}

	@Override
	public Class getType() {
		return int.class;
	}
}
