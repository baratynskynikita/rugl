
package com.ryanm.trace.game.ai.behaviours;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.Bot.Action;
import com.ryanm.util.math.Trig;

/**
 * Avoids the boundary
 * 
 * @author ryanm
 */
public class AvoidBoundary extends Behaviour
{
	private static final float border = 5;

	@Override
	public void process( Trace t, Arena a )
	{
		float turnTime = 360 / t.rotate;
		float turnCirc = t.speed * turnTime;
		float turnRad = turnCirc / Trig.PI / 2;

		float tx = t.position.x + turnRad * Trig.cos( t.angleRads );
		float ty = t.position.y + turnRad * Trig.sin( t.angleRads );

		reset();

		if( tx < border )
		{
			action = ty > t.position.y ? Action.RIGHT : Action.LEFT;
			weight = 1;

			if( t.position.y > 600 - turnRad )
			{
				action = Action.LEFT;
			}
			else if( t.position.y < turnRad )
			{
				action = Action.RIGHT;
			}
		}
		else if( tx > 800 - border )
		{
			action = ty > t.position.y ? Action.LEFT : Action.RIGHT;
			weight = 1;

			if( t.position.y > 600 - turnRad )
			{
				action = Action.RIGHT;
			}
			else if( t.position.y < turnRad )
			{
				action = Action.LEFT;
			}
		}

		if( ty < border )
		{
			action = tx > t.position.x ? Action.LEFT : Action.RIGHT;
			weight = 1;

			if( t.position.x > 800 - turnRad )
			{
				action = Action.RIGHT;
			}
			else if( t.position.x < turnRad )
			{
				action = Action.LEFT;
			}
		}
		else if( ty > 600 - border )
		{
			action = tx > t.position.x ? Action.RIGHT : Action.LEFT;
			weight = 1;

			if( t.position.x > 800 - turnRad )
			{
				action = Action.LEFT;
			}
			else if( t.position.x < turnRad )
			{
				action = Action.RIGHT;
			}
		}
	}

	@Override
	public float[] getGenome()
	{
		return new float[ 0 ];
	}

	@Override
	public void setGenome( float[] gene )
	{
		assert gene.length == 0;
	}
}
