
package com.ryanm.trace.game.ai.behaviours;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.Bot.Action;
import com.ryanm.util.geom.LineUtils;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Trig;

/**
 * Follows a series of random waypoints
 * 
 * @author ryanm
 */
public class Waypoint extends Behaviour
{
	private Random rng = new Random();

	private final Vector2f target = new Vector2f();

	private float distance = Float.MAX_VALUE;

	@Override
	public void reset()
	{
		super.reset();
		float margin = 50;
		target.set( margin + ( 800 - 2 * margin ) * rng.nextFloat(), margin + ( 600 - 2 * margin )
				* rng.nextFloat() );
		distance = Float.MAX_VALUE;

		weight = 0.1f;
	}

	@Override
	public void process( Trace t, Arena a )
	{
		float d = VectorUtils.distanceSquared( target, t.position );

		if( d >= distance )
		{
			reset();
		}
		else
		{
			distance = d;
		}

		action = seek( t, target );
	}

	/**
	 * Steers to some target point
	 * 
	 * @param t
	 * @param target
	 * @return the direction to turn
	 */
	public static Action seek( Trace t, Vector2f target )
	{
		Vector2f toTarget = Vector2f.sub( target, t.position, null );
		Vector2f dir = new Vector2f( Trig.cos( t.angleRads ), Trig.sin( t.angleRads ) );

		int i = LineUtils.relativeCCW( 0, 0, dir.x, dir.y, toTarget.x, toTarget.y );

		switch( i )
		{
			case -1:
				return Action.LEFT;
			case 1:
				return Action.RIGHT;
			default:
				return Action.STRAIGHT;
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
