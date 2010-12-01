
package com.ryanm.droid.config;

/**
 * Use this to indicate failure when codec-ing in your
 * {@link VariableType}
 * 
 * @author ryanm
 */
public class ParseException extends Exception
{
	/**
	 * @param message
	 */
	public ParseException( String message )
	{
		super( message );
	}

	/**
	 * @param cause
	 */
	public ParseException( Throwable cause )
	{
		super( cause );
	}
}
