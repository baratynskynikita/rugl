
package com.ryanm.trace;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;

import com.rugl.DisplayConfigurable;
import com.rugl.DisplayConfigurable.Samples;
import com.rugl.Game;
import com.rugl.GameBox;
import com.rugl.console.Console;
import com.rugl.input.KeyPress;
import com.rugl.renderer.Renderer.Processor;
import com.rugl.renderer.StackedRenderer;
import com.rugl.renderer.proc.Alpha;
import com.rugl.text.Font;
import com.rugl.ui.MouseTranslator;
import com.rugl.util.GLUtil;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Variable;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;
import com.ryanm.trace.game.Arena;

/**
 * @author ryanm
 */
@ConfigurableType( "trace" )
public class TraceGame implements Game
{
	/***/
	public static Random rng = new Random();

	private static int screenWidth = 800;

	private static final String PLAYER_POOL_FILE = "playerPool.txt";

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		GameBox.startGame( new TraceGame(), null );
	}

	/***/
	@Variable
	public static final ClientOptions client = new ClientOptions();

	/***/
	@Variable
	public static final GameOptions game = new GameOptions();

	private Phase currentPhase;

	private StackedRenderer r = new StackedRenderer();

	/***/
	public static Font font = null;

	/**
	 * Holds the last 15 uniquely-named players
	 */
	public static LinkedList<Player> playerPool = new LinkedList<Player>();

	/**
	 * Whether to draw the last arena or not
	 */
	public static boolean drawLastArena = true;

	/**
	 * The last played game
	 */
	public static Arena lastArena = null;

	/**
	 * <code>true</code> if the resources have been loaded
	 */
	public static boolean resourcesLoaded = false;

	private float time = 0;

	private float logicTime = 0;

	private Processor dimmer = new Alpha( 0.5f );

	/***/
	public TraceGame()
	{
		GameBox.dispConf.setResolution( new Dimension( 800, 600 ) );
		GameBox.dispConf.setFullScreen( false );
		GameBox.dispConf.setSamples( Samples.SIXTEEN );
		GameBox.quitTrigger = new KeyPress( false );

		MouseTranslator.setGameDimenion( screenWidth, 600 );

		GameBox.dispConf.addListener( new DisplayConfigurable.Listener() {
			@Override
			public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
					boolean fsaa )
			{
				if( res )
				{
					float f = 600.0f / Display.getDisplayMode().getHeight();
					screenWidth = ( int ) ( f * Display.getDisplayMode().getWidth() );

					MouseTranslator.setGameDimenion( screenWidth, 600 );
				}
			}
		} );

		currentPhase = new TitlePhase();
		currentPhase.reset();

		Console.addCommand( new SaveGame() );
		Console.addCommand( new LoadGame() );
	}

	@Override
	public String getName()
	{
		return TraceGame.class.getAnnotation( ConfigurableType.class ).value();
	}

	@Override
	public void advance( float delta )
	{
		time += delta;

		float logicAdvance = 1 / client.logicRate;

		while( logicTime < time )
		{
			Background.advance( logicAdvance );
			Buttons.advance( logicAdvance );

			if( currentPhase.isFinished() )
			{
				currentPhase = currentPhase.next();
				currentPhase.reset();
			}

			currentPhase.advance( logicAdvance );

			logicTime += logicAdvance;
		}
	}

	@Override
	public void draw()
	{
		GLUtil.scaledOrtho( screenWidth, 600 );
		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
		GL11.glTranslatef( ( screenWidth - 800 ) / 2, 0, 0 );

		if( client.wireFrame )
		{
			GL11.glPolygonMode( GL11.GL_FRONT, GL11.GL_LINE );
			GL11.glDisable( GL11.GL_BLEND );
		}

		Background.render( r );

		currentPhase.draw( r );

		if( drawLastArena && lastArena != null )
		{
			r.push( dimmer );
			lastArena.drawTraces( r );
			r.popProcessor();
		}

		Buttons.draw( r );

		r.render();

		GL11.glPolygonMode( GL11.GL_FRONT, GL11.GL_FILL );
		GL11.glEnable( GL11.GL_BLEND );
	}

	@Override
	public void exit()
	{
		Player.save( PLAYER_POOL_FILE, playerPool );
	}

	/**
	 * Checks to see if there's an existing player with that name
	 * 
	 * @param name
	 * @return the player with that name, or <code>null</code> if none
	 *         is found
	 */
	public static Player checkPool( String name )
	{
		for( Player p : playerPool )
		{
			if( p.name.equals( name ) )
			{
				return p;
			}
		}

		return null;
	}

	/**
	 * Adds a player to the pool
	 * 
	 * @param p
	 */
	public static void addToPool( Player p )
	{
		Iterator<Player> iter = playerPool.iterator();
		while( iter.hasNext() )
		{
			if( iter.next().name.equals( p.name ) )
			{
				iter.remove();
			}
		}

		playerPool.add( p );

		while( playerPool.size() > 15 )
		{
			playerPool.removeLast();
		}
	}

	@Override
	public void loadResources()
	{
		playerPool.clear();
		Player.load( PLAYER_POOL_FILE, playerPool );

		if( font == null )
		{
			try
			{
				font =
						new Font( Thread.currentThread().getContextClassLoader()
								.getResourceAsStream( "Data Control-72.ruglfont" ) );
			}
			catch( Exception e )
			{
				e.printStackTrace();
				GameBox.stop();
			}
		}

		Sounds.init();
	}

	@Override
	public void resourcesLoaded()
	{
		resourcesLoaded = true;

		// we can do this now, since we're in the main thread
		try
		{
			ConfigurationSerialiser.loadConfiguration( new File( GameBox.filebase,
					"lastConf.xml" ), GameBox.configurators );
		}
		catch( IOException e1 )
		{
			e1.printStackTrace();
		}
		catch( ParseException e1 )
		{
			e1.printStackTrace();
		}
	}
}
