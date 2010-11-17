package com.ryanm.droid.config.serial;

/**
 * Indicates that an error has occured in a {@link Codec}
 * 
 * @author ryanm
 */
public class ParseException extends Exception {
	/**
	 * @param cause
	 */
	public ParseException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param string
	 */
	public ParseException(String string) {
		super(string);
	}

}
