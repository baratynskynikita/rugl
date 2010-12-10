
package com.ryanm.droid.rugl.gl.facets.mutable;

import com.ryanm.droid.rugl.gl.Facet;

/**
 * @author ryanm
 * @param <T>
 */
public abstract class MutableFacet<T extends Facet>
{
	/**
	 * Create a {@link Facet}
	 * 
	 * @return a {@link Facet}
	 */
	public abstract T compile();
}
