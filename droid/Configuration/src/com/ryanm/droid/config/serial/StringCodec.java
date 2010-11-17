package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class StringCodec extends Codec<String> {
	@Override
	public String encode(String value) {
		return value;
	}

	@Override
	public String decode(String encoded, Class type) {
		assert type.equals(getType());

		return encoded;
	}

	@Override
	public Class getType() {
		return String.class;
	}
}
