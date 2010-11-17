
package com.ryanm.soundgen.imp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import com.ryanm.soundgen.SoundSpec;
import com.ryanm.soundgen.Variable;
import com.ryanm.soundgen.Waveform;

/**
 * A sound based on various terrains, has some methods that
 * approximate the capabilities of sfxr
 * 
 * @author ryanm
 */
public class TerrainSound extends SoundSpec
{
	/**
	 * Possible waveForms
	 * 
	 * @author ryanm
	 */
	public static enum WaveType
	{
		/***/
		SQUARE
		{
			@Override
			public Waveform getForm()
			{
				return new SquareWave();
			}
		},
		/***/
		SAW
		{
			@Override
			public Waveform getForm()
			{
				return new SawtoothWave();
			}
		},
		/***/
		SINE
		{
			@Override
			public Waveform getForm()
			{
				return new SineWave();
			}
		},
		/***/
		NOISE
		{
			@Override
			public Waveform getForm()
			{
				return new NoiseWave();
			}
		};

		/**
		 * Gets an instance of the appropriate waveform
		 * 
		 * @return A {@link Waveform}
		 */
		public abstract Waveform getForm();
	};

	/**
	 * The basic wave form
	 */
	public WaveType wave = WaveType.SQUARE;

	/**
	 * The volume terrain
	 */
	public Terrain volume = new Terrain();

	/**
	 * The base frequency terrain
	 */
	public Terrain baseFrequency = new Terrain();

	/**
	 * The vibrato wave form
	 */
	public WaveType vibrato = WaveType.SINE;

	/**
	 * The vibrato frequency
	 */
	public Terrain vibratoFrequency = new Terrain();

	/**
	 * The vibrato amplitude
	 */
	public Terrain vibratoAmplitude = new Terrain();

	/**
	 * The flange delay
	 */
	public Terrain flangeDelay = new Terrain();

	/**
	 * The flange alpha
	 */
	public Terrain flangeAlpha = new Terrain();

	/**
	 * 
	 */
	public TerrainSound()
	{

	}

	/**
	 * Deep copy constructor
	 * 
	 * @param sound
	 */
	public TerrainSound( TerrainSound sound )
	{
		super( sound );
		wave = sound.wave;
		volume = new Terrain( sound.volume );
		baseFrequency = new Terrain( sound.baseFrequency );
		vibrato = sound.vibrato;
		vibratoFrequency = new Terrain( sound.vibratoFrequency );
		vibratoAmplitude = new Terrain( sound.vibratoAmplitude );
		flangeDelay = new Terrain( sound.flangeDelay );
		flangeAlpha = new Terrain( sound.flangeDelay );
	}

	/**
	 * Reads the values from the stream
	 * 
	 * @param is
	 * @throws IOException
	 */
	public TerrainSound( InputStream is ) throws IOException
	{
		read( is );
	}

	@Override
	public ByteBuffer generate( int sampleRate )
	{
		setEnvelopes();

		return super.generate( sampleRate );
	}

	private void setEnvelopes()
	{
		volumeEnvelope = volume;

		waveform = wave.getForm();
		waveform.frequency = getFrequency();

		CombFilter cf = new CombFilter();
		cf.delay = flangeDelay;
		cf.alpha = flangeAlpha;

		postProcess = cf;
	}

	/**
	 * Sets the values of this sound from those read from the stream
	 * 
	 * @param is
	 * @throws IOException
	 */
	public void read( InputStream is ) throws IOException
	{
		DataInputStream dis = new DataInputStream( is );

		length = dis.readFloat();

		wave = WaveType.values()[ dis.readInt() ];

		volume.read( dis );
		baseFrequency.read( dis );

		vibrato = WaveType.values()[ dis.readInt() ];

		vibratoFrequency.read( dis );
		vibratoAmplitude.read( dis );

		flangeDelay.read( dis );
		flangeAlpha.read( dis );
	}

