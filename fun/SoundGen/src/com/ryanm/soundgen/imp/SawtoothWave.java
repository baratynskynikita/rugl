
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Waveform;

/**
 * A sawtooth {@link Waveform}
 * 
 * @author ryanm
 */
public class SawtoothWave extends Waveform
{
	@Override
	public float valueForPhase( float phase )
	{
		return 1 - 2 * phase;
	}
}
