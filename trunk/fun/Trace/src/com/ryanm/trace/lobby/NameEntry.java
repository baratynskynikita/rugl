
package com.ryanm.trace.lobby;

import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.rugl.GameBox;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.input.KeyListener;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.ai.MeatBag;

/**
 * Name entry screen
 * 
 * @author ryanm
 */
public class NameEntry implements Phase
{
	private static TexturedShape enterName = null;

	private static ColouredShape caret = null;

	private final Lobby lobby;

	private final BotChoice botChoice;

	private StringBuilder name = new StringBuilder();

	private boolean finished = false;

	private Phase next;

	private float caretBlink = 0.5f;

	private float time = 0;

	private KeyListener kl = new KeyListener() {
		@Override
		public void keyDown( int keyCode, char keyChar, boolean repeat )
		{
			if( !repeat )
			{
				if( keyCode == Keyboard.KEY_BACK )
				{
					if( name.length() > 0 )
					{
						Sounds.click();
						name.deleteCharAt( name.length() - 1 );
					}
				}
				else if( keyChar != Keyboard.CHAR_NONE && name.length() < 3 )
				{
					Sounds.click();
					name.append( keyChar );
				}
			}
		}
	};

	/**
	 * @param lobby
	 * @param botChoice
	 */
	public NameEntry( Lobby lobby, BotChoice botChoice )
	{
		this.lobby = lobby;
		this.botChoice = botChoice;
	}

	@Override
	public void reset()
	{
		GameBox.addKeyListener( kl );
		finished = false;
		TraceGame.drawLastArena = true;
	}

	@Override
	public void advance( float delta )
	{
		time += delta;

		if( name.length() == 3 )
		{
			Buttons.setText( "Cancel", "Accept", null, null, null, null );
		}
		else
		{
			Buttons.setText( "Cancel", null, null, null, null, null );
		}

		if( Buttons.esc() )
		{
			// back to bot choice
			Sounds.no();
			next = botChoice;
			finished = true;
		}
		else if( Buttons.enter() && name.length() == 3 )
		{
			Sounds.yes();
			// next stage
			finished = true;

			String n = name.toString();
			Player p = TraceGame.checkPool( n );

			if( p == null || !( p.bot instanceof MeatBag ) )
			{
				p = new Player( n );
				p.bot = new MeatBag();
			}

			next = new ColourChoice( p, this, lobby );
		}

		if( finished )
		{
			GameBox.removeKeyListener( kl );
		}
	}

	@Override
	public void draw( StackedRenderer r )
	{
		if( enterName == null )
		{
			enterName = TraceGame.font.buildTextShape( "Enter name", Colour.white );
			enterName.translate( 75, 400, 1 );

			caret =
					new ColouredShape( ShapeUtil.filledQuad( 0, -7,
							TraceGame.font.getStringLength( "A" ), -2, 1 ), Colour.white, null );

			caret.translate( 100, 400 - TraceGame.font.size, 0 );
		}

		enterName.render( r );

		float caretPos = 0;

		if( name.length() > 0 )
		{
			TexturedShape nt = TraceGame.font.buildTextShape( name, Colour.white );
			nt.translate( 100, 400 - TraceGame.font.size, 1 );
			caretPos = TraceGame.font.getStringLength( name );

			nt.render( r );
		}

		if( name.length() < 3 && ( int ) ( time / caretBlink ) % 2 == 0 )
		{
			caret.translate( caretPos, 0, 0 );
			caret.render( r );
			caret.translate( -caretPos, 0, 0 );
		}
	}

	@Override
	public boolean isFinished()
	{
		return finished;
	}

	@Override
	public Phase next()
	{
		return next;
	}

	private static String alpha = "abcedefhijklmnopqrstuvwxyz";

	private static Random rng = new Random();

	/**
	 * @return a random name
	 */
	public static String randomName()
	{
		char[] ca =
				new char[] { alpha.charAt( rng.nextInt( alpha.length() ) ),
						alpha.charAt( rng.nextInt( alpha.length() ) ),
						alpha.charAt( rng.nextInt( alpha.length() ) ) };
		return new String( ca );
	}
}
