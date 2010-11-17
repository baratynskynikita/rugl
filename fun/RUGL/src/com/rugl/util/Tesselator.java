
package com.rugl.util;

import java.util.Arrays;

import org.lwjgl.util.vector.Vector3f;

import com.rugl.geom.Shape;
import com.ryanm.util.geom.LineUtils;

/**
 * Builds shapes with a simple ear-pruning technique
 * 
 * @author ryanm
 */
public class Tesselator
{
	/**
	 * Builds a shape from a sequence of points
	 * 
	 * @param verts
	 *           The vertices of the shape
	 * @param z
	 *           The z-coordinate of the shape
	 * @return the {@link Shape}
	 */
	public static Shape tesselate( float[] verts, float z )
	{
		Vector3f[] v = new Vector3f[ verts.length / 2 ];

		int vi = 0;
		for( int i = 0; i < verts.length; i += 2 )
		{
			v[ vi++ ] = new Vector3f( verts[ i ], verts[ i + 1 ], z );
		}

		Vector3f[] vList = buildCounterList( v );
		boolean[] used = new boolean[ vList.length ];
		Arrays.fill( used, false );

		v = null;

		int[] tris = new int[ ( vList.length - 2 ) * 3 ];

		int ti = 0;

		int p = 0;
		int c = 1;
		int n = 2;

		while( ti < tris.length - 1 )
		{
			// System.out.println( ti / 3 );
			assert p != c;
			assert p != n;
			assert c != n;

			boolean isEar = isConcave( vList[ p ], vList[ c ], vList[ n ] );

			for( int i = 0; i < vList.length && isEar; i++ )
			{
				if( !used[ i ] && i != p && i != c && i != n )
				{
					isEar &= !contains( vList[ p ], vList[ c ], vList[ n ], vList[ i ] );
				}
			}

			if( isEar )
			{
				tris[ ti++ ] = p;
				tris[ ti++ ] = c;
				tris[ ti++ ] = n;

				used[ c ] = true;

				c = n;
				n = next( used, n );
			}
			else
			{
				p = c;
				c = n;
				n = next( used, n );
			}
		}

		return new Shape( Shape.extract( vList ), tris );
	}

	private static int next( boolean[] used, int index )
	{
		int count = 0;

		do
		{
			index = ( index + 1 ) % used.length;

			count++;
			assert count < used.length;
		}
		while( used[ index ] );

		return index;
	}

	private static boolean contains( Vector3f a, Vector3f b, Vector3f c, Vector3f p )
	{
		assert LineUtils.relativeCCW( a.x, a.y, b.x, b.y, c.x, c.y ) <= 0;

		if( LineUtils.relativeCCW( a.x, a.y, b.x, b.y, p.x, p.y ) == -1 )
		{
			if( LineUtils.relativeCCW( b.x, b.y, c.x, c.y, p.x, p.y ) == -1 )
			{
				if( LineUtils.relativeCCW( c.x, c.y, a.x, a.y, p.x, p.y ) == -1 )
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isConcave( Vector3f p, Vector3f c, Vector3f n )
	{
		int ccw = LineUtils.relativeCCW( p.x, p.y, c.x, c.y, n.x, n.y );

		return ccw <= 0;
	}

	private static Vector3f[] buildCounterList( Vector3f[] v )
	{
		Vector3f[] vList = new Vector3f[ v.length ];

		// determine winding order
		boolean counter = traverseOrder( v );

		if( counter )
		{
			for( int i = 0; i < v.length; i++ )
			{
				vList[ i ] = v[ i ];
			}
		}
		else
		{
			int li = 0;
			for( int i = v.length - 1; i >= 0; i-- )
			{
				vList[ li++ ] = v[ i ];
			}
		}
		return vList;
	}

	/**
	 * @param v
	 * @return true if vertices are anti-clockwise
	 */
	private static boolean traverseOrder( Vector3f[] v )
	{
		float area = v[ v.length - 1 ].x * v[ 0 ].y - v[ 0 ].x * v[ v.length - 1 ].y;

		for( int i = 0; i < v.length - 1; i++ )
		{
			area += v[ i ].x * v[ i + 1 ].y - v[ i + 1 ].x * v[ i ].y;
		}

		return area >= 0.0;
	}
}
