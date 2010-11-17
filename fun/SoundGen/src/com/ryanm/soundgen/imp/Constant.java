
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Variable;

/**
 * Gadzooks! A non-changing variable!
 * 
 * @author ryanm
 */
public class Constant implements Variable
{
	/**
	 * The value returned by this envelope for any time
	 */
	public float value = 0;

	/**
	 * @param value
	 */
	public Constant( float value )
	{
		this.value = value;
	}

	@Override
	public float getValue( float time )
	{
		return value;
	}

}
