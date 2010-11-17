
package com.rugl.geom;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.ryanm.util.geom.BoundingCuboid;
import com.ryanm.util.geom.TriangleUtils;
import com.ryanm.util.io.DataSink;
import com.ryanm.util.io.DataSource;
import com.ryanm.util.io.SerialUtils;

/**
 * Encapsulates the geometry of a shape
 * 
 * @author ryanm
 */
public class Shape implements Cloneable
{
	/**
	 * The vertices of the shape, in xyz order
	 */
	public final float[] vertices;

	/**
	 * The indices of triangle vertices - points to the x-coordinate
	 */
	public final int[] triangles;

	private final BoundingCuboid bounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private boolean boundsDirty = true;

	private boolean sanity()
	{
		assert vertices.length >= 9 : toString();
		assert vertices.length % 3 == 0 : toString();
		assert triangles.length >= 3 : toString();

		for( int i = 0; i < triangles.length; i++ )
		{
			if( triangles[ i ] < 0 || triangles[ i ] >= vertexCount() )
			{
				assert false : triangles[ i ] + "\n" + toString();
			}
		}

		return true;
	}

	/**
	 * Shallow copy constructor
	 * 
	 * @param s
	 *           The shape to copy
	 */
	public Shape( Shape s )
	{
		vertices = s.vertices;
		triangles = s.triangles;
	}

	/**
	 * Builds a new {@link Shape}
	 * 
	 * @param vertices
	 * @param triangles
	 */
	public Shape( float[] vertices, int[] triangles )
	{
		this.vertices = vertices;
		this.triangles = triangles;

		assert sanity();
	}

	/**
	 * @param data
	 */
	public Shape( DataSource data )
	{
		vertices = SerialUtils.readFloatArray( data );
		triangles = SerialUtils.readIntArray( data );

		assert sanity();
	}

	/**
	 * @param sink
	 */
	public void write( DataSink sink )
	{
		SerialUtils.write( vertices, sink );
		SerialUtils.write( triangles, sink );
	}

	/**
	 * @param verts
	 * @return a vertex array
	 */
	public static float[] extract( Vector3f[] verts )
	{
		float[] v = new float[ 3 * verts.length ];
		for( int i = 0; i < verts.length; i++ )
		{
			v[ 3 * i ] = verts[ i ].x;
			v[ 3 * i + 1 ] = verts[ i ].y;
			v[ 3 * i + 2 ] = verts[ i ].z;
		}
		return v;
	}

	/**
	 * @param verts
	 * @param z
	 * @return a vertex array
	 */
	public static float[] extract( Vector2f[] verts, float z )
	{
		float[] v = new float[ 3 * verts.length ];
		for( int i = 0; i < verts.length; i++ )
		{
			v[ 3 * i ] = verts[ i ].x;
			v[ 3 * i + 1 ] = verts[ i ].y;
			v[ 3 * i + 2 ] = z;
		}
		return v;
	}

	/**
	 * @param verts
	 * @return a texture coordinate array
	 */
	public static float[] extract( Vector2f[] verts )
	{
		float[] v = new float[ 2 * verts.length ];
		for( int i = 0; i < verts.length; i++ )
		{
			v[ 2 * i ] = verts[ i ].x;
			v[ 2 * i + 1 ] = verts[ i ].y;
		}
		return v;
	}

	/**
	 * @param verts
	 * @return a vertex array
	 */
	public static float[] extract( List<Vector3f> verts )
	{
		float[] va = new float[ 3 * verts.size() ];
		int vi = 0;
		for( Vector3f v : verts )
		{
			va[ vi++ ] = v.x;
			va[ vi++ ] = v.y;
			va[ vi++ ] = v.z;
		}
		return va;
	}

	/**
	 * @param verts
	 * @param z
	 * @return a vertex array
	 */
	public static float[] extractVertices( List<Vector2f> verts, float z )
	{
		float[] va = new float[ 3 * verts.size() ];
		int vi = 0;
		for( Vector2f v : verts )
		{
			va[ vi++ ] = v.x;
			va[ vi++ ] = v.y;
			va[ vi++ ] = z;
		}
		return va;
	}

	/**
	 * Extracts a triangle index list
	 * 
	 * @param indexes
	 * @return The triangle index array
	 */
	public static int[] extractIndices( List<Integer> indexes )
	{
		int[] ia = new int[ indexes.size() ];
		int j = 0;
		for( Integer i : indexes )
		{
			ia[ j++ ] = i.intValue();
		}
		return ia;
	}

	/**
	 * Expands a colour into a colour array. Every element of the array
	 * references the same colour object
	 * 
	 * @param c
	 * @param n
	 * @return A colour array
	 */
	public static int[] expand( int c, int n )
	{
		int[] ca = new int[ n ];
		Arrays.fill( ca, c );

		return ca;
	}

	/**
	 * @param cl
	 * @return A colour array
	 */
	public static Color[] extractColours( List<Color> cl )
	{
		return cl.toArray( new Color[ cl.size() ] );
	}

