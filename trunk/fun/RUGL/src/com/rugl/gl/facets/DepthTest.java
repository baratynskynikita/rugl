
package com.rugl.gl.facets;

import org.lwjgl.opengl.GL11;

import com.rugl.gl.Facet;
import com.rugl.gl.enums.ComparisonFunction;

/**
 * Controls the depth test
 * 
 * @author ryanm
 */
public class DepthTest extends Facet<DepthTest>
{
	/**
	 * Whether the depth test is enabled or not
	 */
	public final boolean enabled;

	/**
	 * The depth test function, see glDepthFunc
	 */
	public final ComparisonFunction func;

	/**
	 * Use this to disable the depth test
	 */
	public static final DepthTest disabled = new DepthTest();

	private DepthTest()
	{
		enabled = false;
		func = ComparisonFunction.LESS;
	}

	/**
	 * Depth test is enabled
	 * 
	 * @param func
	 */
	public DepthTest( ComparisonFunction func )
	{
		enabled = true;
		this.func = func;
	}

	@Override
	public void transitionFrom( DepthTest d )
	{
		if( enabled && !d.enabled )
		{
			GL11.glEnable( GL11.GL_DEPTH_TEST );
		}
		else if( !enabled && d.enabled )
		{
			GL11.glDisable( GL11.GL_DEPTH_TEST );
		}

		if( enabled )
		{
			GL11.glDepthFunc( func.value );
		}
	}

	@Override
	public int compareTo( DepthTest d )
	{
		if( !enabled && !d.enabled )
		{
			return 0;
		}

		int i = ( enabled ? 1 : 0 ) - ( d.enabled ? 1 : 0 );

		if( i == 0 )
		{
			i = func.ordinal() - d.func.ordinal();
		}

		return i;
	}

	@Override
	public String toString()
	{
		return "Depth test " + enabled + " func = " + func.value;
	}
}
