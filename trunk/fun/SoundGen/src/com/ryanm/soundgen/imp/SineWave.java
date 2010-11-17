
package com.ryanm.soundgen.imp;

import com.ryanm.soundgen.Waveform;

/**
 * Sine waveform
 * 
 * @author ryanm
 */
public class SineWave extends Waveform
{
	@Override
	public float valueForPhase( float phase )
	{
		return ( float ) Math.sin( Math.PI * 2 * phase );
	}
}
