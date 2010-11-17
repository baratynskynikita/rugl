
package com.ryanm.trace.game;

import java.util.Arrays;
import java.util.Comparator;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.util.Segment;
import com.ryanm.util.geom.LineUtils;
import com.ryanm.util.geom.VectorUtils;

/**
 * Results of a collision check
 * 
 * @author ryanm
 */
public class Collision
{
	private static Vector2f a = new Vector2f(), b = new Vector2f(), c = new Vector2f(),
			d = new Vector2f();

	/**
	 * 
	 */
	public final TraceSegment seg;

	private Vector2f collision = null;

	/**
	 * 
	 */
	public final Segment testSeg;

	/**
	 * @param trace
	 * @param test
	 */
	public Collision( TraceSegment trace, Segment test )
	{
		seg = trace;
		testSeg = test;
	}

	/**
	 * @return the collision point
	 */
	public Vector2f collisionPoint()
	{
		if( collision == null )
		{
			a.set( seg.ax, seg.ay );
			b.set( seg.bx, seg.by );
			c.set( testSeg.ax, testSeg.ay );
			d.set( testSeg.bx, testSeg.by );

			collision = LineUtils.lineIntersection( a, b, c, d, collision );
		}

		return collision;
	}

	private static Vector2f comparisonPoint;

	private static Comparator<Collision> cc = new Comparator<Collision>() {
		@Override
		public int compare( Collision a, Collision b )
		{
			float da = VectorUtils.distanceSquared( a.collisionPoint(), comparisonPoint );
			float db = VectorUtils.distanceSquared( b.collisionPoint(), comparisonPoint );

			if( da < db )
			{
				return -1;
			}
			else if( da > db )
			{
				return 1;
			}
			return 0;
		}
	};

	/**
	 * Sorts the collision array into ascending order of distance to
	 * the point
	 * 
	 * @param p
	 * @param array
	 */
	public static void sort( Vector2f p, Collision[] array )
	{
		comparisonPoint = p;

		Arrays.sort( array, cc );
	}
}
