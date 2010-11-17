package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class VoidCodec extends Codec<Void> {
	@Override
	public String encode(Void value) {
		return "void";
	}

	@Override
	public Void decode(String encoded, Class runtimeType) throws ParseException {
		return null;
	}

	@Override
	public Class getType() {
		return void.class;
	}
}
