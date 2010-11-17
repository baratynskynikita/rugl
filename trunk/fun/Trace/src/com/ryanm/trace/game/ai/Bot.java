
package com.ryanm.trace.game.ai;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;

/**
 * @author ryanm
 */
public interface Bot
{
	/**
	 * The options available to bots
	 * 
	 * @author ryanm
	 */
	public enum Action
	{
		/**
		 * Bot should turn left
		 */
		LEFT,
		/**
		 * Bot should turn right
		 */
		RIGHT,
		/**
		 * Bot should go straight
		 */
		STRAIGHT
	};

	/**
	 * @return a name for this bot
	 */
	public String getName();

	/**
	 * @return a description of the bot
	 */
	public String getDescription();

	/**
	 * Called on game start
	 */
	public void reset();

	/**
	 * Decide how to act
	 * 
	 * @param t
	 *           The trace to control
	 * @param game
	 *           the arena
	 * @return the resultant action
	 */
	public Action process( Trace t, Arena game );
}
