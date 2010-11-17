
package com.ryanm.droid.rugl.gl.facets;

import com.ryanm.droid.rugl.gl.Facet;
import com.ryanm.droid.rugl.gl.enums.DestinationFactor;
import com.ryanm.droid.rugl.gl.enums.SourceFactor;

import android.opengl.GLES10;

/**
 * Controls the blending function
 * 
 * @author ryanm
 */
public class Blend extends Facet<Blend>
{
	/**
	 * Whether blending is enabled
	 */
	public final boolean enabled;

	/**
	 * The blending source factor, see glBlendFunc
	 */
	public final SourceFactor srcFactor;

	/**
	 * The blending destination factor, see glBlendFunc
	 */
	public final DestinationFactor destFactor;

	/**
	 * Use this to disable blending
	 */
	public static final Blend disabled = new Blend();

	private Blend()
	{
		enabled = false;
		srcFactor = SourceFactor.ONE;
		destFactor = DestinationFactor.ZERO;
	}

	/**
	 * Blend is assumed to be enabled
	 * 
	 * @param srcFactor
	 * @param destFactor
	 */
	public Blend( SourceFactor srcFactor, DestinationFactor destFactor )
	{
		enabled = true;
		this.srcFactor = srcFactor;
		this.destFactor = destFactor;
	}

	@Override
	public void transitionFrom( Blend b )
	{
		if( enabled && !b.enabled )
		{
			GLES10.glEnable( GLES10.GL_BLEND );
		}
		else if( !enabled && b.enabled )
		{
			GLES10.glDisable( GLES10.GL_BLEND );
		}

		if( enabled )
		{
			GLES10.glBlendFunc( srcFactor.value, destFactor.value );
		}
	}

	@Override
	public int compareTo( Blend b )
	{
		if( !enabled && !b.enabled )
		{ // both disabled, doesn't matter what the other values are
			return 0;
		}

		int d = ( enabled ? 1 : 0 ) - ( b.enabled ? 1 : 0 );

		if( d == 0 )
		{
			d = srcFactor.ordinal() - b.srcFactor.ordinal();

			if( d == 0 )
			{
				d = destFactor.ordinal() - b.destFactor.ordinal();
			}
		}

		return d;
	}

	@Override
	public String toString()
	{
		return "Blending " + enabled + " src:" + srcFactor + " dest:" + destFactor;
	}
}
