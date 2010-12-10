
package com.ryanm.droid.rugl.gl.facets.mutable;

import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.gl.enums.ComparisonFunction;
import com.ryanm.droid.rugl.gl.facets.AlphaTest;

/**
 * Controls the alpha test
 * 
 * @author ryanm
 */
@Variable( "Alpha test" )
public class MutAlphaTest extends MutableFacet<AlphaTest>
{
	/**
	 * If the alpha test is enabled or not
	 */
	@Variable
	public boolean enabled;

	/**
	 * The alpha function
	 */
	@Variable
	public ComparisonFunction func;

	/**
	 * The reference value
	 */
	@Variable
	public float ref;

	/**
	 * @param at
	 */
	public MutAlphaTest( AlphaTest at )
	{
		enabled = at.enabled;
		func = at.func;
		ref = at.ref;
	}

	@Override
	public AlphaTest compile()
	{
		if( enabled )
		{
			return new AlphaTest( func, ref );
		}
		else
		{
			return AlphaTest.disabled;
		}
	}
}
