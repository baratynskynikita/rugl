
package com.ryanm.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A convenience class to do code profiling.
 * 
 * @author Keith Woodward
 */
public class CodeTimer
{
	private static DecimalFormat fourDP = new DecimalFormat( "0.####" );

	private static DecimalFormat percent = new DecimalFormat( "0.#%" );

	/**
	 * How long a call to {@link #tick(String)} takes
	 */
	private static long SELF_TIME;

	private static final long NANOS_IN_A_SECOND = 1000000000;

	private static final long NANOS_IN_A_MILLISECOND = 1000000;

	static
	{
		recalibrate( 10000 );
	}

	/**
	 * The name of this {@link CodeTimer}, to identify the output
	 */
	public final String name;

	/**
	 * Names of intervals
	 */
	private String[] intervalNames = new String[ 5 ];

	/**
	 * Sum of interval durations
	 */
	private long[] intervalDurationSums = new long[ intervalNames.length ];

	/**
	 * The index of the current interval
	 */
	private int intervalIndex;

	/**
	 * The time of the last call to {@link #tick(String)}
	 */
	private long lastClickTime;

	/**
	 * Count of timing periods
	 */
	private int periodCount = 0;

	private boolean periodStarted = false;

	private long periodStartTime;

	/**
	 * Sum of period durations
	 */
	private long periodDurationSum = 0;

	/**
	 * Defaults to <code>true</code>
	 */
	public boolean enabled = true;

	private long lastPrintOutNanos = System.nanoTime();

	/**
	 * The number of seconds between prints, defaults to 5
	 */
	public int printFrequencySeconds = 5;

	/**
	 * The level of information printed for the total time spent in a
	 * profiling period
	 */
	public Output period;

	/**
	 * The level of information printed for time taken in intervals
	 */
	public Output interval;

	/**
	 * @param name
	 *           A name for this {@link CodeTimer}, so as to identify
	 *           the output
	 * @param period
	 *           output for profiling period duration. May not be null
	 * @param interval
	 *           output for interval durations, May not be null
	 */
	public CodeTimer( String name, Output period, Output interval )
	{
		this.name = name;
		this.period = period;
		this.interval = interval;
	}

	/**
	 * Call to start a profiling period, or to start an interval in an
	 * open period
	 * 
	 * @param name
	 *           A helpful name for this interval. Makes it easy to
	 *           find what bit of code you're measuring
	 */
	public void tick( String name )
	{
		if( enabled )
		{
			long clickTime = System.nanoTime();

			if( !periodStarted )
			{
				periodStarted = true;
				periodStartTime = clickTime;

				intervalNames[ 0 ] = name;
			}
			else
			{
				long duration = clickTime - lastClickTime;
				intervalDurationSums[ intervalIndex ] += duration;

				intervalIndex++;

				if( intervalIndex >= intervalNames.length )
				{
					intervalNames = ArrayUtil.grow( intervalNames );
					intervalDurationSums = ArrayUtil.grow( intervalDurationSums );
				}

				intervalNames[ intervalIndex ] = name;
			}

			lastClickTime = clickTime;
		}
	}

	/**
	 * Call to end a profiling period, and print the results if
	 * {@link #printFrequencySeconds} have passed since we last printed
	 */
	public void lastTick()
	{
		lastTick( System.nanoTime() - lastPrintOutNanos > printFrequencySeconds
				* NANOS_IN_A_SECOND );
	}

	/**
	 * Call to end a profiling period
	 * 
	 * @param print
	 *           <code>true</code> to print results, <code>false</code>
	 *           no to
	 */
	public void lastTick( boolean print )
	{
		if( enabled )
		{
			long clickTime = System.nanoTime();

			long intervalDuration = clickTime - lastClickTime;
			intervalDurationSums[ intervalIndex ] += intervalDuration;

			intervalIndex = 0;

			long periodDuration = clickTime - periodStartTime;
			periodDurationSum += periodDuration;
			periodCount++;

			periodStarted = false;

			if( print )
			{
				System.out.println( name + " period "
						+ period.format( periodDurationSum, periodCount ) );
				long perDur = periodDurationSum / periodCount;

				for( int i = 0; i < intervalNames.length && intervalNames[ i ] != null; i++ )
				{
					float intDur = intervalDurationSums[ i ] / periodCount;
					float p = intDur / perDur;
					System.out.println( "\t" + intervalNames[ i ] + "\t" + percent.format( p )
							+ "\t" + interval.format( intervalDurationSums[ i ], periodCount ) );
				}

				for( int i = 0; i < intervalDurationSums.length; i++ )
				{
					intervalDurationSums[ i ] = 0;
					intervalNames[ i ] = null;
				}
				periodDurationSum = 0;
				periodCount = 0;

				lastPrintOutNanos = clickTime;
			}
		}
	}

