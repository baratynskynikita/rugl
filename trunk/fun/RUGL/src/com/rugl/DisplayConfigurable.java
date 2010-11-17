/*
 * Copyright (c) 2007, Ryan McNally All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <ORGANIZATION> nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.rugl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Dimension;

import com.rugl.console.Console;
import com.ryanm.config.Configurable;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.AbstractConfigurator;
import com.ryanm.config.imp.AnnotatedConfigurator;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.NumberRange;
import com.ryanm.config.imp.Variable;

/**
 * @author ryanm
 */
@ConfigurableType( "display" )
@Description( "Controls resolution and so on" )
public class DisplayConfigurable implements Configurable
{
	private DisplayMode[] availableModes;

	private Conf current = new Conf();

	private Conf target = new Conf();

	int frameRate = 60;

	private AbstractConfigurator conf = null;

	private List<Listener> listeners = new LinkedList<Listener>();

	/**
	 * @author ryanm
	 */
	public static enum Samples
	{
		/***/
		NONE( 0 ),
		/***/
		TWO( 2 ),
		/***/
		FOUR( 4 ),
		/***/
		SIX( 6 ),
		/***/
		EIGHT( 8 ),
		/***/
		SIXTEEN( 16 );

		/**
		 * The number of samples
		 */
		public final int samples;

		private Samples( int samples )
		{
			this.samples = samples;
		}
	};

	DisplayConfigurable()
	{
		try
		{
			availableModes = Display.getAvailableDisplayModes();

			DisplayMode m = Display.getDisplayMode();
			target.res.setSize( m.getWidth(), m.getHeight() );

			target.fullscreen = Display.isFullscreen();
			Display.setVSyncEnabled( target.vsync );
		}
		catch( LWJGLException e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}
	}

	/**
	 * Gets the current target resolution
	 * 
	 * @return the current resolution
	 */
	@Variable( "resolution" )
	public Dimension getResolution()
	{
		return target.res;
	}

	/**
	 * Sets the target resolution
	 * 
	 * @param d
	 *           the new resolution
	 */
	@Variable( "resolution" )
	public void setResolution( Dimension d )
	{
		target.res.setSize( d );
	}

	/**
	 * Determines if the display is in fullscreen mode
	 * 
	 * @return <code>true</code> if fullscreen, <code>false</code> is
	 *         windowed
	 */
	@Variable( "fullscreen" )
	public boolean isFullscreen()
	{
		return target.fullscreen;
	}

	/**
	 * Sets the display to fullscreen mode
	 * 
	 * @param b
	 *           <code>true</code> for fullscreen, <code>false</code>
	 *           for windowed
	 */
	@Variable( "fullscreen" )
	public void setFullScreen( boolean b )
	{
		target.fullscreen = b;
	}

	/**
	 * Determines if the display is vsynced
	 * 
	 * @return <code>true</code> if vsynced, <code>false</code>
	 *         otherwise
	 */
	@Variable( "vsync" )
	public boolean isVSynced()
	{
		return target.vsync;
	}

	/**
	 * Sets the display target to be vsynced
	 * 
	 * @param b
	 *           <code>true</code> to vsync, <code>false</code>
	 *           otherwise
	 */
	@Variable( "vsync" )
	public void setVSyned( boolean b )
	{
		target.vsync = b;
	}

	/**
	 * Gets the target frame rate
	 * 
	 * @return The maximum frames per second
	 */
	@Variable( "frame rate" )
	public int getFrameRate()
	{
		return target.framerate;
	}

	/**
	 * Sets the target frame rate
	 * 
	 * @param fr
	 *           The desired maximum frames per second
	 */
	@Variable( "frame rate" )
	@NumberRange( { 0 } )
	public void setFrameRate( int fr )
	{
		target.framerate = fr;
	}

	/**
	 * Gets the target sampling mode
	 * 
	 * @return The target sampling mode, or null for no samples
	 */
	@Variable( "fsaa" )
	@Description( "The full screen anti-aliasing mode" )
	public Samples getSamples()
	{
		return target.samples;
	}

	/**
	 * Sets the target sampling mode
	 * 
	 * @param s
	 *           The sampling mode to try for
	 */
	@Variable( "fsaa" )
	public void setSamples( Samples s )
	{
		target.samples = s;
	}

