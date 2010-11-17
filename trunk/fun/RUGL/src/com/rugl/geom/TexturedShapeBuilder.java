
package com.rugl.geom;

import com.rugl.gl.State;
import com.rugl.texture.Texture;

/**
 * Composites {@link TexturedShape}s
 * 
 * @author ryanm
 */
public class TexturedShapeBuilder extends ColouredShapeBuilder
{
	private Texture texture = null;

	/**
	 * Adds a shape to the builder. Non-textured {@link Shape}s and
	 * {@link TexturedShape}s that have a different rendering
	 * {@link State} will be ignored
	 */
	@Override
	public boolean addShape( Shape s )
	{
		if( s instanceof TexturedShape )
		{
			TexturedShape ts = ( TexturedShape ) s;

			if( texture == null )
			{
				texture = ts.texture;
			}

			if( ts.texture.getTextureID() == texture.getTextureID() )
			{
				return super.addShape( ts );
			}
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
	public TexturedShape compile()
	{
		float[] verts = new float[ vertexCount * 3 ];
		int[] tris = new int[ triangleCount ];
		int[] colours = new int[ vertexCount ];
		float[] texCoords = new float[ vertexCount * 2 ];

		int vi = 0;
		int ti = 0;
		int ci = 0;
		int tci = 0;

		while( !shapes.isEmpty() )
		{
			TexturedShape s = ( TexturedShape ) shapes.removeFirst();

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
