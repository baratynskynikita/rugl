
package com.ryanm.trace.game;

import java.util.Arrays;

import com.ryanm.util.geom.Pointf;

/**
 * @author ryanm
 */
public class SegmentPath
{
	/**
	 * x-coordinates of segment start points
	 */
	public float[] xCoords = new float[ 8 ];

	/**
	 * y-coordinates of segment start points
	 */
	public float[] yCoords = new float[ 8 ];

	/**
	 * Type of segment
	 */
	public byte[] segType = new byte[ 8 ];

	/**
	 * Number of segments
	 */
	private int segCount = 0;

	/**
	 * The length of the path. If you monkey with the coordinates
	 * yourself, you need to make this correct manually
	 */
	public float length = 0;

	/**
	 * @return the number of segments
	 */
	public int segCount()
	{
		return segCount;
	}

	/**
	 * Adds a segment start point to the path
	 * 
	 * @param x
	 * @param y
	 * @param type
	 */
	public void startSeg( float x, float y, byte type )
	{
		if( segCount == xCoords.length )
		{
			xCoords = Arrays.copyOf( xCoords, 2 * xCoords.length );
			yCoords = Arrays.copyOf( yCoords, 2 * yCoords.length );
			segType = Arrays.copyOf( segType, 2 * segType.length );
		}

		xCoords[ segCount ] = x;
		yCoords[ segCount ] = y;
		segType[ segCount ] = type;

		if( segCount > 0 )
		{
			length += length( segCount - 1 );
		}

		segCount++;
	}

	/**
	 * Removes some segments from the start of the path
	 * 
	 * @param toRemove
	 *           The length of path to remove
	 * @return An [x,y,x,y] array of the segments removed
	 */
	public float[] removeFromStart( float toRemove )
	{
		float[] removed;

		if( toRemove >= length )
		{
			segCount = 1;
			length = 0;

			removed = new float[ segCount * 2 + 2 ];
			int ri = 0;
			for( int i = 0; i <= segCount; i++ )
			{
				removed[ ri++ ] = xCoords[ i ];
				removed[ ri++ ] = yCoords[ i ];
			}
		}
		else if( toRemove > 0 )
		{
			length -= toRemove;

			int snip = -1;
			float segLength = 0;

			while( toRemove >= segLength )
			{
				snip++;
				toRemove -= segLength;
				segLength = length( snip );
			}

			removed = new float[ 2 * snip + 4 ];
			int ri = 0;
			for( int i = 0; i <= snip; i++ )
			{
				removed[ ri++ ] = xCoords[ i ];
				removed[ ri++ ] = yCoords[ i ];
			}

			System.arraycopy( xCoords, snip, xCoords, 0, xCoords.length - snip );
			System.arraycopy( yCoords, snip, yCoords, 0, yCoords.length - snip );
			System.arraycopy( segType, snip, segType, 0, segType.length - snip );
			segCount -= snip;

			// adjust first point
			float desired = segLength - toRemove;
			float p = desired / segLength;

			float dx = xCoords[ 0 ] - xCoords[ 1 ];
			float dy = yCoords[ 0 ] - yCoords[ 1 ];

			xCoords[ 0 ] = xCoords[ 1 ] + dx * p;
			yCoords[ 0 ] = yCoords[ 1 ] + dy * p;

			removed[ removed.length - 2 ] = xCoords[ 0 ];
			removed[ removed.length - 1 ] = yCoords[ 0 ];
		}
		else
		{
			removed = new float[ 0 ];
		}

		return removed;
	}

	private float length( int seg )
	{
		return Pointf
				.distance( xCoords[ seg ], yCoords[ seg ], xCoords[ seg + 1 ], yCoords[ seg + 1 ] );
	}
}
