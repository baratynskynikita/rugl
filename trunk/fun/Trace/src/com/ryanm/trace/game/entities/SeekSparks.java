
package com.ryanm.trace.game.entities;

import org.lwjgl.util.vector.Vector2f;

/**
 * Sparks that accelerate towards a trace head
 * 
 * @author ryanm
 */
public class SeekSparks extends Sparks
{
	/**
	 * Target vector
	 */
	public Vector2f target;

	/**
	 * Spark acceleration
	 */
	public float acceleration;

	/**
	 * @param sparks
	 * @param width
	 * @param length
	 * @param maxLife
	 * @param colour
	 */
	public SeekSparks( int sparks, float width, float length, float maxLife, int colour )
	{
		super( sparks, width, length, maxLife, colour );
	}

	/**
	 * Sets the target
	 * 
	 * @param t
	 *           target point
	 * @param acc
	 *           acceleration
	 * @return this
	 */
	public SeekSparks target( Vector2f t, float acc )
	{
		target = t;
		acceleration = acc;

		return this;
	}

	@Override
	public boolean advance( float delta )
	{
		// accelerate towards target
		Vector2f v = new Vector2f();
		for( int i = 0; i < velocities.length; i += 2 )
		{
			v.set( target.x - positions[ i ], target.y - positions[ i + 1 ] );

			if( v.x != 0 || v.y != 0 )
			{
				v.normalise();
				v.scale( acceleration );

				velocities[ i ] += v.x;
				velocities[ i + 1 ] += v.y;
			}
		}

		return super.advance( delta );
	}
}
