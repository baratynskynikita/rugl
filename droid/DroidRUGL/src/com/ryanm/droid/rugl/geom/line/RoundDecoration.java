
package com.ryanm.droid.rugl.geom.line;

import java.util.List;

import com.ryanm.droid.rugl.util.geom.LineUtils;
import com.ryanm.droid.rugl.util.geom.Vector2f;
import com.ryanm.droid.rugl.util.geom.VectorUtils;


/**
 * Rounded line decorations
 * 
 * @author ryanm
 */
public class RoundDecoration implements LineCap, LineJoin
{
	/**
	 * The maximum length of the segments used to form the rounded bits
	 */
	public float maxSegmentLength = 10;

	@Override
	public void createVerts( Vector2f endPoint, Vector2f lineDirection, short leftIndex,
			short rightIndex, float width, List<Vector2f> verts, List<Short> indices )
	{
		float c = ( float ) ( Math.PI * width / 2.0f );
		int segments = ( int ) Math.ceil( c / maxSegmentLength );

		VectorUtils.rotate90( lineDirection );
		lineDirection.scale( width / 2 );

		float angleIncrement = ( float ) ( Math.PI / segments );

		Short left = new Short( leftIndex );
		Short prev = null;
		for( int i = 0; i < segments - 1; i++ )
		{
			VectorUtils.rotate( lineDirection, angleIncrement );

			verts.add( Vector2f.add( endPoint, lineDirection, null ) );
			Short index = new Short( ( short ) ( verts.size() - 1 ) );

			if( prev != null )
			{
				indices.add( left );
				indices.add( prev );
				indices.add( index );
			}

			prev = index;
		}

		if( prev != null )
		{
			indices.add( left );
			indices.add( prev );
			indices.add( new Short( rightIndex ) );
		}
	}

	@Override
	public void createVerts( Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner,
			List<Vector2f> verts, List<Short> indices )
	{
		Short i1 = new Short( ( short ) ( verts.size() - 3 ) );
		Short root = new Short( ( short ) ( verts.size() - 2 ) );
		Short i2 = new Short( ( short ) ( verts.size() - 1 ) );

		Vector2f n1 = Vector2f.sub( v1, join, null );
		n1.scale( 0.5f );

		Vector2f n2 = Vector2f.sub( v2, join, null );
		n2.scale( 0.5f );

		int ccw = LineUtils.relativeCCW( v1, join, v2 );

		float angle = Vector2f.angle( n1, n2 );

		float circ = ( float ) ( n1.length() * 2 * Math.PI * angle / ( Math.PI * 2 ) );
		int segments = ( int ) Math.ceil( circ / maxSegmentLength );
		float angleIncrement = ccw * angle / segments;

		Short prev = new Short( ( short ) verts.size() );

		verts.add( Vector2f.add( corner, n1, null ) );

		addTriangle( root, i1, prev, ccw, indices );

		for( int i = 0; i < segments - 1; i++ )
		{
			VectorUtils.rotate( n1, angleIncrement );
			verts.add( Vector2f.add( corner, n1, null ) );
			Short current = new Short( ( short ) ( verts.size() - 1 ) );

			addTriangle( root, prev, current, ccw, indices );

			prev = current;
		}

		verts.add( Vector2f.add( corner, n2, null ) );
		Short j = new Short( ( short ) ( verts.size() - 1 ) );

		addTriangle( root, prev, j, ccw, indices );

		addTriangle( root, j, i2, ccw, indices );
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
