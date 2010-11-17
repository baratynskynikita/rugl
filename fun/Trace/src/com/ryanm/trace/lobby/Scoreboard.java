
package com.ryanm.trace.lobby;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextShape;
import com.rugl.util.Colour;
import com.ryanm.trace.Player;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.ai.MeatBag;

/**
 * @author ryanm
 */
public class Scoreboard
{
	private static Shape leftArrow, rightArrow, chip;
	static
	{
		leftArrow = ShapeUtil.triangle( 0, 0, 10, 5, 0, 10 );
		leftArrow.translate( 0, -5, 1 );
		rightArrow = ShapeUtil.triangle( 10, 0, 10, 10, 0, 5 );
		rightArrow.translate( 0, -5, 1 );

		chip = ShapeUtil.chipIcon( 5, 0.1f, 0.04f, 0.04f );
		chip.scale( 30, 30, 0 );
		chip.translate( 0, 0, 1 );
	}

	/**
	 * The player names last rendered
	 */
	public static TextShape[] lastRendered;

	private static final Comparator<Player> pc = new Comparator<Player>() {
		@Override
		public int compare( Player o1, Player o2 )
		{
			return o1.score - o2.score;
		};
	};

	/**
	 * Renders a player list
	 * 
	 * @param r
	 * @param players
	 * @param x
	 *           the bottom-left corner x
	 * @param y
	 *           the bottom-left corner y
	 * @param height
	 *           the maximum height of the board
	 * @param sort
	 *           true to sort in descending score order
	 */
	public static void render( StackedRenderer r, List<Player> players, float x, float y,
			float height, boolean sort )
	{
		Vector2f trans = new Vector2f( x, y + height );

		TextShape names = TraceGame.font.buildTextShape( "user", Colour.white );
		names.scale( 0.5f, 0.5f, 1 );
		names.translate( trans.x, trans.y, 0 );

		TextShape scores = TraceGame.font.buildTextShape( "perf", Colour.white );
		scores.scale( 0.5f, 0.5f, 1 );
		scores.translate( 250, 0, 1 );
		scores.translate( trans.x, trans.y, 0 );

		height -= TraceGame.font.size / 2;

		float eh = height / players.size();
		eh = Math.min( eh, TraceGame.font.size );
		float scale = eh / TraceGame.font.size;

		names.render( r );
		scores.render( r );

		Player[] pa = players.toArray( new Player[ players.size() ] );
		lastRendered = new TextShape[ pa.length ];

		if( sort )
		{
			Arrays.sort( pa, pc );
			Collections.reverse( Arrays.asList( pa ) );
		}

		for( int i = 0; i < pa.length; i++ )
		{
			trans.y += -eh;

			TextShape n = TraceGame.font.buildTextShape( pa[ i ].name, pa[ i ].colour );
			n.scale( scale, scale, 1 );
			n.translate( trans.x, trans.y, 0 );

			lastRendered[ i ] = n;

			n.render( r );

			if( pa[ i ].bot.getName() == MeatBag.NAME && pa[ i ].leftKey != -1
					&& pa[ i ].rightKey != -1 )
			{
				{
					ColouredShape la =
							new ColouredShape( leftArrow.clone(), pa[ i ].colour, null );
					la.scale( scale, scale, 1 );
					la.translate( 110 * scale, 0.6f * n.getBounds().getHeight(), 0 );
					la.translate( trans.x, trans.y, 0 );
					la.render( r );

					TexturedShape ln =
							TraceGame.font.buildTextShape(
									Keyboard.getKeyName( pa[ i ].leftKey ), pa[ i ].colour );
					ln.scale( 0.3f * scale, 0.3f * scale, 1 );
					ln.translate(
							la.getBounds().x.getMin() + la.getBounds().getWidth() * 1.5f,
							la.getBounds().y.getMin() - 0.3f * la.getBounds().getHeight(), 0 );
					ln.render( r );
				}
				{
					ColouredShape ra =
							new ColouredShape( rightArrow.clone(), pa[ i ].colour, null );
					ra.scale( scale, scale, 1 );
					ra.translate( 110 * scale, 0.25f * n.getBounds().getHeight(), 0 );
					ra.translate( trans.x, trans.y, 0 );
					ra.render( r );

					TexturedShape rn =
							TraceGame.font.buildTextShape(
									Keyboard.getKeyName( pa[ i ].rightKey ), pa[ i ].colour );
					rn.scale( 0.3f * scale, 0.3f * scale, 1 );
					rn.translate(
							ra.getBounds().x.getMin() + ra.getBounds().getWidth() * 1.5f,
							ra.getBounds().y.getMin() - 0.3f * ra.getBounds().getHeight(), 0 );
					rn.render( r );
				}
			}
			else
			{
				ColouredShape ch = new ColouredShape( chip.clone(), pa[ i ].colour, null );
				ch.scale( scale, scale, 1 );
				ch.translate( 120 * scale, 15 * scale, 0 );
				ch.translate( trans.x, trans.y, 0 );
				ch.render( r );

				TextShape bn =
						TraceGame.font.buildTextShape( pa[ i ].bot.getName(), pa[ i ].colour );
				bn.scale( 0.3f * scale, 0.3f * scale, 1 );
				bn.translate( 110 * scale, 0, 0 );
				bn.translate( trans.x, trans.y, 0 );
				bn.render( r );
			}

			TexturedShape s =
					TraceGame.font.buildTextShape( String.valueOf( pa[ i ].score ),
							pa[ i ].colour );
			s.scale( scale, scale, 1 );
			s.translate( 250, 0, 0 );
			s.translate( trans.x, trans.y, 0 );
			s.render( r );
		}
	}
}
