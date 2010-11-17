
package com.ryanm.trace.game.ai.behaviours;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.util.math.Range;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
public class Hunt extends Behaviour
{
	private Random rng = new Random();

	/***/
	public float huntWeight = 0.2f;

	/**
	 * Time on target
	 */
	public Range preySwitch = new Range( 3, 8 );

	private Trace prey = null;

	private float preySwitchTime = 0;

	private float side = 10;

	@Override
	public void reset()
	{
		super.reset();
		prey = null;
		preySwitchTime = 0;
	}

	@Override
	public void process( Trace t, Arena a )
	{
		reset();

		if( preySwitchTime < a.time )
		{
			preySwitchTime = a.time + preySwitch.toValue( rng.nextFloat() );

			int aliveCount = 0;
			for( int i = 0; i < a.traces.length; i++ )
			{
				if( !a.traces[ i ].player.dead && t != a.traces[ i ] )
				{
					aliveCount++;
				}
			}

			prey = null;
			if( aliveCount > 0 )
			{
				aliveCount = rng.nextInt( aliveCount );

				for( int i = 0; i < a.traces.length; i++ )
				{
					if( !a.traces[ i ].player.dead && t != a.traces[ i ] )
					{
						aliveCount--;
					}

					if( aliveCount < 0 )
					{
						prey = a.traces[ i ];
						break;
					}
				}
			}
		}

		if( prey != null )
		{
			assert prey != t;
			weight = huntWeight;

			Vector2f v = new Vector2f( prey.position );
			v.x += 3 * Trig.cos( prey.angleRads );
			v.y += 3 * Trig.sin( prey.angleRads );

			if( a.time % 4 < 2 )
			{
				v.x += side * Trig.cos( prey.angleRads + Trig.PI / 2 );
				v.y += side * Trig.sin( prey.angleRads + Trig.PI / 2 );
			}
			else
			{
				v.x += -side * Trig.cos( prey.angleRads + Trig.PI / 2 );
				v.y += -side * Trig.sin( prey.angleRads + Trig.PI / 2 );
			}

			action = Waypoint.seek( t, v );
		}
	}

	@Override
	public float[] getGenome()
	{
		return new float[] { huntWeight, preySwitch.getMin(), preySwitch.getMax() };
	}

	@Override
	public void setGenome( float[] gene )
	{
		huntWeight = gene[ 0 ];
		preySwitch.set( gene[ 1 ], gene[ 2 ] );
	}
}
