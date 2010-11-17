
package com.ryanm.trace.game.entities;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeBuilder;
import com.rugl.geom.ShapeUtil;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;

/**
 * Increases trace length
 * 
 * @author ryanm
 */
public class SizePlus extends Powerup
{
	private static ColouredShape shape;
	static
	{
		Shape circle = ShapeUtil.innerCircle( 0, 0, 1, 0.1f, 0.3f, 0 );
		Shape plus = ShapeUtil.cross( 0.3f / 1.6f );
		plus.translate( -0.5f, -0.5f, 0 );
		plus.scale( 1.6f, 1.6f, 1 );

		shape = new ColouredShape( ShapeBuilder.fuse( circle, plus ), SizeInf.color, null );
	}

	/**
	 * @param a
	 */
	public SizePlus( Arena a )
	{
		super( a, shape );
	}

	@Override
	protected void hit( Trace t )
	{
		if( t.length != -1 )
		{
			float mul = 1 + TraceGame.game.powerups.traceLength.amount;
			mul = Math.max( 0.1f, mul );
			t.length *= mul;
		}
	}
}
