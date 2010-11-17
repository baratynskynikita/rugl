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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Timer;

import com.rugl.config.KeyPressCodec;
import com.rugl.console.Console;
import com.rugl.console.KeyBinds;
import com.rugl.gl.State;
import com.rugl.input.KeyListener;
import com.rugl.input.KeyPress;
import com.rugl.input.MouseListener;
import com.rugl.sound.SoundSystem;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.ConfGet;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.NumberRange;
import com.ryanm.config.imp.Variable;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;
import com.ryanm.util.CodeTimer;
import com.ryanm.util.CodeTimer.Output;

/**
 * Container framework for games, handles setup, configuration etc
 * 
 * @author ryanm
 */
@ConfigurableType( "rugl" )
@Description( "Holds options for framework elements" )
public class GameBox
{
	/**
	 * The time when the GameBox class was loaded
	 */
	public static final long startUpTime = System.currentTimeMillis();

	/**
	 * The embedded game
	 */
	public static Game game;

	/**
	 * Controls resolution, frame rate, fullscreen
	 */
	@Variable
	public static final DisplayConfigurable dispConf = new DisplayConfigurable();

	/**
	 * Not actually used, since everything in {@link Console} is
	 * static, but includes the console in the configurators
	 */
	@Variable
	public static final Console console = new Console();

	private static boolean shouldStop = false;

	/**
	 * Time over which the fps is calculated
	 */
	@Variable( "fps resolution" )
	@Description( "The time period over which fps is calculated" )
	@NumberRange( { 0.1f } )
	public static float fpsPeriod = 1;

	/**
	 * Multiplier applied to time deltas
	 */
	@Variable( "time multiplier" )
	@Description( "Affects the speed of the game 0 = stopped, 1 = normal, 2 = twice as fast, etc" )
	@NumberRange( 0 )
	public static float timeMultiplier = 1;

	/**
	 * The time when the fps was last calculated
	 */
	private static float lastFPSTime = 0;

	/**
	 * Number of frames since the last fps calculation
	 */
	private static int frames = 0;

	/**
	 * The last fps calculation result
	 */
	private static float fps = 0;

	private static Timer timer;

	/**
	 * The console toggle keypress. Defaults to
	 * {@link Keyboard#KEY_GRAVE}
	 */
	@Variable( "console key" )
	@Description( "The key that triggers the console to show/hide" )
	public static KeyPress consoleTrigger = new KeyPress( true, Keyboard.KEY_GRAVE );

	/**
	 * The quit keypress. Defaults to {@link Keyboard#KEY_ESCAPE} set
	 * to a {@link KeyPress} with no keycodes to disable
	 */
	@Variable( "quit key" )
	@Description( "The key that immediately quits the game" )
	public static KeyPress quitTrigger = new KeyPress( false, Keyboard.KEY_ESCAPE );

	/**
	 * The base for file saves
	 */
	@Variable( "save base" )
	@Description( "The base directory where games should put saves, cache resources, etc" )
	public static File filebase = new File( System.getProperty( "user.home" ) );

	/***/
	@Variable( "screengrab width" )
	@Description( "The width of captured images" )
	public static int tiledGrabSize = 800;

	/**
	 * Indicates if we are running in a sandbox
	 */
	public static final boolean secureEnvironment =
			System.getProperty( "com.rugl.secure" ) != null;

	private static ArrayList<KeyListener> keyListeners = new ArrayList<KeyListener>();

	private static ArrayList<MouseListener> mouseListeners =
			new ArrayList<MouseListener>();

	private static Thread resourceLoader = new Thread( "Resource loader" ) {
		@Override
		public void run()
		{
			Console.loadCommandHistory();
			Console.loadFont();

			game.loadResources();
		};
	};

	/**
	 * Holds the roots of the configurator forest. First element is the
	 * rugl stuff - display config, console, etc, second element is the
	 * game.
	 */
	public static final Configurator[] configurators = new Configurator[ 2 ];

