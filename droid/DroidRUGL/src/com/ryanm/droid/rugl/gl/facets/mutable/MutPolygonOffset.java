
package com.ryanm.droid.rugl.gl.facets.mutable;

import javax.microedition.khronos.opengles.GL11;

import com.ryanm.droid.rugl.gl.facets.PolygonOffset;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;

/**
 * Controls polygon offset
 * 
 * @author ryanm
 */
@Variable( "Polygon offset" )
public class MutPolygonOffset extends MutableFacet<PolygonOffset>
{
	/**
	 * Controls {@link GL11#GL_POLYGON_OFFSET_FILL}
	 */
	@Variable
	public boolean enabled;

	/***/
	@Variable
	@Summary( "A scale factor that is used to create a "
			+ "variable depth offset for each polygon." )
	public float factor;

	/***/
	@Variable
	@Summary( "Multiplied by an implementation-specific value "
			+ "to create a constant depth offset." )
	public float units;

	/**
	 * @param po
	 */
	public MutPolygonOffset( PolygonOffset po )
	{
		enabled = po.enabled;
		factor = po.factor;
		units = po.units;
	}

	@Override
	public PolygonOffset compile()
	{
		if( enabled )
		{
			return new PolygonOffset( factor, units );
		}
		else
		{
			return PolygonOffset.disabled;
		}
	}
}
