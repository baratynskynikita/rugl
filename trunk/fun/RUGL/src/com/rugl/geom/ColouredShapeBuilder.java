
package com.rugl.geom;

import com.rugl.gl.State;

/**
 * Composites {@link ColouredShape}s
 * 
 * @author ryanm
 */
public class ColouredShapeBuilder extends ShapeBuilder
{
	/**
	 * @param cs
	 * @return The gestalt shape
	 */
	public static ColouredShape fuse( ColouredShape... cs )
	{
		ColouredShapeBuilder csb = new ColouredShapeBuilder();
		for( int i = 0; i < cs.length; i++ )
		{
			csb.addShape( cs[ i ] );
		}

		return csb.compile();
	}

	/**
	 * Rendering state for the gestalt shape
	 */
	protected State state = null;

	/**
	 * Adds a {@link ColouredShape} to the builder. Non-coloured
	 * {@link Shape}s and shapes with render {@link State}s different
	 * to the first shape added will be ignored
	 */
	@Override
	public boolean addShape( Shape s )
	{
		if( s instanceof ColouredShape )
		{
			ColouredShape cs = ( ColouredShape ) s;
			if( state == null )
			{
				state = cs.state;
			}

			if( state.equals( cs.state ) )
			{
				return super.addShape( s );
			}
		}

		return false;
	}

	@Override
	public ColouredShape compile()
	{
		float[] verts = new float[ vertexCount * 3 ];
		int[] tris = new int[ triangleCount ];
		int[] colours = new int[ vertexCount ];

		int vi = 0;
		int ti = 0;
		int ci = 0;

		while( !shapes.isEmpty() )
		{
			ColouredShape s = ( ColouredShape ) shapes.removeFirst();

			System.arraycopy( s.vertices, 0, verts, vi, s.vertices.length );
			System.arraycopy( s.colours, 0, colours, ci, s.colours.length );

			System.arraycopy( s.triangles, 0, tris, ti, s.triangles.length );
			for( int i = 0; i < s.triangles.length; i++ )
			{
				tris[ ti + i ] += vi / 3;
			}

			vi += s.vertices.length;
			ti += s.triangles.length;
			ci += s.colours.length;
		}

		clear();
		return new ColouredShape( new Shape( verts, tris ), colours, state );
	}

	@Override
	public void clear()
	{
		super.clear();

		state = null;
	}

}
