
package com.ryanm.trace.game.ai;

import com.ryanm.trace.game.ai.behaviours.AvoidBoundary;
import com.ryanm.trace.game.ai.behaviours.AvoidTrace;
import com.ryanm.trace.game.ai.behaviours.Hunt;
import com.ryanm.trace.game.ai.behaviours.Waypoint;

/**
 * Cannot be reasoned with
 * 
 * @author ryanm
 */
public class HunterBot extends GeneBot
{
	/***/
	public HunterBot()
	{
		super( "hunter", "cannot be reasoned with", new AvoidBoundary(), new AvoidTrace(),
				new Hunt(), new Waypoint() );
	}
}
