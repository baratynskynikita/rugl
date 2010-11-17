
package com.ryanm.trace;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.lobby.Lobby;
import com.ryanm.util.math.FunctionApproximation;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
public class TitlePhase implements Phase
{
	private float time = 0.1f;

	private float duration = 1f;

	private float angleRange = Trig.toRadians( 1080 );

	private FunctionApproximation alpha = new FunctionApproximation( 0, 0,
			0.15f * duration, 1, 0.85f * duration, 1, duration, 0 );

	private int c = Colour.packInt( 255, 255, 255, 128 );

	@Override
	public void advance( float delta )
	{
		time += delta;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		float f = time / duration;
		c = Colour.withAlphai( c, ( int ) ( 255 * alpha.evaluate( time ) ) );

		ColouredShape spiral =
				new ColouredShape( ShapeUtil.goldenSpiral( f * angleRange,
						Trig.toRadians( 10 ), 1 ), c, null );
		float s = ( 1 - f ) * 100;
		spiral.scale( s, s, 1 );

		spiral.translate( 400, 300, 0 );

		spiral.render( r );
	}

	@Override
	public boolean isFinished()
	{
		return time > duration && TraceGame.resourcesLoaded;
	}

	@Override
	public Phase next()
	{
		assert TraceGame.resourcesLoaded;
		TraceGame.font.init( true );
		Buttons.init();

		Lobby l = new Lobby();

		return l;
	}

	@Override
	public void reset()
	{
		time = 0.1f;
		Buttons.visible = false;
		TraceGame.drawLastArena = false;
	}
}
