
package com.ryanm.droid.rugl.gl.facets;

import android.opengl.GLES10;

import com.ryanm.droid.rugl.gl.Facet;
import com.ryanm.droid.rugl.gl.enums.FogMode;
import com.ryanm.droid.rugl.util.Colour;

/**
 * Controls fog
 * 
 * @author ryanm
 */
public class Fog extends Facet<Fog>
{
	/**
	 * Only going to need one of these
	 */
	public static final Fog disabled = new Fog();

	private static float[] fa = new float[ 4 ];

	/***/
	public final boolean enabled;

	/***/
	public final FogMode mode;

	/***/
	public final float density;

	/***/
	public final float start;

	/***/
	public final float end;

	/***/
	public final int colour;

	/**
	 * OpenGL default state
	 */
	private Fog()
	{
		enabled = false;
		mode = FogMode.EXP;
		density = 1;
		start = 0;
		end = 1;
		colour = Colour.packFloat( 0, 0, 0, 0 );
	}

	/**
	 * @param mode
	 * @param density
	 * @param start
	 * @param end
	 * @param colour
	 */
	public Fog( FogMode mode, float density, float start, float end, int colour )
	{
		enabled = true;
		this.mode = mode;
		this.density = density;
		this.start = start;
		this.end = end;
		this.colour = colour;
	}

	@Override
	public int compareTo( Fog another )
	{
		float d = ( enabled ? 1 : 0 ) - ( another.enabled ? 1 : 0 );

		if( d == 0 )
		{
			d = mode.mode - another.mode.mode;

			if( d == 0 )
			{
				d = density - another.density;

				if( d == 0 )
				{
					d = start - another.start;

					if( d == 0 )
					{
						d = end - another.end;

						if( d == 0 )
						{
							d = colour - another.colour;
						}
					}
				}
			}
		}

		return ( int ) Math.signum( d );
	}

	@Override
	public void transitionFrom( Fog facet )
	{
		if( enabled != facet.enabled )
		{
			GLES10.glEnable( GLES10.GL_FOG );

			if( enabled )
			{
				// force settings
				GLES10.glFogx( GLES10.GL_FOG_MODE, mode.mode );
				GLES10.glFogf( GLES10.GL_FOG_DENSITY, density );
				GLES10.glFogf( GLES10.GL_FOG_START, start );
				GLES10.glFogf( GLES10.GL_FOG_END, end );
				Colour.toArray( colour, fa );
				GLES10.glFogfv( GLES10.GL_FOG_COLOR, fa, 0 );
			}
		}
		else if( enabled )
		{ // minimal transition
			if( mode != facet.mode )
			{
				GLES10.glFogx( GLES10.GL_FOG_MODE, mode.mode );
			}
			if( density != facet.density )
			{
				GLES10.glFogf( GLES10.GL_FOG_DENSITY, density );
			}
			if( start != facet.start )
			{
				GLES10.glFogf( GLES10.GL_FOG_START, start );
			}
			if( end != facet.end )
			{
				GLES10.glFogf( GLES10.GL_FOG_END, end );
			}
			if( colour != facet.colour )
			{
				Colour.toArray( colour, fa );
				GLES10.glFogfv( GLES10.GL_FOG_COLOR, fa, 0 );
			}
		}
	}
}
