package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class FloatCodec extends Codec<Number> {
	@Override
	public String encode(Number value) {
		return value.toString();
	}

	@Override
	public Number decode(String encoded, Class type) throws ParseException {
		try {
			return new Float(encoded);
		} catch (NumberFormatException nfe) {
			throw new ParseException(nfe);
		}
	}

	@Override
	public Class getType() {
		return float.class;
	}
}
