
package com.ryanm.util.math;

import java.util.Random;

/**
 * Fast trigonometric operations
 * 
 * @author Riven
 */
public class Trig
{
	/**
	 * I'm sick of casting to float
	 */
	public static final float PI = ( float ) Math.PI;

	private static final int ATAN2_BITS = 7;

	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;

	private static final int ATAN2_MASK = ~( -1 << ATAN2_BITS2 );

	private static final int ATAN2_COUNT = ATAN2_MASK + 1;

	private static final int ATAN2_DIM = ( int ) Math.sqrt( ATAN2_COUNT );

	private static final float ATAN2_DIM_MINUS_1 = ATAN2_DIM - 1;

	private static final float[] atan2 = new float[ ATAN2_COUNT ];

	private static final int SIN_BITS, SIN_MASK, SIN_COUNT;

	private static final float radFull, radToIndex;

	private static final float[] sin, cos;

	static
	{
		for( int i = 0; i < ATAN2_DIM; i++ )
		{
			for( int j = 0; j < ATAN2_DIM; j++ )
			{
				float x0 = ( float ) i / ATAN2_DIM;
				float y0 = ( float ) j / ATAN2_DIM;

				atan2[ j * ATAN2_DIM + i ] = ( float ) Math.atan2( y0, x0 );
			}
		}

		SIN_BITS = 12;
		SIN_MASK = ~( -1 << SIN_BITS );
		SIN_COUNT = SIN_MASK + 1;

		radFull = ( float ) ( Math.PI * 2.0 );
		radToIndex = SIN_COUNT / radFull;

		sin = new float[ SIN_COUNT ];
		cos = new float[ SIN_COUNT ];

		for( int i = 0; i < SIN_COUNT; i++ )
		{
			sin[ i ] = ( float ) Math.sin( ( i + 0.5f ) / SIN_COUNT * radFull );
			cos[ i ] = ( float ) Math.cos( ( i + 0.5f ) / SIN_COUNT * radFull );
		}
	}

	/**
	 * Like {@link Math#sin(double)}, but a lot faster and a bit less
	 * accurate
	 * 
	 * @param rad
	 * @return sin( rad )
	 */
	public static final float sin( float rad )
	{
		return sin[ ( int ) ( rad * radToIndex ) & SIN_MASK ];
	}

	/**
	 * Like {@link Math#cos(double)}, but a lot faster and a bit less
	 * accurate
	 * 
	 * @param rad
	 * @return cos( rad )
	 */
	public static final float cos( float rad )
	{
		return cos[ ( int ) ( rad * radToIndex ) & SIN_MASK ];
	}

	/**
	 * @param y
	 * @param x
	 * @return the angle to (x,y)
	 */
	public static final float atan2( float y, float x )
	{
		float add, mul;

		if( x < 0.0f )
		{
			if( y < 0.0f )
			{
				x = -x;
				y = -y;

				mul = 1.0f;
			}
			else
			{
				x = -x;
				mul = -1.0f;
			}

			add = -3.141592653f;
		}
		else
		{
			if( y < 0.0f )
			{
				y = -y;
				mul = -1.0f;
			}
			else
			{
				mul = 1.0f;
			}

			add = 0.0f;
		}

		float invDiv = ATAN2_DIM_MINUS_1 / ( x < y ? y : x );

		int xi = ( int ) ( x * invDiv );
		int yi = ( int ) ( y * invDiv );

		return ( atan2[ yi * ATAN2_DIM + xi ] + add ) * mul;
	}

	/**
	 * @param degrees
	 * @return the radians value
	 */
	public static float toRadians( float degrees )
	{
		return degrees / 180.0f * Trig.PI;
	}

	/**
	 * @param radians
	 * @return the degrees value
	 */
	public static float toDegrees( float radians )
	{
		return radians * 180.0f / Trig.PI;
	}

