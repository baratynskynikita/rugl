
package com.ryanm.config.serial;

import com.ryanm.config.Configurator;

/**
 * Converts between the current state of a configurator tree and
 * strings
 * 
 * @author s9902505
 */
public interface ConfigurationStringFormatter
{
	/**
	 * Generates a formatted String, suitable for writing to a file,
	 * from the supplied Configurators
	 * 
	 * @param confs
	 *           An array of configurators, whose state should be
	 *           encoded into the string
	 * @return A formatted String
	 */
	public String format( Configurator[] confs );

	/**
	 * Sets the state of the supplied configurators according to the
	 * supplied string
	 * 
	 * @param confs
	 *           The configurators to set
	 * @param string
	 *           The string to set from
	 * @throws ParseException
	 */
	public void parse( Configurator[] confs, String string ) throws ParseException;
}
