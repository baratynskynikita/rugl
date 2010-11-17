
package com.ryanm.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.ryanm.util.math.Range;
import com.ryanm.util.math.Trig;

/**
 * A data structure for fast line segment intersection tests
 * 
 * @author ryanm
 * @param <T>
 *           The segment type
 */
public class BucketGrid<T extends Segment>
{
	private final Node[][] heads;

	private final Node[][] tails;

	// private final List[][] segs;

	private final GridIterate gridIterate;

	private static class Node
	{
		private Segment seg;

		private Node next;
	}

	/**
	 * @param bucketSize
	 * @param xBuckets
	 * @param yBuckets
	 */
	public BucketGrid( float bucketSize, int xBuckets, int yBuckets )
	{
		gridIterate =
				new GridIterate( bucketSize, 0, 0, xBuckets * bucketSize, yBuckets * bucketSize );
		heads = new Node[ xBuckets ][ yBuckets ];
		tails = new Node[ xBuckets ][ yBuckets ];

		// segs = new ArrayList[ xBuckets ][ yBuckets ];
		//
		// for( int i = 0; i < segs.length; i++ )
		// {
		// for( int j = 0; j < segs[ i ].length; j++ )
		// {
		// segs[ i ][ j ] = new ArrayList();
		// }
		// }
	}

	/**
	 * Adds a segment to the grid
	 * 
	 * @param seg
	 */
	public void add( T seg )
	{
		gridIterate.setSeg( seg.ax, seg.ay, seg.bx, seg.by );

		while( !gridIterate.isDone() )
		{
			gridIterate.next();

			if( inBounds( gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY() ) )
			{
				add( seg, gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY() );
			}
		}
	}

	/**
	 * Removes a segment from the grid
	 * 
	 * @param seg
	 */
	public void remove( T seg )
	{
		gridIterate.setSeg( seg.ax, seg.ay, seg.bx, seg.by );

		while( !gridIterate.isDone() )
		{
			gridIterate.next();

			if( inBounds( gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY() ) )
			{
				remove( seg, gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY() );
			}
		}
	}

	/**
	 * Tests a segment against the occupant of the grid. Segments
	 * intersect the test segment are added to the result set
	 * 
	 * @param seg
	 * @param results
	 *           The list in which to place the results
	 */
	public void test( Segment seg, Set<T> results )
	{
		gridIterate.setSeg( seg.ax, seg.ay, seg.bx, seg.by );

		while( !gridIterate.isDone() )
		{
			gridIterate.next();

			if( inBounds( gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY() ) )
			{
				test( seg, gridIterate.lastGridCoords.getX(), gridIterate.lastGridCoords.getY(),
						results );
			}
		}

		// do the intersection tests
		for( Iterator<T> i = results.iterator(); i.hasNext(); )
		{
			if( !seg.intersects( i.next() ) )
			{
				i.remove();
			}
		}
	}

	boolean inBounds( int x, int y )
	{
		return x >= 0 && x < heads.length && y >= 0 && y < heads[ 0 ].length;
	}

	/**
	 * Removes all segments
	 */
	public void clear()
	{
		for( int i = 0; i < heads.length; i++ )
		{
			for( int j = 0; j < heads[ i ].length; j++ )
			{
				heads[ i ][ j ] = null;
				tails[ i ][ j ] = null;
			}
		}
	}

	private void add( Segment seg, int x, int y )
	{
		Node n = new Node();
		n.seg = seg;
		if( heads[ x ][ y ] == null )
		{ // empty
			heads[ x ][ y ] = n;
			tails[ x ][ y ] = n;
		}
		else
		{
			tails[ x ][ y ].next = n;
			tails[ x ][ y ] = n;
		}

		// segs[ x ][ y ].add( seg );
	}

	private void remove( Segment seg, int x, int y )
	{
		Node last = null;
		Node next = heads[ x ][ y ];

		while( !seg.equals( next.seg ) )
		{
			last = next;
			next = next.next;
		}

		assert seg.equals( next.seg );

		if( last == null )
		{ // remove head
			heads[ x ][ y ] = heads[ x ][ y ].next;
		}
		else
		{
			last.next = next.next;
		}

		if( tails[ x ][ y ] == next )
		{ // and tail
			tails[ x ][ y ] = last;
		}
	}

