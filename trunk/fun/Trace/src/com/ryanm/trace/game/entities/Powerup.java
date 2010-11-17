
package com.ryanm.trace.game.entities;

import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.ColouredShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Entity;
import com.ryanm.trace.game.Trace;
import com.ryanm.util.math.FunctionApproximation;
import com.ryanm.util.math.Trig;

/**
 * Powerups roam the arena till they are picked up
 * 
 * @author ryanm
 */
public abstract class Powerup implements Entity
{
	private static final float resIn = 0.5f;

	private static final float resOut = 3f;

	/***/
	private FunctionApproximation rFunc = new FunctionApproximation( 0, 0, resIn,
			TraceGame.game.powerups.radius, TraceGame.game.powerups.life - resOut,
			TraceGame.game.powerups.radius, TraceGame.game.powerups.life, 0 );

	/***/
	protected final Arena arena;

	/***/
	public final Vector2f position;

	/***/
	protected float radius;

	/***/
	protected final Vector2f motion;

	/***/
	protected float time = 0;

	/**
	 * Set to true when you're done
	 */
	protected boolean done = false;

	/***/
	protected float rotation = 0;

	/***/
	protected float angle = 0;

	/***/
	private final ColouredShape shape;

	/**
	 * @param arena
	 * @param shape
	 */
	protected Powerup( Arena arena, ColouredShape shape )
	{
		this.arena = arena;
		this.shape = shape;
		radius = 0;

		position =
				new Vector2f( radius + TraceGame.rng.nextFloat() * ( 800 - 2 * radius ),
						radius + TraceGame.rng.nextFloat() * ( 600 - 2 * radius ) );
		float a = 2 * Trig.PI * TraceGame.rng.nextFloat();
		motion = new Vector2f( Trig.cos( a ), Trig.sin( a ) );
		motion.scale( TraceGame.game.powerups.speed );
		rotation = ( 2 * TraceGame.rng.nextFloat() - 1 ) * Trig.toRadians( 40 );
	}

	/**
	 * Does the standard wandering motion, calls {@link #hit(Trace)},
	 * always returns false
	 */
	@Override
	public boolean advance( float delta )
	{
		time += delta;

		if( time > TraceGame.game.powerups.life )
		{
			done = true;
		}

		if( !done )
		{
			radius = getRadius();

			motion( delta );

			collision();
		}

		return done;
	}

	/**
	 * @return the radius of the powerup
	 */
	protected float getRadius()
	{
		return rFunc.evaluate( time );
	}

	/***/
	protected void collision()
	{
		Trace c = traceCollision();

		if( c != null )
		{
			done = true;
			hit( c );

			if( done )
			{
				Sounds.powerup();
				SeekSparks ss = new SeekSparks( 40, 3, 5, 2, shape.colours[ 0 ] );
				ss.position( c.position.x, c.position.y, radius );
				ss.velocity( motion.x, motion.y, 200, 1 );
				ss.target( c.position, 5 );

				arena.addEntity( ss );
			}
		}
	}

	/**
	 * @param delta
	 */
	protected void motion( float delta )
	{
		angle += rotation * delta;

		position.x += motion.x * delta;
		position.y += motion.y * delta;

		if( position.x < radius )
		{
			position.x = radius;
			motion.x = -motion.x;
		}
		else if( position.x > 800 - radius )
		{
			position.x = 800 - radius;
			motion.x = -motion.x;
		}
		if( position.y < radius )
		{
			position.y = radius;
			motion.y = -motion.y;
		}
		else if( position.y > 600 - radius )
		{
			position.y = 600 - radius;
			motion.y = -motion.y;
		}
	}

	/**
	 * Checks for collision with traces
	 * 
	 * @return A colliding trace, or null if no collision
	 */
	protected Trace traceCollision()
	{
		for( int i = 0; i < arena.traces.length; i++ )
		{
			float dx = position.x - arena.traces[ i ].position.x;
			float dy = position.y - arena.traces[ i ].position.y;

			if( !arena.traces[ i ].player.dead && dx * dx + dy * dy < radius * radius )
			{
				return arena.traces[ i ];
			}
		}

		return null;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		r.pushMatrix();
		r.translate( position.x, position.y, 0 );
		r.rotate( angle, 0, 0, 1 );
		r.scale( radius, radius, 1 );

		Colour.withAlphai( shape.colours,
				( int ) ( 255 * radius / TraceGame.game.powerups.radius ) );
		shape.render( r );

		r.popMatrix();
	}

	/**
	 * Called when the powerup has hit a {@link Trace}
	 * 
	 * @param t
	 */
	protected abstract void hit( Trace t );

}
