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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.ReadableDimension;
import org.lwjgl.util.WritableDimension;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.DisplayConfigurable;
import com.rugl.GameBox;
import com.rugl.texture.Image.Format;
import com.rugl.util.GLUtil;
import com.rugl.util.RectanglePacker;

/**
 * Builds and manages {@link Texture}s. Textures build through this
 * class will be automagically restored in the event of the loss of
 * the opengl context
 * 
 * @author ryanm
 */
public class TextureFactory
{
	/**
	 * A list of OpenGL textures
	 */
	private static List<GLTexture> textures = new LinkedList<GLTexture>();

	/**
	 * The default dimension of the OpenGL textures
	 */
	private static int textureDimension = 1024;

	static
	{
		GameBox.dispConf.addListener( new DisplayConfigurable.Listener() {
			@Override
			public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
					boolean fsaa )
			{
				if( fsaa )
				{
					recreateTextures();
				}
			}
		} );
	}

	/**
	 * Called in response to the opengl context going away, perhaps as
	 * a result of the display mode changing
	 */
	public static void recreateTextures()
	{
		IntBuffer texNames = BufferUtils.createIntBuffer( textures.size() );
		for( GLTexture glt : textures )
		{
			texNames.put( glt.id );
		}
		texNames.flip();

		GL11.glDeleteTextures( texNames );

		for( GLTexture glt : textures )
		{
			glt.recreate();
		}
	}

	/**
	 * Gets the current list of textures
	 * 
	 * @return an unmodifiable list of {@link GLTexture} objects
	 */
	public static List<GLTexture> getTextures()
	{
		return Collections.unmodifiableList( textures );
	}

	/**
	 * Loads the specified image into OpenGL. Does not specify which
	 * texture the image will reside on, and will create new OpenGL
	 * textures as needed
	 * 
	 * @param image
	 *           the texture image
	 * @param lonesome
	 *           <code>true</code> if this texture is to be allocated
	 *           in a gl texture of minimal size, <code>false</code>
	 *           for the default size
	 * @param mipmap
	 *           <code>true</code> to generate mipmaps, false otherwise
	 * @return A {@link Texture} object, or null if it was not able to
	 *         be constructed
	 */
	public static Texture buildTexture( Image image, boolean lonesome, boolean mipmap )
	{
		if( lonesome )
		{
			try
			{
				GLTexture parent =
						new GLTexture( image.format, image, mipmap, lonesome ? 0 : 1 );
				textures.add( parent );
				Texture texture = parent.addImage( image );

				assert texture.getWidth() == image.getWidth();
				assert texture.getHeight() == image.getHeight();

				return texture;
			}
			catch( OpenGLException e )
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			for( GLTexture tex : textures )
			{
				// match formats
				if( tex.mipmap == mipmap && tex.format == image.format )
				{
					// try to insert
					Texture t = tex.addImage( image );

					if( t != null )
					{ // it worked!
						return t;
					}
					// else there was no room
				}
			}

			// build a new texture
			try
			{
				GLTexture parent = new GLTexture( image.format, null, mipmap, 1 );
				textures.add( parent );
				return parent.addImage( image );
			}
			catch( OpenGLException e )
			{ // we were not able to build the texture
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Requests that a new texture be built
	 * 
	 * @param format
	 *           The desired format
	 * @param minDimensions
	 *           The minimum dimensions
	 * @param mipmap
	 *           <code>true</code> to build mipmaps, <code>false</code>
	 *           otherwise
	 * @param border
	 *           The border, in pixels, that will be placed around
	 *           images added to this texture
	 * @return A dummy {@link Texture} object
	 */
	public static GLTexture createTexture( Format format, ReadableDimension minDimensions,
			boolean mipmap, int border )
	{
		try
		{
			GLTexture parent = new GLTexture( format, minDimensions, mipmap, border );
			textures.add( parent );

			return parent;
		}
		catch( OpenGLException e )
		{ // we were not able to build the texture
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Frees up the space allocated to the supplied texture
	 * 
	 * @param t
	 *           The texture to delete
	 * @return <code>true</code> if the texture was released,
	 *         <code>false</code> if it could not be found
	 */
	public static boolean deleteTexture( Texture t )
	{
		for( GLTexture tex : textures )
		{
			if( tex.release( t ) )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Regenerates any dirty mipmaps. e.g.: If there's a texture that
	 * has has data added after creation, that data will not have made
	 * it into the lower levels. This method makes sure that it does
	 */
	public static void regenerateMipMaps()
	{
		for( GLTexture t : textures )
		{
			t.regenerateMipmaps();
		}
	}

	/**
	 * Clears the state of all textures. Called in response to the
	 * OpenGL context being recreated. Does not release OpenGL
	 * resources
	 */
	public static void clear()
	{
		textures.clear();
	}

	/**
	 * Encapsulates an OpenGL texture object
	 * 
	 * @author ryanm
	 */
	public static class GLTexture implements ReadableDimension
	{
		/**
		 * The texture id
		 */
		private int id;

		/**
		 * The texture format
		 */
		public final Format format;

		/**
		 * The texture size
		 */
		public final ReadableDimension size;

		/**
		 * If mipmaps are build for the texture
		 */
		public final boolean mipmap;

		private boolean mipmapDirty = false;

		private List<Texture> residentTextures = new LinkedList<Texture>();

		private RectanglePacker<Image> packer;

		private Texture pan = null;

		/**
		 * Builds a new GLTexture
		 * 
		 * @param format
		 *           The texture format
		 * @param dim
		 *           The texture size, or null for the default
		 * @param mipmap
		 *           Whether to mipmap or not
		 */
		private GLTexture( Format format, ReadableDimension dim, boolean mipmap, int border )
				throws OpenGLException
		{
			this.format = format;
			size =
					dim != null ? new Dimension( GLUtil.nextPowerOf2( dim.getWidth() ),
							GLUtil.nextPowerOf2( dim.getHeight() ) ) : new Dimension(
							textureDimension, textureDimension );
			this.mipmap = mipmap;

			packer = new RectanglePacker<Image>( size.getWidth(), size.getHeight(), border );

			recreate();
		}

		private void recreate()
		{
			id = GL11.glGenTextures();

			GL11.glBindTexture( GL11.GL_TEXTURE_2D, id );

			GL11.glPixelStorei( GL11.GL_UNPACK_ALIGNMENT, format.bytes );

			ByteBuffer data =
					BufferUtils.createByteBuffer( size.getWidth() * size.getHeight()
							* format.bytes );

			if( mipmap )
			{
				GLU.gluBuild2DMipmaps( GL11.GL_TEXTURE_2D, format.glInternalFormat,
						size.getWidth(), size.getHeight(), format.glFormat,
						GL11.GL_UNSIGNED_BYTE, data );
			}
			else
			{
				GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0, format.glInternalFormat,
						size.getWidth(), size.getHeight(), 0, format.glFormat,
						GL11.GL_UNSIGNED_BYTE, data );
			}
			GLUtil.checkGLError();

			for( Texture t : residentTextures )
			{
				RectanglePacker.Rectangle rpr = packer.findRectangle( t.getSourceImage() );

				if( rpr != null )
				{
					writeToTexture( rpr, t.getSourceImage().getData() );
				}
			}

			regenerateMipmaps();
		}

		/**
		 * Gets the texture ID
		 * 
		 * @return the opengl texture name
		 */
		public int id()
		{
			return id;
		}

		/**
		 * Gets the resident {@link Texture} objects
		 * 
		 * @return an unmodifiable list of the resident {@link Texture}
		 *         objects
		 */
		public List<Texture> getResidents()
		{
			return Collections.unmodifiableList( residentTextures );
		}

		/**
		 * Gets the {@link Texture} that displays the whole of this
		 * {@link GLTexture}
		 * 
		 * @return A {@link Texture} that encompasses the entirety of
		 *         this {@link GLTexture}
		 */
		public Texture getTexture()
		{
			if( pan == null )
			{
				pan = new Texture( this );
			}

			return pan;
		}

		/**
		 * Regenerates any dirty mimaps.
		 */
		public void regenerateMipmaps()
		{
			if( mipmapDirty )
			{
				ByteBuffer data1 =
						BufferUtils.createByteBuffer( size.getWidth() * size.getHeight()
								* format.bytes );
				ByteBuffer data = data1;

				GL11.glBindTexture( GL11.GL_TEXTURE_2D, id );

				// read the texture out
				GL11.glGetTexImage( GL11.GL_TEXTURE_2D, 0, format.glFormat,
						GL11.GL_UNSIGNED_BYTE, data );

				data.rewind();

				// regenerate the mipmaps
				GLU.gluBuild2DMipmaps( GL11.GL_TEXTURE_2D, format.glInternalFormat,
						size.getWidth(), size.getHeight(), format.glFormat,
						GL11.GL_UNSIGNED_BYTE, data );

				mipmapDirty = false;
			}
		}

		/**
		 * Attempts to add an image to this {@link GLTexture}
		 * 
		 * @param image
		 *           The {@link Image} to add
		 * @return The resulting {@link Texture}, or <code>null</code>
		 *         if it didn't fit
		 */
		public Texture addImage( Image image )
		{
			assert image.format == format;

			RectanglePacker.Rectangle rpr =
					packer.insert( image.getWidth(), image.getHeight(), image );

			if( rpr != null )
			{
				assert rpr.width == image.getWidth();
				assert rpr.height == image.getHeight();

				writeToTexture( rpr, image.getData() );

				Vector2f bottomleft = new Vector2f( rpr.x, rpr.y );
				Vector2f topRight = new Vector2f( rpr.x + rpr.width, rpr.y + rpr.height );

				bottomleft.x /= size.getWidth();
				bottomleft.y /= size.getHeight();
				topRight.x /= size.getWidth();
				topRight.y /= size.getHeight();

				Texture t = new Texture( this, bottomleft, topRight, image );
				residentTextures.add( t );

				return t;
			}
			else
			{
				return null;
			}
		}

		/**
		 * Releases the space reserved for the supplied texture. Note
		 * that the data will still be resident on this
		 * {@link GLTexture} until it is overwritten or the mipmap is
		 * regenerated
		 * 
		 * @param t
		 *           The {@link Texture} to release
		 * @return <code>true</code> if the texture was released,
		 *         <code>false</code> if was not resident on this
		 *         {@link GLTexture}
		 */
		private boolean release( Texture t )
		{
			if( residentTextures.remove( t ) )
			{
				packer.remove( t.getSourceImage() );
				return true;
			}
			else
			{
				return false;
			}
		}

		private void writeToTexture( RectanglePacker.Rectangle r, ByteBuffer data )
		{
			assert r.width * r.height * format.bytes == data.capacity() : r + " * "
					+ format.bytes + " != " + data.capacity();

			GL11.glBindTexture( GL11.GL_TEXTURE_2D, id );

			GL11.glPixelStorei( GL11.GL_UNPACK_ALIGNMENT, format.bytes );

			GL11.glTexSubImage2D( GL11.GL_TEXTURE_2D, 0, r.x, r.y, r.width, r.height,
					format.glFormat, GL11.GL_UNSIGNED_BYTE, data );

			GLUtil.checkGLError();

			if( mipmap )
			{
				mipmapDirty = true;
			}
		}

		@Override
		public String toString()
		{
			StringBuilder buff = new StringBuilder( "GLTexture id = " );
			buff.append( id );
			buff.append( " format = " );
			buff.append( format );
			buff.append( " mimap = " );
			buff.append( mipmap );
			buff.append( " size = [" );
			buff.append( size.getWidth() );
			buff.append( "," );
			buff.append( size.getHeight() );
			buff.append( "]" );
			buff.append( " residents: " );
			buff.append( residentTextures.size() );

			if( residentTextures.size() < 10 )
			{
				for( Texture t : residentTextures )
				{
					buff.append( "\n\t" );
					buff.append( t.toString() );
				}
			}

			return buff.toString();
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
	}
}
