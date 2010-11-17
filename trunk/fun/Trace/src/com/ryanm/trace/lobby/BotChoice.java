
package com.ryanm.trace.lobby;

import java.util.Random;

import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.text.TextShape;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.ai.BotManager;
import com.ryanm.trace.game.ai.MeatBag;
import com.ryanm.util.Util;

/**
 * @author ryanm
 */
class BotChoice implements Phase
{
	private static TextShape chooseBot;

	private Lobby lobby;

	private Phase next = null;

	private String[] bots = BotManager.getNames();

	private int selectedIndex = 0;

	private float displayedIndex = 0;

	private float sep = 55;

	/**
	 * @param lobby
	 */
	public BotChoice( Lobby lobby )
	{
		this.lobby = lobby;
	}

	@Override
	public void advance( float delta )
	{
		if( Buttons.esc() )
		{
			Sounds.no();
			next = lobby;
		}
		else if( Buttons.enter() )
		{
			Sounds.yes();

			// set bot
			if( bots[ selectedIndex ] == MeatBag.NAME )
			{
				// go to name choice
				next = new NameEntry( lobby, this );
			}
			else
			{ // build a bot player
				Player p = new Player( NameEntry.randomName() );
				p.bot = BotManager.forName( bots[ selectedIndex ] );
				p.colour = unusedColour();

				lobby.addPlayer( p );
				next = lobby;
			}
		}

		if( Buttons.down() && selectedIndex < bots.length - 1 )
		{
			Sounds.click();
			selectedIndex++;
		}

		if( Buttons.up() && selectedIndex > 0 )
		{
			Sounds.click();
			selectedIndex--;
		}

		// animate
		float d = selectedIndex - displayedIndex;
		d *= 0.1f;
		displayedIndex += d;
	}

	private int unusedColour()
	{
		int[] rc = ColourChoice.allColours.clone();
		Util.shuffle( rc, new Random() );
		for( int c : rc )
		{
			if( !lobby.colourInUse( c ) )
			{
				return c;
			}
		}

		assert false;
		return -1;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		for( int i = 0; i < bots.length; i++ )
		{
			float delta = i - displayedIndex;

			float base = -sep * delta;
			float alpha = selectedIndex == i ? 1 : 0.2f;

			int c = Colour.withAlphai( Colour.white, ( int ) ( 255 * alpha ) );
			TextShape name = TraceGame.font.buildTextShape( bots[ i ], c );
			name.translate( 100, 400 - TraceGame.font.size, 1 );

			name.translate( 0, base, 0 );
			name.render( r );
			name.translate( 0, -base, 0 );
		}

		// desc
		String desc = BotManager.getDescription( bots[ selectedIndex ] );
		TexturedShape dts =
				new TextLayout( desc, TraceGame.font, Alignment.LEFT, 1000, Colour.white ).textShape;
		dts.scale( 0.5f, 0.5f, 1 );
		dts.translate( 75, 280, 1 );
		dts.render( r );

		if( chooseBot == null )
		{
			chooseBot = TraceGame.font.buildTextShape( "choose trace type", Colour.white );
			chooseBot.translate( 75, 400, 1 );
		}

		chooseBot.render( r );
	}

	@Override
	public boolean isFinished()
	{
		return next != null;
	}

	@Override
	public Phase next()
	{
		return next;
	}

	@Override
	public void reset()
	{
		next = null;
		Buttons.setText( "Back", "Accept", " ", " ", null, null );
		TraceGame.drawLastArena = true;
	}
}
