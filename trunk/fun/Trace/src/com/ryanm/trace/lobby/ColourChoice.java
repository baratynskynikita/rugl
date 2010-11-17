
package com.ryanm.trace.lobby;

import java.util.Arrays;

import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;

/**
 * Colour choice
 * 
 * @author ryanm
 */
public class ColourChoice implements Phase
{
	private static TexturedShape chooseColour;

	/***/
	public static int[] allColours = new int[] { Colour.white, Colour.red, Colour.green,
			Colour.blue, Colour.grey, Colour.orange, Colour.violet, Colour.yellow };

	private final Player player;

	private final Lobby lobby;

	private final NameEntry nameEntry;

	private boolean finished = false;

	private Phase next;

	private final int[] colours;

	private int selectedIndex = 0;

	private float displayedIndex = 0;

	private float sep = 55;

	private TexturedShape name;

	/**
	 * @param player
	 * @param nameEntry
	 * @param lobby
	 */
	public ColourChoice( Player player, NameEntry nameEntry, Lobby lobby )
	{
		this.player = player;
		this.nameEntry = nameEntry;
		this.lobby = lobby;

		name = TraceGame.font.buildTextShape( player.name, Colour.white );
		name.translate( 100, 400 - TraceGame.font.size, 1 );

		colours = new int[ allColours.length - lobby.playerCount() ];
		int avi = 0;
		for( int i = 0; i < allColours.length; i++ )
		{
			if( !lobby.colourInUse( allColours[ i ] ) )
			{
				colours[ avi++ ] = allColours[ i ];
			}
		}

		for( int i = 0; i < colours.length; i++ )
		{
			if( player.colour == colours[ i ] )
			{
				displayedIndex = i;
				selectedIndex = i;
				break;
			}
		}
	}

	@Override
	public void reset()
	{
		Buttons.setText( "Back", "Accept", " ", " ", null, null );
		finished = false;
		TraceGame.drawLastArena = true;
	}

	@Override
	public void advance( float delta )
	{
		if( Buttons.esc() )
		{
			Sounds.no();
			next = nameEntry;
			finished = true;
		}
		else if( Buttons.enter() )
		{
			Sounds.yes();
			player.colour = colours[ selectedIndex ];
			next = new ControlChoice( player, lobby, this );
			finished = true;
		}

		if( Buttons.down() && selectedIndex < colours.length - 1 )
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

	@Override
	public void draw( StackedRenderer r )
	{
		for( int i = 0; i < colours.length; i++ )
		{
			float delta = i - displayedIndex;

			float base = -sep * delta;
			float alpha = selectedIndex == i ? 1 : 0.2f;

			int c = Colour.withAlphai( colours[ i ], ( int ) ( 255 * alpha ) );
			Arrays.fill( name.colours, c );

			name.translate( 0, base, 0 );
			name.render( r );
			name.translate( 0, -base, 0 );
		}

		if( chooseColour == null )
		{
			chooseColour = TraceGame.font.buildTextShape( "Choose colour", Colour.white );
			chooseColour.translate( 75, 400, 1 );
		}

		chooseColour.render( r );
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
