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

package com.ryanm.droid.rugl.gl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLException;

import com.ryanm.droid.rugl.gl.enums.ComparisonFunction;
import com.ryanm.droid.rugl.gl.enums.DestinationFactor;
import com.ryanm.droid.rugl.gl.enums.SourceFactor;
import com.ryanm.droid.rugl.gl.facets.AlphaTest;
import com.ryanm.droid.rugl.gl.facets.Blend;
import com.ryanm.droid.rugl.gl.facets.DepthTest;

/**
 * @author foo
 */
public class GLUtil
{
	/** A map of constant names to values */
	private static HashMap<String, Integer> glConstantsMap;

	private static ByteBuffer scratch = BufferUtils.createByteBuffer( 4 * 16 );

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
	 * @param size
	 * @return An {@link IntBuffer} handy for temporary use
	 */
	public static IntBuffer intScratch( int size )
	{
		scratch.clear();
		scratch.limit( 4 * size );
		return scratch.asIntBuffer();
	}

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
	 * @throws GLException
	 *            if the constant is not found
	 */
	public static int decode( String glstring )
	{
		if( glConstantsMap == null )
		{
			glConstantsMap = new HashMap<String, Integer>( 513, 0.1f );
			loadGLConstants();
		}

		Integer i = glConstantsMap.get( glstring.toUpperCase() );
		if( i == null )
		{
			throw new GLException( 0, glstring + " not found" );
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
	 * @return The name of the constant with that value or
	 *         <code>null</code>
	 */
	public static String recode( int code )
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
		return null;
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
		Class[] classes = new Class[] { GLES11.class };
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
	 * Sets an orthographic projection, origin in the bottom-left
	 * 
	 * @param desiredWidth
	 * @param desiredHeight
	 * @param screenWidth
	 * @param screenHeight
	 */
	public static void scaledOrtho( int desiredWidth, int desiredHeight, int screenWidth,
			int screenHeight )
	{
		GLES10.glMatrixMode( GL10.GL_PROJECTION );
		GLES10.glLoadIdentity();
		GLES10.glOrthof( 0, desiredWidth, 0, desiredHeight, -1, 1 );

		GLES10.glMatrixMode( GL10.GL_MODELVIEW );
		GLES10.glLoadIdentity();

		GLES10.glViewport( 0, 0, screenWidth, screenHeight );
	}

	/**
	 * Throws {@link GLException} if {@link GL11#glGetError()} returns
	 * anything other than {@link GL11#GL_NO_ERROR}
	 * 
	 * @throws GLException
	 */
	public static void checkGLError() throws GLException
	{
		int err = GLES10.glGetError();
		if( err != GL10.GL_NO_ERROR )
		{
			throw new GLException( err );
		}
	}

	/**
	 * Enables the client state for using vertex arrays and VBOs
	 */
	public static void enableVertexArrays()
	{
		GLES10.glEnableClientState( GLES10.GL_VERTEX_ARRAY );
		GLES10.glEnableClientState( GLES10.GL_TEXTURE_COORD_ARRAY );
		GLES10.glEnableClientState( GLES10.GL_COLOR_ARRAY );
	}
}
