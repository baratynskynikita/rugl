
package com.rugl.geom.line;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.ryanm.util.geom.LineUtils;

/**
 * A simple bevelled join
 * 
 * @author ryanm
 */
public class BevelJoin implements LineJoin
{

	@Override
	public void createVerts( Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner,
			List<Vector2f> verts, List<Integer> indices )
	{
		Vector2f e1 = Vector2f.sub( v1, join, null );
		e1.scale( 0.5f );
		Vector2f.add( corner, e1, e1 );

		Vector2f e2 = Vector2f.sub( v2, join, null );
		e2.scale( 0.5f );
		Vector2f.add( corner, e2, e2 );

		Integer i1 = new Integer( verts.size() - 3 );
		Integer ij = new Integer( verts.size() - 2 );
		Integer i2 = new Integer( verts.size() - 1 );

		Integer b1 = new Integer( verts.size() );
		verts.add( e1 );
		Integer b2 = new Integer( verts.size() );
		verts.add( e2 );

		int ccw = LineUtils.relativeCCW( v1, join, v2 );

		addTriangle( ij, i1, b1, ccw, indices );

		addTriangle( ij, b1, b2, ccw, indices );

		addTriangle( ij, b2, i2, ccw, indices );
	}

	private void addTriangle( Integer i1, Integer i2, Integer i3, int ccw,
			List<Integer> indices )
	{
		if( ccw == 1 )
		{
			indices.add( i1 );
			indices.add( i2 );
			indices.add( i3 );
		}
		else
		{
			indices.add( i1 );
			indices.add( i3 );
			indices.add( i2 );
		}
	}
}
