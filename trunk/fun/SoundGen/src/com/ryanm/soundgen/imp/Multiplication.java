
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Variable;

/**
 * Multiplies two variables together
 * 
 * @author ryanm
 */
public class Multiplication implements Variable
{
	/**
	 * 
	 */
	public Variable e;

	/**
	 * 
	 */
	public Variable f;

	/**
	 * @param e
	 * @param f
	 */
	public Multiplication( Variable e, Variable f )
	{
		this.e = e;
		this.f = f;
	}

	@Override
	public float getValue( float time )
	{
		return e.getValue( time ) * f.getValue( time );
	}

}
