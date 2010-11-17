
package com.ryanm.util.march;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Combines a sequence of directions into a path that is rooted at
 * some point in the plane. No restrictions are placed on paths; they
 * may be zero length, open/closed, self-intersecting. Path objects
 * are immutable.
 * 
 * @author Tom Gibara
 */

public class Path
{

	// statics

	private static final double ADJ_LEN = Math.sqrt( 2.0 ) / 2.0 - 1;

	// fields

	private final Direction[] directions;

	private final List<Direction> directionList;

	private final double length;

	private final int originX;

	private final int originY;

	private final int terminalX;

	private final int terminalY;

	// constructors

	private Path( Path that, int deltaX, int deltaY )
	{
		directions = that.directions;
		directionList = that.directionList;
		length = that.length;
		originX = that.originX + deltaX;
		originY = that.originY + deltaY;
		terminalX = that.terminalX + deltaX;
		terminalY = that.terminalY + deltaY;
	}

	/**
	 * Constructs a path which starts at the specified point in the
	 * plane. The array may be zero length.
	 * 
	 * @param startX
	 *           the x coordinate of the path's origin in the plane
	 * @param startY
	 *           the y coordinate of the path's origin in the plane
	 * @param directions
	 *           an array of directions, never null
	 */

	public Path( int startX, int startY, Direction[] directions )
	{
		originX = startX;
		originY = startY;
		this.directions = directions.clone();
		directionList = Collections.unmodifiableList( Arrays.asList( directions ) );

		int endX = startX;
		int endY = startY;
		int diagonals = 0;
		for( Direction direction : directions )
		{
			endX += direction.screenX;
			endY += direction.screenY;
			if( direction.screenX != 0 && direction.screenY != 0 )
			{
				diagonals++;
			}
		}

		terminalX = endX;
		terminalY = endY;

		length = directions.length + diagonals * ADJ_LEN;
	}

	/**
	 * Convenience constructor that converts the supplied direction
	 * list into an array which is then passed to another constructor.
	 * 
	 * @param startX
	 *           the x coordinate of the path's origin in the plane
	 * @param startY
	 *           the y coordinate of the path's origin in the plane
	 * @param directions
	 *           a list of the directions in the path
	 */

	public Path( int startX, int startY, List<Direction> directions )
	{
		this( startX, startY, directions.toArray( new Direction[ directions.size() ] ) );
	}

	/**
	 * Constructs the {@link Polygon} of this path, in screen
	 * coordinates
	 * 
	 * @return The {@link Polygon} bounded by this path
	 */
	public Polygon buildShape()
	{
		List<Point> verts = new LinkedList<Point>();
		Point cursor = new Point( originX, -originY );
		Direction currentDir = directions[ 0 ];

		verts.add( new Point( cursor ) );

		for( int i = 0; i < directions.length; i++ )
		{
			if( directions[ i ] != currentDir )
			{
				verts.add( new Point( cursor ) );
				currentDir = directions[ i ];
			}

			cursor.x += directions[ i ].screenX;
			cursor.y += directions[ i ].screenY;
		}

		int[] xp = new int[ verts.size() ];
		int[] yp = new int[ verts.size() ];
		int i = 0;

		for( Point p : verts )
		{
			xp[ i ] = p.x;
			yp[ i ] = p.y;
			i++;
		}

		return new Polygon( xp, yp, xp.length );
	}

	// accessors

	/**
	 * @return an immutable list of the directions that compose this
	 *         path, never null
	 */

	public List<Direction> getDirections()
	{
		return directionList;
	}

	/**
	 * @return the x coordinate in the plane at which the path begins
	 */

	public int getOriginX()
	{
		return originX;
	}

	/**
	 * @return the y coordinate in the plane at which the path begins
	 */

	public int getOriginY()
	{
		return originY;
	}

	/**
	 * @return the x coordinate in the plane at which the path ends
	 */

	public int getTerminalX()
	{
		return terminalX;
	}

	/**
	 * @return the y coordinate in the plane at which the path ends
	 */

	public int getTerminalY()
	{
		return terminalY;
	}

	/**
	 * @return the length of the path using the standard euclidean
	 *         metric
	 */

	public double getLength()
	{
		return length;
	}

	/**
	 * @return true if and only if the path's point of origin is the
	 *         same as that of its point of termination
	 */

	public boolean isClosed()
	{
		return originX == terminalX && originY == terminalY;
	}

	// methods

	/**
	 * Creates a new path by translating this path in the plane.
	 * 
	 * @param deltaX
	 *           the change in the path's x coordinate
	 * @param deltaY
	 *           the change in the path's y coordinate
	 * @return a new path whose origin has been translated
	 */

	public Path translate( int deltaX, int deltaY )
	{
		return new Path( this, deltaX, deltaY );
	}

	// TODO add rotate, mirror and reverse methods

	// object methods

	/**
	 * Two paths are equal if they have the same origin and the same
	 * directions.
	 */

	@Override
	public boolean equals( Object obj )
	{
		if( obj == this )
		{
			return true;
		}
		if( !( obj instanceof Path ) )
		{
			return false;
		}
		Path that = ( Path ) obj;

		if( originX != that.originX )
		{
			return false;
		}
		if( originY != that.originY )
		{
			return false;
		}
		if( terminalX != that.terminalX )
		{
			return false; // optimization
		}
		if( terminalY != that.terminalY )
		{
			return false; // optimization
		}
		if( !Arrays.equals( directions, that.directions ) )
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return originX ^ 7 * originY ^ directions.hashCode();
	}

	@Override
	public String toString()
	{
		return "X: " + originX + ", Y: " + originY + " " + Arrays.toString( directions );
	}

}
