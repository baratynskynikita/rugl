package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class BooleanCodec extends Codec<Boolean> {
	@Override
	public String encode(Boolean value) {
		return value.toString();
	}

	@Override
	public Boolean decode(String encoded, Class type) {
		return new Boolean(encoded);
	}

	@Override
	public Class getType() {
		return boolean.class;
	}
}
