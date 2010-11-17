/*
 * Copyright (c) 2002 Shaven Puppy Ltd All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of 'Shaven Puppy' nor the
 * names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.rugl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.opengl.ARBDepthTexture;
import org.lwjgl.opengl.ARBFragmentProgram;
import org.lwjgl.opengl.ARBMatrixPalette;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBPointParameters;
import org.lwjgl.opengl.ARBShadow;
import org.lwjgl.opengl.ARBShadowAmbient;
import org.lwjgl.opengl.ARBTextureBorderClamp;
import org.lwjgl.opengl.ARBTextureCompression;
import org.lwjgl.opengl.ARBTextureCubeMap;
import org.lwjgl.opengl.ARBTextureEnvCombine;
import org.lwjgl.opengl.ARBTextureEnvDot3;
import org.lwjgl.opengl.ARBTextureMirroredRepeat;
import org.lwjgl.opengl.ARBTransposeMatrix;
import org.lwjgl.opengl.ARBVertexBlend;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.ARBVertexProgram;
import org.lwjgl.opengl.ARBWindowPos;
import org.lwjgl.opengl.ATIElementArray;
import org.lwjgl.opengl.ATIEnvmapBumpmap;
import org.lwjgl.opengl.ATIFragmentShader;
import org.lwjgl.opengl.ATIPnTriangles;
import org.lwjgl.opengl.ATISeparateStencil;
import org.lwjgl.opengl.ATITextureMirrorOnce;
import org.lwjgl.opengl.ATIVertexArrayObject;
import org.lwjgl.opengl.ATIVertexStreams;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTAbgr;
import org.lwjgl.opengl.EXTBgra;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
import org.lwjgl.opengl.EXTBlendSubtract;
import org.lwjgl.opengl.EXTCompiledVertexArray;
import org.lwjgl.opengl.EXTDrawRangeElements;
import org.lwjgl.opengl.EXTFogCoord;
import org.lwjgl.opengl.EXTMultiDrawArrays;
import org.lwjgl.opengl.EXTPackedPixels;
import org.lwjgl.opengl.EXTPointParameters;
import org.lwjgl.opengl.EXTRescaleNormal;
import org.lwjgl.opengl.EXTSecondaryColor;
import org.lwjgl.opengl.EXTSeparateSpecularColor;
import org.lwjgl.opengl.EXTSharedTexturePalette;
import org.lwjgl.opengl.EXTStencilTwoSide;
import org.lwjgl.opengl.EXTStencilWrap;
import org.lwjgl.opengl.EXTTextureCompressionS3TC;
import org.lwjgl.opengl.EXTTextureEnvCombine;
import org.lwjgl.opengl.EXTTextureEnvDot3;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.EXTTextureLODBias;
import org.lwjgl.opengl.EXTVertexShader;
import org.lwjgl.opengl.EXTVertexWeighting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.NVCopyDepthToColor;
import org.lwjgl.opengl.NVDepthClamp;
import org.lwjgl.opengl.NVEvaluators;
import org.lwjgl.opengl.NVFence;
import org.lwjgl.opengl.NVFogDistance;
import org.lwjgl.opengl.NVLightMaxExponent;
import org.lwjgl.opengl.NVOcclusionQuery;
import org.lwjgl.opengl.NVPackedDepthStencil;
import org.lwjgl.opengl.NVPointSprite;
import org.lwjgl.opengl.NVRegisterCombiners;
import org.lwjgl.opengl.NVRegisterCombiners2;
import org.lwjgl.opengl.NVTexgenReflection;
import org.lwjgl.opengl.NVTextureEnvCombine4;
import org.lwjgl.opengl.NVTextureRectangle;
import org.lwjgl.opengl.NVTextureShader;
import org.lwjgl.opengl.NVTextureShader2;
import org.lwjgl.opengl.NVTextureShader3;
import org.lwjgl.opengl.NVVertexArrayRange;
import org.lwjgl.opengl.NVVertexArrayRange2;
import org.lwjgl.opengl.NVVertexProgram;
import org.lwjgl.opengl.OpenGLException;

import com.rugl.ScreenGrabber;
import com.rugl.gl.State;
import com.rugl.gl.enums.ComparisonFunction;
import com.rugl.gl.enums.DestinationFactor;
import com.rugl.gl.enums.SourceFactor;
import com.rugl.gl.facets.AlphaTest;
import com.rugl.gl.facets.Blend;
import com.rugl.gl.facets.DepthTest;

/**
 * @author foo
 */
