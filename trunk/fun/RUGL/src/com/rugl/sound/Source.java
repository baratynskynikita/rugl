
package com.rugl.sound;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL10;

/**
 * Encapsulates an OpenAL source object
 * 
 * @author ryanm
 */
public class Source
{
	/**
	 * The source id
	 */
	public final int id;

	/**
	 * The priority of this {@link Source}. Lower priority
	 * {@link Source}s are recycled preferentially
	 */
	public float priority = -1;

	private boolean isIdle = true;

	private int boundBuffer = 0;

	/**
	 * Locked sources are not returned by
	 * {@link SoundSystem#getSource()}
	 */
	private Object lock = null;

	Source() throws LWJGLException
	{
		id = AL10.alGenSources();

		assert AL10.alIsSource( id );

		int e = AL10.alGetError();
		if( e != AL10.AL_NO_ERROR )
		{
			throw new LWJGLException( "Problem creating a source " + e );
		}
	}

	/**
	 * Destroys the source
	 */
	void destroy()
	{
		AL10.alDeleteSources( id );
	}

	/**
	 * Reserves this source for exclusive use
	 * 
	 * @param owner
	 *           the owner of the source
	 * @return this {@link Source}
	 */
	public Source lock( Object owner )
	{
		assert lock == null : "Attempt to lock source already locked by " + lock;
		lock = owner;

		return this;
	}

	/**
	 * Releases this source for other users
	 * 
	 * @param owner
	 *           the owner of the source
	 * @return this {@link Source}
	 */
	public Source release( Object owner )
	{
		assert lock == owner : "Attempt to release with wrong lock: " + lock + " != "
				+ owner;

		lock = null;

		return this;
	}

	/**
	 * Checks if the source is currently locked *
	 * 
	 * @return <code>true</code> if locked, <code>false</code> if free
	 *         for use
	 */
	public boolean isLocked()
	{
		return lock != null;
	}

	/**
	 * Resets the source to default state (looping, gain etc)
	 * 
	 * @return this {@link Source}
	 */
	public Source reset()
	{
		AL10.alSourcef( id, AL10.AL_GAIN, 1 );
		AL10.alSourcef( id, AL10.AL_PITCH, 1 );
		AL10.alSourcei( id, AL10.AL_LOOPING, AL10.AL_FALSE );
		AL10.alSource3f( id, AL10.AL_POSITION, 0, 0, 0 );
		AL10.alSource3f( id, AL10.AL_VELOCITY, 0, 0, 0 );

		return this;
	}

	/**
	 * Determines if this source is currently playing
	 * 
	 * @return <code>true</code> if stopped or paused, false if playing
	 */
	public boolean isIdle()
	{
		return isIdle;
	}

	/**
	 * Sets the source's position
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this {@link Source}
	 */
	public Source setPosition( float x, float y, float z )
	{
		AL10.alSource3f( id, AL10.AL_POSITION, x, y, z );

		return this;
	}

	/**
	 * Sets the source's velocity
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this {@link Source}
	 */
	public Source setVelocity( float x, float y, float z )
	{
		AL10.alSource3f( id, AL10.AL_VELOCITY, x, y, z );
		return this;
	}

	void tick()
	{
		int s = AL10.alGetSourcei( id, AL10.AL_SOURCE_STATE );
		isIdle = s == AL10.AL_STOPPED || s == AL10.AL_INITIAL;
	}

	/**
	 * Binds a sound to this source
	 * 
	 * @param s
	 *           the new sound, or null for no sound
	 * @return This {@link Source}
	 */
	public Source bindSound( Sound s )
	{
		if( !isIdle )
		{
			stop();
		}

		if( s == null )
		{
			boundBuffer = 0;
			AL10.alSourcei( id, AL10.AL_BUFFER, boundBuffer );
		}
		else
		{
			if( boundBuffer != s.getBufferName() )
			{
				boundBuffer = s.getBufferName();
				AL10.alSourcei( id, AL10.AL_BUFFER, boundBuffer );
			}
		}

		return this;
	}

	/**
	 * Plays the source
	 * 
	 * @return this {@link Source}
	 */
	public Source play()
	{
		AL10.alSourcePlay( id );

		return this;
	}

	/**
	 * Determines if this source is playing or not
	 * 
	 * @return <code>true</code> if currently playing,
	 *         <code>false</code> otherwise
	 */
	public boolean isPlaying()
	{
		return AL10.alGetSourcei( id, AL10.AL_SOURCE_STATE ) == AL10.AL_PLAYING;
	}

	/**
	 * Sets the source to loop or not
	 * 
	 * @param loop
	 *           <code>true</code> to loop playback
	 * @return This {@link Source}
	 */
	public Source setLoop( boolean loop )
	{
		int i = loop ? AL10.AL_TRUE : AL10.AL_FALSE;

		AL10.alSourcei( id, AL10.AL_LOOPING, i );

		return this;
	}

	/**
	 * Pauses the source
	 * 
	 * @return this {@link Source}
	 */
	public Source pause()
	{
		AL10.alSourcePause( id );

		return this;
	}

	/**
	 * Stops the source
	 * 
	 * @return this {@link Source}
	 */
	public Source stop()
	{
		AL10.alSourceStop( id );
		isIdle = true;
		return this;
	}

	/**
	 * Stops and rewinds the source
	 * 
	 * @return this {@link Source}
	 */
	public Source rewind()
	{
		AL10.alSourceRewind( id );

		return this;
	}

	/**
	 * Sets the gain
	 * 
	 * @param f
	 *           will be clamped to >= 0
	 * @return this {@link Source}
	 */
	public Source setGain( float f )
	{
		f = f < 0 ? 0 : f;

		AL10.alSourcef( id, AL10.AL_GAIN, f );

		return this;
	}

	/**
	 * Gets the gain for this source
	 * 
	 * @return the gain
	 */
	public float getGain()
	{
		return AL10.alGetSourcef( id, AL10.AL_GAIN );
	}

	/**
	 * Sets the pitch multiplier
	 * 
	 * @param pitch
	 *           will be clamped to >= 0
	 * @return this {@link Source}
	 */
	public Source setPitch( float pitch )
	{
		pitch = pitch < 0 ? 0 : pitch;

		AL10.alSourcef( id, AL10.AL_PITCH, pitch );
		return this;
	}

	/**
	 * Gets the pitch multiplier for this source
	 * 
	 * @return the pitch
	 */
	public float getPitch()
	{
		return AL10.alGetSourcef( id, AL10.AL_PITCH );
	}

	@Override
	public String toString()
	{
		return "id:" + id + " p:" + priority + " i:" + isIdle + " l:" + lock;
	}
}
