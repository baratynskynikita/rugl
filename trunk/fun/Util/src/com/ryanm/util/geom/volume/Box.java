
package com.ryanm.util.geom.volume;

import org.lwjgl.util.vector.Vector3f;

/**
 * A volume topographically similar to a cube. Vertices should be set
 * in <code>nbl, btl, nbr, ntr, fbl, ftl, fbr, ftr</code> order, for
 * (n)ear, (f)ar, (t)op, (b)ottom, (l)eft (r)ight surfaces
 */
public class Box extends IntersectionVolume
{
	private final Vector3f n23 = new Vector3f();

	private final Vector3f n31 = new Vector3f();

	private final Vector3f n12 = new Vector3f();

	/**
	 * Defaults to be am axis-aligned 2-unit cube, centred on the
	 * origin
	 */
	public Box()
	{
		super( 8, 12, 6 );

		setVerts( -1, -1, 1, -1, 1, 1, 1, -1, 1, 1, 1, 1, -1, -1, -1, -1, 1, -1, 1, -1, -1, 1, 1, -1 );

		edges[ 0 ] = new Edge( 1, 3 ); // nt
		edges[ 1 ] = new Edge( 0, 2 ); // nb
		edges[ 2 ] = new Edge( 2, 3 ); // nr
		edges[ 3 ] = new Edge( 0, 1 ); // nl

		edges[ 4 ] = new Edge( 5, 7 ); // ft
		edges[ 5 ] = new Edge( 4, 6 ); // fb
		edges[ 6 ] = new Edge( 6, 7 ); // fr
		edges[ 7 ] = new Edge( 4, 5 ); // fl

		edges[ 8 ] = new Edge( 3, 7 ); // tr
		edges[ 9 ] = new Edge( 1, 5 ); // tl

		edges[ 10 ] = new Edge( 2, 6 ); // br
		edges[ 11 ] = new Edge( 0, 4 ); // bl

		// near
		faces[ 0 ] = new Face( 0, 1, 2, 3 );
		// top
		faces[ 1 ] = new Face( 0, 4, 8, 9 );
		// right
		faces[ 2 ] = new Face( 2, 6, 8, 10 );
		// left
		faces[ 3 ] = new Face( 3, 7, 9, 11 );
		// bottom
		faces[ 4 ] = new Face( 1, 5, 10, 11 );
		// far
		faces[ 5 ] = new Face( 4, 5, 6, 7 );
	}

	/**
	 * Updates the box vertices according to the planes that its faces
	 * lie on
	 * 
	 * @param nearPoint
	 *           a point on the near plane
	 * @param nearNormal
	 *           the near plane normal (normalised please)
	 * @param topPoint
	 *           etc
	 * @param topNormal
	 * @param rightPoint
	 * @param rightNormal
	 * @param leftPoint
	 * @param leftNormal
	 * @param bottomPoint
	 * @param bottomNormal
	 * @param farPoint
	 * @param farNormal
	 */
	public void setVertsFromPlanes( Vector3f nearPoint, Vector3f nearNormal, Vector3f topPoint,
			Vector3f topNormal, Vector3f rightPoint, Vector3f rightNormal, Vector3f leftPoint,
			Vector3f leftNormal, Vector3f bottomPoint, Vector3f bottomNormal, Vector3f farPoint,
			Vector3f farNormal )
	{
		float nd = Vector3f.dot( nearNormal, nearPoint );
		float td = Vector3f.dot( topNormal, topPoint );
		float bd = Vector3f.dot( bottomNormal, bottomPoint );
		float rd = Vector3f.dot( rightNormal, rightPoint );
		float ld = Vector3f.dot( leftNormal, leftPoint );
		float fd = Vector3f.dot( farNormal, farPoint );

		intersection( nearNormal, nd, bottomNormal, bd, leftNormal, ld, verts[ 0 ] );
		intersection( nearNormal, nd, topNormal, td, leftNormal, ld, verts[ 1 ] );
		intersection( nearNormal, nd, bottomNormal, bd, rightNormal, rd, verts[ 2 ] );
		intersection( nearNormal, nd, topNormal, td, rightNormal, rd, verts[ 3 ] );

		intersection( farNormal, fd, bottomNormal, bd, leftNormal, ld, verts[ 4 ] );
		intersection( farNormal, fd, topNormal, td, leftNormal, ld, verts[ 5 ] );
		intersection( farNormal, fd, bottomNormal, bd, rightNormal, rd, verts[ 6 ] );
		intersection( farNormal, fd, topNormal, td, rightNormal, rd, verts[ 7 ] );

		convex = true;
	}

	/**
	 * Calculates a plane-plane-plane intersection
	 * 
	 * @param n1
	 *           the first plane's normal
	 * @param d1
	 *           the displacement of the plane from the origin
	 * @param n2
	 *           and so on
	 * @param d2
	 * @param n3
	 * @param d3
	 * @param result
	 *           where to put the result
	 */
	private void intersection( Vector3f n1, float d1, Vector3f n2, float d2, Vector3f n3, float d3,
			Vert result )
	{
		Vector3f.cross( n2, n3, n23 );
		Vector3f.cross( n3, n1, n31 );
		Vector3f.cross( n1, n2, n12 );

		float f = Vector3f.dot( n1, n23 );

		assert f != 0 : "Coplanar surfaces";

		result.x = ( d1 * n23.x + d2 * n31.x + d3 * n12.x ) / f;
		result.y = ( d1 * n23.y + d2 * n31.y + d3 * n12.y ) / f;
		result.z = ( d1 * n23.z + d2 * n31.z + d3 * n12.z ) / f;
	}

}
