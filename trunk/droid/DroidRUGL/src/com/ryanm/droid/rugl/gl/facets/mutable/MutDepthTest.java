
package com.ryanm.droid.rugl.gl.facets.mutable;

import com.ryanm.droid.rugl.gl.enums.ComparisonFunction;
import com.ryanm.droid.rugl.gl.facets.DepthTest;
import com.ryanm.preflect.annote.Variable;

/**
 * Controls the depth test
 * 
 * @author ryanm
 */
@Variable( "Depth test" )
public class MutDepthTest extends MutableFacet<DepthTest>
{
	/**
	 * Whether the depth test is enabled or not
	 */
	@Variable
	public boolean enabled;

	/**
	 * The depth test function, see glDepthFunc
	 */
	@Variable
	public ComparisonFunction func;

	/**
	 * @param dt
	 */
	public MutDepthTest( DepthTest dt )
	{
		enabled = dt.enabled;
		func = dt.func;
	}

	@Override
	public DepthTest compile()
	{
		if( enabled )
		{
			return new DepthTest( func );
		}
		else
		{
			return DepthTest.disabled;
		}
	}
}