public class GLUtil
{
	/** A map of constant names to values */
	private static HashMap<String, Integer> glConstantsMap;

	/**
	 * A rendering state containing typical, but not default, state:
	 * blending, depth test, alpha test etc
	 */
	public static final State typicalState =
			new State()
					.with(
							new Blend( SourceFactor.SRC_ALPHA,
									DestinationFactor.ONE_MINUS_SRC_ALPHA ) )
					.with( new DepthTest( ComparisonFunction.LEQUAL ) )
					.with( new AlphaTest( ComparisonFunction.GREATER, 0 ) );

	/**
	 * Returns the nearest power of 2, which is either n if n is
	 * already a power of 2, or the next higher number than n which is
	 * a power of 2.
	 * 
	 * @param n
	 * @return The smallest power of two that is larger than n
	 */
	public static int nextPowerOf2( int n )
	{
		int x = 1;

		while( x < n )
		{
			x <<= 1;
		}

		return x;
	}

	/**
	 * Decode a gl string constant
	 * 
	 * @param glstring
	 *           The name of the GL constant
	 * @return The value
	 * @throws OpenGLException
	 *            if there is no such constant
	 */
	public static int decode( String glstring ) throws OpenGLException
	{
		if( glConstantsMap == null )
		{
			glConstantsMap = new HashMap<String, Integer>( 513, 0.1f );
			loadGLConstants();
		}

		Integer i = glConstantsMap.get( glstring.toUpperCase() );
		if( i == null )
		{
			throw new OpenGLException( glstring + " is not a recognised GL constant" );
		}
		else
		{
			return i.intValue();
		}
	}

	/**
	 * Recode a gl constant back into a string
	 * 
	 * @param code
	 *           the value
	 * @return The name of the constant with that value
	 * @throws OpenGLException
	 *            if there is no such constant
	 */
	public static String recode( int code ) throws OpenGLException
	{
		if( glConstantsMap == null )
		{
			glConstantsMap = new HashMap<String, Integer>( 513, 0.1f );
			loadGLConstants();
		}

		for( Iterator i = glConstantsMap.keySet().iterator(); i.hasNext(); )
		{
			String s = ( String ) i.next();
			Integer n = glConstantsMap.get( s );
			if( n.intValue() == code )
			{
				return s;
			}
		}
		throw new OpenGLException( code + " is not a known GL code" );
	}

