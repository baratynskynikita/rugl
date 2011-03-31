
package com.ryanm.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

/**
 * @author ryanm
 */
public class BufferTest
{
	private ByteBuffer bb;

	private FloatBuffer floats;

	private float[] data;

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		new BufferTest().run();
	}

	private void run()
	{
		final int size = 100000;

		bb = ByteBuffer.allocateDirect( 4 * size ).order( ByteOrder.nativeOrder() );
		floats = bb.asFloatBuffer();

		data = data( size );

		System.out.println( "# size\tsingle\tindexed\tbatch" );

		int base = 0;

		int e = size;
		while( base + e > 100 )
		{
			test( base + e );
			e /= 2;
		}
	}

	private void test( int elements )
	{
		int time = 1000;

		System.out.print( elements + "\t" );
		long kbps = singlePut( data, elements, floats, time );
		System.out.print( kbps + "\t" );
		kbps = singleIndexedPut( data, elements, floats, time );
		System.out.print( kbps + "\t" );
		kbps = bulkPut( data, elements, floats, time );
		System.out.println( kbps + "\t" );
	}

	private long singlePut( float[] data, int elements, FloatBuffer target, int testTime )
	{
		long start = System.currentTimeMillis();
		long elapsed = 0;
		int reps = 0;

		target.clear();

		do
		{
			for( int i = 0; i < elements; i++ )
			{
				target.put( data[ i ] );
			}
			target.flip();
			reps++;
			elapsed = System.currentTimeMillis() - start;
		}
		while( elapsed < testTime );

		long bytes = ( long ) elements * ( long ) reps * 4;

		return bytes / elapsed / 1024;
	}

	private long singleIndexedPut( float[] data, int elements, FloatBuffer target,
			int testTime )
	{
		long start = System.currentTimeMillis();
		long elapsed = 0;
		int reps = 0;

		target.clear();

		do
		{
			for( int i = 0; i < elements; i++ )
			{
				target.put( i, data[ i ] );
			}
			reps++;
			elapsed = System.currentTimeMillis() - start;
		}
		while( elapsed < testTime );

		long bytes = ( long ) elements * ( long ) reps * 4;

		return bytes / elapsed / 1024;
	}

	private long bulkPut( float[] data, int elements, FloatBuffer target, int testTime )
	{
		long start = System.currentTimeMillis();
		long elapsed = 0;
		int reps = 0;

		target.clear();

		do
		{
			target.put( data, 0, elements );
			target.flip();

			reps++;
			elapsed = System.currentTimeMillis() - start;
		}
		while( elapsed < testTime );

		long bytes = ( long ) elements * ( long ) reps * 4;

		return bytes / elapsed / 1024;
	}

	private float[] data( int elements )
	{
		float[] data = new float[ elements ];
		Random rng = new Random();

		for( int i = 0; i < data.length; i++ )
		{
			data[ i ] = 100 * rng.nextFloat();
		}

		return data;
	}
}
