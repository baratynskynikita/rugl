
package com.ryanm.soundgen.imp;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.ryanm.soundgen.PostProcess;
import com.ryanm.soundgen.Variable;

/**
 * A generalised comb filter, for flanging
 * 
 * @author ryanm
 */
public class CombFilter implements PostProcess
{
	/**
	 * The multiplier applied to the delayed sample
	 */
	public Variable alpha;

	/**
	 * The delay of the filter, in seconds
	 */
	public Variable delay;

	@Override
	public void process( ShortBuffer data, int sampleRate )
	{
		ShortBuffer copy =
				ByteBuffer.allocateDirect( data.capacity() * 2 ).order( data.order() )
						.asShortBuffer();
		copy.put( data );
		data.rewind();
		copy.rewind();

		for( int i = 0; i < data.limit(); i++ )
		{
			float time = ( float ) i / sampleRate;

			int ds = Math.round( delay.getValue( time ) * sampleRate );

			int dsi = i - ds;
			short sample;
			if( dsi < 0 || dsi > data.limit() - 1 )
			{
				sample = 0;
			}
			else
			{
				sample = copy.get( dsi );
			}

			float a = alpha.getValue( time );

			int s = ( int ) ( copy.get( i ) + a * sample );

			if( s > Short.MAX_VALUE )
			{
				s = Short.MAX_VALUE;
			}
			if( s < Short.MIN_VALUE )
			{
				s = Short.MIN_VALUE;
			}

			data.put( i, ( short ) s );
		}
	}
}