	@SuppressWarnings( "unchecked" )
	private void test( Segment seg, int x, int y, Set<T> results )
	{
		Node t = heads[ x ][ y ];

		while( t != null )
		{
			results.add( ( T ) t.seg );
			t = t.next;
		}

		// results.addAll( segs[ x ][ y ] );
	}

	/**
	 * tests performance
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		int segCount = 1000;
		int queryCount = 1000;
		Range segSize = new Range( 0.1f, 50 );
		Range querySize = new Range( 0.1f, 10 );
		float bucketSize = 100.0f / 64;

		Random rng = new Random( 1652732 );

		BucketGrid<Segment> bg =
				new BucketGrid<Segment>( bucketSize, ( int ) ( 100 / bucketSize ),
						( int ) ( 100 / bucketSize ) );

		// generate segs
		Segment[] segs = new Segment[ segCount ];
		for( int i = 0; i < segs.length; i++ )
		{
			float x = rng.nextFloat() * 100;
			float y = rng.nextFloat() * 100;
			float a = rng.nextFloat() * Trig.PI * 2;
			float l = segSize.toValue( rng.nextFloat() );

			segs[ i ] = new Segment( x, y, x + Trig.cos( a ) * l, y + Trig.sin( a ) * l );
			segs[ i ].limit( 100 );
		}

		System.out.println( "segs generated" );

		for( int i = 0; i < segs.length; i++ )
		{
			bg.add( segs[ i ] );
		}

		System.out.println( "\tadded to grid" );

		// generate query segs
		Segment[] queries = new Segment[ queryCount ];
		for( int i = 0; i < queries.length; i++ )
		{
			float x = rng.nextFloat() * 100;
			float y = rng.nextFloat() * 100;
			float a = rng.nextFloat() * Trig.PI * 2;
			float l = querySize.toValue( rng.nextFloat() );

			queries[ i ] = new Segment( x, y, x + Trig.cos( a ) * l, y + Trig.sin( a ) * l );
			queries[ i ].limit( 100 );
		}

		System.out.println( "Query segs generated" );

		System.out.println( "checking result correctness..." );
		// check correctness
		Set<Segment> brute = new HashSet<Segment>();
		Set<Segment> grid = new HashSet<Segment>();
		int passed = 0;
		int intersections = 0;
		for( int i = 0; i < queries.length; i++ )
		{
			brute.clear();
			grid.clear();

			// brute
			for( int j = 0; j < segs.length; j++ )
			{
				if( queries[ i ].intersects( segs[ j ] ) )
				{
					brute.add( segs[ j ] );
				}
			}

			intersections += brute.size();

			// grid
			bg.test( queries[ i ], grid );

			// compare results
			boolean same = brute.size() == grid.size();
			for( Segment s : brute )
			{
				same &= grid.contains( s );
			}
			for( Segment s : grid )
			{
				same &= brute.contains( s );
			}

			if( !same )
			{
				System.out.println( "not the same!" );
				System.out.println( "query " + queries[ i ] );
				System.out.println( "brute " + brute );
				System.out.println( "grid " + grid );
			}
			else
			{
				passed++;
			}
		}

		System.out.println( "found " + intersections + " intersections" );
		System.out.println( "passed " + passed + " out of " + queries.length );

		System.out.println( "measuring performance..." );
		for( int iter = 0; iter < 10; iter++ )
		{
			int i = 0;
			int brutecount = 0;
			long time = System.currentTimeMillis();

			while( System.currentTimeMillis() - time < 5000 )
			{
				brute.clear();

				for( int j = 0; j < segs.length; j++ )
				{
					if( queries[ i ].intersects( segs[ j ] ) )
					{
						brute.add( segs[ j ] );
					}
				}

				brutecount++;
				i++;
				i %= queries.length;
			}

			System.out.println( "brute force " + brutecount + " queries" );

			i = 0;
			int gridcount = 0;
			time = System.currentTimeMillis();

			while( System.currentTimeMillis() - time < 5000 )
			{
				grid.clear();

				bg.test( queries[ i ], grid );

				gridcount++;
				i++;
				i %= queries.length;
			}

			System.out.println( "bucket grid " + gridcount + " queries" );

			System.out.println( ( float ) gridcount / brutecount + " * speedup" );
		}
	}

}
