
package com.ryanm.trace.game.entities;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeBuilder;
import com.rugl.geom.ShapeUtil;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;

/**
 * Reduces trace length
 * 
 * @author ryanm
 */
public class SizeMinus extends Powerup
{
	private static ColouredShape shape;
	static
	{
		Shape circle = ShapeUtil.innerCircle( 0, 0, 1, 0.1f, 0.3f, 0 );
		Shape minus = ShapeUtil.filledQuad( -0.8f, -0.15f, 0.8f, 0.15f, 0 );

		shape = new ColouredShape( ShapeBuilder.fuse( circle, minus ), SizeInf.color, null );
	}

	/**
	 * @param a
	 */
	public SizeMinus( Arena a )
	{
		super( a, shape );
	}

	@Override
	protected void hit( Trace t )
	{
		if( t.length == -1 )
		{
			t.length = t.path.length;
		}
		else
		{
			float mul = 1 - TraceGame.game.powerups.traceLength.amount;
			mul = Math.max( 0.1f, mul );
			t.length *= mul;
		}
	}
}
