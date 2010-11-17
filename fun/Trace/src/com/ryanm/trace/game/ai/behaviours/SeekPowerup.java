
package com.ryanm.trace.game.ai.behaviours;

import java.util.Arrays;

import com.ryanm.trace.PowerupOptions;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.entities.Powerup;
import com.ryanm.util.geom.VectorUtils;

/**
 * Seeks powerups
 * 
 * @author ryanm
 */
public class SeekPowerup extends Behaviour
{
	/**
	 * Distance weights for powerups, smaller values=more attractive
	 */
	public float[] preferences = new float[ PowerupOptions.powerupClasses.length ];
	{
		Arrays.fill( preferences, 1 );
	}

	@Override
	public void process( Trace t, Arena a )
	{
		reset();

		Powerup closest = null;

		float minD = 10000;
		for( int i = 0; i < a.entities.size(); i++ )
		{
			if( a.entities.get( i ) instanceof Powerup )
			{
				Powerup p = ( Powerup ) a.entities.get( i );

				float pref = Float.MAX_VALUE;

				for( int j = 0; j < PowerupOptions.powerupClasses.length; j++ )
				{
					if( p.getClass().equals( PowerupOptions.powerupClasses[ j ] ) )
					{
						pref = preferences[ j ];
						break;
					}
				}

				float d = pref * VectorUtils.distance( t.position, p.position );

				if( d < minD )
				{
					minD = d;
					closest = p;
				}
			}
		}

		if( closest != null )
		{
			action = Waypoint.seek( t, closest.position );
			weight = 0.1f;
		}
	}

	@Override
	public float[] getGenome()
	{
		return preferences.clone();
	}

	@Override
	public void setGenome( float[] gene )
	{
		System.arraycopy( gene, 0, preferences, 0, gene.length );
	}
}
