
package com.ryanm.soundgen.imp;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import com.ryanm.soundgen.Variable;

/**
 * Traverses a landscape of (time, value) pairs. Querying for times
 * less than the minimum-time point will return the minimum-time
 * value, likewise for maximum
 * 
 * @author ryanm
 */
public class Terrain implements Variable
{
	/**
	 * 
	 */
	public ArrayList<float[]> points = new ArrayList<float[]>();

	/**
	 * Set it true if you've been monkeying with the points
	 */
	public boolean orderDirty = true;

	private static float[] temp = new float[ 2 ];

	private static Comparator<float[]> c = new Comparator<float[]>() {

		@Override
		public int compare( float[] o1, float[] o2 )
		{
			if( o1[ 0 ] < o2[ 0 ] )
			{
				return -1;
			}
			else if( o1[ 0 ] > o2[ 0 ] )
			{
				return 1;
			}
			return 0;
		}

	};

	/**
	 * @param points
	 *           x, y value points
	 */
	public Terrain( float... points )
	{
		for( int i = 0; i < points.length; i += 2 )
		{
			this.points.add( new float[] { points[ i ], points[ i + 1 ] } );
		}
	}

	/**
	 * Deep copy constructor
	 * 
	 * @param t
	 */
	public Terrain( Terrain t )
	{
		for( float[] p : t.points )
		{
			addPoint( p[ 0 ], p[ 1 ] );
		}
		orderDirty = true;
	}

	@Override
	public float getValue( float time )
	{
		enforceOrder();

		if( points.size() == 0 )
		{
			return 0;
		}
		else
		{
			temp[ 0 ] = time;
			int index = Collections.binarySearch( points, temp, c );
			if( index >= 0 )
			{
				return points.get( index )[ 1 ];
			}
			else
			{
				index++;
				index = -index;

				if( index == 0 )
				{
					return points.get( 0 )[ 1 ];
				}
				else if( index >= points.size() )
				{
					return points.get( points.size() - 1 )[ 1 ];
				}
				else
				{ // general case
					float[] low = points.get( index - 1 );
					float[] high = points.get( index );
					float lt = low[ 0 ];
					float ht = high[ 0 ];
					float lv = low[ 1 ];
					float hv = high[ 1 ];

					float dt = ht - lt;
					float dv = hv - lv;

					float result = lv + dv * ( time - lt ) / dt;

					return result;
				}
			}
		}
	}

	private void enforceOrder()
	{
		if( orderDirty )
		{
			Collections.sort( points, c );
		}
	}

	/**
	 * Adds a point to this envelope. If this point coincides with an
	 * existing point, that point is replaced
	 * 
	 * @param time
	 * @param value
	 */
	public void addPoint( float time, float value )
	{
		points.add( new float[] { time, value } );
		orderDirty = true;
	}

	/**
	 * Clears the current terrain
	 */
	public void clear()
	{
		points.clear();
	}

	/**
	 * Reads the values of this terrain from a stream
	 * 
	 * @param dis
	 * @throws IOException
	 */
	public void read( DataInputStream dis ) throws IOException
	{
		clear();

		int l = dis.readInt();

		for( int i = 0; i < l; i++ )
		{
			addPoint( dis.readFloat(), dis.readFloat() );
		}

		orderDirty = true;
	}

	/**
	 * Writes this {@link Terrain} to a buffer
	 * 
	 * @param buffer
	 */
	public void write( ByteBuffer buffer )
	{
		buffer.putInt( points.size() );

		for( float[] p : points )
		{
			buffer.putFloat( p[ 0 ] );
			buffer.putFloat( p[ 1 ] );
		}
	}

	/**
	 * Counts the number of bytes needed to store the terrain
	 * 
	 * @return the number of bytes
	 */
	public int dataSize()
	{
		return 4 + 2 * 4 * points.size();
	}

	/**
	 * Removes a point from the terrain
	 * 
	 * @param x
	 * @param y
	 * @return <code>true</code> if a point was removed
	 */
	public boolean removePoint( float x, float y )
	{
		for( int i = 0; i < points.size(); i++ )
		{
			if( points.get( i )[ 0 ] == x && points.get( i )[ 1 ] == y )
			{
				points.remove( i );
				return true;
			}
		}
		return false;
	}

	/**
	 * Alters the points in this terrain slightly, while preserving
	 * their order
	 * 
	 * @param rng
	 * @param xMag
	 *           the maximum magnitude of the mutation in the x-axis
	 * @param yMag
	 *           the maximum magnitude of the mutation in the y-axis
	 */
	public void mutate( Random rng, float xMag, float yMag )
	{
		for( int j = 0; j < points.size(); j++ )
		{
			int i = rng.nextInt( points.size() );

			float px = i < 1 ? -Float.MAX_VALUE : points.get( i - 1 )[ 0 ];
			float[] p = points.get( i );
			float nx = i == points.size() - 1 ? Float.MAX_VALUE : points.get( i + 1 )[ 0 ];

			float mx = ( 2 * rng.nextFloat() - 1 ) * xMag;

			mx = Math.max( px - p[ 0 ], mx );
			mx = Math.min( nx - p[ 0 ], mx );

			float my = ( 2 * rng.nextFloat() - 1 ) * yMag;

			p[ 0 ] += mx;
			p[ 1 ] += my;
		}
	}

	/**
	 * Ensures that all points lie within the specified range
	 * 
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public void enforceBounds( float minX, float minY, float maxX, float maxY )
	{
		for( int i = 0; i < points.size(); i++ )
		{
			float[] p = points.get( i );

			if( p[ 0 ] < minX )
			{
				p[ 0 ] = minX;
			}
			else if( p[ 0 ] > maxX )
			{
				p[ 0 ] = maxX;
			}

			if( p[ 1 ] < minY )
			{
				p[ 1 ] = minY;
			}
			else if( p[ 1 ] > maxY )
			{
				p[ 1 ] = maxY;
			}
		}
	}

	/**
	 * Sets this terrain to have a number of random points within
	 * specified bounds
	 * 
	 * @param rng
	 * @param p
	 *           The number of points
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public void randomise( Random rng, int p, float minX, float minY, float maxX,
			float maxY )
	{
		clear();

		float dx = maxX - minX;
		float dy = maxY - minY;

		for( int i = 0; i < p; i++ )
		{
			addPoint( minX + rng.nextFloat() * dx, minY + rng.nextFloat() * dy );
		}
	}
}
