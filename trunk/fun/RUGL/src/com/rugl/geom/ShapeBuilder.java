
package com.rugl.geom;

import java.util.LinkedList;

/**
 * Composites {@link Shape}s
 * 
 * @author ryanm
 */
public class ShapeBuilder
{
	private static ShapeBuilder sb = new ShapeBuilder();

	/**
	 * Convenience method to join shapes
	 * 
	 * @param shapes
	 * @return The gestalt shape
	 */
	public static Shape fuse( Shape... shapes )
	{
		for( int i = 0; i < shapes.length; i++ )
		{
			sb.addShape( shapes[ i ] );
		}

		return sb.compile();
	}

	/**
	 * Holds the input shapes
	 */
	protected LinkedList<Shape> shapes = new LinkedList<Shape>();

	/**
	 * Holds the current number of vertices
	 */
	protected int vertexCount = 0;

	/**
	 * Holds the current number of triangle indices
	 */
	protected int triangleCount = 0;

	/**
	 * Adds a shape to this {@link ShapeBuilder}
	 * 
	 * @param s
	 * @return <code>true</code> if the {@link Shape} was successfully
	 *         added, <code>false</code> otherwise
	 */
	public boolean addShape( Shape s )
	{
		shapes.add( s );
		vertexCount += s.vertexCount();
		triangleCount += s.triangles.length;

		return true;
	}

	/**
	 * Removes all shapes from this builder
	 */
	public void clear()
	{
		shapes.clear();
		vertexCount = 0;
		triangleCount = 0;
	}

	/**
	 * Compiles the current set of input {@link Shape}s to a single
	 * shape. Also clears the current set of input {@link Shape}s
	 * 
	 * @return The composite shape
	 */
	public Shape compile()
	{
		float[] verts = new float[ vertexCount * 3 ];
		int[] tris = new int[ triangleCount ];

		int vi = 0;
		int ti = 0;

		while( !shapes.isEmpty() )
		{
			Shape s = shapes.removeFirst();

			System.arraycopy( s.vertices, 0, verts, vi, s.vertices.length );

			System.arraycopy( s.triangles, 0, tris, ti, s.triangles.length );
			for( int i = 0; i < s.triangles.length; i++ )
			{
				tris[ ti + i ] += vi / 3;
			}

			vi += s.vertices.length;
			ti += s.triangles.length;
		}

		clear();
		return new Shape( verts, tris );
	}
}
