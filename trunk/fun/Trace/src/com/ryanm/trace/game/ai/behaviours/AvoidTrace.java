
package com.ryanm.trace.game.ai.behaviours;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Collision;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.Bot.Action;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Trig;

/**
 * Steers away from other traces, steers towards gaps
 * 
 * @author ryanm
 */
public class AvoidTrace extends Behaviour
{
	/**
	 * Length of test segments, in terms of trace speed
	 */
	public float testRange = 0.5f;

	/**
	 * Angle of test segments from straight
	 */
	public float testAngle = Trig.toRadians( 20 );

	private static final Vector2f v = new Vector2f();

	@Override
	public void process( Trace t, Arena a )
	{
		float testSegLength = testRange * t.speed;

		// left
		v.set( Trig.cos( t.angleRads + testAngle ), Trig.sin( t.angleRads + testAngle ) );
		v.scale( testSegLength );
		Collision[] leftC =
				a.collision( t.position.x, t.position.y, t.position.x + v.x, t.position.y + v.y );
		Collision.sort( t.position, leftC );
		Collision left = leftC.length == 0 || leftC[ 0 ].seg.type == Trace.GAP ? null : leftC[ 0 ];

		// right
		v.set( Trig.cos( t.angleRads - testAngle ), Trig.sin( t.angleRads - testAngle ) );
		v.scale( testSegLength );
		Collision[] rightC =
				a.collision( t.position.x, t.position.y, t.position.x + v.x, t.position.y + v.y );
		Collision.sort( t.position, rightC );
		Collision right = rightC.length == 0 || rightC[ 0 ].seg.type == Trace.GAP ? null : rightC[ 0 ];

		// see which is closer
		float rd =
				right == null ? Float.MAX_VALUE : VectorUtils.distance( t.position,
						right.collisionPoint() );
		float ld =
				left == null ? Float.MAX_VALUE : VectorUtils.distance( t.position,
						left.collisionPoint() );

		if( rd < ld )
		{
			action = Action.LEFT;
			weight = 1 - rd / testSegLength;
		}
		else if( ld < rd )
		{
			action = Action.RIGHT;
			weight = 1 - ld / testSegLength;
		}
		else
		{
			action = Action.STRAIGHT;
			weight = 0;
		}
	}

	@Override
	public float[] getGenome()
	{
		return new float[] { testRange, testAngle };
	}

	@Override
	public void setGenome( float[] gene )
	{
		testRange = gene[ 0 ];
		testAngle = gene[ 1 ];
	}
}
