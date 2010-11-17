
package com.ryanm.soundgen.imp;

import java.util.Random;

import com.ryanm.soundgen.Waveform;

/**
 * @author ryanm
 */
public class NoiseWave extends Waveform
{
	/**
	 * Different offsets give different noise values
	 */
	public int rngOffset = 133780085;

	private Random rng = new Random();

	private int valuesPerCycle = 10;

	private int oldCycle = -1;

	private float[] noise = new float[ 32 ];

	@Override
	public float valueForPhase( float phase )
	{
		if( oldCycle != cycleNumber )
		{
			rng.setSeed( ( cycleNumber + 1 ) * rngOffset );
			for( int i = 0; i < noise.length; i++ )
			{
				noise[ i ] = 2 * rng.nextFloat() - 1;
			}
		}

		int p = ( int ) ( phase * valuesPerCycle ) + 1;

		long seed = rngOffset * ( cycleNumber + 1 ) * p;

		assert p != 0;
		assert seed != 0;

		rng.setSeed( seed );

		return noise[ rng.nextInt( noise.length ) ];
	}
}
