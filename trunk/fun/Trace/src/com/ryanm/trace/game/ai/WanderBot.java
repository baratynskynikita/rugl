
package com.ryanm.trace.game.ai;

import com.ryanm.trace.game.ai.behaviours.AvoidBoundary;
import com.ryanm.trace.game.ai.behaviours.AvoidTrace;
import com.ryanm.trace.game.ai.behaviours.Waypoint;

/**
 * Just wanders randomly
 * 
 * @author ryanm
 */
public class WanderBot extends GeneBot
{
	/***/
	public WanderBot()
	{
		super( "itinerant", "just enjoys the journey", new Waypoint(), new AvoidBoundary(),
				new AvoidTrace() );
	}
}
