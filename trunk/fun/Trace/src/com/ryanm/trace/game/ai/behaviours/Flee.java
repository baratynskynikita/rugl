
package com.ryanm.trace.game.ai.behaviours;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.Bot.Action;
import com.ryanm.util.geom.VectorUtils;

/**
 * @author ryanm
 */
public class Flee extends Behaviour
{
	/***/
	public float fleeWeight = 0.1f;

	@Override
	public void process( Trace t, Arena a )
	{
		reset();

		Trace closest = null;
		float minD = Float.MAX_VALUE;

		for( int i = 0; i < a.traces.length; i++ )
		{
			if( t != a.traces[ i ] )
			{
				float d = VectorUtils.distanceSquared( t.position, a.traces[ i ].position );

				if( d < minD && !a.traces[ i ].player.dead )
				{
					minD = d;
					closest = a.traces[ i ];
				}
			}
		}

		if( closest != null )
		{
			weight = fleeWeight;

			action = Waypoint.seek( t, closest.position );

			if( action == Action.LEFT )
			{
				action = Action.RIGHT;
			}
			else
			{
				action = Action.LEFT;
			}
		}
	}

	@Override
	public float[] getGenome()
	{
		return new float[] { fleeWeight };
	}

	@Override
	public void setGenome( float[] gene )
	{
		fleeWeight = gene[ 0 ];
	}
}
