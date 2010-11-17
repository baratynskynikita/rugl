
package com.ryanm.util.geom;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.util.Util;

/**
 * @author ryanm
 */
public class GeomUtils
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		Random rng = new Random();

		float[] p = new float[ 100 ];
		for( int i = 0; i < p.length; i++ )
		{
			p[ i ] = 50 + 300 * rng.nextFloat();
		}

		float[] c = minimumEnclosingtCircle( p );

		for( int i = 0; i < p.length; i += 2 )
		{
			if( !contains( c, p[ i ], p[ i + 1 ] ) )
			{
				System.out.println( "error" );
			}
		}

		BufferedImage bi = new BufferedImage( 400, 400, BufferedImage.TYPE_INT_ARGB );

		Graphics2D g = bi.createGraphics();
		g.setColor( Color.white );
		g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
		g.setColor( new Color( 0, 0, 0, 64 ) );

		g.setColor( Color.red );
		for( int i = 0; i < p.length; i += 2 )
		{
			g.drawRect( ( int ) p[ i ] - 1, ( int ) p[ i + 1 ] - 1, 2, 2 );
		}

		g.setColor( Color.blue );
		g.drawRect( ( int ) c[ 0 ] - 1, ( int ) c[ 1 ] - 1, 2, 2 );
		g.drawOval( ( int ) ( c[ 0 ] - c[ 2 ] ), ( int ) ( c[ 1 ] - c[ 2 ] ), ( int ) ( 2 * c[ 2 ] ),
				( int ) ( 2 * c[ 2 ] ) );

		try
		{
			ImageIO.write( bi, "PNG", new File( "circle.png" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	private static final float[] circle = new float[ 3 ];

	/**
	 * @param points
	 * @return The [x,y,radius] of the minimum enclosing circle of the
	 *         points
	 */
	public static float[] minimumEnclosingtCircle( float[] points )
	{
		Vector2f[] verts = new Vector2f[ points.length / 2 ];
		for( int i = 0; i < points.length; i += 2 )
		{
			verts[ i / 2 ] = new Vector2f( points[ i ], points[ i + 1 ] );
		}

		Util.shuffle( verts, new Random() );

		Vector2f[] boundary = new Vector2f[ 3 ];

		Arrays.fill( circle, 0 );

		mec( verts, verts.length, boundary, 0 );

		return circle.clone();
	}

	private static float[] mec( Vector2f[] points, int n, Vector2f[] boundary, int b )
	{
		float[] localCircle = null;

		// terminal cases
		if( b == 3 )
		{
			localCircle = calcCircle( boundary[ 0 ], boundary[ 1 ], boundary[ 2 ] );
		}
		else if( n == 1 && b == 0 )
		{
			localCircle = new float[] { points[ 0 ].x, points[ 0 ].y, 0 };
		}
		else if( n == 0 && b == 2 )
		{
			localCircle = calcCircle( boundary[ 0 ], boundary[ 1 ] );
		}
		else if( n == 1 && b == 1 )
		{
			localCircle = calcCircle( boundary[ 0 ], points[ 0 ] );
		}
		else
		{
			localCircle = mec( points, n - 1, boundary, b );
			if( !contains( localCircle, points[ n - 1 ].x, points[ n - 1 ].y ) )
			{
				boundary[ b++ ] = points[ n - 1 ];
				localCircle = mec( points, n - 1, boundary, b );
			}
		}
		if( localCircle != null )
		{
			for( int i = 0; i < localCircle.length; i++ )
			{
				circle[ i ] = localCircle[ i ];
			}
		}
		return localCircle;
	}

	private static boolean contains( float[] c, float x, float y )
	{
		float dx = c[ 0 ] - x;
		float dy = c[ 1 ] - y;

		return dx * dx + dy * dy <= c[ 2 ] * c[ 2 ];
	}

	private static float[] calcCircle( Vector2f p1, Vector2f p2, Vector2f p3 )
	{
		float p1x = p1.x;
		float p1y = p1.y;
		float p2x = p2.x;
		float p2y = p2.y;
		float p3x = p3.x;
		float p3y = p3.y;

		float a = p2x - p1x;
		float b = p2y - p1y;
		float c = p3x - p1x;
		float d = p3y - p1y;
		float e = a * ( p2x + p1x ) * 0.5f + b * ( p2y + p1y ) * 0.5f;
		float f = c * ( p3x + p1x ) * 0.5f + d * ( p3y + p1y ) * 0.5f;
		float det = a * d - b * c;

		float cx = ( d * e - b * f ) / det;
		float cy = ( -c * e + a * f ) / det;

		return new float[] { cx, cy,
				( float ) Math.sqrt( ( p1x - cx ) * ( p1x - cx ) + ( p1y - cy ) * ( p1y - cy ) ) };
	}

	private static float[] calcCircle( Vector2f p1, Vector2f p2 )
	{
		float p1x = p1.x;
		float p1y = p1.y;
		float p2x = p2.x;
		float p2y = p2.y;

		float cx = 0.5f * ( p1x + p2x );
		float cy = 0.5f * ( p1y + p2y );

		return new float[] { cx, cy,
				( float ) Math.sqrt( ( p1x - cx ) * ( p1x - cx ) + ( p1y - cy ) * ( p1y - cy ) ) };
	}

	/**
	 * @param input
	 * @param minFeature
	 * @return a decimated vertex array
	 */
	public static float[] decimate( float[] input, float minFeature )
	{
		// build vertex list
		ArrayList<Vector2f> verts = new ArrayList<Vector2f>();
		for( int i = 0; i < input.length; i += 2 )
		{
			verts.add( new Vector2f( input[ i ], input[ i + 1 ] ) );
		}

		assert !selfIntersects( verts );

		for( int i = 1; i < verts.size() - 1; i++ )
		{
			int base = i - 1;
			int test = i;

			// find a run of adjacent verts that can be approximated with
			// a straight line
			while( test < verts.size() && testPoints( verts, base, test, minFeature ) )
			{
				test++;
			}

			// remove the unnecessary points
			verts.subList( base + 1, test - 2 ).clear();
		}

		assert !selfIntersects( verts );

		float[] output = new float[ 2 * verts.size() ];
		int vi = 0;
		for( int i = 0; i < verts.size(); i++ )
		{
			output[ vi++ ] = verts.get( i ).x;
			output[ vi++ ] = verts.get( i ).y;
		}

		return output;
	}

	private static boolean selfIntersects( ArrayList<Vector2f> verts )
	{
		for( int i = 0; i < verts.size(); i++ )
		{
			Vector2f a = verts.get( i );
			Vector2f b = verts.get( ( i + 1 ) % verts.size() );

			for( int j = i + 2; j < verts.size(); j++ )
			{
				Vector2f c = verts.get( j % verts.size() );
				Vector2f d = verts.get( ( j + 1 ) % verts.size() );

				if( LineUtils.segmentsIntersect( a, b, c, d ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	private static boolean testPoints( ArrayList<Vector2f> verts, int base, int test, float limit )
	{
		Vector2f bp = verts.get( base % verts.size() );
		Vector2f tp = verts.get( test % verts.size() );

		// test all vertices between base and test to see if they lie
		// outwith the threshold distance to the line between base and
		// test
		for( int i = base + 1; i < test - 1; i++ )
		{
			Vector2f ip = verts.get( i );

			float d = VectorUtils.distance( LineUtils.closestPointOnLine( ip, bp, tp ), ip );
			if( d > limit )
			{
				return false;
			}
		}

		return true;
	}

}