	/**
	 * Writes this sound to a buffer
	 * 
	 * @param buffer
	 */
	public void write( ByteBuffer buffer )
	{
		buffer.putFloat( length );

		buffer.putInt( wave.ordinal() );

		volume.write( buffer );
		baseFrequency.write( buffer );

		buffer.putInt( vibrato.ordinal() );

		vibratoFrequency.write( buffer );
		vibratoAmplitude.write( buffer );

		flangeDelay.write( buffer );
		flangeAlpha.write( buffer );
	}

	/**
	 * Writes this sound to a file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void write( String fileName ) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile( fileName, "rw" );
		FileChannel ch = raf.getChannel();
		int fileLength = dataSize();
		raf.setLength( fileLength );
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );

		write( buffer );

		buffer.force();
		ch.close();
	}

	/**
	 * Calculates the size of this sound in bytes
	 * 
	 * @return the minimum size of buffer passed into
	 *         {@link #write(ByteBuffer)}
	 */
	public int dataSize()
	{
		int size = 0;

		size += 4;
		size += 4;
		size += volume.dataSize();
		size += baseFrequency.dataSize();
		size += 4;
		size += vibratoFrequency.dataSize();
		size += vibratoAmplitude.dataSize();
		size += flangeDelay.dataSize();
		size += flangeAlpha.dataSize();

		return size;
	}

	/**
	 * Gets the frequency variable
	 * 
	 * @return the frequency
	 */
	public Variable getFrequency()
	{
		Waveform vib = vibrato.getForm();
		vib.frequency = vibratoFrequency;
		Variable frequency =
				new Addition( baseFrequency, new Multiplication( vib, vibratoAmplitude ) );
		return frequency;
	}

	/**
	 * Clears all {@link Terrain}s
	 */
	public void clear()
	{
		volume.clear();
		baseFrequency.clear();
		vibratoAmplitude.clear();
		vibratoFrequency.clear();
		flangeDelay.clear();
		flangeAlpha.clear();
	}

	/**
	 * Sets this sound to be a random coin pickup sound
	 */
	public void coin()
	{
		clear();

		wave = WaveType.SQUARE;

		Random rng = new Random();
		float t = 0.05f + rng.nextFloat() * 0.3f;
		float sus = rng.nextFloat() * t * 0.4f;

		length = t;

		volume.addPoint( sus, 1 );
		volume.addPoint( t, 0 );

		float freq = 800 + 500 * rng.nextFloat();
		float freqChange = 1500 + 500 * rng.nextFloat();
		float ct = rng.nextFloat() * t;

		baseFrequency.addPoint( ct, freq );
		baseFrequency.addPoint( ct + 0.001f, freqChange );
	}

	/**
	 * Sets this sound to be a random laser sound
	 */
	public void laser()
	{
		clear();

		Random rng = new Random();

		do
		{
			wave = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];
		}
		while( wave == WaveType.NOISE );

		float t = 0.1f + rng.nextFloat() * 0.3f;

		length = t;

		volume.addPoint( 0, 1 );
		volume.addPoint( t, 0 );

		float freq = 800 + 1200 * rng.nextFloat();

		baseFrequency.addPoint( 0, 0 );
		baseFrequency.addPoint( 0.01f, freq );
		baseFrequency.addPoint( t, 0 );

		vibratoAmplitude.addPoint( 0, 100 * rng.nextFloat() );
		vibratoAmplitude.addPoint( length, 0 );

