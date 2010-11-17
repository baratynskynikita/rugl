
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Waveform;

/**
 * A square wave
 * 
 * @author ryanm
 */
public class SquareWave extends Waveform
{
	/**
	 * The proportion of time that the wave spends at 1, the rest of
	 * the time the wave is at -1;
	 */
	public float dutyCycle = 0.5f;

	@Override
	public float valueForPhase( float phase )
	{
		return phase < dutyCycle ? 1 : -1;
	}
}
