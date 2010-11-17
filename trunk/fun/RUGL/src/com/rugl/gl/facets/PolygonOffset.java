
package com.rugl.gl.facets;

import org.lwjgl.opengl.GL11;

import com.rugl.gl.Facet;

/**
 * Controls polygon offset
 * 
 * @author ryanm
 */
public class PolygonOffset extends Facet<PolygonOffset>
{
	/**
	 * Controls {@link GL11#GL_POLYGON_OFFSET_FILL}
	 */
	public final boolean enabled;

	/**
	 * A scale factor that is used to create a variable depth offset
	 * for each polygon.
	 */
	public final float factor;

	/**
	 * Multiplied by an implementation-specific value to create a
	 * constant depth offset.
	 */
	public final float units;

	/**
	 * Use this to disable the polygon offset
	 */
	public static final PolygonOffset disabled = new PolygonOffset();

	private PolygonOffset()
	{
		enabled = false;
		factor = 0;
		units = 0;
	}

	/**
	 * Polygon offset is enabled
	 * 
	 * @param factor
	 * @param units
	 */
	public PolygonOffset( float factor, float units )
	{
		enabled = true;
		this.factor = factor;
		this.units = units;
	}

	@Override
	public void transitionFrom( PolygonOffset po )
	{
		if( enabled != po.enabled )
		{
			if( enabled )
			{
				GL11.glEnable( GL11.GL_POLYGON_OFFSET_FILL );
			}
			else
			{
				GL11.glDisable( GL11.GL_POLYGON_OFFSET_FILL );
			}
		}

		if( factor != po.factor || units != po.units )
		{
			GL11.glPolygonOffset( factor, units );
		}
	}

	@Override
	public int compareTo( PolygonOffset po )
	{
		int f = enabled ? 1 : 0;
		int pof = po.enabled ? 1 : 0;

		float d = f - pof;

		if( d == 0 )
		{
			d = factor - po.factor;

			if( d == 0 )
			{
				d = units - po.units;
			}
		}

		return ( int ) Math.signum( d );
	}

	@Override
	public String toString()
	{
		return "Polygon offset: "
				+ ( enabled ? "factor = " + factor + " units = " + units : "disabled " );
	}
}
