
package com.ryanm.util.geom.volume;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

/**
 * Calculates the intersection polygon between a defined volume and a
 * plane
 * 
 * @author ryanm
 */
public abstract class IntersectionVolume
{
	private static final Vector3f temp = new Vector3f();

	/**
	 * Volume vertices
	 */
	public final Vert[] verts;

	/**
	 * Volume edges
	 */
	public final Edge[] edges;

	/**
	 * Volume faces - must be convex polygons
	 */
	public final Face[] faces;

	/**
	 * Set to true if you're sure that the volume is convex - you'll
	 * get quicker intersection queries
	 */
	public boolean convex = false;

	/**
	 * Constructs the vertex, edge and face arrays. It's down to the
	 * subclass to fill in sensible values. Remember to keep faces flat
	 * and convex
	 * 
	 * @param vertCount
	 * @param edgeCount
	 * @param faceCount
	 */
	public IntersectionVolume( int vertCount, int edgeCount, int faceCount )
	{
		verts = new Vert[ vertCount ];
		for( int i = 0; i < verts.length; i++ )
		{
			verts[ i ] = new Vert();
		}

		edges = new Edge[ edgeCount ];
		faces = new Face[ faceCount ];
	}

	/**
	 * Sets the location of the vertices. Remember to keep faces flat
	 * and convex
	 * 
	 * @param v
	 *           desired vertex coordinates, in x,y,z,x,y,z,... order.
	 *           As many as possible of these will be used.
	 */
	public void setVerts( float... v )
	{
		assert v.length % 3 == 0;
		int i = 0;
		while( i < v.length && i < verts.length * 3 )
		{
			int index = i / 3;
			verts[ index ].x = v[ i++ ];
			verts[ index ].y = v[ i++ ];
			verts[ index ].z = v[ i++ ];
		}
	}

	/**
	 * Sets the location of the vertices. Remember to keep faces flat
	 * and convex
	 * 
	 * @param v
	 *           Desired vertex positions. As many as possible of these
	 *           will be used.
	 */
	public void setVerts( Vector3f... v )
	{
		int l = v.length;
		l = l > verts.length ? verts.length : l;

		for( int i = 0; i < l; i++ )
		{
			verts[ i ].x = v[ i ].x;
			verts[ i ].y = v[ i ].y;
			verts[ i ].z = v[ i ].z;
		}
	}

	/**
	 * Finds the vertices of intersections of the volume and a
	 * comparison plane
	 * 
	 * @param test
	 *           the value of the plane
	 * @return The ordered vertices of the intersection polygons
	 */
	private Vector3f[][] findIntersection( float test )
	{
		clearVisits();

		List<Vector3f> v = new ArrayList<Vector3f>();
		List<Vector3f[]> result = new ArrayList<Vector3f[]>();

		for( int i = 0; i < edges.length; i++ )
		{
			// find the first edge of a new intersection
			if( !edges[ i ].visited && edges[ i ].intersects( test ) )
			{
				Edge edge = edges[ i ];
				Face face = edge.adj[ 0 ];

				// walk the graph
				do
				{
					v.add( edge.intersection( test ) );
					edge.visited = true;

					// search the next face for the next edge
					face = edge.nextFace( face );
					edge = face.nextEdge( edge, test );
				}
				while( !edge.visited );

				assert v.size() > 0;
				result.add( v.toArray( new Vector3f[ v.size() ] ) );
				v.clear();

				if( convex )
				{ // there is only one intersection on convex volumes
					break;
				}
			}
		}

		return result.toArray( new Vector3f[ result.size() ][] );
	}

	/**
	 * Finds the vertices of the intersections of the volume and an
	 * arbitrary plane
	 * 
	 * @param point
	 *           a point on the plane
	 * @param normal
	 *           the plane normal (unit vector please)
	 * @return The vertices of the intersection polygons
	 */
	public Vector3f[][] findIntersection( Vector3f point, Vector3f normal )
	{
		// change comparison values to be displacement from plane
		for( int i = 0; i < verts.length; i++ )
		{
			temp.set( verts[ i ].x, verts[ i ].y, verts[ i ].z );
			Vector3f.sub( temp, point, temp );
			verts[ i ].value = Vector3f.dot( temp, normal );
		}

		return findIntersection( 0 );
	}

	/**
	 * Finds the intersections with an x-plane
	 * 
	 * @param x
	 *           the x-coordinate of the intersection vertices
	 * @return The vertices of the intersection polygons
	 */
	public Vector3f[][] findXPlaneIntersection( float x )
	{
		for( int i = 0; i < verts.length; i++ )
		{
			verts[ i ].value = verts[ i ].x;
		}

		return findIntersection( x );
	}

