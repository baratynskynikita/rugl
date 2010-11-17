
package com.ryanm.droid.rugl.geom.line;

import java.util.List;

import com.ryanm.droid.rugl.util.geom.LineUtils;
import com.ryanm.droid.rugl.util.geom.Vector2f;


/**
 * A simple bevelled join
 * 
 * @author ryanm
 */
public class BevelJoin implements LineJoin
{

	@Override
	public void createVerts( Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner,
			List<Vector2f> verts, List<Short> indices )
	{
		Vector2f e1 = Vector2f.sub( v1, join, null );
		e1.scale( 0.5f );
		Vector2f.add( corner, e1, e1 );

		Vector2f e2 = Vector2f.sub( v2, join, null );
		e2.scale( 0.5f );
		Vector2f.add( corner, e2, e2 );

		Short i1 = new Short( ( short ) ( verts.size() - 3 ) );
		Short ij = new Short( ( short ) ( verts.size() - 2 ) );
		Short i2 = new Short( ( short ) ( verts.size() - 1 ) );

		Short b1 = new Short( ( short ) verts.size() );
		verts.add( e1 );
		Short b2 = new Short( ( short ) verts.size() );
		verts.add( e2 );

		int ccw = LineUtils.relativeCCW( v1, join, v2 );

		addTriangle( ij, i1, b1, ccw, indices );

		addTriangle( ij, b1, b2, ccw, indices );

		addTriangle( ij, b2, i2, ccw, indices );
	}

	private void addTriangle( Short i1, Short i2, Short i3, int ccw, List<Short> indices )
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
