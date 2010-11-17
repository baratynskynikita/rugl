
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.State;

/**
 * Composites {@link ColouredShape}s
 * 
 * @author ryanm
 */
public class ColouredShapeWelder extends ShapeWelder<ColouredShape>
{
	/**
	 * @param cs
	 * @return The gestalt shape
	 */
	public static ColouredShape fuse( ColouredShape... cs )
	{
		ColouredShapeWelder csb = new ColouredShapeWelder();
		for( int i = 0; i < cs.length; i++ )
		{
			csb.addShape( cs[ i ] );
		}

		return csb.fuse();
	}

	/**
	 * Rendering state for the gestalt shape
	 */
	private State state = null;

	/**
	 * Adds a {@link ColouredShape} to the builder. Non-coloured
	 * {@link Shape}s and shapes with render {@link State}s different
	 * to the first shape added will be ignored
	 */
	@Override
	public boolean addShape( ColouredShape s )
	{
		if( state == null )
		{
			state = s.state;
		}

		if( state.equals( s.state ) )
		{
			return super.addShape( s );
		}

		return false;
	}

	@Override
	public ColouredShape fuse()
	{
		float[] verts = new float[ vertexCount * 3 ];
		short[] tris = new short[ triangleCount ];
		int[] colours = new int[ vertexCount ];

		int vi = 0;
		int ti = 0;
		int ci = 0;

		while( !shapes.isEmpty() )
		{
			ColouredShape s = shapes.removeFirst();

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
