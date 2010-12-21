
package com.ryanm.droid.rugl.util.geom;

import com.ryanm.droid.rugl.util.math.Range;

/**
 * An axis-aligned rectangle
 * 
 * @author ryanm
 */
public class BoundingRectangle
{
	/**
	 * Extent on the x axis
	 */
	public final Range x = new Range( 0, 0 );

	/**
	 * Extent on the y axis
	 */
	public final Range y = new Range( 0, 0 );

	/***/
	public BoundingRectangle()
	{
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public BoundingRectangle( float x, float y, float width, float height )
	{
		this.x.set( x, x + width );
		this.y.set( y, y + height );
	}

	/**
	 * Deep copy constructor
	 * 
	 * @param c
	 */
	public BoundingRectangle( BoundingRectangle c )
	{
		x.set( c.x );
		y.set( c.y );
	}

	/**
	 * @param minx
	 * @param maxx
	 * @param miny
	 * @param maxy
	 */
	public void set( float minx, float maxx, float miny, float maxy )
	{
		x.set( minx, maxx );
		y.set( miny, maxy );
	}

	/**
	 * @param r
	 */
	public void set( BoundingRectangle r )
	{
		x.set( r.x );
		y.set( r.y );
	}

	/**
	 * Alters this {@link BoundingRectangle} as necessary to contain
	 * the specified point
	 * 
	 * @param px
	 * @param py
	 */
	public void encompass( float px, float py )
	{
		x.encompass( px );
		y.encompass( py );
	}

	/**
	 * Alters this {@link BoundingRectangle} to entirely encompass
	 * another
	 * 
	 * @param c
	 */
	public void encompass( BoundingRectangle c )
	{
		x.encompass( c.x );
		y.encompass( c.y );
	}

	/**
	 * Determines if this {@link BoundingRectangle} contains the
	 * supplied point
	 * 
	 * @param px
	 * @param py
	 * @return <code>true</code> if the point lies within this
	 *         {@link BoundingRectangle}'s volume, <code>false</code>
	 *         otherwise
	 */
	public boolean contains( float px, float py )
	{
		return x.contains( px ) && y.contains( py );
	}

	/**
	 * @param b
	 * @return <code>true</code> if this and b intersect
	 */
	public boolean intersects( BoundingRectangle b )
	{
		return x.intersects( b.x ) && y.intersects( b.y );
	}

	/**
	 * @param b
	 * @param dest
	 * @return true if the intersection exists
	 */
	public boolean intersection( BoundingRectangle b, BoundingRectangle dest )
	{
		if( intersects( b ) )
		{
			x.intersection( b.x, dest.x );
			y.intersection( b.y, dest.y );
			return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "( " );
		buff.append( x.getMin() );
		buff.append( ", " );
		buff.append( y.getMin() );
		buff.append( " ) [ " );
		buff.append( x.getSpan() );
		buff.append( " x " );
		buff.append( y.getSpan() );
		buff.append( " ]" );

		return buff.toString();
	}

	/**
	 * Shifts this rectangle
	 * 
	 * @param dx
	 * @param dy
	 */
	public void translate( float dx, float dy )
	{
		x.translate( dx );
		y.translate( dy );
	}

	/**
	 * Scales this rectangle around the origin
	 * 
	 * @param sx
	 * @param sy
	 */
	public void scale( float sx, float sy )
	{
		x.scale( sx );
		x.scale( sy );
	}
}