	/**
	 * @return the number of vertices
	 */
	public int vertexCount()
	{
		return vertices.length / 3;
	}

	/**
	 * @param index
	 * @param v
	 * @return v, filled with the appropriate data
	 */
	public Vector3f getVertex( int index, Vector3f v )
	{
		if( v == null )
		{
			v = new Vector3f();
		}

		v.set( vertices[ 3 * index ], vertices[ 3 * index + 1 ], vertices[ 3 * index + 2 ] );

		return v;
	}

	/**
	 * Transforms this shape
	 * 
	 * @param m
	 *           The transformation
	 * @return this
	 */
	public Shape transform( Matrix4f m )
	{
		Vector4f v = new Vector4f();

		for( int i = 0; i < vertices.length; i += 3 )
		{
			v.set( vertices[ i ], vertices[ i + 1 ], vertices[ i + 2 ], 1 );
			Matrix4f.transform( m, v, v );
			vertices[ i ] = v.x;
			vertices[ i + 1 ] = v.y;
			vertices[ i + 2 ] = v.z;
		}

		boundsDirty = true;

		return this;
	}

	/**
	 * Gets the axis-aligned bounds for this {@link Shape}.
	 * 
	 * @return The bounds of this shape
	 */
	public BoundingCuboid getBounds()
	{
		if( boundsDirty )
		{
			recomputeBounds();
		}

		return bounds;
	}

	/**
	 * Recomputes the bounds. Only necessary if you monkey with the
	 * vertices manually, the methods that alter the vertices in this
	 * class take care of this for you
	 */
	public void recomputeBounds()
	{
		bounds.x.set( vertices[ 0 ], vertices[ 0 ] );
		bounds.y.set( vertices[ 1 ], vertices[ 1 ] );
		bounds.z.set( vertices[ 2 ], vertices[ 2 ] );

		for( int i = 0; i < vertices.length; i += 3 )
		{
			bounds.encompass( vertices[ i ], vertices[ i + 1 ], vertices[ i + 2 ] );
		}

		boundsDirty = false;
	}

	/**
	 * Translates the {@link Shape}
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this
	 */
	public Shape translate( float x, float y, float z )
	{
		for( int i = 0; i < vertices.length; i += 3 )
		{
			vertices[ i + 0 ] += x;
			vertices[ i + 1 ] += y;
			vertices[ i + 2 ] += z;
		}

		bounds.translate( x, y, z );

		return this;
	}

	/**
	 * Scales the {@link Shape}
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this
	 */
	public Shape scale( float x, float y, float z )
	{
		for( int i = 0; i < vertices.length; i += 3 )
		{
			vertices[ i + 0 ] *= x;
			vertices[ i + 1 ] *= y;
			vertices[ i + 2 ] *= z;
		}

		bounds.scale( x, y, z );

		return this;
	}

	/**
	 * @param x
	 * @param y
	 * @return <code>true</code> if the specified point lies on the
	 *         interior of this shape, as projected onto the z = 0
	 *         plane
	 */
	public boolean contains( float x, float y )
	{
		for( int i = 0; i < triangles.length; i += 3 )
		{
			int a = triangles[ i ] * 3;
			int b = triangles[ i + 1 ] * 3;
			int c = triangles[ i + 2 ] * 3;

			if( TriangleUtils.contains( x, y, vertices[ a ], vertices[ a + 1 ],
					vertices[ b ], vertices[ b + 1 ], vertices[ c ], vertices[ c + 1 ] ) )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "Shape " );
		buff.append( vertexCount() );
		buff.append( " verts " );
		buff.append( triangles.length / 3.0 );
		buff.append( " tris" );

		for( int i = 0; i < vertices.length; i += 3 )
		{
			buff.append( "\n\t" );
			buff.append( vertices[ i ] );
			buff.append( ", " );
			buff.append( vertices[ i + 1 ] );
			buff.append( ", " );
			buff.append( vertices[ i + 2 ] );
		}

		for( int i = 0; i < triangles.length; i += 3 )
		{
			buff.append( "\n\t" );
			buff.append( triangles[ i ] );
			buff.append( "-" );
			buff.append( triangles[ i + 1 ] );
			buff.append( "-" );
			buff.append( triangles[ i + 2 ] );
		}

		return buff.toString();
	}

	/**
	 * Compute the surface area of this shape
	 * 
	 * @return the surface area
	 */
	public float getSurfaceArea()
	{
		float sum = 0;
		for( int i = 0; i < triangles.length; i += 3 )
		{
			int a = triangles[ i ];
			int b = triangles[ i + 1 ];
			int c = triangles[ i + 2 ];
			sum +=
					TriangleUtils.area( vertices[ a ], vertices[ a + 1 ], vertices[ a + 2 ],
							vertices[ b ], vertices[ b + 1 ], vertices[ b + 2 ], vertices[ c ],
							vertices[ c + 1 ], vertices[ c + 2 ] );
		}

		return sum;
	}

	@Override
	public Shape clone()
	{
		return new Shape( Arrays.copyOf( vertices, vertices.length ), Arrays.copyOf(
				triangles, triangles.length ) );
	}
}