	static
	{
		ConfigurationSerialiser.registerCodec( new KeyPressCodec() );

		configurators[ 0 ] = ConfGet.getConfigurator( new GameBox() );

		GameBox.dispConf.addListener( new DisplayConfigurable.Listener() {
			@Override
			public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
					boolean fsaa )
			{
				if( fsaa )
				{ // the gl context has been renewed, need to notify
					// state-handling stuff
					State.stateReset();
				}
			}
		} );
	}

	/**
	 * Sets the game, loads a configuration and then runs. This method
	 * ends with a {@link System#exit(int)}, so don't be expecting it
	 * to return
	 * 
	 * @param g
	 *           The game to embed
	 * @param configurationPath
	 *           The path to the configuration resource file (on the
	 *           classpath), or null
	 */
	public static void startGame( Game g, String configurationPath )
	{
		CodeTimer ct = new CodeTimer( "RUGL loop ", Output.Millis, Output.Millis );
		ct.enabled = System.getProperty( "com.rugl.profile" ) != null;
		ct.tick( "start" );

		game = g;

		long loadStart = System.currentTimeMillis();
		resourceLoader.start();

		Display.setTitle( game.getName() );
		Console.log( secureEnvironment ? "Sandboxed" : "Not sandboxed" );
		Console.log( "Setting game \"" + game.getName() + "\"" );

		String s = game.getName();
		if( LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX )
		{
			s = "." + s;
		}

		filebase = new File( filebase, s );
		filebase.mkdirs();

		ct.tick( "building configurators" );

		configurators[ 1 ] = ConfGet.getConfigurator( game );

		ct.tick( "saving default" );

		// save default configuration
		try
		{
			ConfigurationSerialiser.saveConfiguration( new File( filebase, "defaults.xml" ),
					configurators );
		}
		catch( IOException e1 )
		{
			e1.printStackTrace();
		}

		ct.tick( "loading config" );

		// load configuration
		if( configurationPath != null )
		{
			Console.log( "Loading configuration " + configurationPath );
			try
			{
				ConfigurationSerialiser.loadConfiguration(
						GameBox.class.getResourceAsStream( configurationPath ), configurators );
			}
			catch( ParseException e )
			{
				Console.error( "Could not parse configuration stream" );
				Console.error( e.getMessage() );
			}
			catch( IOException e )
			{
				Console.error( "Problem when reading configuration stream" );
				Console.error( e.getMessage() );
			}
		}

		try
		{
			ct.tick( "openGL" );

			initOpenGL();

			ct.tick( "openAL" );

			SoundSystem.init();

			ct.tick( "last" );

			Keyboard.enableRepeatEvents( true );

			timer = new Timer();
			Timer.tick();
			float lastTime = timer.getTime();
			float delta = processTimer( lastTime );

			Console.log( "Startup in " + ( System.currentTimeMillis() - startUpTime )
					/ 1000.0f + " seconds" );

			ct.lastTick( true );

			while( !shouldStop )
			{
				ct.tick( "updt" );

				if( resourceLoader != null && !resourceLoader.isAlive() )
				{
					long loadTime = System.currentTimeMillis() - loadStart;
					Console.log( "Resources loaded in " + ( float ) loadTime / 1000 );

					game.resourcesLoaded();

					resourceLoader = null;
				}

				Display.update();

				ct.tick( "inpt" );

				delta = processTimer( lastTime );
				lastTime += delta;

				processKeyEvents();

				processMouseEvents();

				if( Display.isCloseRequested() )
				{
					shouldStop = true;
				}
				else if( Display.isActive() )
				{
					ct.tick( "adva" );

					console.advance( delta );

					if( !Console.isVisible() )
					{
						game.advance( delta * timeMultiplier );
					}

					ct.tick( "draw" );

					draw();

					ct.tick( "sync" );

					Display.sync( dispConf.frameRate );
				}
				else
				{
					try
					{
						Thread.sleep( 100 );
					}
					catch( InterruptedException e )
					{
					}

					if( Display.isVisible() || Display.isDirty() )
					{
						draw();
					}

					ct.tick( "adva" );
					ct.tick( "draw" );
					ct.tick( "sync" );
				}

				Util.checkGLError();

				ct.lastTick();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				ConfigurationSerialiser.saveConfiguration(
						new File( filebase, "lastConf.xml" ), configurators );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			game.exit();

			// save window placement

			// moar cleanup
			Display.destroy();
			Console.exit();
			SoundSystem.destroy();
		}

		System.exit( 0 );
	}

	private static void initOpenGL()
	{
		try
		{
			dispConf.apply();

			if( !Display.isCreated() )
			{ // applying the dispConf may have created the display
				// already
				Display.create();
			}

			Console.log( "OpenGL adapter = " + Display.getAdapter() );
			Console.log( "OpenGL driver version = " + Display.getVersion() );

			// restore clearcolor to opengl default
			GL11.glClearColor( 0, 0, 0, 0 );
		}
		catch( LWJGLException e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}
	}

	private static void draw()
	{
		game.draw();

		Console.draw();

		frames++;
	}

	private static float processTimer( float lastTime )
	{
		Timer.tick();
		float time = timer.getTime();
		float delta = time - lastTime;
		lastTime = time;
		if( time > lastFPSTime + fpsPeriod )
		{
			fps = frames / fpsPeriod;
			frames = 0;
			lastFPSTime = time;
		}

		return delta;
	}

	private static void processKeyEvents()
	{
		while( Keyboard.next() )
		{
			int key = Keyboard.getEventKey();
			boolean down = Keyboard.getEventKeyState();
			boolean repeat = Keyboard.isRepeatEvent();
			char c = Keyboard.getEventCharacter();

			if( !Console.isVisible() )
			{
				notifyListeners( keyListeners, key, down, repeat, c );
			}
			else
			{
				Console.input( key, down, repeat, c );
			}
		}

		if( consoleTrigger != null && consoleTrigger.isActive() )
		{
			Console.toggle();
		}
		if( quitTrigger != null && quitTrigger.isActive() )
		{
			stop();
		}

		if( !Console.isVisible() )
		{
			KeyBinds.checkBinds();
		}
	}

	private static void notifyListeners( ArrayList<KeyListener> listeners, int key,
			boolean down, boolean repeat, char c )
	{
		for( int i = 0; i < listeners.size(); i++ )
		{
			if( down )
			{
				listeners.get( i ).keyDown( key, c, repeat );
			}
			else
			{
				listeners.get( i ).keyUp( key );
			}
		}
	}

	private static void processMouseEvents()
	{
		while( Mouse.next() )
		{
			int button = Mouse.getEventButton();
			int x = Mouse.getEventX();
			int y = Mouse.getEventY();
			int dx = Mouse.getEventDX();
			int dy = Mouse.getEventDY();
			int dz = Mouse.getEventDWheel();

			notifyListeners( mouseListeners, button, x, y, dx, dy, dz );
		}
	}

	private static void notifyListeners( ArrayList<MouseListener> listeners, int button,
			int x, int y, int dx, int dy, int dz )
	{
		if( button != -1 )
		{
			for( int i = 0; i < mouseListeners.size(); i++ )
			{
				mouseListeners.get( i ).mouseButton( button, Mouse.isButtonDown( button ) );
			}
		}
		else if( dz != 0 )
		{
			for( int i = 0; i < mouseListeners.size(); i++ )
			{
				mouseListeners.get( i ).mouseWheel( dz );
			}
		}
		else
		{
			for( int i = 0; i < mouseListeners.size(); i++ )
			{
				mouseListeners.get( i ).mouseMoved( x, y, dx, dy );
			}
		}
	}

	/**
	 * Call when the game should exit as soon as possible
	 */
	public static void stop()
	{
		shouldStop = true;
		Thread.currentThread().interrupt();
	}

	/**
	 * Gets the current fps
	 * 
	 * @return The current number of frames rendered per second
	 */
	public static float getFPS()
	{
		return fps;
	}

	/**
	 * Makes a screenshot and saves to the current directory
	 */
	@Variable( "screengrab" )
	@Description( "Takes a screengrab" )
	public static void screenshot()
	{
		Dimension d = dispConf.getResolution();
		Dimension g =
				new Dimension( tiledGrabSize, tiledGrabSize * d.getHeight() / d.getWidth() );
		ScreenGrabber.screenshot( g );
	}

	/**
	 * Registers a {@link KeyListener}'s interest in forthcoming
	 * keyboard events. The listener will not be notified while the
	 * game is paused
	 * 
	 * @param l
	 */
	public static void addKeyListener( KeyListener l )
	{
		keyListeners.add( l );
	}

	/**
	 * Registers a {@link KeyListener}'s disinterest in forthcoming
	 * keyboard events
	 * 
	 * @param l
	 */
	public static void removeKeyListener( KeyListener l )
	{
		keyListeners.remove( l );
	}

	/**
	 * Registers a {@link MouseListener}'s interest in forthcoming
	 * mouse events. The listener will not be notified while the game
	 * is paused
	 * 
	 * @param l
	 */
	public static void addMouseListener( MouseListener l )
	{
		mouseListeners.add( l );
	}

	/**
	 * Registers a {@link KeyListener}'s disinterest in forthcoming
	 * mouse events
	 * 
	 * @param l
	 */
	public static void removeMouseListener( MouseListener l )
	{
		mouseListeners.remove( l );
	}
}
