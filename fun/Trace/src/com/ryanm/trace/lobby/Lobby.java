
package com.ryanm.trace.lobby;

import java.util.LinkedList;

import org.lwjgl.input.Mouse;

import com.rugl.GameBox;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextShape;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.ArenaStart;
import com.ryanm.trace.game.ai.MeatBag;

/**
 * @author ryanm
 */
public class Lobby implements Phase
{
	private boolean finished = false;

	private Phase next = null;

	private LinkedList<Player> players = new LinkedList<Player>();

	{
		Player.load( "players.txt", players );
	}

	/**
	 * @return The number of players in the lobby
	 */
	public int playerCount()
	{
		return players.size();
	}

	/**
	 * Determines if a colour is in use by a player
	 * 
	 * @param c
	 * @return <code>true</code> if someone already has that colour,
	 *         <code>false</code> otherwise
	 */
	public boolean colourInUse( int c )
	{
		for( Player p : players )
		{
			if( p.colour == c )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if a key is in use by a player
	 * 
	 * @param k
	 * @return <code>true</code> if in use, <code>false</code>
	 *         otherwise
	 */
	public boolean keyInUse( int k )
	{
		for( Player p : players )
		{
			if( p.leftKey == k || p.rightKey == k )
			{
				return true;
			}
		}
		return false;
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

	@Override
	public void reset()
	{
		finished = false;
		next = null;

		Buttons.visible = true;
		TraceGame.drawLastArena = true;
	}

	/**
	 * Adds a player
	 * 
	 * @param player
	 */
	public void addPlayer( Player player )
	{
		players.add( player );

		if( player.bot instanceof MeatBag )
		{
			TraceGame.addToPool( player );
		}
	}

	/**
	 * @param delta
	 */
	@Override
	public void advance( float delta )
	{
		if( Buttons.esc() )
		{
			if( Mouse.isGrabbed() )
			{
				Mouse.setGrabbed( false );
				Sounds.no();
			}
			else
			{
				Player.save( "players.txt", players );
				GameBox.stop();
			}
		}
		else if( Buttons.enter() )
		{
			if( !players.isEmpty() )
			{
				// refresh the active players into the pool
				for( Player p : players )
				{
					TraceGame.addToPool( p );
				}

				// start the game
				Player.save( "players.txt", players );

				Sounds.yes();
				next = new ArenaStart( this, players );
				finished = true;
			}
		}
		else if( Buttons.down() )
		{ // remove player
			if( !players.isEmpty() )
			{
				Sounds.no();
				players.removeLast();
			}
		}
		else if( Buttons.up() && players.size() < 8 )
		{ // add player
			Sounds.yes();
			next = new BotChoice( this );
			finished = true;
		}
		else if( Buttons.right() )
		{
			Sounds.yes();
			next = new FaqPhase( this );
			finished = true;
		}
		else if( Buttons.left() )
		{
			Sounds.yes();
			next = new GameChoice( this );
			finished = true;
		}

		Buttons.setText( "Quit", players.isEmpty() ? null : "Start",
				players.size() < 8 ? "add\nuser" : null, players.isEmpty() ? null
						: "remove\nuser", "game\ntype", "faq" );
	}

	@Override
	public void draw( StackedRenderer r )
	{
		Scoreboard.render( r, players, 130, 20, 410, false );

		TextShape type =
				TraceGame.font.buildTextShape( "gametype:\n" + TraceGame.game.name,
						Colour.white );
		type.scale( 0.75f, 0.75f, 1 );
		type.translate( 530, 365, 1 );

		type.render( r );
	}
}
