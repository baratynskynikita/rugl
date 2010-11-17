
package com.rugl.sound;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.Util;

import com.rugl.console.Console;

/**
 * Encapsulates OpenAL
 * 
 * @author ryanm
 */
public class SoundSystem
{
	/**
	 * The maximum number of sources that will be constructed. The
	 * actual number available may be less than this
	 */
	public static int MAX_SOURCES = 64;

	private static Source[] sources;

	private static List<WeakReference<Sound>> sounds =
			new LinkedList<WeakReference<Sound>>();

	/**
	 * Initialises the {@link SoundSystem}
	 */
	public static void init()
	{
		try
		{
			AL.create();
			Console.log( "OpenAL vendor: " + AL10.alGetString( AL10.AL_VENDOR ) );
			Console.log( "OpenAL version: " + AL10.alGetString( AL10.AL_VERSION ) );
			Console.log( "OpenAL renderer: " + AL10.alGetString( AL10.AL_RENDERER ) );
			Console.log( "Default OpenAL device: "
					+ ALC10.alcGetString( null, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER ) );
		}
		catch( LWJGLException e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}

		// lets see how many sources we can make
		List<Source> sourceList = new ArrayList<Source>();
		Source s = null;
		do
		{
			s = null;

			try
			{
				s = new Source();
				sourceList.add( s );
			}
			catch( LWJGLException e )
			{
			}
		}
		while( s != null && MAX_SOURCES > sourceList.size() );

		sources = sourceList.toArray( new Source[ sourceList.size() ] );

		Console.log( sources.length + " OpenAL sources available" );

		if( sources.length == 0 )
		{
			System.err.println( "No sources could be created" );
			System.exit( -1 );
		}
	}

	/**
	 * Regsiters a sound to be deleted cleanly on {@link #destroy()}
	 * call
	 * 
	 * @param s
	 */
	static void registerSound( Sound s )
	{
		sounds.add( new WeakReference<Sound>( s ) );
	}

	/**
	 * Deletes all {@link Sound}s and {@link Source}s, tears down the
	 * OpenAL context
	 */
	public static void destroy()
	{
		if( sources != null )
		{
			// unbind sources
			for( int i = 0; i < sources.length; i++ )
			{
				sources[ i ].bindSound( null );
			}
		}

		// delete sounds
		for( WeakReference<Sound> ws : sounds )
		{
			if( ws.get() != null )
			{
				ws.get().destroy();
			}
		}

		if( sources != null )
		{
			// delete sources
			for( int i = 0; i < sources.length; i++ )
			{
				sources[ i ].destroy();
			}
		}

		AL.destroy();
	}

	/**
	 * Advances the {@link SoundSystem}, updating the idle status of
	 * all sources
	 */
	public static void advance()
	{
		for( int i = 0; i < sources.length; i++ )
		{
			sources[ i ].tick();
		}

		Util.checkALError();
	}

	/**
	 * Gets the first idle {@link Source}, or the source with the
	 * minimum {@link Source#priority}
	 * 
	 * @return A {@link Source}, or null if all sources are reserved
	 */
	public static Source getSource()
	{
		advance();

		Source minPriority = sources[ 0 ];

		for( int i = 0; i < sources.length; i++ )
		{
			if( sources[ i ].isIdle() && !sources[ i ].isLocked() )
			{
				return sources[ i ].reset();
			}

			if( sources[ i ].priority < minPriority.priority || minPriority.isLocked() )
			{
				minPriority = sources[ i ];
			}
		}

		if( minPriority.isLocked() )
		{
			return null;
		}
		else
		{
			return minPriority.reset();
		}
	}

	/**
	 * List the sources that are currently playing
	 * 
	 * @return the non-idle sources
	 */
	public static Source[] getPlayingSources()
	{
		List<Source> sl = new ArrayList<Source>();
		for( int i = 0; i < sources.length; i++ )
		{
			if( sources[ i ].isPlaying() )
			{
				sl.add( sources[ i ] );
			}
		}

		return sl.toArray( new Source[ sl.size() ] );
	}

	/**
	 * Sets the master volume
	 * 
	 * @param gain
	 *           0 for mute, 1 for no change, otherwise logarithmic
	 *           scale
	 */
	public static void setListenerGain( float gain )
	{
		if( gain < 0 )
		{
			gain = 0;
		}

		AL10.alListenerf( AL10.AL_GAIN, gain );
	}

	/**
	 * Sets the listener's position
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void setListenerPosition( float x, float y, float z )
	{
		AL10.alListener3f( AL10.AL_POSITION, x, y, z );
	}

	/**
	 * Sets the listener's velocity
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void setListenerVelocity( float x, float y, float z )
	{
		AL10.alListener3f( AL10.AL_VELOCITY, x, y, z );
	}

	/**
	 * Sets the listener's orientation, by means of forward and up
	 * vectors
	 * 
	 * @param fx
	 * @param fy
	 * @param fz
	 * @param ux
	 * @param uy
	 * @param uz
	 */
	public static void setListenerOrientation( float fx, float fy, float fz, float ux,
			float uy, float uz )
	{
		FloatBuffer buff = BufferUtils.createFloatBuffer( 6 );
		buff.put( fx ).put( fy ).put( fz );
		buff.put( ux ).put( uy ).put( uz );

		AL10.alListener( AL10.AL_ORIENTATION, buff );
	}

	/**
	 * @return Source status
	 */
	public static String sourceStatus()
	{
		StringBuffer buff = new StringBuffer( "Sound system\n" );

		for( int i = 0; i < sources.length; i++ )
		{
			buff.append( "\t" );
			buff.append( sources[ i ] );
			buff.append( "\n" );
		}

		return buff.toString();
	}
}
