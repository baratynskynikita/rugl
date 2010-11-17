
package com.ryanm.trace.lobby;

import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;

/**
 * @author ryanm
 */
public class FaqPhase implements Phase
{
	private boolean done = false;

	private final Lobby lobby;

	private String[] questions = new String[] { "what's going on here?",
			"So what do I do?", "Steer?", "Don't die?", "Score points?", "Powerups?" };

	private String[] answers = new String[] {
			"Remember the lightcycles bit from Tron? Basically that",
			"Steer, don't die, score points, get powerups",
			"Steer your trace left and right",
			"Don't hit the edges, or your own or other's traces",
			"Hit the gaps in traces to get points",
			"Adjust your trace length, speed, gaps. Watch out for virii" };

	private int qi = 0;

	private int pupqIndex = 5;

	private int redStart = 27, redEnd = 39, blueStart = 41, blueEnd = 46, greenStart = 48,
			greenEnd = 52;

	/**
	 * @param lobby
	 */
	public FaqPhase( Lobby lobby )
	{
		this.lobby = lobby;
	}

	@Override
	public void reset()
	{
		done = false;
		Buttons.visible = true;
		TraceGame.drawLastArena = true;
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
	public void advance( float delta )
	{
		Buttons.setText( "back", null, qi == 0 ? null : "last",
				qi == questions.length - 1 ? null : "next", null, null );

		if( Buttons.down() && qi < questions.length - 1 )
		{
			Sounds.yes();
			qi++;
		}
		if( Buttons.up() && qi > 0 )
		{
			Sounds.yes();
			qi--;
		}

		if( Buttons.esc() )
		{
			Sounds.no();
			done = true;
		}
	}

	@Override
	public void draw( StackedRenderer r )
	{
		String text = "q) " + questions[ qi ] + "\na) " + answers[ qi ];
		TextLayout tl =
				new TextLayout( text, TraceGame.font, Alignment.LEFT, 600, Colour.white );

		if( qi == pupqIndex )
		{
			// change the colours
			int[] ca = tl.textShape.colours;
			for( int i = 4 * redStart; i < 4 * redEnd; i++ )
			{
				ca[ i ] = Colour.red;
			}
			for( int i = 4 * blueStart; i < 4 * blueEnd; i++ )
			{
				ca[ i ] = Colour.blue;
			}
			for( int i = 4 * greenStart; i < 4 * greenEnd; i++ )
			{
				ca[ i ] = Colour.green;
			}
		}

		tl.textShape.translate( 10, 400, 0 );

		tl.textShape.render( r );
	}
}
