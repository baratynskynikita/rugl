
package com.ryanm.util.math;

import java.util.Arrays;
import java.util.Comparator;

import org.lwjgl.util.vector.Vector2f;

/**
 * Approximates some function with a series of line segments
 * 
 * @author ryanm
 */
public class FunctionApproximation
{
	/***/
	public Vector2f[] funcPoints;

	private static Comparator<Vector2f> pc = new Comparator<Vector2f>() {

		@Override
		public int compare( Vector2f o1, Vector2f o2 )
		{
			float diff = o1.x - o2.x;

			if( diff == 0 )
			{
				diff = o1.y - o2.y;
			}

			return ( int ) Math.signum( diff );
		}
	};

	/**
	 * @param points
	 */
	public FunctionApproximation( float... points )
	{
		assert points.length % 2 == 0;

		funcPoints = new Vector2f[ points.length / 2 ];

		int index = 0;
		for( int i = 0; i < funcPoints.length; i++ )
		{
			funcPoints[ i ] = new Vector2f( points[ index++ ], points[ index++ ] );
		}

		pointAltered();
	}

	/**
	 * @param value
	 * @return The value of the function, or 0 if there are no function
	 *         points
	 */
	public float evaluate( float value )
	{
		if( funcPoints.length == 0 )
		{
			return 0;
		}
		if( value <= funcPoints[ 0 ].x )
		{
			return funcPoints[ 0 ].y;
		}
		else if( value >= funcPoints[ funcPoints.length - 1 ].x )
		{
			return funcPoints[ funcPoints.length - 1 ].y;
		}
		else
		{
			for( int i = funcPoints.length - 1; i >= 0; i-- )
			{
				if( value >= funcPoints[ i ].x )
				{
					Vector2f min = funcPoints[ i ];
					Vector2f max = funcPoints[ i + 1 ];

					assert min.x <= value : min.x + " " + value + " " + max.x;
					assert max.x >= value : min.x + " " + value + " " + max.x;

					double d = Range.toRatio( value, min.x, max.x );

					float result = ( float ) ( min.y + d * ( max.y - min.y ) );

					assert result >= min.y && result <= max.y || result >= max.y && result <= min.y : min.y
							+ " " + result + " " + max.y;

					return result;
				}
			}
		}

		return 0;
	}

	/**
	 * @param a
	 * @param b
	 * @return the area under the function between a and b
	 */
	public float integrate( float a, float b )
	{
		if( funcPoints.length == 0 )
		{
			return 0;
		}

		if( a > b )
		{
			float t = a;
			a = b;
			b = t;
		}

		int startPoint = findPoint( a );
		int endPoint = findPoint( b );

		startPoint++;
		float area = trapeziumArea( a, funcPoints[ startPoint ].x );

		for( int i = startPoint; i < endPoint; i++ )
		{
			area += trapeziumArea( funcPoints[ i ].x, funcPoints[ i + 1 ].x );
		}

		if( endPoint == -1 )
		{
			return ( b - a ) * evaluate( a );
		}
		else
		{
			area += trapeziumArea( funcPoints[ endPoint ].x, b );

			return area;
		}
	}

	/**
	 * @param a
	 * @param b
	 * @return the area of the trapezium a, func(a), func(b), b
	 */
	private float trapeziumArea( float a, float b )
	{
		return trapeziumArea( a, evaluate( a ), b, evaluate( b ) );
	}

	/**
	 * @param a
	 * @param area
	 * @return b, where {@link #integrate(float, float)} == area
	 */
	public float deintegrate( final float a, final float area )
	{
		// System.out.printf(
		// "FunctionApproximation.deintegrate( %1s, %2s )\n",
		// a,
		// area );

		float limit = 1e-6f;
		float bottom = a;
		float top = bottom;
		while( integrate( a, top ) < area )
		{
			top = top == 0 ? 1 : top * 2;
		}

		float testx = ( top + bottom ) / 2;
		float ta = integrate( a, testx );

		while( Math.abs( ta - area ) > limit )
		{
			assert integrate( a, bottom ) < area;
			assert integrate( a, top ) > area;

			if( ta < area )
			{
				// System.out.println( "moving up" );
				bottom = testx;
			}
			else
			{
				assert ta > area;
				// System.out.println( "moving down" );
				top = testx;
			}

			testx = ( top + bottom ) / 2;

			ta = integrate( a, testx );

			// System.out.println( "looking for " + area + " between "
			// + integrate( a, bottom ) + " and " + integrate( a, top )
			// );
		}

		return testx;
	}

