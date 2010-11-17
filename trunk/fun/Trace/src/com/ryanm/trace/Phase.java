
package com.ryanm.trace;

import com.rugl.renderer.StackedRenderer;

/**
 * Interface for a phase of a game. title screen, menu, game itself,
 * etc
 * 
 * @author ryanm
 */
public interface Phase
{
	/**
	 * Resets the phase to a starting state, called just before the
	 * phase becomes active
	 */
	public void reset();

	/**
	 * Draws the phase
	 * 
	 * @param r
	 *           The renderer to use
	 */
	public void draw( StackedRenderer r );

	/**
	 * Advances the phase
	 * 
	 * @param delta
	 *           The timestep
	 */
	public void advance( float delta );

	/**
	 * Gets the next phase. Call this when {@link #isFinished()} return
	 * true
	 * 
	 * @return The next phase of the game.
	 */
	public Phase next();

	/**
	 * Tests if this {@link Phase} is finished
	 * 
	 * @return <code>true</code> if this phase is finished
	 */
	public boolean isFinished();
}
