
package com.ryanm.soundgen;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Specifies the parameters of a sound
 * 
 * @author ryanm
 */
public class SoundSpec implements Cloneable, Serializable
{
	/**
	 * How volume varies over time
	 */
	public Variable volumeEnvelope;

	/**
	 * The basic sound waveform
	 */
	public Waveform waveform;

	/**
	 * A post-processing step
	 */
	public PostProcess postProcess;

	/**
	 * The length of the sound, in seconds
	 */
	public float length = 1;

	/**
	 * The number of supersamples to calculate for every sample. More
	 * is slower, but less and some waveforms sound like ass
	 */
	public int superSamples = 8;

	/**
	 * 
	 */
	public SoundSpec()
	{
	}

	/**
	 * Deep copy constructor
	 * 
	 * @param sound
	 */
	public SoundSpec( SoundSpec sound )
	{
		length = sound.length;
		superSamples = sound.superSamples;
	}

	/**
	 * Generates the sound from this {@link SoundSpec}'s parameters, in
	 * 16-bit little-endian format
	 * 
	 * @param sampleRate
	 * @return A buffer containing 16-bit pcm data
	 */
	public ByteBuffer generate( int sampleRate )
	{
		assert volumeEnvelope != null;
		assert waveform != null;

		int samples = ( int ) Math.ceil( sampleRate * length );
		ByteBuffer buffer =
				ByteBuffer.allocateDirect( samples * 2 ).order( ByteOrder.LITTLE_ENDIAN );

		float time = 0;
		float sampleDelta = 1.0f / sampleRate;
		float ssDelta = sampleDelta / superSamples;

		assert ssDelta != 0;

		while( buffer.hasRemaining() )
		{
			buffer.putShort( sample( ssDelta, time ) );

			time += sampleDelta;
		}

		buffer.rewind();

		if( postProcess != null )
		{
			postProcess.process( buffer.asShortBuffer(), sampleRate );
		}

		return buffer;
	}

	private short sample( float ssDelta, float time )
	{
		float vAccum = 0;
		float wAccum = 0;

		for( int i = 0; i < superSamples; i++ )
		{
			float t = time + i * ssDelta;

			vAccum += volumeEnvelope.getValue( t );
			wAccum += waveform.getValue( t );
		}

		vAccum /= superSamples;
		wAccum /= superSamples;

		float sample = vAccum * wAccum;

		if( sample > 1 )
		{
			sample = 1;
		}
		if( sample < -1 )
		{
			sample = -1;
		}

		return ( short ) ( Short.MAX_VALUE * sample );
	}
}
