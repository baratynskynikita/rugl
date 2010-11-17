/*
 * Created on 26-Jul-2004 by Ryan McNally
 */

package com.ryanm.config;

/**
 * All objects that need to keep up to date with changes made to the
 * values of a Configurator should implement this interface, and then
 * register their interest with Configurator.addValueListener()
 * 
 * @author ryanm
 */
public interface ValueListener
{
	/**
	 * Is called by the configurator when a variable is changed
	 * 
	 * @param name
	 *           The name of the variable
	 */
	public void valueChanged( String name );
}