	/**
	 * Finds the index of the function point to the left of the
	 * supplied point
	 * 
	 * @param v
	 * @return The index of the function point, or -1 if there is no
	 *         function point to the left
	 */
	private int findPoint( float v )
	{
		if( funcPoints.length == 0 || v <= funcPoints[ 0 ].x )
		{
			return -1;
		}
		else
		{
			for( int i = 0; i < funcPoints.length - 1; i++ )
			{
				if( v >= funcPoints[ i ].x && v < funcPoints[ i + 1 ].x )
				{
					return i;
				}
			}

			return funcPoints.length - 1;
		}
	}

	private static float trapeziumArea( float ax, float ay, float bx, float by )
	{
		float dx = bx - ax;
		float dy = by - ay;
		float area = ay * dx + 0.5f * dy * dx;

		return area;
	}

	/**
	 * @param x
	 * @param y
	 */
	public void addPoint( float x, float y )
	{
		Vector2f p = new Vector2f( x, y );
		Vector2f[] nfp = new Vector2f[ funcPoints.length + 1 ];
		System.arraycopy( funcPoints, 0, nfp, 0, funcPoints.length );

		nfp[ nfp.length - 1 ] = p;

		funcPoints = nfp;

		pointAltered();
	}

	/**
	 * Removes the function point closest to the specified coordinates
	 * 
	 * @param x
	 * @param y
	 */
	public void removeNearest( float x, float y )
	{
		Vector2f p = nearest( x, y );

		if( p != null )
		{
			Vector2f[] nfp = new Vector2f[ funcPoints.length - 1 ];
			int index = 0;
			for( int i = 0; i < funcPoints.length; i++ )
			{
				if( funcPoints[ i ] != p )
				{
					nfp[ index++ ] = funcPoints[ i ];
				}
			}

			funcPoints = nfp;
		}
	}

	/**
	 * Call this when you alter one of the function points to maintain
	 * point ordering
	 */
	public void pointAltered()
	{
		Arrays.sort( funcPoints, pc );
	}

	/**
	 * Copies the values of the supplied {@link FunctionApproximation}
	 * 
	 * @param fa
	 */
	public void set( FunctionApproximation fa )
	{
		funcPoints = new Vector2f[ fa.funcPoints.length ];

		for( int i = 0; i < funcPoints.length; i++ )
		{
			funcPoints[ i ] = new Vector2f( fa.funcPoints[ i ] );
		}
	}

	/**
	 * Finds the function point closest to the specified coordinates
	 * 
	 * @param x
	 * @param y
	 * @return the closest function point, or null if there are no
	 *         points
	 */
	public Vector2f nearest( float x, float y )
	{
		float delta = Float.MAX_VALUE;
		Vector2f nearest = null;

		for( int i = 0; i < funcPoints.length; i++ )
		{
			float dx = funcPoints[ i ].x - x;
			float dy = funcPoints[ i ].y - y;
			float d = dx * dx + dy * dy;

			if( d < delta )
			{
				delta = d;
				nearest = funcPoints[ i ];
			}
		}

		return nearest;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof FunctionApproximation )
		{
			FunctionApproximation fa = ( FunctionApproximation ) obj;

			for( int i = 0; i < funcPoints.length; i++ )
			{
				if( funcPoints[ i ].x != fa.funcPoints[ i ].x
						|| funcPoints[ i ].y != fa.funcPoints[ i ].y )
				{
					return false;
				}
			}

			return true;
		}

		return false;
	}
}
