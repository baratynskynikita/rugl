
package com.ryanm.trace.game.entities;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeBuilder;
import com.rugl.geom.ShapeUtil;
import com.rugl.util.Colour;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;

/**
 * Increases trace speed
 * 
 * @author ryanm
 */
public class SpeedPlus extends Powerup
{
	/***/
	public static final int colour = Colour.packInt( 0, 0, 255, 255 );

	/***/
	public static ColouredShape shape;
	static
	{
		Shape circle = ShapeUtil.innerCircle( 0, 0, 1, 0.1f, 0.3f, 0 );
		Shape plus = ShapeUtil.cross( 0.3f / 1.6f );
		plus.translate( -0.5f, -0.5f, 0 );
		plus.scale( 1.6f, 1.6f, 1 );

		shape = new ColouredShape( ShapeBuilder.fuse( circle, plus ), colour, null );
	}

	/**
	 * @param a
	 */
	public SpeedPlus( Arena a )
	{
		super( a, shape );
	}

	@Override
	protected void hit( Trace t )
	{
		float mul = 1 + TraceGame.game.powerups.traceSpeed.amount;
		mul = Math.max( 0.1f, mul );
		t.speed *= mul;
	}

}