	/**
	 * Compares accuracy of LUT-trig methods
	 * 
	 * @param args
	 */
	@SuppressWarnings( "unused" )
	public static void main( String[] args )
	{
		int angleCount = 10800;

		float[] angles = new float[ angleCount ];

		float[] sinerror = new float[ angleCount ];
		float[] coserror = new float[ angleCount ];
		float[] atanerror = new float[ angleCount ];

		for( int i = 0; i < angleCount; i++ )
		{
			float angle = ( float ) ( Math.PI * 2 * i / angleCount );

			angles[ i ] = angle;

			float rs = ( float ) Math.sin( angle );
			float ls = sin( angle );
			sinerror[ i ] = Math.abs( rs - ls );

			float rc = ( float ) Math.cos( angle );
			float lc = cos( angle );
			coserror[ i ] = Math.abs( rc - lc );

			float ratan = ( float ) Math.atan2( 100 * rs, 100 * rc );
			float latan = atan2( 100 * rs, 100 * rc );

			atanerror[ i ] = MathUtils.angleDiff( ratan, latan );
		}

		// analyse errors
		float maxSin = -1, minSin = Float.MAX_VALUE, meanSin = 0;
		float maxCos = -1, minCos = Float.MAX_VALUE, meanCos = 0;
		float maxAtan = -1, minAtan = Float.MAX_VALUE, meanAtan = 0;

		for( int i = 0; i < angleCount; i++ )
		{
			maxSin = Math.max( maxSin, sinerror[ i ] );
			minSin = Math.min( minSin, sinerror[ i ] );
			maxCos = Math.max( maxCos, coserror[ i ] );
			minCos = Math.min( minCos, coserror[ i ] );

			maxAtan = Math.max( maxAtan, atanerror[ i ] );
			minAtan = Math.min( minAtan, atanerror[ i ] );

			meanSin += sinerror[ i ];
			meanCos += coserror[ i ];
			meanAtan += atanerror[ i ];
		}

		meanSin /= angleCount;
		meanCos /= angleCount;
		meanAtan /= angleCount;

		System.out.println( "Accuracy:" );
		System.out.println( "sin/cos table size = " + sin.length );
		System.out.println( "Sin\tmin\t\tmax\t\tmean" );
		System.out.println( "\t" + minSin + "\t" + maxSin + "\t" + meanSin );
		System.out.println( "Cos\tmin\t\tmax\t\tmean" );
		System.out.println( "\t" + minCos + "\t" + maxCos + "\t" + meanCos );
		System.out.println( "atan2 table size = " + atan2.length );
		System.out.println( "Atan2\tmin\t\tmax\t\tmean (in degrees)" );
		System.out.println( "\t" + Math.toDegrees( minAtan ) + "\t" + Math.toDegrees( maxAtan )
				+ "\t" + Math.toDegrees( meanAtan ) );

		System.out.println( "Performance:" );
		Random rng = new Random();

		// shuffle angle array - apparently consecutive angles are a
		// special fast case for Math.sin, etc
		for( int i = angles.length - 1; i >= 0; i-- )
		{
			int index = rng.nextInt( i + 1 );
			float a = angles[ index ];
			angles[ index ] = angles[ i ];
			angles[ i ] = a;
		}

		System.out.println( "testing" );
		int tests = ( int ) 2E7;

		for( int i = 0; i < 10; i++ )
		{
			long t = System.currentTimeMillis();

			float jm = testMathSin( tests, angles );
			// System.out.println(
			// "can't optimise it out if I print it eh?" + jm );
			long duration = System.currentTimeMillis() - t;
			double jp = ( double ) tests / duration;
			// System.out.println( "Java trig does " + jp +
			// " sin ops per millisecond" );

			t = System.currentTimeMillis();
			float fm = testFastSin( tests, angles );
			// System.out.println(
			// "can't optimise it out if I print it eh?" + fm );
			duration = System.currentTimeMillis() - t;
			double fp = ( double ) tests / duration;
			// System.out.println( "LUT trig does " + fp +
			// " sin ops per millisecond" );

			System.out.println( "Fast sin is " + fp / jp + " times faster than java sin" );
		}

		float[] coords = new float[ 100 ];
		float r = 100;
		for( int i = 0; i < coords.length; i++ )
		{
			coords[ i ] = rng.nextFloat() * 2 * r - r;
		}

		for( int i = 0; i < 10; i++ )
		{
			long t = System.currentTimeMillis();

			float jm = testMathAtan( tests, coords );
			// System.out.println(
			// "can't optimise it out if I print it eh?" + jm );
			long duration = System.currentTimeMillis() - t;
			double jp = ( double ) tests / duration;
			// System.out.println( "Java trig does " + jp +
			// " atan ops per millisecond" );

			t = System.currentTimeMillis();
			float fm = testFastAtan( tests, coords );
			// System.out.println(
			// "can't optimise it out if I print it eh?" + fm );
			duration = System.currentTimeMillis() - t;
			double fp = ( double ) tests / duration;
			// System.out.println( "LUT trig does " + fp +
			// " atan ops per millisecond" );

			System.out.println( "Fast atan is " + fp / jp + " times faster than java atan" );

		}
	}

	private static float testMathSin( int tests, float[] angles )
	{
		float sum = 0;

		for( int i = 0; i < tests; i++ )
		{
			float y = ( float ) Math.sin( angles[ ( i % angles.length ) ] );

			sum += y;
		}

		return sum;
	}

	private static float testFastSin( long tests, float[] angles )
	{
		float sum = 0;

		for( int i = 0; i < tests; i++ )
		{
			float y = sin( angles[ i % angles.length ] );

			sum += y;
		}

		return sum;
	}

	private static float testMathAtan( long tests, float[] coords )
	{
		float sum = 0;

		for( int i = 0; i < tests; i++ )
		{
			float x = coords[ i % coords.length ];
			float y = coords[ ( i + 1 ) % coords.length ];

			sum += Math.atan2( y, x );
		}

		return sum;
	}

	private static float testFastAtan( long tests, float[] coords )
	{
		float sum = 0;

		for( int i = 0; i < tests; i++ )
		{
			float x = coords[ i % coords.length ];
			float y = coords[ ( i + 1 ) % coords.length ];

			sum += atan2( y, x );
		}

		return sum;
	}
}
