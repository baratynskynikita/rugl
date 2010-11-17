
package com.ryanm.trace.game;

import java.text.DecimalFormat;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.renderer.proc.Alpha;
import com.rugl.text.TextShape;
import com.rugl.util.Colour;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.lobby.Lobby;
import com.ryanm.trace.lobby.Scoreboard;
import com.ryanm.util.Util;
import com.ryanm.util.geom.MatrixUtils;
import com.ryanm.util.math.FunctionApproximation;
import com.ryanm.util.math.Range;
import com.ryanm.util.math.Trig;

/**
 * Sets up arena positions etc
 * 
 * @author ryanm
 */
public class ArenaStart implements Phase
{
	private static final DecimalFormat format = new DecimalFormat( "0.00" );

	private static final String[] messages = new String[] { "compile", "link", "load",
			"init" };

	private static final float duration = 3;

	private static final float nameMotionTime = duration / 3;

	private static final FunctionApproximation statusAlpha = new FunctionApproximation( 0,
			1, duration, 0 );

	private static final FunctionApproximation nameMotion = new FunctionApproximation( 0,
			0, nameMotionTime, 1 );

	private static final FunctionApproximation arrowAlpha = new FunctionApproximation( 0,
			0, nameMotionTime, 1, 2 * duration / 3, 1, duration, 0 );

	private static final FunctionApproximation traceAlpha = new FunctionApproximation(
			nameMotionTime, 0, duration, 1 );

	private final Lobby lobby;

	private Phase next;

	private float time = 0;

	private final TextShape[] names;

	private final Vector3f[] nameStarts;

	private final Vector2f[] nameTargets;

	private final float[] angles;

	private Alpha dimmer = new Alpha( 1 );

	private static final float t = 4, ht = 10, sl = 20, hl = 45;

	private Shape arrow = ShapeUtil.line( 2, 0, t, sl, t, sl, ht, hl, 0, sl, -ht, sl, -t,
			0, -t );

	/**
	 * @param lobby
	 * @param p
	 */
	public ArenaStart( Lobby lobby, List<Player> p )
	{
		this.lobby = lobby;
		Player[] players = p.toArray( new Player[ p.size() ] );
		names = Scoreboard.lastRendered;
		nameStarts = new Vector3f[ players.length ];
		nameTargets = new Vector2f[ players.length ];
		angles = new float[ players.length ];

		Trace[] traces = new Trace[ players.length ];

		float sa = TraceGame.rng.nextFloat() * 2 * Trig.PI;

		int[] positions = new int[ players.length ];
		for( int i = 0; i < positions.length; i++ )
		{
			positions[ i ] = i;
		}
		Util.shuffle( positions, TraceGame.rng );

		for( int i = 0; i < players.length; i++ )
		{
			assert names[ i ].string.equals( players[ i ].name );
			players[ i ].score = 0;
			players[ i ].dead = false;

			float angle = sa + positions[ i ] * 2 * Trig.PI / players.length;

			float cos = Trig.cos( angle );
			float x = 400 + 200 * cos;
			float sin = Trig.sin( angle );
			float y = 300 + 200 * sin;

			angle += 0.751f * Trig.PI;

			while( angle < -Trig.PI )
			{
				angle += 2 * Trig.PI;
			}
			while( angle > Trig.PI )
			{
				angle -= 2 * Trig.PI;
			}

			angles[ i ] = angle;

			traces[ i ] = new Trace( players[ i ], x, y, angle );
			nameStarts[ i ] = names[ i ].getBounds().getCenter();
			nameTargets[ i ] = new Vector2f( x, y );
		}

		TraceGame.lastArena = new Arena( lobby, traces );
		next = TraceGame.lastArena;
	}

	@Override
	public void reset()
	{
		time = 0;
		Buttons.visible = false;
		dimmer.mult = 1;
		TraceGame.drawLastArena = false;
	}

	@Override
	public void advance( float delta )
	{
		time += delta;

		if( Buttons.esc() )
		{
			next = lobby;
			time = duration;
		}
	}

	@Override
	public void draw( StackedRenderer r )
	{
		r.push( dimmer );
		dimmer.mult = traceAlpha.evaluate( time );

		TraceGame.lastArena.draw( r );

		if( time <= duration )
		{
			dimmer.mult = statusAlpha.evaluate( time );

			TexturedShape si =
					TraceGame.font.buildTextShape( messages[ ( int ) Range.limit(
							( int ) ( messages.length * time / duration ), 0,
							messages.length - 1 ) ], Colour.white );
			si.translate( 300, 300, 1 );
			TexturedShape cd =
					TraceGame.font.buildTextShape( format.format( duration - time ),
							Colour.white );
			cd.translate( 300, 250, 1 );
			si.render( r );
			cd.render( r );

			for( int i = 0; i < names.length; i++ )
			{
				dimmer.mult = 1;
				TexturedShape n = names[ i ].clone();
				n.transform( MatrixUtils.scaleAround( ( duration - time ) / duration,
						nameStarts[ i ].x, nameStarts[ i ].y ) );

				float m = nameMotion.evaluate( time );
				n.translate( m * ( nameTargets[ i ].x - nameStarts[ i ].x ), m
						* ( nameTargets[ i ].y - nameStarts[ i ].y ), 0 );
				n.render( r );

				dimmer.mult = arrowAlpha.evaluate( time );
				ColouredShape a =
						new ColouredShape( arrow.clone(), names[ i ].colours[ 0 ], null );
				Matrix4f mat = new Matrix4f();
				mat.translate( new Vector2f( nameStarts[ i ].x + m
						* ( nameTargets[ i ].x - nameStarts[ i ].x ), nameStarts[ i ].y + m
						* ( nameTargets[ i ].y - nameStarts[ i ].y ) ) );
				mat.rotate( m * angles[ i ], new Vector3f( 0, 0, 1 ) );
				a.transform( mat );
				a.render( r );
			}
		}

		r.popProcessor();
	}

	@Override
	public boolean isFinished()
	{
		return time >= duration;
	}

	@Override
	public Phase next()
	{
		return next;
	}

}