	/**
	 * Applies the fullscreen, resolution and FSAA targets
	 * 
	 * @throws LWJGLException
	 */
	@Variable( "apply" )
	public void apply() throws LWJGLException
	{
		boolean resDirty = !target.res.equals( current.res );
		boolean fsDirty = target.fullscreen != current.fullscreen;
		boolean vsyncDirty = target.vsync != current.vsync;
		boolean frDirty = target.framerate != current.framerate;
		boolean samplesDirty = target.samples != current.samples;

		if( samplesDirty )
		{
			Display.destroy();
		}

		if( resDirty || samplesDirty )
		{
			DisplayMode dm = null;
			int minDiff = Integer.MAX_VALUE;

			for( DisplayMode displaymode : availableModes )
			{
				int diff =
						Math.abs( displaymode.getWidth() - target.res.getWidth() )
								+ Math.abs( displaymode.getHeight() - target.res.getHeight() );

				if( diff < minDiff )
				{
					minDiff = diff;
					dm = displaymode;
				}
			}

			if( dm != null )
			{
				target.res.setSize( dm.getWidth(), dm.getHeight() );
				Display.setDisplayMode( dm );
			}
			else
			{
				System.err.println( "Could not find displaymode" );
			}
		}

		if( frDirty || samplesDirty )
		{
			frameRate = target.framerate;
		}

		if( vsyncDirty || samplesDirty )
		{
			Display.setVSyncEnabled( target.vsync );
		}

		if( fsDirty || samplesDirty )
		{
			Display.setFullscreen( target.fullscreen );
		}

		if( samplesDirty )
		{
			boolean created = false;

			while( !created )
			{
				try
				{
					PixelFormat pf = new PixelFormat( 0, 8, 0, target.samples.samples );
					Display.create( pf );
					created = true;
				}
				catch( LWJGLException e )
				{
					if( target.samples.ordinal() == 0 )
					{
						// we're out of luck, and the pixelformat with 0
						// samples has also failed, oddly
						if( Display.isCreated() )
						{
							Display.destroy();
						}

						Display.create();
						target.samples = Samples.NONE;
						created = true;
					}
					else
					{
						target.samples = Samples.values()[ target.samples.ordinal() - 1 ];
					}
				}
			}
		}

		current.set( target );

		Console.log( "Set display mode " + current.res.getWidth() + "x"
				+ current.res.getHeight() + " fullscreen = " + current.fullscreen
				+ " vSync = " + current.vsync + " framerate = " + current.framerate
				+ " FSAA = " + current.samples );

		for( Listener l : listeners )
		{
			l.displayChanged( resDirty, fsDirty, vsyncDirty, frDirty, samplesDirty );
		}
	}

	/**
	 * Sets the target display mode from the currently set one
	 */
	@Variable( "reset" )
	public void reset()
	{
		target.set( current );
	}

	/**
	 * Registers an object's interest in changes to the display
	 * 
	 * @param l
	 */
	public void addListener( Listener l )
	{
		listeners.add( l );
	}

	/**
	 * Registers and object's lack of interest in changes to the
	 * display
	 * 
	 * @param l
	 */
	public void removeListener( Listener l )
	{
		listeners.remove( l );
	}

	@Override
	public Configurator getConfigurator()
	{
		if( conf == null )
		{
			conf = AnnotatedConfigurator.buildConfigurator( this );

			Set<String> resRange = new TreeSet<String>();
			for( int i = 0; i < availableModes.length; i++ )
			{
				resRange.add( availableModes[ i ].getWidth() + "x"
						+ availableModes[ i ].getHeight() );
			}

			conf.setRange( "resolution", resRange.toArray( new String[ resRange.size() ] ) );
		}

		return conf;
	}

	/**
	 * Encapsulates a display configuration
	 * 
	 * @author ryanm
	 */
	private class Conf
	{
		private Dimension res = new Dimension();

		private boolean fullscreen = false;

		private boolean vsync = true;

		private int framerate = 60;

		private Samples samples = Samples.NONE;

		private void set( Conf conf )
		{
			res.setSize( conf.res );
			fullscreen = conf.fullscreen;
			vsync = conf.vsync;
			framerate = conf.framerate;
			samples = conf.samples;
		}
	}

	/**
	 * Interface for objects that want to be notified when the screen
	 * configuration changes
	 * 
	 * @author ryanm
	 */
	public static interface Listener
	{
		/**
		 * Called when some aspect of the screen configuration is
		 * changed
		 * 
		 * @param res
		 *           <code>true</code> if the resolution has been
		 *           changed
		 * @param fs
		 *           <code>true</code> if we've changed from fullscreen
		 *           to windowed or vice versa
		 * @param vsync
		 *           <code>true</code> if we used to be vsynced and now
		 *           are not, or vice versa
		 * @param fr
		 *           <code>true</code> if the traget frame rate has
		 *           changed
		 * @param fsaa
		 *           <code>true</code> if the fullscreen anti-aliasing
		 *           samples value has changed. NB: if this is true, we
		 *           have lost opengl state, so textures will need to be
		 *           init()ed again, texcoords recalculated, etc
		 */
		public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
				boolean fsaa );
	}
}
