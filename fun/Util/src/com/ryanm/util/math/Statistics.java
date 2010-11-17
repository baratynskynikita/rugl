
package com.ryanm.util.math;

import java.nio.ShortBuffer;

/**
 * @author ryanm
 */
public class Statistics
{
	/**
	 * Calculates the mean and the standard deviation
	 * 
	 * @param values
	 *           The input values. NaN values will be ignored
	 * @return A two element array, the first element is the mean
	 *         value, the second is the standard deviation. Or null if
	 *         value is null or empty, or entirely NaN filled
	 */
	public static double[] calculateMeanDeviation( double[] values )
	{
		if( values != null && values.length > 0 )
		{
			double mean = 0;
			int count = 0;

			for( int i = 0; i < values.length; i++ )
			{
				if( !Double.isNaN( values[ i ] ) )
				{
					mean += values[ i ];
					count++;
				}
			}

			if( count == 0 )
			{
				return null;
			}

			mean /= count;

			double variance = 0;

			for( int i = 0; i < values.length; i++ )
			{
				if( !Double.isNaN( values[ i ] ) )
				{
					double delta = values[ i ] - mean;
					variance += delta * delta;
				}
			}

			variance /= count;

			double deviation = Math.sqrt( variance );

			return new double[] { mean, deviation };
		}

		return null;
	}

	/**
	 * Calculates the mean and the standard deviation
	 * 
	 * @param values
	 *           The input values.
	 * @return A two element array, the first element is the mean
	 *         value, the second is the standard deviation. Or null if
	 *         value is null or empty, or entirely NaN filled
	 */
	public static double[] calculateMeanDeviation( ShortBuffer values )
	{
		if( values != null && values.limit() > 0 )
		{
			double mean = 0;
			int count = 0;

			for( int i = 0; i < values.limit(); i++ )
			{
				mean += values.get( i );
				count++;
			}

			if( count == 0 )
			{
				return null;
			}

			mean /= count;

			double variance = 0;

			for( int i = 0; i < values.limit(); i++ )
			{
				double delta = values.get( i ) - mean;
				variance += delta * delta;
			}

			variance /= count;

			double deviation = Math.sqrt( variance );

			return new double[] { mean, deviation };
		}

		return null;
	}
}