	/**
	 * Finds the intersections with a y-plane
	 * 
	 * @param y
	 *           the y-coordinate of the intersection vertices
	 * @return The vertices of the intersection polygons
	 */
	public Vector3f[][] findYPlaneIntersection( float y )
	{
		for( int i = 0; i < verts.length; i++ )
		{
			verts[ i ].value = verts[ i ].y;
		}

		return findIntersection( y );
	}

	/**
	 * Finds the intersections with a z-plane
	 * 
	 * @param z
	 *           the z-coordinate of the intersection vertices
	 * @return The vertices of the intersection polygons
	 */
	public Vector3f[][] findZPlaneIntersection( float z )
	{
		for( int i = 0; i < verts.length; i++ )
		{
			verts[ i ].value = verts[ i ].z;
		}

		return findIntersection( z );
	}

	private void clearVisits()
	{
		for( int i = 0; i < edges.length; i++ )
		{
			edges[ i ].visited = false;
		}
	}

	/**
	 * Volume surface face
	 * 
	 * @author ryanm
	 */
	public class Face
	{
		/**
		 * The {@link Edge}s surrounding this face
		 */
		public final Edge[] adj;

		/**
		 * Constructs a new {@link Face}
		 * 
		 * @param edgeIndices
		 *           the indices of the {@link Face}'s edges
		 */
		protected Face( int... edgeIndices )
		{
			adj = new Edge[ edgeIndices.length ];

			for( int i = 0; i < edgeIndices.length; i++ )
			{
				adj[ i ] = edges[ edgeIndices[ i ] ];
				adj[ i ].addFace( this );
			}
		}

		/**
		 * @param previous
		 *           The {@link Edge} that we're traversing from
		 * @param z
		 *           The z-value of the plane
		 * @return the next {@link Edge} to traverse
		 */
		private Edge nextEdge( Edge previous, float z )
		{
			for( int i = 0; i < adj.length; i++ )
			{
				if( adj[ i ] != previous && adj[ i ].intersects( z ) )
				{
					return adj[ i ];
				}
			}

			assert false;
			return null;
		}
	}

	/**
	 * Volume edge
	 * 
	 * @author ryanm
	 */
	public class Edge
	{
		/**
		 * First vertex
		 */
		public final Vert p;

		/**
		 * Second vertex
		 */
		public final Vert q;

		/**
		 * Adjacent faces
		 */
		public final Face[] adj = new Face[ 2 ];

		private boolean visited = false;

		/**
		 * Constructs a new edge
		 * 
		 * @param i
		 *           the index of the edge's first vertex
		 * @param j
		 *           the index of the edge's second index
		 */
		protected Edge( int i, int j )
		{
			p = verts[ i ];
			q = verts[ j ];
		}

		/**
		 * Call this to inform an {@link Edge} of it's adjacent
		 * {@link Face}s
		 * 
		 * @param f
		 */
		protected void addFace( Face f )
		{
			if( adj[ 0 ] == null )
			{
				adj[ 0 ] = f;
			}
			else if( adj[ 1 ] == null )
			{
				adj[ 1 ] = f;
			}
			else
			{ // this shouldn't be called more than twice
				assert false : indexOf( this, edges );
			}
		}

		private boolean intersects( float test )
		{
			// we don't want to intersect with an edge that lies parallel
			// to the intersection plane
			return p.value != q.value
					&& ( p.value <= test && test <= q.value || q.value <= test && test <= p.value );
		}

		private Vector3f intersection( float test )
		{
			float d = ( test - p.value ) / ( q.value - p.value );

			Vector3f v = new Vector3f( p.x, p.y, p.z );
			v.x += d * ( q.x - p.x );
			v.y += d * ( q.y - p.y );
			v.z += d * ( q.z - p.z );

			return v;
		}

		/**
		 * @param previous
		 *           The {@link Face} that are traversing from
		 * @return the next {@link Face} to traverse
		 */
		private Face nextFace( Face previous )
		{
			if( previous == adj[ 0 ] )
			{
				return adj[ 1 ];
			}
			else
			{
				assert previous == adj[ 1 ];
				return adj[ 0 ];
			}
		}

		@Override
		public String toString()
		{
			return p + "-" + q;
		}
	}

	/**
	 * Volume vertex
	 * 
	 * @author ryanm
	 */
	public class Vert
	{
		/**
		 * Vertex x coordinate
		 */
		public float x;

		/**
		 * Vertex x coordinate
		 */
		public float y;

		/**
		 * Vertex x coordinate
		 */
		public float z;

		/**
		 * The value that we test and lerp against
		 */
		private float value;

		/**
		 * No one else should be constructing these
		 */
		private Vert()
		{
		}

		/**
		 * @param x
		 * @param y
		 * @param z
		 */
		public void set( float x, float y, float z )
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String toString()
		{
			return "(" + x + "," + y + "," + z + ")";
		}
	}

	private int indexOf( Object o, Object[] array )
	{
		for( int i = 0; i < array.length; i++ )
		{
			if( o == array[ i ] )
			{
				return i;
			}
		}

		return -1;
	}
}