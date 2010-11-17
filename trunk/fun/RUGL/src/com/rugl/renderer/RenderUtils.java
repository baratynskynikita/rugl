
package com.rugl.renderer;

import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.Color;

import com.rugl.gl.State;
import com.rugl.gl.enums.ComparisonFunction;
import com.rugl.gl.enums.MagFilter;
import com.rugl.gl.enums.MinFilter;
import com.rugl.gl.facets.AlphaTest;
import com.rugl.gl.facets.Blend;
import com.rugl.gl.facets.TextureState.Filters;
import com.rugl.gl.shader.DistanceFieldShader;

/**
 * Utility methods for rendering stuff
 * 
 * @author ryanm
 */
public class RenderUtils
{
	// shaders hard-crash my laptop with kubuntu 10.04. Thanks Intel!
	private static final boolean disableShaders = "true".equals( System
			.getProperty( "com.rugl.disableshaders" ) );

	/**
	 * Calculates the vertex indices to form a sequence of quads from
	 * consecutive vertices. Assumes vertices are in bl tl br tr format
	 * 
	 * @param vertexCount
	 *           The number of vertices. Should be a multiple of 4
	 * @param startVertex
	 *           The index of the lowest vertex
	 * @param dest
	 *           An array in which to store the resulting vertices, or
	 *           null
	 * @param start
	 *           The position in dest to store. Ignored if dest is null
	 * @return dest, or a new int array
	 */
	public static int[] makeQuads( int vertexCount, int startVertex, int[] dest, int start )
	{
		if( dest == null )
		{
			dest = new int[ 6 * ( vertexCount / 4 ) ];
			start = 0;
		}

		int index = start;
		for( int i = startVertex; i < vertexCount + startVertex; i += 4 )
		{
			dest[ index++ ] = i;
			dest[ index++ ] = i + 3;
			dest[ index++ ] = i + 1;
			dest[ index++ ] = i;
			dest[ index++ ] = i + 2;
			dest[ index++ ] = i + 3;
		}

		return dest;
	}

	/**
	 * Gets the basic full-coverage texture coordinates for a series of
	 * quads, in bl, tl, br, tr order
	 * 
	 * @param quads
	 *           the number of quads
	 * @return texture coordinates
	 */
	public static float[] getQuadTexCoords( int quads )
	{
		float[] tc = new float[ 8 * quads ];
		for( int i = 0; i < quads; i++ )
		{
			int b = 8 * i;
			int o = 0;
			tc[ b + o++ ] = 0;
			tc[ b + o++ ] = 0;
			tc[ b + o++ ] = 0;
			tc[ b + o++ ] = 1;
			tc[ b + o++ ] = 1;
			tc[ b + o++ ] = 0;
			tc[ b + o++ ] = 1;
			tc[ b + o++ ] = 1;
		}
		return tc;
	}

	/**
	 * Enables distance field rendering
	 * 
	 * @param s
	 *           the {@link State} to enable
	 * @return The altered {@link State}
	 */
	public static State distanceFieldRendering( State s )
	{
		s =
				s.with( s.texture.with( new Filters( MinFilter.LINEAR_MIPMAP_NEAREST,
						MagFilter.LINEAR ) ) );

		if( !disableShaders && GLContext.getCapabilities().OpenGL20 )
		{ // we can use the shader
			s = s.with( new DistanceFieldShader() );
		}
		else
		{ // no anti-aliasing / blending for us :-(
			s =
					s.with( Blend.disabled ).with(
							new AlphaTest( ComparisonFunction.GEQUAL, 0.5f ) );
		}

		return s;
	}

	/**
	 * Sets the supplied colours
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @param colours
	 */
	public static void setColours( int r, int g, int b, int a, Color... colours )
	{
		for( int i = 0; i < colours.length; i++ )
		{
			colours[ i ].set( r, g, b, a );
		}
	}
}
