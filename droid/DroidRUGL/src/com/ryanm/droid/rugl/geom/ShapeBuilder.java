
package com.ryanm.droid.rugl.geom;

/**
 * Convenience class for piecing together a shape
 * 
 * @author ryanm
 */
public class ShapeBuilder
{
	/**
	 * Count of vertices added so far
	 */
	public int vertexCount = 0;

	/**
	 * Index at which to add the next vertex
	 */
	public int vertexOffset = 0;

	/***/
	public float[] vertices = new float[ 30 ];

	/**
	 * Index at which to add the next texcoord
	 */
	public int texCoordOffset = 0;

	/***/
	public float[] texCoords = new float[ 20 ];

	/**
	 * Index at which to add the next colour
	 */
	public int colourOffset = 0;

	/***/
	public int[] colours = new int[ 10 ];

	/**
	 * Index at which to add the next triangle
	 */
	public int triangleOffset;

	/***/
	public short[] triangles = new short[ 30 ];

	/**
	 * Specify a vertex
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param c
	 * @param u
	 * @param v
	 */
	public void vertex( float x, float y, float z, int c, float u, float v )
	{
		vertexCount++;

		ensureCapacity( 1, 0 );
		vertices[ vertexOffset++ ] = x;
		vertices[ vertexOffset++ ] = y;
		vertices[ vertexOffset++ ] = z;

		colours[ colourOffset++ ] = c;

		texCoords[ texCoordOffset++ ] = u;
		texCoords[ texCoordOffset++ ] = v;
	}

	/**
	 * Directly specify a triangle
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public void triangle( short a, short b, short c )
	{
		ensureCapacity( 0, 1 );

		triangles[ triangleOffset++ ] = a;
		triangles[ triangleOffset++ ] = b;
		triangles[ triangleOffset++ ] = c;
	}

	/**
	 * Specify a triangle relative to {@link #vertexCount}
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public void relTriangle( int a, int b, int c )
	{
		ensureCapacity( 0, 1 );

		triangles[ triangleOffset++ ] = ( short ) ( a + vertexCount );
		triangles[ triangleOffset++ ] = ( short ) ( b + vertexCount );
		triangles[ triangleOffset++ ] = ( short ) ( c + vertexCount );
	}

	/**
	 * Ensures capacity for a number of upcoming vertices and triangles
	 * 
	 * @param verts
	 * @param tris
	 */
	public void ensureCapacity( int verts, int tris )
	{
		int vleft = colours.length - colourOffset;
		while( vleft < verts )
		{
			vertices = grow( vertices );
			texCoords = grow( texCoords );
			colours = grow( colours );
			vleft = ( vertices.length - vertexOffset ) / 3;
		}

		int tleft = triangles.length - triangleOffset;
		while( tleft < tris * 3 )
		{
			triangles = grow( triangles );
			tleft = ( triangles.length - triangleOffset ) / 3;
		}
	}

	/***/
	public void clear()
	{
		vertexCount = 0;
		vertexOffset = 0;
		colourOffset = 0;
		texCoordOffset = 0;
		triangleOffset = 0;
	}

	/**
	 * @return The shape so far
	 */
	public TexturedShape compile()
	{
		if( vertexCount > 3 )
		{
			float[] verts = new float[ vertexOffset ];
			System.arraycopy( vertices, 0, verts, 0, verts.length );

			float[] txc = new float[ texCoordOffset ];
			System.arraycopy( texCoords, 0, txc, 0, txc.length );

			int[] col = new int[ colourOffset ];
			System.arraycopy( colours, 0, col, 0, col.length );

			short[] tris = new short[ triangleOffset ];
			System.arraycopy( triangles, 0, tris, 0, tris.length );

			clear();

			Shape s = new Shape( verts, tris );
			ColouredShape cs = new ColouredShape( s, col, null );
			TexturedShape pts = new TexturedShape( cs, txc, null );

			return pts;
		}

		clear();

		return null;
	}

	private static int[] grow( int[] in )
	{
		int[] na = new int[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}

	private static float[] grow( float[] in )
	{
		float[] na = new float[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}

	private static short[] grow( short[] in )
	{
		short[] na = new short[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}
}