		vibratoFrequency.randomise( rng, 1, 0, 0, length, 100 );
	}

	/**
	 * Sets this sound to be a random explosion sound
	 */
	public void explosion()
	{
		clear();

		Random rng = new Random();

		wave = WaveType.NOISE;
		vibrato = WaveType.NOISE;

		length = 0.1f + rng.nextFloat();

		volume.addPoint( 0, 1 );
		volume.addPoint( length * rng.nextFloat(), 1 );
		volume.addPoint( length, 0 );

		baseFrequency.randomise( rng, 5, 0, 0, length, 1000 );

		vibratoFrequency.randomise( rng, 3, 0, 0, length, 10 );
		vibratoAmplitude.randomise( rng, 3, 0, 0, length, 500 );
	}

	/**
	 * Sets this sound to be a random powerup sound
	 */
	public void powerup()
	{
		clear();

		Random rng = new Random();

		do
		{
			wave = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];
		}
		while( wave == WaveType.NOISE );

		vibrato = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];

		length = 0.1f + rng.nextFloat();

		volume.addPoint( length * rng.nextFloat(), 1 );
		volume.addPoint( length, 0 );

		float f = 500 + 500 * rng.nextFloat();
		baseFrequency.addPoint( ( 0.2f + 0.5f * rng.nextFloat() ) * length, f );
		baseFrequency.addPoint( length, 1000 + 1000 * rng.nextFloat() );

		vibratoAmplitude.randomise( rng, 3, 0, 0, length, 500 );
		vibratoFrequency.randomise( rng, 3, 0, 0, length, 10 );
	}

	/**
	 * Sets this sound to be a random hit/hurt sound
	 */
	public void hit()
	{
		clear();

		Random rng = new Random();

		wave = WaveType.NOISE;
		vibrato = WaveType.NOISE;

		length = 0.1f + 0.1f * rng.nextFloat();

		volume.addPoint( 0, 1 );
		volume.addPoint( length, 0 );

		baseFrequency.randomise( rng, 5, 0, 0, length, 1000 );

		vibratoFrequency.randomise( rng, 3, 0, 0, length, 10 );
		vibratoAmplitude.randomise( rng, 3, 0, 0, length, 500 );
	}

	/**
	 * Sets this sound to be a random jump sound
	 */
	public void jump()
	{
		clear();

		Random rng = new Random();

		do
		{
			wave = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];
		}
		while( wave == WaveType.NOISE || wave == WaveType.SAW );

		length = 0.2f + 0.1f * rng.nextFloat();

		volume.addPoint( length * rng.nextFloat(), 1 );
		volume.addPoint( length, 0 );

		float f = 500 + 500 * rng.nextFloat();
		baseFrequency.addPoint( rng.nextFloat() * 0.5f * length, f );
		baseFrequency.addPoint( length, f + 1000 * rng.nextFloat() );
	}

	/**
	 * Alters the terrains slightly
	 */
	public void mutate()
	{
		Random rng = new Random();

		float lm = length / 20.0f;

		volume.mutate( rng, lm, 0.1f );
		volume.enforceBounds( 0, 0, length, 1 );

		baseFrequency.mutate( rng, lm, 100 );
		baseFrequency.enforceBounds( 0, 0, length, 2000 );

		vibratoFrequency.mutate( rng, lm, 5 );
		vibratoFrequency.enforceBounds( 0, 0, length, 100 );

		vibratoAmplitude.mutate( rng, lm, 50 );
		vibratoAmplitude.enforceBounds( 0, 0, length, 500 );

		flangeDelay.mutate( rng, lm, 0.0025f );
		flangeDelay.enforceBounds( 0, 0, length, 0.02f );

		flangeAlpha.mutate( rng, lm, 0.05f );
		flangeAlpha.enforceBounds( 0, 0, length, 1 );
	}

	/**
	 * Sets all parameters, except for length, randomly
	 */
	public void randomise()
	{
		Random rng = new Random();

		wave = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];

		volume.randomise( rng, 5, 0, 0, length, 1 );
		baseFrequency.randomise( rng, 5, 0, 0, length, 2000 );

		vibrato = WaveType.values()[ rng.nextInt( WaveType.values().length ) ];

		vibratoFrequency.randomise( rng, 5, 0, 0, length, 100 );
		vibratoAmplitude.randomise( rng, 5, 0, 0, length, 500 );
		flangeDelay.randomise( rng, 5, 0, 0, length, 0.02f );
		flangeAlpha.randomise( rng, 5, 0, 0, length, 1 );
	}
}