	/**
	 * Calibrates the timer for the machine
	 * 
	 * @param numTests
	 *           10000 might be about right
	 */
	public static void recalibrate( int numTests )
	{
		boolean print = false;
		CodeTimer codeTimer = new CodeTimer( "calibrate", null, null );
		// warm the JIT
		for( int i = 0; i < 1024; i++ )
		{
			codeTimer.tick( "foo" );
			codeTimer.lastTick( false );
		}

		// find how out long it takes to call click(), so that time can
		// be accounted for
		ArrayList<Long> selfTimeObservations = new ArrayList<Long>( numTests );
		for( int i = 0; i < numTests; i++ )
		{
			long nanoSelfTime = -( System.nanoTime() - System.nanoTime() );

			codeTimer.tick( "foo" );

			long t0 = System.nanoTime();
			codeTimer.tick( "bar" );
			long t1 = System.nanoTime();

			codeTimer.tick( "baz" );
			codeTimer.lastTick( false );

			long currentSelfTime = t1 - t0 - nanoSelfTime;
			if( print )
			{
				System.out.println( "calibrating : currentSelfTime == " + currentSelfTime
						+ ", nanoSelfTime == " + nanoSelfTime );
			}
			selfTimeObservations.add( new Long( currentSelfTime ) );
		}
		// sort the times
		Collections.sort( selfTimeObservations );
		if( print )
		{
			for( int i = 0; i < selfTimeObservations.size(); i++ )
			{
				System.out.println( "calibrating : selfTimeObservations.get(i) == "
						+ selfTimeObservations.get( i ) );
			}
		}
		// cut out the slowest 5% which are assumed to be outliers
		for( int i = 0; i < ( int ) ( numTests * 0.05 ); i++ )
		{
			selfTimeObservations.remove( 0 );
		}
		// cut out the fastest 5% which are assumed to be outliers
		for( int i = 0; i < ( int ) ( numTests * 0.05 ); i++ )
		{
			selfTimeObservations.remove( selfTimeObservations.size() - 1 );
		}
		if( print )
		{
			System.out
					.println( "calibrating : Slimmed list: selfTimeObservations.size() == "
							+ selfTimeObservations.size() );
			for( int i = 0; i < selfTimeObservations.size(); i++ )
			{
				System.out.println( "calibrating : selfTimeObservations.get(i) == "
						+ selfTimeObservations.get( i ) );
			}
		}
		// find the average
		long sumOfSelfTimes = 0;
		for( int i = 0; i < selfTimeObservations.size(); i++ )
		{
			sumOfSelfTimes += selfTimeObservations.get( i ).longValue();
		}
		SELF_TIME = sumOfSelfTimes / selfTimeObservations.size();
		if( print )
		{
			System.out.println( "calibrating : SELF_TIME == " + SELF_TIME );
		}
	}

	/**
	 * Time unit that is printed
	 * 
	 * @author ryanm
	 */
	public enum Output
	{
		/**
		 * Second-level granularity
		 */
		Seconds
		{
			@Override
			public String format( long totalNanos, long count )
			{
				double avTotalSeconds = ( double ) totalNanos / ( count * NANOS_IN_A_SECOND );
				return fourDP.format( avTotalSeconds ) + "s";
			}
		},
		/**
		 * Millisecond-level granularity
		 */
		Millis
		{
			@Override
			public String format( long totalNanos, long count )
			{
				double avTotalMillis =
						( double ) totalNanos / ( count * NANOS_IN_A_MILLISECOND );
				return fourDP.format( avTotalMillis ) + "ms";
			}
		},
		/**
		 * Nanosecond-level granularity
		 */
		Nanos
		{
			@Override
			public String format( long totalNanos, long count )
			{
				double avTotalNanos = ( double ) totalNanos / count;
				return fourDP.format( avTotalNanos ) + "ns";
			}
		};

		/**
		 * @param totalNanos
		 *           The sum of some number of measurements
		 * @param count
		 *           The number of measurements
		 * @return A string describing the average time
		 */
		public abstract String format( long totalNanos, long count );
	};
}