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

package com.rugl.texture;

import org.lwjgl.util.Point;
import org.lwjgl.util.ReadableDimension;
import org.lwjgl.util.WritableDimension;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.gl.State;
import com.rugl.texture.TextureFactory.GLTexture;

/**
 * It's a texture, residing inside a larger texture
 * 
 * @author ryanm
 */
public class Texture implements ReadableDimension
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
	private final Image sourceImage;

	/**
	 * The bottom left corner, in pixels
	 */
	private final Point pixelOrigin;

	private final ReadableDimension size;

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
		size = sourceImage;
		pixelOrigin =
				new Point( ( int ) ( origin.x * parent.getWidth() ),
						( int ) ( origin.y * parent.getHeight() ) );
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
		size = parent;

		pixelOrigin =
				new Point( ( int ) ( origin.x * parent.getWidth() ),
						( int ) ( origin.y * parent.getHeight() ) );
	}

	/**
	 * Applies this texture to a rendering {@link State}
	 * 
	 * @param state
	 * @return The altered state
	 */
	public State applyTo( State state )
	{
		parent.regenerateMipmaps();

		if( state.texture.id != getTextureID() )
		{
			state = state.with( state.texture.with( getTextureID() ) );
		}

		return state;
	}

	/**
	 * Gets the OpenGL handle for this texture
	 * 
	 * @return the OpenGL texture ID
	 */
	public int getTextureID()
	{
		return parent.id();
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
		for( int i = 0; i < texCoords.length; i += 2 )
		{
			texCoords[ i ] = origin.x + extent.x * texCoords[ i ];
			texCoords[ i + 1 ] = origin.y + extent.y * texCoords[ i + 1 ];
		}
	}

	/**
	 * Gets the texture's image source. Note that changing this won't
	 * alter the texture
	 * 
	 * @return The source image
	 */
	public Image getSourceImage()
	{
		return sourceImage;
	}

	@Override
	public int getHeight()
	{
		return size.getHeight();
	}

	@Override
	public void getSize( WritableDimension dest )
	{
		size.getSize( dest );
	}

	@Override
	public int getWidth()
	{
		return size.getWidth();
	}

	/**
	 * @return the x position of this texture within the parent
	 *         texture, in pixels
	 */
	public int getXPosition()
	{
		return pixelOrigin.getX();
	}

	/**
	 * @return the y position of this texture within the parent
	 *         texture, in pixels
	 */
	public int getYPosition()
	{
		return pixelOrigin.getY();
	}

	@Override
	public String toString()
	{
		return size.getWidth() + "x" + size.getHeight() + " @ (" + pixelOrigin.getX()
				+ ", " + pixelOrigin.getY() + ")";
	}
}
