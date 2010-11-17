
package com.ryanm.trace.game.ai.behaviours;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.Bot.Action;

/**
 * @author ryanm
 */
public abstract class Behaviour
{
	/***/
	protected float weight = 0;

	/***/
	protected Action action = Action.STRAIGHT;

	/**
	 * Reset state
	 */
	public void reset()
	{
		action = Action.STRAIGHT;
		weight = 0;
	}

	/**
	 * Process the arean and deicde what to do
	 * 
	 * @param t
	 * @param a
	 */
	public abstract void process( Trace t, Arena a );

	/**
	 * @return The desired action
	 */
	public Action getAction()
	{
		return action;
	}

	/**
	 * @return the weight of the action
	 */
	public float getWeight()
	{
		return weight;
	}

	/**
	 * @return genetic data
	 */
	public abstract float[] getGenome();

	/**
	 * @param gene
	 */
	public abstract void setGenome( float[] gene );
}
