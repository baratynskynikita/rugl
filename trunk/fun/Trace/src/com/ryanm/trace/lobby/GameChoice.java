
package com.ryanm.trace.lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Console;
import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.text.TextShape;
import com.rugl.util.Colour;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.AnnotatedConfigurator;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.GameOptions;
import com.ryanm.trace.LoadGame;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.util.Util;

/**
 * @author ryanm
 */
public class GameChoice implements Phase
{
	private static TextShape chooseText;

	private final Lobby lobby;

	private int selectedIndex = 0;

	private float displayIndex = 0;

	private boolean done = false;

	private final GameOptions[] gametypes;

	private float sep = 55;

	/**
	 * @param lobby
	 */
	public GameChoice( Lobby lobby )
	{
		this.lobby = lobby;
		gametypes = getGameOptions();
	}

	@Override
	public void advance( float delta )
	{
		if( Buttons.enter() )
		{
			Sounds.yes();
			Util.copyFields( gametypes[ selectedIndex ], TraceGame.game );
			done = true;
		}
		if( Buttons.esc() )
		{
			Sounds.no();
			done = true;
		}
		if( Buttons.down() && selectedIndex < gametypes.length - 1 )
		{
			Sounds.click();
			selectedIndex++;
		}
		if( Buttons.up() && selectedIndex > 0 )
		{
			Sounds.click();
			selectedIndex--;
		}

		float d = selectedIndex - displayIndex;
		d *= 0.1f;
		displayIndex += d;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		for( int i = 0; i < gametypes.length; i++ )
		{
			float delta = i - displayIndex;

			float base = -sep * delta;
			float alpha = selectedIndex == i ? 1 : 0.2f;

			int c = Colour.withAlphai( Colour.white, ( int ) ( 255 * alpha ) );
			TextShape name = TraceGame.font.buildTextShape( gametypes[ i ].name, c );
			name.translate( 100, 400 - TraceGame.font.size, 1 );

			name.translate( 0, base, 0 );
			name.render( r );
			name.translate( 0, -base, 0 );
		}

		// desc
		String desc = gametypes[ selectedIndex ].description;
		TexturedShape dts =
				new TextLayout( desc, TraceGame.font, Alignment.LEFT, 1000, Colour.white ).textShape;
		dts.scale( 0.5f, 0.5f, 1 );
		dts.translate( 75, 280, 1 );
		dts.render( r );

		if( chooseText == null )
		{
			chooseText = TraceGame.font.buildTextShape( "choose game type", Colour.white );
			chooseText.translate( 75, 400, 1 );
		}

		chooseText.render( r );
	}

	@Override
	public boolean isFinished()
	{
		return done;
	}

	@Override
	public Phase next()
	{
		return lobby;
	}

	@Override
	public void reset()
	{
		done = false;

		Buttons.setText( "Back", "Accept", " ", " ", null, null );
		TraceGame.drawLastArena = true;

		// find current
		for( int i = 0; i < gametypes.length; i++ )
		{
			if( gametypes[ i ].name.equals( TraceGame.game.name ) )
			{
				selectedIndex = i;
				displayIndex = i;
				break;
			}
		}
	}

	private GameOptions[] getGameOptions()
	{
		List<GameOptions> gl = new ArrayList<GameOptions>();

		gl.add( new GameOptions() );

		{
			GameOptions g = new GameOptions();
			g.name = "training";
			g.description = "safety protocols engaged";
			g.endgame.set( 0, 0, false );
			g.trace.startShields = 10;
			g.trace.maxShields = 10;
			g.trace.speed = 80;
			g.trace.gapLength = 25;
			g.trace.traceBounce = 50;
			g.trace.arenaBounce = 90;
			g.powerups.spawnRate.set( 1, 5 );

			gl.add( g );
		}

		{
			GameOptions g = new GameOptions();
			g.name = "retro";
			g.description = "9 out of 10 nostalgic masochists love it";
			g.powerups.life = -1;
			g.trace.set( 0, 0, 100, 150, 80, 15, -1, 1, 1, 1, 1, 1, 1 );
			g.endgame.set( 0, 0, false );
			g.score.strike.set( 0, 0, 0 );
			g.score.death.set( 0, 0, 0, 0 );
			g.score.gap.set( 20, 20, 0 );

			gl.add( g );
		}

		{
			GameOptions g = new GameOptions();
			g.name = "hunters";
			g.description = "there can be only one";
			g.trace.set( 3, 3, 100, 150, 100, 0, 200, 30, 30, 2, 1, 2, 20 );
			g.score.strike.set( 5, -5, 0 );
			g.score.death.set( 20, 0, 0, 40 );
			g.score.gap.set( 0, 0, 0 );
			g.powerups.gapSize.inf = 0;
			g.powerups.gapSize.minus = 0;
			g.powerups.gapSize.plus = 0;
			g.endgame.traceGrowth = 1000;
			g.endgame.speedUp = 60;

			gl.add( g );
		}

		{
			GameOptions g = new GameOptions();
			g.name = "zero-sum";
			g.description = "we respect the laws of thermodynamics in this house";
			g.score.strike.set( 5, 0, -5 );
			g.score.death.set( 10, -10, 0, 10 );
			g.score.gap.set( 20, 0, -20 );
			gl.add( g );
		}

		{
			GameOptions g = new GameOptions();
			g.name = "plague";
			g.description = "bring disinfectant";
			g.trace.traceLength = 200;
			g.trace.gapLength = 0;
			g.powerups.spawnRate.set( 1, 5 );
			g.powerups.virus = 50;
			gl.add( g );
		}

		// load files
		File dir = new File( GameBox.filebase, LoadGame.dirName );
		dir.mkdirs();
		File[] possibles = dir.listFiles();
		for( File f : possibles )
		{
			GameOptions go = new GameOptions();
			Configurator conf = AnnotatedConfigurator.buildConfigurator( go );

			try
			{
				ConfigurationSerialiser.loadConfiguration( f, conf );
				gl.add( go );
			}
			catch( IOException e )
			{
				Console.log( e.getMessage() + " when trying to load gametype " + f.getName() );
			}
			catch( ParseException e )
			{
				Console.log( e.getMessage() + " when trying to load gametype " + f.getName() );
			}
		}

		return gl.toArray( new GameOptions[ gl.size() ] );
	}
}
