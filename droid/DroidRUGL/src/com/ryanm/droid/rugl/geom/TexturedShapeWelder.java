
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.texture.Texture;

/**
 * Composites {@link TexturedShape}s
 * 
 * @author ryanm
 */
public class TexturedShapeWelder extends ShapeWelder<TexturedShape>
{
	private Texture texture = null;

	/**
	 * Adds a shape to the builder. Non-textured {@link Shape}s and
	 * {@link TexturedShape}s that have a different rendering
	 * {@link State} will be ignored
	 */
	@Override
	public boolean addShape( TexturedShape s )
	{
		if( texture == null )
		{
			texture = s.texture;
		}

		if( s.texture.parent.id() == texture.parent.id() )
		{
			return super.addShape( s );
		}

		return false;
	}

	@Override
	public void clear()
	{
		super.clear();

		texture = null;
	}

	@Override
	public TexturedShape fuse()
	{
		float[] verts = new float[ vertexCount * 3 ];
		short[] tris = new short[ triangleCount ];
		int[] colours = new int[ vertexCount ];
		float[] texCoords = new float[ vertexCount * 2 ];

		int vi = 0;
		int ti = 0;
		int ci = 0;
		int tci = 0;

		State state = shapes.getFirst().state;

		while( !shapes.isEmpty() )
		{
			TexturedShape s = shapes.removeFirst();

			System.arraycopy( s.vertices, 0, verts, vi, s.vertices.length );
			System.arraycopy( s.colours, 0, colours, ci, s.colours.length );
			System.arraycopy( s.texCoords, 0, texCoords, tci, s.texCoords.length );

			System.arraycopy( s.triangles, 0, tris, ti, s.triangles.length );
			for( int i = 0; i < s.triangles.length; i++ )
			{
				tris[ ti + i ] += vi / 3;
			}

			vi += s.vertices.length;
			ti += s.triangles.length;
			ci += s.colours.length;
			tci += s.texCoords.length;
		}

		TexturedShape ts =
				new TexturedShape( new ColouredShape( new Shape( verts, tris ), colours,
						state ), texCoords, texture );
		clear();
		return ts;
	}
}
