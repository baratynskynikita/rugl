
package com.ryanm.util;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.util.geom.LineUtils;

/***/
public class Segment
{
	private static final Vector2f p = new Vector2f(), q = new Vector2f(), r = new Vector2f(),
			s = new Vector2f();

	/***/
	public float ax;

	/***/
	public float ay;

	/***/
	public float bx;

	/***/
	public float by;

	/**
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 */
	public Segment( float ax, float ay, float bx, float by )
	{
		this.ax = ax;
		this.ay = ay;
		this.bx = bx;
		this.by = by;
	}

	/**
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 */
	public void set( float ax, float ay, float bx, float by )
	{
		this.ax = ax;
		this.ay = ay;
		this.bx = bx;
		this.by = by;
	}

	/**
	 * Tests to see if the endpoints are identical. Override this if
	 * you're adding more complex segment types
	 * 
	 * @param seg
	 * @return <code>true</code> if the two segments are functionally
	 *         identical
	 */
	public boolean isEqual( Segment seg )
	{
		return ax == seg.ax && ay == seg.ay && bx == seg.bx && by == seg.by;
	}

	/**
	 * Tests for segment intersection
	 * 
	 * @param seg
	 * @return <code>true</code> if the segments intersect, false
	 *         otherwise
	 */
	public boolean intersects( Segment seg )
	{
		p.set( ax, ay );
		q.set( bx, by );
		r.set( seg.ax, seg.ay );
		s.set( seg.bx, seg.by );

		return LineUtils.segmentsIntersect( p, q, r, s );
	}

	@Override
	public String toString()
	{
		return "( " + ax + ", " + ay + " )( " + bx + ", " + by + " )";
	}

	void limit( float l )
	{
		float f = Math.min( ax, bx );
		if( f < 0 )
		{
			ax -= f;
			bx -= f;
		}
		assert ax >= 0 : ax;
		assert bx >= 0 : bx;

		f = Math.max( ax, bx ) - l;
		if( f > 0 )
		{
			ax -= f;
			bx -= f;
		}
		assert ax <= l : ax;
		assert bx <= l : bx;

		f = Math.min( ay, by );
		if( f < 0 )
		{
			ay -= f;
			by -= f;
		}
		assert ay >= 0 : ay;
		assert by >= 0 : by;

		f = Math.max( ay, by ) - l;
		if( f > 0 )
		{
			ay -= f;
			by -= f;
		}

		assert ay <= l : ay;
		assert by <= l : by;
	}
}