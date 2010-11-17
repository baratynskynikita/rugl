
package com.ryanm.trace.game.ai;

import com.ryanm.trace.game.ai.behaviours.AvoidBoundary;
import com.ryanm.trace.game.ai.behaviours.AvoidTrace;
import com.ryanm.trace.game.ai.behaviours.Flee;

/**
 * Runs away from others
 * 
 * @author ryanm
 */
public class TimidBot extends GeneBot
{
	/***/
	public TimidBot()
	{
		super( "coward", "would rather be left alone", new AvoidBoundary(), new AvoidTrace(),
				new Flee() );
	}
}
