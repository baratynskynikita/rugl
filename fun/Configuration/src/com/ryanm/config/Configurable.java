/*
 * Created on 20-Jul-2004 by Ryan McNally
 */

package com.ryanm.config;

/**
 * Interface for objects that can supply a Configurator, or are
 * annotated such that a configurator can be constructed reflectively
 * 
 * @author ryanm
 */
public interface Configurable
{
	/**
	 * Gets a Configurator object that describes and can manipulate the
	 * variables of this Configurable. This method must only return
	 * null if the Configurable object is Annotated such that an
	 * AnnotatedConfigurator can be built from it.
	 * 
	 * @return A configurator object, or null if annotated
	 */
	public Configurator getConfigurator();
}
