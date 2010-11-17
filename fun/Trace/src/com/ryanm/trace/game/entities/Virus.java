
package com.ryanm.trace.game.entities;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.util.geom.GeomUtils;
import com.ryanm.util.io.DataSource;
import com.ryanm.util.io.DataStream;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
public class Virus extends Powerup
{
	/***/
	public static final ColouredShape shape;
	static
	{
		DataSource s =
				new DataStream( Thread.currentThread().getContextClassLoader()
						.getResourceAsStream( "biohazard.ruglshp" ) );
		ColouredShape bio = new ColouredShape( new Shape( s ), Colour.green, null );

		float[] mec = GeomUtils.minimumEnclosingtCircle( ShapeUtil.to2D( bio.vertices ) );
		bio.translate( -mec[ 0 ], -mec[ 1 ], 0 );

		float scale = 1 / mec[ 2 ];
		bio.scale( scale, scale, 1 );

		shape = bio;
	}

	private static final ColouredShape cloakShape = new ColouredShape(
			ShapeUtil.outerQuad( -1, -1, 1, 1, 0.1f, 0 ),
			Colour.packInt( 255, 255, 255, 128 ), null );

	private float cloakAngle = 0;

	private float cloakRotate = Trig.toRadians( 90 );

	/**
	 * @param a
	 */
	public Virus( Arena a )
	{
		super( a, shape );
	}

	@Override
	public boolean advance( float delta )
	{
		super.advance( delta );
		cloakAngle += cloakRotate * delta;

		return done;
	}

	@Override
	protected void collision()
	{
		Trace c = traceCollision();

		if( c != null )
		{
			done = true;
			c.cloakTime +=
					TraceGame.game.powerups.virusCloak.toValue( TraceGame.rng.nextFloat() );
			Sounds.cloak();

			// unleash
			arena.addEntity( new ActiveVirus( arena, this ) );
		}
	}

	@Override
	protected void hit( Trace t )
	{
	}

	@Override
	public void draw( StackedRenderer r )
	{
		super.draw( r );

		drawCloak( position.x, position.y, cloakAngle, radius, 1, r );
	}

	/**
	 * @param x
	 * @param y
	 * @param angle
	 * @param radius
	 * @param alpha
	 * @param r
	 */
	public static void drawCloak( float x, float y, float angle, float radius,
			float alpha, StackedRenderer r )
	{
		r.pushMatrix();

		Colour.withAlphai( cloakShape.colours, ( int ) ( 128 * alpha ) );

		r.translate( x, y, 0 );
		r.rotate( angle, 0, 0, 1 );
		r.scale( radius, radius, 1 );
		cloakShape.render( r );

		r.rotate( -2 * angle + Trig.PI / 2, 0, 0, 1 );
		cloakShape.render( r );

		r.popMatrix();
	}
}
