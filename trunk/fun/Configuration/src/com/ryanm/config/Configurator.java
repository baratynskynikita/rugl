/*
 * Created on 20-Jul-2004 by Ryan McNally
 */

package com.ryanm.config;

/**
 * The Configurator object provides information about variables, and
 * methods to change them.
 * 
 * @author ryanm
 */
public interface Configurator
{
	/**
	 * Gets the name of this configurator. This name must not contain
	 * any "/" characters
	 * 
	 * @return the name of this configurator
	 */
	public String getName();

	/**
	 * Gets the names of all configurable variables. The array of
	 * objects returned can consist of two types:
	 * <ul>
	 * <li>Strings - Names of variables.</li>
	 * <li>Configurators - a subgroup of variables.</li>
	 * </ul>
	 * 
	 * @return A list of Objects defining the variables and
	 *         sub-configurators
	 */
	public Object[] getNames();

	/**
	 * Gets the type of a given variable
	 * 
	 * @param name
	 *           The name of the variable
	 * @return The type of the variable, or null if no type has been
	 *         set
	 */
	public Class getType( String name );

	/**
	 * Gets a descriptive string for a variable, such as could be put
	 * in a tooltip
	 * 
	 * @param name
	 *           The variable to describe
	 * @return A descriptive string for the variable
	 */
	public String getDescription( String name );

	/**
	 * Gets a descriptive string for this Configurator
	 * 
	 * @return A descriptive String for the configurator
	 */
	public String getDescription();

	/**
	 * Gets an object that describes the range for a variable. This
	 * will be different for different types of variables. A numerical
	 * variable could have a 2-element array describing the min and max
	 * values, a String variable could have a list of possible values,
	 * a file type could have a list of acceptable file suffixes.
	 * 
	 * @param name
	 *           The variable to get the range for
	 * @return An object describing the range
	 */
	public Object getRange( String name );

	/**
	 * Gets the current value of a variable.
	 * 
	 * @param name
	 *           The name of the variable to inspect
	 * @return The current value of the variable
	 */
	public Object getValue( String name );

	/**
	 * Sets the value of a variable
	 * 
	 * @param name
	 *           The name of the variable to set
	 * @param value
	 *           The new value of that variable
	 */
	public void setValue( String name, Object value );

	/**
	 * Registers a ValueListener to this configuration. All listeners
	 * should be notified whenever a variable is changed.
	 * 
	 * @param listener
	 *           The listener to add
	 */
	public void addValueListener( ValueListener listener );

	/**
	 * Removes a ValueListener. The specified listener will no longer
	 * receive notification about variable changes.
	 * 
	 * @param listener
	 *           The listener to remove
	 */
	public void removeValueListener( ValueListener listener );

	/**
	 * Registers a ConfiguratorListener to this configuration. All
	 * listeners should be notified whenever the Configurator is
	 * altered
	 * 
	 * @param listener
	 *           The listener to add
	 */
	public void addConfiguratorListener( ConfiguratorListener listener );

	/**
	 * Removes a ConfiguratorListener. The specified listener will no
	 * longer receive notification about Configurator changes
	 * 
	 * @param listener
	 *           The listener to remove
	 */
	public void removeConfiguratorListener( ConfiguratorListener listener );

	/**
	 * Enables or disables any widgets associated with a particular
	 * variable. When a variable is disabled with a particular key
	 * object, it cannot be enabled again without using the same key
	 * object.
	 * 
	 * @param variable
	 *           The name of the variable to alter
	 * @param enabled
	 *           true to enable, false to disable
	 * @param key
	 *           The locking object
	 */
	public void setGUIEnabled( String variable, boolean enabled, Object key );

	/**
	 * Gets the status of a variable's widgets, vis-a-vis enablement
	 * 
	 * @param variable
	 *           The variable to query
	 * @return true if enabled, false otherwise
	 */
	public boolean isGUIEnabled( String variable );

	/**
	 * Gets a "/" separated sequence of configurator names that lead to
	 * this configurator
	 * 
	 * @return the path to this configurator
	 */
	public String getPath();

	/**
	 * Gets a reference to the Configurator that contains this
	 * configurator.
	 * 
	 * @return The parent configurator, or null if this is a root
	 *         configurator
	 */
	public Configurator getParent();
}