
package com.ryanm.soundgen;

import java.io.Serializable;

/**
 * Defines how a value changes over time
 * 
 * @author ryanm
 */
public interface Variable extends Cloneable, Serializable
{
	/**
	 * Gets the value
	 * 
	 * @param time
	 * @return the value at the specified time
	 */
	public float getValue( float time );
}
