
package com.rugl.geom.line;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.util.geom.VectorUtils;

/**
 * Decorates the line end as if the end point had a width-dimension
 * square on it, aligned with the line direction
 * 
 * @author ryanm
 */
public class SquareCap implements LineCap
{

	@Override
	public void createVerts( Vector2f endPoint, Vector2f lineDirection, int leftIndex,
			int rightIndex, float width, List<Vector2f> verts, List<Integer> indices )
	{
		lineDirection.scale( 0.5f * width );

		VectorUtils.rotate90( lineDirection );

		Vector2f l = Vector2f.add( endPoint, lineDirection, null );

		VectorUtils.rotate90( lineDirection );

		l = Vector2f.add( l, lineDirection, l );

		VectorUtils.rotate90( lineDirection );
		lineDirection.scale( 2 );

		Vector2f r = Vector2f.add( l, lineDirection, null );

		Integer li = new Integer( verts.size() );
		verts.add( l );

		Integer ri = new Integer( verts.size() );
		verts.add( r );

		Integer eli = new Integer( leftIndex );
		Integer eri = new Integer( rightIndex );

		indices.add( eli );
		indices.add( li );
		indices.add( eri );

		indices.add( eri );
		indices.add( li );
		indices.add( ri );
	}
}
