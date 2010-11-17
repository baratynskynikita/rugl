
package com.rugl.sound;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.openal.AL10;

import com.ryanm.soundgen.SoundSpec;
import com.ryanm.util.math.Statistics;

/**
 * @author ryanm
 */
public class Sound
{
	/**
	 * Sound data format
	 */
	public final Format format;

	/**
	 * Sound sample rate
	 */
	public final int sampleRate;

	/**
	 * Sound data
	 */
	public final ByteBuffer data;

	/**
	 * OpenAL buffer name
	 */
	private int bufferID = -1;

	/**
	 * Builds a new {@link Sound}
	 * 
	 * @param format
	 * @param sampleRate
	 * @param data
	 */
	public Sound( Format format, int sampleRate, ByteBuffer data )
	{
		this.format = format;
		this.sampleRate = sampleRate;
		this.data = data;

		assert format.bytesPerSample * sampleRate == data.limit();

		SoundSystem.registerSound( this );
	}

	/**
	 * Builds a new {@link Sound}
	 * 
	 * @param soundSpec
	 * @param sampleRate
	 */
	public Sound( SoundSpec soundSpec, int sampleRate )
	{
		format = Format.MONO_16;
		this.sampleRate = sampleRate;
		data = soundSpec.generate( sampleRate );

		SoundSystem.registerSound( this );
	}

	/**
	 * Reads a {@link Sound} from a stream
	 * 
	 * @param is
	 * @throws IOException
	 */
	public Sound( InputStream is ) throws IOException
	{
		DataInputStream dis = new DataInputStream( is );

		int alf = dis.readInt();
		sampleRate = dis.readInt();
		byte[] bd = new byte[ dis.readInt() ];

		dis.readFully( bd );

		data = ByteBuffer.wrap( bd );

		Format possibleFormat = null;

		for( Format f : Format.values() )
		{
			if( f.alFormat == alf )
			{
				possibleFormat = f;
				break;
			}
		}

		if( possibleFormat == null )
		{
			throw new IOException( "Unrecognised AL format : " + alf );
		}

		format = possibleFormat;

		SoundSystem.registerSound( this );
	}

	/**
	 * Loads this sound into OpenAL if needed, and returns the buffer
	 * name
	 * 
	 * @return the OpenAL buffer ID
	 */
	int getBufferName()
	{
		if( bufferID == -1 )
		{
			bufferID = AL10.alGenBuffers();

			AL10.alBufferData( bufferID, format.alFormat, data, sampleRate );
		}

		return bufferID;
	}

	/**
	 * Unloads this sound from OpenAL
	 */
	public void destroy()
	{
		if( bufferID != -1 )
		{
			AL10.alDeleteBuffers( bufferID );

			bufferID = -1;
		}
	}

	/**
	 * Writes this {@link Sound} to a file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void write( String fileName ) throws IOException
	{
		RandomAccessFile rf = new RandomAccessFile( fileName, "rw" );
		FileChannel ch = rf.getChannel();
		int fileLength = 4 * 3 + data.capacity();
		rf.setLength( fileLength );
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );

		write( buffer );

		buffer.force();
		ch.close();
	}

	/**
	 * Writes this {@link Sound} to a buffer
	 * 
	 * @param buffer
	 */
	public void write( ByteBuffer buffer )
	{
		buffer.putInt( format.alFormat );
		buffer.putInt( sampleRate );
		buffer.putInt( data.capacity() );
		buffer.put( data );
	}

	/**
	 * Possible sound formats
	 * 
	 * @author ryanm
	 */
	public static enum Format
	{
		/***/
		MONO_16( AL10.AL_FORMAT_MONO16, 2 ),
		/***/
		MONO_8( AL10.AL_FORMAT_MONO8, 1 ),
		/***/
		STEREO_16( AL10.AL_FORMAT_STEREO16, 4 ),
		/***/
		STEREO_8( AL10.AL_FORMAT_STEREO8, 2 );

		/**
		 * The OpenAL format flag
		 */
		public final int alFormat;

		/**
		 * The number of bytes used for each sample
		 */
		public final int bytesPerSample;

		private Format( int f, int b )
		{
			alFormat = f;
			bytesPerSample = b;
		}
	}

	@Override
	public String toString()
	{
		double[] md = Statistics.calculateMeanDeviation( data.asShortBuffer() );
		StringBuilder buff = new StringBuilder( "Sound length = " );
		buff.append( ( double ) data.limit() / format.bytesPerSample / sampleRate );

		buff.append( " mean = " );
		buff.append( md[ 0 ] );
		buff.append( " stdDev = " );
		buff.append( md[ 1 ] );
		buff.append( "\n" );

		return buff.toString();
	}
}