	/**
	 * Reads all the constant enumerations from this class and stores
	 * them so we can decode them from strings.
	 * 
	 * @see #decode(String)
	 * @see #recode(int)
	 */
	private static void loadGLConstants()
	{
		Class[] classes =
				new Class[] { GL11.class, GL12.class, GL13.class, GL14.class, GL15.class,
						ARBMultitexture.class, ARBTextureCubeMap.class, ARBDepthTexture.class,
						ARBFragmentProgram.class, ARBMatrixPalette.class, ARBMultisample.class,
						ARBPointParameters.class, ARBShadow.class, ARBShadowAmbient.class,
						ARBTextureBorderClamp.class, ARBTextureCompression.class,
						ARBTextureEnvCombine.class, ARBTextureEnvDot3.class,
						ARBTextureMirroredRepeat.class, ARBTransposeMatrix.class,
						ARBVertexBlend.class, ARBVertexBufferObject.class,
						ARBVertexProgram.class, ARBWindowPos.class, EXTDrawRangeElements.class,
						EXTAbgr.class, EXTBgra.class, EXTBlendFuncSeparate.class,
						EXTBlendSubtract.class, EXTCompiledVertexArray.class,
						EXTFogCoord.class, EXTMultiDrawArrays.class, EXTPackedPixels.class,
						EXTPointParameters.class, EXTRescaleNormal.class,
						EXTSecondaryColor.class, EXTSeparateSpecularColor.class,
						EXTSharedTexturePalette.class, EXTStencilTwoSide.class,
						EXTStencilWrap.class, EXTTextureCompressionS3TC.class,
						EXTTextureEnvCombine.class, EXTTextureEnvDot3.class,
						EXTTextureFilterAnisotropic.class, EXTTextureLODBias.class,
						EXTVertexShader.class, EXTVertexWeighting.class, ATIElementArray.class,
						ATIEnvmapBumpmap.class, ATIFragmentShader.class, ATIPnTriangles.class,
						ATISeparateStencil.class, ATITextureMirrorOnce.class,
						ATIVertexArrayObject.class, ATIVertexStreams.class,
						NVCopyDepthToColor.class, NVDepthClamp.class, NVEvaluators.class,
						NVFence.class, NVFogDistance.class, NVLightMaxExponent.class,
						NVOcclusionQuery.class, NVPackedDepthStencil.class,
						NVPointSprite.class, NVRegisterCombiners.class,
						NVRegisterCombiners2.class, NVTexgenReflection.class,
						NVTextureEnvCombine4.class, NVTextureRectangle.class,
						NVTextureShader.class, NVTextureShader2.class, NVTextureShader3.class,
						NVVertexArrayRange.class, NVVertexArrayRange2.class,
						NVVertexProgram.class };
		for( int i = 0; i < classes.length; i++ )
		{
			loadGLConstants( classes[ i ] );
		}
	}

	private static void loadGLConstants( Class intf )
	{
		Field[] field = intf.getFields();
		for( int i = 0; i < field.length; i++ )
		{
			try
			{
				if( Modifier.isStatic( field[ i ].getModifiers() )
						&& Modifier.isPublic( field[ i ].getModifiers() )
						&& Modifier.isFinal( field[ i ].getModifiers() )
						&& field[ i ].getType().equals( int.class ) )
				{
					glConstantsMap.put( field[ i ].getName(),
							new Integer( field[ i ].getInt( null ) ) );
				}
			}
			catch( Exception e )
			{
			}
		}
	}

	/**
	 * Sets up a standard orthographic projection, with 1:1 units to
	 * pixels ratio. Matrix mode is modelview after this call
	 */
	public static void standardOrtho()
	{
		int w = Display.getDisplayMode().getWidth();
		int h = Display.getDisplayMode().getHeight();

		scaledOrtho( w, h );
	}

	/**
	 * Sets an orthographic projection, with the specified width and
	 * height
	 * 
	 * @param width
	 * @param height
	 */
	public static void scaledOrtho( int width, int height )
	{
		int w = Display.getDisplayMode().getWidth();
		int h = Display.getDisplayMode().getHeight();

		if( !ScreenGrabber.doingTiledRender )
		{
			if( ScreenGrabber.tr != null )
			{
				ScreenGrabber.tr.trOrtho( 0, width, 0, height, -1, 1 );
			}
			else
			{
				GL11.glMatrixMode( GL11.GL_PROJECTION );
				GL11.glLoadIdentity();
				GL11.glOrtho( 0, width, 0, height, -1, 1 );
			}

			GL11.glMatrixMode( GL11.GL_MODELVIEW );
			GL11.glLoadIdentity();
			GL11.glViewport( 0, 0, w, h );
		}
	}

	/**
	 * Throws OpenGLException if {@link GL11#glGetError()} returns
	 * anything other than {@link GL11#GL_NO_ERROR}
	 * 
	 * @throws OpenGLException
	 */
	public static void checkGLError() throws OpenGLException
	{
		int err = GL11.glGetError();
		if( err != GL11.GL_NO_ERROR )
		{
			throw new OpenGLException( err );
		}
	}
}
