
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.util.geom.BoundingCuboid;
import com.ryanm.droid.rugl.util.geom.Matrix4f;
import com.ryanm.droid.rugl.util.geom.TriangleUtils;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.droid.rugl.util.geom.Vector4f;
import com.ryanm.droid.rugl.util.io.DataSink;
import com.ryanm.droid.rugl.util.io.DataSource;
import com.ryanm.droid.rugl.util.io.SerialUtils;

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
	public final short[] indices;

	private final BoundingCuboid bounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private boolean boundsDirty = true;

	private void sanity() throws IllegalArgumentException
	{
		if( vertices.length % 3 != 0 )
		{
			throw new IllegalArgumentException( "vertex count error\n" + shortString() );
		}

		for( int i = 0; i < indices.length; i++ )
		{
			if( indices[ i ] < 0 || indices[ i ] >= vertexCount() )
			{
				throw new IllegalArgumentException( "triangle index error : " + indices[ i ]
						+ "\n" + toString() );
			}
		}
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
		indices = s.indices;
	}

	/**
	 * Builds a new {@link Shape}
	 * 
	 * @param vertices
	 * @param indices
	 * @throws IllegalArgumentException
	 *            If there's some problem with the supplied data
	 */
	public Shape( float[] vertices, short[] indices ) throws IllegalArgumentException
	{
		this.vertices = vertices;
		this.indices = indices;

		sanity();
	}

	/**
	 * @param data
	 * @throws IllegalArgumentException
	 *            If there's some problem with the supplied data
	 */
	public Shape( DataSource data ) throws IllegalArgumentException
	{
		vertices = SerialUtils.readFloatArray( data );
		indices = SerialUtils.readShortArray( data );

		sanity();
	}

	/**
	 * @param sink
	 */
	public void write( DataSink sink )
	{
		SerialUtils.write( vertices, sink );
		SerialUtils.write( indices, sink );
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
	 * Used in {@link #transform(Matrix4f)}
	 */
	private static final Vector4f transformVector = new Vector4f();

	/**
	 * Transforms this shape
	 * 
	 * @param m
	 *           The transformation
	 * @return this
	 */
	public Shape transform( Matrix4f m )
	{
		for( int i = 0; i < vertices.length; i += 3 )
		{
			transformVector.set( vertices[ i ], vertices[ i + 1 ], vertices[ i + 2 ], 1 );
			Matrix4f.transform( m, transformVector, transformVector );
			vertices[ i ] = transformVector.x;
			vertices[ i + 1 ] = transformVector.y;
			vertices[ i + 2 ] = transformVector.z;
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
	 * Used in {@link #rotate(float, float, float, float)}
	 */
	private static Matrix4f rotateMatrix = new Matrix4f();

	/**
	 * Rotates this shape
	 * 
	 * @param angle
	 *           The angle through which to rotate, in radians
	 * @param ax
	 *           rotation axis
	 * @param ay
	 *           rotation axis
	 * @param az
	 *           rotation axis
	 * @return this
	 */
	public Shape rotate( float angle, float ax, float ay, float az )
	{
		rotateMatrix.setIdentity();
		rotateMatrix.rotate( angle, ax, ay, az );
		transform( rotateMatrix );

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
		for( int i = 0; i < indices.length; i += 3 )
		{
			int a = indices[ i ] * 3;
			int b = indices[ i + 1 ] * 3;
			int c = indices[ i + 2 ] * 3;

			if( TriangleUtils.contains( x, y, vertices[ a ], vertices[ a + 1 ],
					vertices[ b ], vertices[ b + 1 ], vertices[ c ], vertices[ c + 1 ] ) )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @return A shorter description than {@link #toString()}
	 */
	public String shortString()
	{
		StringBuilder buff = new StringBuilder( "Shape " );
		buff.append( vertexCount() );
		buff.append( " verts " );
		buff.append( indices.length / 3.0 );
		buff.append( " tris" );
		return buff.toString();
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "Shape " );
		buff.append( vertexCount() );
		buff.append( " verts " );
		buff.append( indices.length / 3.0 );
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

		for( int i = 0; i < indices.length; i += 3 )
		{
			buff.append( "\n\t" );
			buff.append( indices[ i ] );
			buff.append( "-" );
			buff.append( indices[ i + 1 ] );
			buff.append( "-" );
			buff.append( indices[ i + 2 ] );
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
		for( int i = 0; i < indices.length; i += 3 )
		{
			int a = indices[ i ];
			int b = indices[ i + 1 ];
			int c = indices[ i + 2 ];
			sum +=
					TriangleUtils.area( vertices[ a ], vertices[ a + 1 ], vertices[ a + 2 ],
							vertices[ b ], vertices[ b + 1 ], vertices[ b + 2 ], vertices[ c ],
							vertices[ c + 1 ], vertices[ c + 2 ] );
		}

		return sum;
	}

	/**
	 * @return the number of data bytes
	 */
	public int bytes()
	{
		return vertices.length * 4 + indices.length * 2;
	}

	@Override
	public Shape clone()
	{
		return new Shape( vertices.clone(), indices.clone() );
	}
}
