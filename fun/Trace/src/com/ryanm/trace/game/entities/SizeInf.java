
package com.ryanm.trace.game.entities;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeBuilder;
import com.rugl.geom.ShapeUtil;
import com.rugl.util.Colour;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.util.geom.Lissajous;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
public class SizeInf extends Powerup
{
	/**
	 * Color for size powerups
	 */
	public static final int color = Colour.red;

	/***/
	public static final ColouredShape shape;
	static
	{
		float[] plot = Lissajous.plot( 1, 2, Trig.PI / 2, 42 );
		for( int i = 0; i < plot.length; i += 2 )
		{
			plot[ i ] *= 0.7f;
			plot[ i + 1 ] *= 0.4f;
		}

		Shape inf = ShapeUtil.outline( 0.1f, 0.5f, plot );
		Shape circle = ShapeUtil.innerCircle( 0, 0, 1, 0.1f, .4f, 0 );

		shape = new ColouredShape( ShapeBuilder.fuse( inf, circle ), color, null );
	}

	/**
	 * @param a
	 */
	public SizeInf( Arena a )
	{
		super( a, shape );
	}

	@Override
	protected void hit( Trace t )
	{
		t.length = -1;
	}
}
