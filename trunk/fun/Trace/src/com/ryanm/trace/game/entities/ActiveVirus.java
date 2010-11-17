
package com.ryanm.trace.game.entities;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
public class ActiveVirus extends Powerup
{
	private static float acceleration = 300;

	private static float drag = 2;

	ActiveVirus( Arena a, Virus v )
	{
		super( a, Virus.shape );
		position.set( v.position );
		angle = v.angle;
		rotation = v.rotation;
	}

	@Override
	public boolean advance( float delta )
	{
		super.advance( delta );

		time = 0;

		float da = Trig.toRadians( 180 ) - rotation;
		rotation += da * delta;
		angle += rotation * delta;

		Trace closest = null;
		float min = Float.MAX_VALUE;
		for( int i = 0; i < arena.traces.length; i++ )
		{
			if( !arena.traces[ i ].player.dead && arena.traces[ i ].cloakTime <= 0 )
			{
				float d = VectorUtils.distanceSquared( position, arena.traces[ i ].position );
				if( d < min )
				{
					closest = arena.traces[ i ];
					min = d;
				}
			}
		}

		if( closest != null )
		{
			Vector2f acc =
					new Vector2f( closest.position.x - position.x, closest.position.y - position.y );
			acc.normalise();
			acc.scale( acceleration * delta );
			motion.x += acc.x;
			motion.y += acc.y;

			motion.scale( 1 - drag * delta );
		}

		return done;
	}

	@Override
	protected float getRadius()
	{
		return TraceGame.game.powerups.radius;
	}

	@Override
	protected void hit( Trace t )
	{
		if( t.cloakTime <= 0 )
		{
			t.rotate *= -1;
			done = true;
		}
		else
		{
			done = false;
		}
	}

}
