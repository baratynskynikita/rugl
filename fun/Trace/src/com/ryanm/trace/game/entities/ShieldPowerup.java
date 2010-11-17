
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
 * Adds a shield
 * 
 * @author ryanm
 */
public class ShieldPowerup extends Powerup
{
	private static ColouredShape shape;
	static
	{
		Shape circle = ShapeUtil.innerCircle( 0, 0, 1, 0.1f, 0.3f, 0 );
		Shape shield = Trace.shieldShape.clone();

		shield.translate( -shield.getBounds().getCenter().x, -shield.getBounds()
				.getCenter().y, 0 );
		float max =
				Math.max( shield.getBounds().getWidth(), shield.getBounds().getHeight() );
		shield.scale( 1.0f / max, 1.0f / max, 1 );
		shield.scale( 1.5f, 1.5f, 1 );

		shape = new ColouredShape( ShapeBuilder.fuse( circle, shield ), Colour.white, null );
	}

	/**
	 * @param arena
	 */
	public ShieldPowerup( Arena arena )
	{
		super( arena, shape );
	}

	@Override
	protected void hit( Trace t )
	{
		t.shields += 1;
		if( t.shields > TraceGame.game.trace.maxShields )
		{
			t.shields = TraceGame.game.trace.maxShields;
		}
	}

}
