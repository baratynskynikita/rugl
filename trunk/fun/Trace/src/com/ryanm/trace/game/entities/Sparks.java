
package com.ryanm.trace.game.entities;

import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Entity;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Trig;

/**
 * A burst of sparks
 * 
 * @author ryanm
 */
public class Sparks implements Entity
{
	/**
	 * in x1,y1,x2,y2, order
	 */
	public final float[] positions;

	/**
	 * in x1,y1,x2,,y2 order
	 */
	public final float[] velocities;

	private final float maxLife;

	private float time = 0;

	private final float width;

	private final float length;

	private float drag = 0;

	private final ColouredShape shape;

	/**
	 * @param sparks
	 *           The number of sparks
	 * @param width
	 *           spark width
	 * @param length
	 *           spark length
	 * @param maxLife
	 *           max spark life
	 * @param colour
	 *           base colour
	 */
	public Sparks( int sparks, float width, float length, float maxLife, int colour )
	{
		positions = new float[ sparks * 2 ];
		velocities = new float[ sparks * 2 ];

		this.maxLife = maxLife;
		this.width = width;
		this.length = length;

		shape =
				new ColouredShape( new Shape( new float[ 3 * 3 * sparks ],
						new int[ 3 * sparks ] ), colour, null );
		int ti = 0;
		for( int i = 0; i < sparks; i++ )
		{
			shape.triangles[ ti++ ] = 3 * i;
			shape.triangles[ ti++ ] = 3 * i + 1;
			shape.triangles[ ti++ ] = 3 * i + 2;
		}
	}

	/**
	 * @param x
	 *           x center of deployment
	 * @param y
	 *           y center of deployment
	 * @param radius
	 *           radius of deployment
	 * @return this
	 */
	public Sparks position( final float x, final float y, final float radius )
	{
		for( int i = 0; i < positions.length; i += 2 )
		{
			float a = TraceGame.rng.nextFloat() * 2 * Trig.PI;
			float r = radius * TraceGame.rng.nextFloat();

			positions[ i ] = x + r * Trig.cos( a );
			positions[ i + 1 ] = y + r * Trig.sin( a );
		}
		return this;
	}

	/**
	 * @param x
	 *           x velocity of burst
	 * @param y
	 *           y velocity of burst
	 * @param burstSpeed
	 *           burst speed
	 * @param d
	 *           drag factor
	 * @return this
	 */
	public Sparks velocity( float x, float y, float burstSpeed, float d )
	{
		drag = d;
		for( int i = 0; i < positions.length; i += 2 )
		{
			float a = TraceGame.rng.nextFloat() * 2 * Trig.PI;
			float r = burstSpeed * TraceGame.rng.nextFloat();
			velocities[ i ] = x + r * Trig.cos( a );
			velocities[ i + 1 ] = y + r * Trig.sin( a );
		}
		return this;
	}

	@Override
	public boolean advance( float delta )
	{
		time += delta;

		for( int i = 0; i < positions.length; i += 2 )
		{
			positions[ i ] += velocities[ i ] * delta;
			positions[ i + 1 ] += velocities[ i + 1 ] * delta;

			velocities[ i ] *= 1 - delta * drag;
			velocities[ i + 1 ] *= 1 - delta * drag;
		}

		return time >= maxLife;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		int sparks = positions.length / 2;

		Vector2f v = new Vector2f();

		int vi = 0;
		for( int i = 0; i < sparks; i++ )
		{
			final float x = positions[ 2 * i ];
			final float y = positions[ 2 * i + 1 ];
			final float vx = velocities[ 2 * i ];
			final float vy = velocities[ 2 * i + 1 ];

			v.set( vx, vy );
			v.normalise();

			final float tx = x - v.x * length;
			final float ty = y - v.y * length;

			v.scale( width / 2 );
			VectorUtils.rotate90( v );
			shape.vertices[ vi++ ] = x - v.x;
			shape.vertices[ vi++ ] = y - v.y;
			shape.vertices[ vi++ ] = 0;

			shape.vertices[ vi++ ] = x + v.x;
			shape.vertices[ vi++ ] = y + v.y;
			shape.vertices[ vi++ ] = 0;

			shape.vertices[ vi++ ] = tx;
			shape.vertices[ vi++ ] = ty;
			shape.vertices[ vi++ ] = 0;
		}

		Colour.withAlphai( shape.colours, ( int ) ( 255 * ( 1.0f - time / maxLife ) ) );

		shape.render( r );
	}
}
