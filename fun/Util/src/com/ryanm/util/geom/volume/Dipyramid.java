
package com.ryanm.util.geom.volume;

/**
 * A dipyramid volume. The first and second vertices are the pyramid
 * peaks
 * 
 * @author ryanm
 */
public class Dipyramid extends IntersectionVolume
{
	/**
	 * @param n
	 *           the number of base sides
	 */
	public Dipyramid( int n )
	{
		super( n + 2, 3 * n, 2 * n );
		assert n >= 2;

		verts[ 0 ].set( 0, 1, 0 );
		verts[ 1 ].set( 0, -1, 0 );

		for( int i = 0; i < n; i++ )
		{
			verts[ i + 2 ].set( ( float ) Math.cos( 2 * Math.PI * i / n ), 0,
					( float ) Math.sin( 2 * Math.PI * i / n ) );
		}

		int edgeIndex = 0;
		for( int i = 0; i < n; i++ )
		{
			// top to rim
			edges[ edgeIndex++ ] = new Edge( 0, i + 2 );
			// rim to next rim
			edges[ edgeIndex++ ] = new Edge( i + 2, ( i + 1 ) % n + 2 );
			// bottom to rim. how rude.
			edges[ edgeIndex++ ] = new Edge( 1, i + 2 );
		}

		int faceIndex = 0;
		for( int i = 0; i < n; i++ )
		{
			int fi = 3 * i;
			faces[ faceIndex++ ] = new Face( fi, fi + 1, ( fi + 3 ) % edges.length );
			faces[ faceIndex++ ] = new Face( fi + 2, fi + 1, ( fi + 5 ) % edges.length );
		}
	}
}
