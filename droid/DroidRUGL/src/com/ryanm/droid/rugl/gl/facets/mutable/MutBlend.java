
package com.ryanm.droid.rugl.gl.facets.mutable;

import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.gl.enums.DestinationFactor;
import com.ryanm.droid.rugl.gl.enums.SourceFactor;
import com.ryanm.droid.rugl.gl.facets.Blend;

/**
 * Controls the blending function
 * 
 * @author ryanm
 */
@Variable( "Blend" )
public class MutBlend extends MutableFacet<Blend>
{
	/**
	 * Whether blending is enabled
	 */
	@Variable
	public boolean enabled;

	/**
	 * The blending source factor, see glBlendFunc
	 */
	@Variable
	public SourceFactor srcFactor;

	/**
	 * The blending destination factor, see glBlendFunc
	 */
	@Variable
	public DestinationFactor destFactor;

	/**
	 * @param b
	 */
	public MutBlend( Blend b )
	{
		enabled = b.enabled;
		srcFactor = b.srcFactor;
		destFactor = b.destFactor;
	}

	@Override
	public Blend compile()
	{
		if( enabled )
		{
			return new Blend( srcFactor, destFactor );
		}
		else
		{
			return Blend.disabled;
		}
	}
}
