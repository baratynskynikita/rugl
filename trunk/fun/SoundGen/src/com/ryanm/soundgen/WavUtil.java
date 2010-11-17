
package com.ryanm.soundgen;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Utility for working with wav files
 * 
 * @author ryanm
 */
public class WavUtil
{
	/**
	 * Gets an {@link AudioInputStream} for a sound
	 * 
	 * @param sampleRate
	 * @param bytesPerSample
	 * @param pcm
	 * @return An {@link AudioInputStream}
	 */
	public static AudioInputStream getAudioStream( int sampleRate, int bytesPerSample,
			ByteBuffer pcm )
	{
		AudioFormat af =
				new AudioFormat( sampleRate, bytesPerSample * 8, 1, bytesPerSample == 2,
						false );

		byte[] data = new byte[ pcm.limit() ];
		pcm.get( data );
		pcm.rewind();
		ByteArrayInputStream bais = new ByteArrayInputStream( data );

		return new AudioInputStream( bais, af, data.length / bytesPerSample );
	}

	/**
	 * Saves some sound data as a wav file
	 * 
	 * @param sampleRate
	 * @param bytesPerSample
	 * @param pcm
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveAsWav( int sampleRate, int bytesPerSample, ByteBuffer pcm,
			String fileName ) throws IOException
	{
		AudioSystem.write( getAudioStream( sampleRate, bytesPerSample, pcm ),
				AudioFileFormat.Type.WAVE, new File( fileName ) );
	}
}
