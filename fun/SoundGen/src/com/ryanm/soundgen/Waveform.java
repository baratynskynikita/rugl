
package com.ryanm.soundgen;

/**
 * The base waveform
 * 
 * @author ryanm
 */
public abstract class Waveform implements Variable
{
	/**
	 * The frequency of the waveform
	 */
	public Variable frequency;

	/**
	 * Calculate the value of the wave at a particular point in the
	 * form
	 * 
	 * @param phase
	 *           The point at which to calculate, varies from 0 to 1
	 * @return The value of the wave at that point
	 */
	public abstract float valueForPhase( float phase );

	private float p = 0;

	private float ptime = 0;

	/**
	 * The index of the wave that is currently being calculated
	 */
	protected int cycleNumber = 0;

	@Override
	public float getValue( float time )
	{
		if( time < ptime )
		{
			ptime = 0;
			p = 0;
			cycleNumber = 0;
		}

		// the amount by which the phase will rise in a second
		float pinc = frequency.getValue( time );

		if( pinc < 0 )
		{
			pinc = -pinc;
		}

		// the time difference between the last p calc and now
		float td = time - ptime;

		assert td >= 0;

		ptime = time;

		p += pinc * td;

		cycleNumber += ( int ) p;
		p -= ( int ) p;

		assert p >= 0 : p;
		assert p < 1 : p;

		return valueForPhase( p );
	}
}
