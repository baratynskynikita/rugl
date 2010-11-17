
package com.rugl.gl.facets;

import org.lwjgl.opengl.GL11;

import com.rugl.gl.Facet;
import com.rugl.gl.enums.RasterMode;

/**
 * Controls polygon rasterisation
 * 
 * @author ryanm
 */
public class PolygonMode extends Facet<PolygonMode>
{
	/**
	 * Mode for back-facing polygons
	 */
	public final RasterMode front;

	/**
	 * Mode for front-facing polygons
	 */
	public final RasterMode back;

	/***/
	public PolygonMode()
	{
		front = RasterMode.FILL;
		back = RasterMode.FILL;
	}

	/**
	 * @param front
	 * @param back
	 */
	public PolygonMode( RasterMode front, RasterMode back )
	{
		this.front = front;
		this.back = back;
	}

	@Override
	public void transitionFrom( PolygonMode facet )
	{
		if( front != facet.front )
		{
			GL11.glPolygonMode( GL11.GL_FRONT, front.value );
		}

		if( back != facet.back )
		{
			GL11.glPolygonMode( GL11.GL_BACK, back.value );
		}
	}

	@Override
	public int compareTo( PolygonMode o )
	{
		int d = front.ordinal() - o.front.ordinal();

		if( d == 0 )
		{
			d = back.ordinal() - o.back.ordinal();
		}

		return d;
	}

	@Override
	public String toString()
	{
		return "Polygon mode front = " + front + " back = " + back;
	}
}
