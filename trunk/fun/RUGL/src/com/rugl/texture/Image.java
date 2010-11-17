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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.ReadableDimension;
import org.lwjgl.util.WritableDimension;

/**
 * Represents a chunk of image data.
 * 
 * @author ryanm
 */
public class Image implements ReadableDimension
{
	/**
	 * Flags for different image formats
	 * 
	 * @author ryanm
	 */
	public static enum Format
	{
		/**
		 * Uses 8 bits for each component
		 */
		RGBA( 4, GL11.GL_RGBA8, GL11.GL_RGBA )
		{
			@Override
			public void write( int pixel, ByteBuffer buffer )
			{
				buffer.put( ( byte ) ( pixel >> 16 & 0xFF ) );
				buffer.put( ( byte ) ( pixel >> 8 & 0xFF ) );
				buffer.put( ( byte ) ( pixel >> 0 & 0xFF ) );
				buffer.put( ( byte ) ( pixel >> 24 & 0xFF ) );
			}
		},

		/**
		 * Uses 8 bits for each component. Note that when loading
		 * images, the green channel determines the luminance value
		 */
		LUMINANCE_ALPHA( 2, GL11.GL_LUMINANCE8_ALPHA8, GL11.GL_LUMINANCE_ALPHA )
		{
			@Override
			public void write( int pixel, ByteBuffer buffer )
			{
				buffer.put( ( byte ) ( pixel >> 8 & 0xFF ) );
				buffer.put( ( byte ) ( pixel >> 24 & 0xFF ) );
			}
		};

		/**
		 * The number of bytes that the format uses per pixel
		 */
		public final int bytes;

		/**
		 * The internal OpenGL format when loading this image as a
		 * texture, e.g.: GL_RGBA8, GL_LUMINANCE8_ALPHA8
		 */
		public final int glInternalFormat;

		/**
		 * The OpenGL format, e.g.: GL_RGBA, GL_LUMINANCE_ALPHA
		 */
		public final int glFormat;

		private Format( int bytes, int glInternalFormat, int glFormat )
		{
			this.bytes = bytes;
			this.glInternalFormat = glInternalFormat;
			this.glFormat = glFormat;
		}

		/**
		 * Writes the appropriate values to a buffer for a given pixel
		 * 
		 * @param argb
		 *           A pixel, in packed argb form
		 * @param buffer
		 *           The buffer to write to
		 */
		public abstract void write( int argb, ByteBuffer buffer );
	};

	/**
	 * The image format
	 */
	public final Format format;

	/**
	 * The image width
	 */
	private final int width;

	/**
	 * The image height
	 */
	private final int height;

	/**
	 * The image data
	 */
	private final ByteBuffer data;

	/**
	 * @param width
	 * @param height
	 * @param format
	 * @param data
	 */
	public Image( int width, int height, Format format, ByteBuffer data )
	{
		this.width = width;
		this.height = height;
		this.format = format;
		this.data = data;

		assert this.width * this.height * this.format.bytes == this.data.capacity();
	}

	/**
	 * Constructs an image from the data in the buffer, which is of the
	 * format "int:width int:height int:formatOrdinal data"
	 * 
	 * @param input
	 *           contains the image data
	 */
	public Image( ByteBuffer input )
	{
		width = input.getInt();
		height = input.getInt();

		int formatOrdinal = input.getInt();
		assert formatOrdinal >= 0 && formatOrdinal < Format.values().length : "Format with ordinal "
				+ formatOrdinal + " not found";
		format = Format.values()[ formatOrdinal ];

		data = input.slice();

		assert width * height * format.bytes == data.capacity();
	}

	/**
	 * Reads an image from a stream
	 * 
	 * @param is
	 * @throws IOException
	 */
	public Image( InputStream is ) throws IOException
	{
		DataInputStream dis = new DataInputStream( is );

		width = dis.readInt();
		height = dis.readInt();

		int formatOrdinal = dis.readInt();
		assert formatOrdinal >= 0 && formatOrdinal < Format.values().length : "Format with ordinal "
				+ formatOrdinal + " not found";
		format = Format.values()[ formatOrdinal ];

		byte[] bd = new byte[ width * height * format.bytes ];

		dis.readFully( bd );

		data = BufferUtils.createByteBuffer( bd.length );
		data.put( bd );

		assert width * height * format.bytes == data.capacity();
	}

	/**
	 * Reads an image from a file
	 * 
	 * @param fileName
	 * @return An {@link Image}
	 * @throws IOException
	 */
	public static Image loadImage( String fileName ) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile( fileName, "r" );
		FileChannel ch = raf.getChannel();
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_ONLY, 0, raf.length() );

		Image i = new Image( buffer );

		ch.close();

		return i;
	}

	/**
	 * Write the image to a buffer, in a format suitable for reading
	 * with {@link #Image(ByteBuffer)}
	 * 
	 * @param output
	 *           The buffer to write to
	 */
	public void write( ByteBuffer output )
	{
		assert output.remaining() >= dataSize();

		output.putInt( width );
		output.putInt( height );
		output.putInt( format.ordinal() );

		output.put( data );
	}

	/**
	 * Saves the image to a file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void write( String fileName ) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile( fileName, "rw" );
		FileChannel ch = raf.getChannel();
		int fileLength = dataSize();
		raf.setLength( fileLength );
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );

		write( buffer );

		buffer.force();
		ch.close();
	}

	/**
	 * Calculates the size of buffer needed to store this image
	 * 
	 * @return The necessary number of bytes
	 */
	public int dataSize()
	{
		return data.capacity() + 3 * 4;
	}

	/**
	 * Gets the image data. Also rewinds the buffer, so be careful with
	 * concurrent access
	 * 
	 * @return the image data
	 */
	public ByteBuffer getData()
	{
		data.rewind();
		return data;
	}

	@Override
	public int getWidth()
	{
		return width;
	}

	@Override
	public int getHeight()
	{
		return height;
	}

	@Override
	public void getSize( WritableDimension dest )
	{
		dest.setSize( width, height );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof Image )
		{
			Image i = ( Image ) obj;
			if( format == i.format && height == i.height && width == i.width
					&& data.capacity() == i.data.capacity() )
			{
				// check image data
				for( int j = 0; j < data.capacity(); j++ )
				{
					if( data.get( j ) != i.data.get( j ) )
					{
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}
}
