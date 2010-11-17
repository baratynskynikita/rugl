
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Variable;

/**
 * Simply adds 2 envelopes together
 * 
 * @author ryanm
 */
public class Addition implements Variable
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
	public Addition( Variable e, Variable f )
	{
		super();
		this.e = e;
		this.f = f;
	}

	@Override
	public float getValue( float time )
	{
		return e.getValue( time ) + f.getValue( time );
	}
}
