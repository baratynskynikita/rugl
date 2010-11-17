/*
 * Copyright (c) 2007, Ryan McNally All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <ORGANIZATION> nor
 * the names of its contributors may be used to endorse or promote
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

package com.ryanm.droid.rugl.texture;

import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.gl.enums.MinFilter;
import com.ryanm.droid.rugl.gl.facets.TextureState.Filters;
import com.ryanm.droid.rugl.texture.TextureFactory.GLTexture;
import com.ryanm.droid.rugl.util.geom.Vector2f;

import android.graphics.Point;

/**
 * It's a texture, residing inside a larger texture
 * 
 * @author ryanm
 */
public class Texture
{
	/**
	 * The parent texture
	 */
	public final GLTexture parent;

	/**
	 * The bottom left corner of the texture, in texture coordinates
	 */
	private final Vector2f origin;

	/**
	 * The vector from the top bottom corner to the top right, in
	 * texture coordinates
	 */
	private final Vector2f extent;

	/**
	 * The source of the texture data
	 */
	public final Image sourceImage;

	/**
	 * The bottom left corner, in pixels
	 */
	private final Point pixelOrigin;

	/***/
	public final int width;

	/***/
	public final int height;

	/**
	 * Constructs a new Texture
	 * 
	 * @param parent
	 * @param bottomLeft
	 * @param topRight
	 * @param source
	 */
	Texture( GLTexture parent, Vector2f bottomLeft, Vector2f topRight, Image source )
	{
		this.parent = parent;
		origin = bottomLeft;
		extent = Vector2f.sub( topRight, bottomLeft, null );
		sourceImage = source;
		width = sourceImage.width;
		height = sourceImage.height;
		pixelOrigin =
				new Point( ( int ) ( origin.x * parent.width ),
						( int ) ( origin.y * parent.height ) );
	}

	/**
	 * Constructs a new Texture
	 * 
	 * @param parent
	 */
	Texture( GLTexture parent )
	{
		this.parent = parent;
		origin = new Vector2f( 0, 0 );
		extent = new Vector2f( 1, 1 );
		sourceImage = null;
		width = parent.width;
		height = parent.height;

		pixelOrigin =
				new Point( ( int ) ( origin.x * parent.width ),
						( int ) ( origin.y * parent.height ) );
	}

	/**
	 * Applies this texture to a rendering {@link State}
	 * 
	 * @param state
	 * @return The altered state
	 */
	public State applyTo( State state )
	{
		if( state.texture.id != parent.id() )
		{
			state = state.with( state.texture.with( parent.id() ) );
		}

		if( !parent.mipmap )
		{
			// no mipmaps, so try for the closest compatible filter mode
			Filters f = state.texture.filter;
			if( f.min == MinFilter.LINEAR_MIPMAP_LINEAR
					|| f.min == MinFilter.LINEAR_MIPMAP_NEAREST )
			{
				f = f.with( MinFilter.LINEAR );
			}
			else if( f.min == MinFilter.NEAREST_MIPMAP_LINEAR
					|| f.min == MinFilter.NEAREST_MIPMAP_NEAREST )
			{
				f = f.with( MinFilter.NEAREST );
			}

			if( f != state.texture.filter )
			{
				state = state.with( state.texture.with( f ) );
			}
		}

		return state;
	}

	/**
	 * Translates s and t coordinates into values that can be passed to
	 * openGL.
	 * 
	 * @param s
	 *           The desired s coordinate, in range 0 to 1
	 * @param t
	 *           The desired t coordinate, in range 0 to 1
	 * @param dest
	 *           A vector2f in which to store the result, or null
	 * @return The texture coordinates in terms of the containing
	 *         openGL texture
	 */
	public Vector2f getTexCoords( float s, float t, Vector2f dest )
	{
		if( dest == null )
		{
			dest = new Vector2f();
		}

		dest.x = origin.x + extent.x * s;
		dest.y = origin.y + extent.y * t;

		return dest;
	}

	/**
	 * Translates s and t coordinates into values that can be passed to
	 * openGL.
	 * 
	 * @param coords
	 *           The desired texture coordinates, as if the texture
	 *           used up a whole openGL texture
	 * @param dest
	 *           A vector2f in which to store the result, or null
	 * @return The texture coordinates in terms of the containing
	 *         openGL texture
	 */
	public Vector2f getTexCoords( Vector2f coords, Vector2f dest )
	{
		return getTexCoords( coords.x, coords.y, dest );
	}

	/**
	 * Adjusts the supplied texture coordinates (which are in terms of
	 * the entire texture object to point to this subtexture
	 * 
	 * @param texCoords
	 */
	public void correctTexCoords( float[] texCoords )
	{
		if( origin.x != 0 || origin.y != 0 || extent.x != 1 || extent.y != 1 )
		{
			for( int i = 0; i < texCoords.length; i += 2 )
			{
				texCoords[ i ] = origin.x + extent.x * texCoords[ i ];
				texCoords[ i + 1 ] = origin.y + extent.y * texCoords[ i + 1 ];
			}
		}
	}

	/**
	 * @return the x position of this texture within the parent
	 *         texture, in pixels
	 */
	public int getXPosition()
	{
		return pixelOrigin.x;
	}

	/**
	 * @return the y position of this texture within the parent
	 *         texture, in pixels
	 */
	public int getYPosition()
	{
		return pixelOrigin.y;
	}

	@Override
	public String toString()
	{
		return width + "x" + height + " @ (" + pixelOrigin.x + ", " + pixelOrigin.y + ")";
	}
}
