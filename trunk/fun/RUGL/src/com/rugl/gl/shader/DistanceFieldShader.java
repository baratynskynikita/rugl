
package com.rugl.gl.shader;

import com.rugl.util.Colour;

/**
 * Performs distance field rendering. See <a href=
 * "http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf"
 * >here</a>
 * 
 * @author ryanm
 */
public class DistanceFieldShader extends Program
{
	/**
	 * Note that these are static - it's important
	 */
	private static UniformFloatVariable aawVar = null;

	private static UniformFloatVariable minBorderVar = null;

	private static UniformFloatVariable maxBorderVar = null;

	private static UniformVec4Variable borderColourVar = null;

	private static UniformFloatVariable minGlowVar = null;

	private static UniformVec4Variable glowColourVar = null;

	/**
	 * Scaling factor for AA. Greater means more fuzziness
	 */
	public final float aaWidth;

	/**
	 * The distance value at the outside of the border. Defaults to 1
	 * (no border)
	 */
	public final float minBorder;

	/**
	 * The distance value at the edge of the glyph. Defaults to 0.5
	 */
	public final float maxBorder;

	/**
	 * The border colour in packed rgba format. Defaults to 0,0,0,0
	 */
	public final int borderColour;

	/**
	 * The alpha value where the glow fades out, defaults to 1
	 */
	public final float minGlow;

	/**
	 * The glow colour in packed rgba format. Defaults to 0,0,0,0
	 */
	public final int glowColour;

	/***/
	public DistanceFieldShader()
	{
		this( 10, 1, 0.5f, 0, 1, 0 );
	}

	/**
	 * @param aaWidth
	 * @param minBorder
	 * @param maxBorder
	 * @param borderColour
	 * @param minGlow
	 * @param glowColour
	 */
	public DistanceFieldShader( float aaWidth, float minBorder, float maxBorder,
			int borderColour, float minGlow, int glowColour )
	{
		super( null, "shader/DistanceFieldFrag.glsl" );
		this.aaWidth = aaWidth;
		this.minBorder = minBorder;
		this.maxBorder = maxBorder;
		this.borderColour = borderColour;
		this.minGlow = minGlow;
		this.glowColour = glowColour;

		if( aawVar == null )
		{
			aawVar = createUniformFloatVariable( "aaWidth" );
			minBorderVar = createUniformFloatVariable( "minBorder" );
			maxBorderVar = createUniformFloatVariable( "maxBorder" );
			minGlowVar = createUniformFloatVariable( "minGlow" );
			borderColourVar = createUniformVec4Variable( "borderColour" );
			glowColourVar = createUniformVec4Variable( "glowColour" );
		}
	}

	@Override
	public int compareTo( Program o )
	{
		float d = super.compareTo( o );

		if( d == 0 )
		{ // shader ids are the same, we should be the same type
			DistanceFieldShader dfs = ( DistanceFieldShader ) o;

			d = aaWidth - aaWidth;

			if( d == 0 )
			{
				d = minBorder - dfs.minBorder;

				if( d == 0 )
				{
					d = maxBorder - dfs.maxBorder;

					if( d == 0 )
					{
						d = minGlow - dfs.minGlow;

						if( d == 0 )
						{
							d = borderColour - dfs.borderColour;

							if( d == 0 )
							{
								d = glowColour - dfs.glowColour;
							}
						}
					}
				}
			}
		}

		return ( int ) Math.signum( d );
	}

	@Override
	public void setUniforms()
	{
		// set the uniforms, UniformFloatVariable takes care of
		// dirtiness
		aawVar.set( aaWidth );
		minBorderVar.set( minBorder );
		maxBorderVar.set( maxBorder );
		minGlowVar.set( minGlow );
		borderColourVar.set( Colour.redf( borderColour ),
				Colour.greenf( borderColour ), Colour.bluef( borderColour ),
				Colour.alphaf( borderColour ) );
		glowColourVar.set( Colour.redf( glowColour ), Colour.greenf( glowColour ),
				Colour.bluef( glowColour ), Colour.alphaf( glowColour ) );
	}

	@Override
	public String toString()
	{
		return "Distance field rendering " + super.toString();
	}
}
