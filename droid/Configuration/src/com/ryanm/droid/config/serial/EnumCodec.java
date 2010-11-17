package com.ryanm.droid.config.serial;

/**
 * @author ryanm
 */
public class EnumCodec extends Codec<Enum> {
	@SuppressWarnings("unchecked")
	@Override
	public Enum decode(String encoded, Class type) throws ParseException {
		try {
			return Enum.valueOf(type, encoded);
		} catch (IllegalArgumentException iae) {
			throw new ParseException("No value \"" + encoded
					+ "\" exists in enum \"" + type.getName() + "\"");
		}
	}

	@Override
	public String encode(Enum value) {
		return value.name();
	}

	@Override
	public Class getType() {
		return Enum.class;
	}
}
