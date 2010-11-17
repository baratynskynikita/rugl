
package com.ryanm.trace.lobby;

import org.lwjgl.input.Keyboard;

import com.rugl.GameBox;
import com.rugl.geom.TexturedShape;
import com.rugl.input.KeyListener;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;

/**
 * @author ryanm
 */
public class ControlChoice implements Phase
{
	private boolean finished = false;

	private final Player player;

	private final Lobby lobby;

	private final ColourChoice colourChoice;

	private Phase next;

	private KeyListener kl = new KeyListener() {
		@Override
		public void keyDown( int keyCode, char keyChar, boolean repeat )
		{
			if( keyCode != Keyboard.KEY_ESCAPE )
			{
				if( player.leftKey == -1 )
				{
					Sounds.click();
					player.leftKey = keyCode;
				}
				else if( player.rightKey == -1 )
				{
					Sounds.click();
					player.rightKey = keyCode;
				}
			}
		}
	};

	/**
	 * @param player
	 * @param lobby
	 * @param colourChoice
	 */
	public ControlChoice( Player player, Lobby lobby, ColourChoice colourChoice )
	{
		this.player = player;
		this.lobby = lobby;
		this.colourChoice = colourChoice;
	}

	@Override
	public void reset()
	{
		finished = false;
		next = null;
		GameBox.addKeyListener( kl );
		TraceGame.drawLastArena = true;
	}

	@Override
	public void advance( float delta )
	{
		if( Buttons.esc() )
		{
			Sounds.no();
			next = colourChoice;
			finished = true;
		}
		else
		{
			if( player.leftKey != -1 && player.rightKey != -1 )
			{
				Buttons.setText( "back", "accept", null, null, "set", "set" );

				if( Buttons.left() )
				{
					Sounds.click();
					player.leftKey = -1;
				}
				else if( Buttons.right() )
				{
					Sounds.click();
					player.rightKey = -1;
				}

				if( Buttons.enter() )
				{
					GameBox.removeKeyListener( kl );
					Sounds.yes();
					next = lobby;
					lobby.addPlayer( player );
					finished = true;
				}
			}
			else
			{
				Buttons.setText( "back", null, null, null, null, null );
			}
		}
	}

	private static TexturedShape chooseLeft, chooseRight, left, right;

	@Override
	public void draw( StackedRenderer r )
	{
		if( chooseLeft == null )
		{
			chooseLeft = TraceGame.font.buildTextShape( "Choose left key", Colour.white );
			chooseLeft.translate( 75, 400, 1 );

			chooseRight = TraceGame.font.buildTextShape( "Choose right key", Colour.white );
			chooseRight.translate( 75, 400, 1 );

			left = TraceGame.font.buildTextShape( "left", Colour.white );
			left.translate( 100, 400, 1 );
			right = TraceGame.font.buildTextShape( "right", Colour.white );
			right.translate( 400, 400, 1 );
		}

		if( player.leftKey == -1 )
		{
			chooseLeft.render( r );
		}
		else if( player.rightKey == -1 )
		{
			chooseRight.render( r );
		}
		else
		{
			left.render( r );

			TexturedShape lc =
					TraceGame.font.buildTextShape( Keyboard.getKeyName( player.leftKey ),
							player.colour );
			lc.translate( 100, 400 - TraceGame.font.size, 1 );
			lc.render( r );

			right.render( r );
			TexturedShape rc =
					TraceGame.font.buildTextShape( Keyboard.getKeyName( player.rightKey ),
							player.colour );
			rc.translate( 400, 400 - TraceGame.font.size, 1 );
			rc.render( r );
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
}
