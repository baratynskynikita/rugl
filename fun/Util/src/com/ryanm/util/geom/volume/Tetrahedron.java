
package com.ryanm.util.geom.volume;

/**
 * @author ryanm
 */
public class Tetrahedron extends IntersectionVolume
{
	/***/
	public Tetrahedron()
	{
		super( 4, 6, 4 );

		// nbr, ntl, fbl, ftr
		setVerts( 1, -1, 1, -1, 1, 1, -1, -1, -1, 1, 1, -1 );

		edges[ 0 ] = new Edge( 0, 1 );
		edges[ 1 ] = new Edge( 0, 2 );
		edges[ 2 ] = new Edge( 1, 2 );
		edges[ 3 ] = new Edge( 0, 3 );
		edges[ 4 ] = new Edge( 1, 3 );
		edges[ 5 ] = new Edge( 2, 3 );

		faces[ 0 ] = new Face( 2, 4, 5 );
		faces[ 1 ] = new Face( 1, 3, 5 );
		faces[ 2 ] = new Face( 0, 3, 4 );
		faces[ 3 ] = new Face( 0, 1, 2 );

		// there's no such thing as a non-convex tetrahedron
		convex = true;
	}
}
