
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Variable;

/**
 * Output varies linearly with time
 * 
 * @author ryanm
 */
public class Linear implements Variable
{
	/**
	 * The coefficient to multiply time with
	 */
	public float coeff = 0;

	@Override
	public float getValue( float time )
	{
		return time * coeff;
	}

}
