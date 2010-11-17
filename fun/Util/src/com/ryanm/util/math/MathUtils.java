
package com.ryanm.util.math;

/***/
public class MathUtils
{
	/**
	 * @param floor
	 * @param ceil
	 * @param value
	 * @return 0 if value <= floor, 1 if value >= ceil, otherwise
	 *         interpolated
	 */
	public static float lerp( float floor, float ceil, float value )
	{
		if( value <= floor )
		{
			return 0;
		}
		else if( value >= ceil )
		{
			return 1;
		}
		else
		{
			return ( value - floor ) / ( ceil - floor );
		}
	}

	/**
	 * First-order continuous, whatever that means
	 * 
	 * @param t
	 *           must lie between 0 and 1
	 * @return A smooth range between 0 and 1
	 */
	public static float smoothStep( float t )
	{
		return t * t * ( 3.f - ( t + t ) );
	}

	/**
	 * @param t
	 *           must lie between 0 and 1
	 * @return A smooth range between 0 and 1
	 */
	public static float smoothStep2( float t )
	{
		return t * t * t * ( 10.f + t * ( -15.f + 6.f * t ) );
	}

	/**
	 * Calculates the mean and concentration of a number of angles
	 * 
	 * @param angles
	 *           An array of angles, in radians
	 * @return A two-element array { mean, concentration }. The
	 *         concentration metric will be 1 if all input angles are
	 *         identical and zero if the angles are distributed evenly
	 *         around the circle. I doubt it varies linearly though.
	 */
	public static float[] meanAngle( float[] angles )
	{
		double meanX = 0;
		double meanY = 0;

		for( int i = 0; i < angles.length; i++ )
		{
			meanY += Math.sin( angles[ i ] );
			meanX += Math.cos( angles[ i ] );
		}

		meanX /= angles.length;
		meanY /= angles.length;

		float[] results = new float[ 2 ];

		results[ 0 ] = ( float ) Math.atan2( meanY, meanX );
		results[ 1 ] = ( float ) Math.sqrt( meanX * meanX + meanY * meanY );

		return results;
	}

	/**
	 * @param a
	 * @param b
	 * @return The distance between angles a and b
	 */
	public static float angleDiff( float a, float b )
	{
		float diff = Math.abs( a - b );
		if( diff > Math.PI )
		{
			diff = ( float ) ( 2 * Math.PI - diff );
		}

		return diff;
	}
}
