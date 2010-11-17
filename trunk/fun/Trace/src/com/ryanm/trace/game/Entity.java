
package com.ryanm.trace.game;

import com.rugl.renderer.StackedRenderer;

/**
 * @author ryanm
 */
public interface Entity
{
	/**
	 * @param delta
	 * @return <code>true</code> if the entity is finished, and should
	 *         be removed from the arena
	 */
	public boolean advance( float delta );

	/**
	 * @param r
	 */
	public void draw( StackedRenderer r );
}
