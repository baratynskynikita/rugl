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

package com.rugl.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import com.rugl.texture.Image;
import com.rugl.texture.Image.Format;

/**
 * Utility methods for building {@link Image}s
 * 
 * @author ryanm
 */
public class ImageFactory
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		if( args.length == 0 )
		{
			System.out.println( "ImageFactory - coverts images to rugl format" );
			System.out
					.println( "\t prepend -d <shrinkfactor> <scanSize> to generate distance maps" );
		}
		else
		{
			if( args[ 0 ].equals( "-d" ) )
			{
				int scale = Integer.parseInt( args[ 1 ] );
				int scan = Integer.parseInt( args[ 2 ] );

				for( int i = 3; i < args.length; i++ )
				{
					try
					{
						System.out.println( "Loading " + args[ i ] + "..." );
						BufferedImage bi = ImageIO.read( new File( args[ i ] ) );

						System.out.println( "Padding..." );
						BufferedImage pbi =
								new BufferedImage( bi.getWidth() + 2 * scan, bi.getHeight() + 2
										* scan, BufferedImage.TYPE_INT_ARGB );

						pbi.getGraphics().drawImage( bi, scan, scan, null );

						System.out.println( "Distance field generation..." );
						System.out.println( pbi.getWidth() + "x" + pbi.getHeight() + " -> "
								+ pbi.getWidth() / scale + "x" + pbi.getHeight() / scale );

						DistanceFieldFilter dff =
								new DistanceFieldFilter( pbi, scale, scan,
										new DistanceFieldFilter.Listener() {
											private final int maxStars = 60;

											private int stars = 0;

											@Override
											public void progress( float progress )
											{
												while( stars < progress * maxStars )
												{
													stars++;
													System.out.print( "*" );
												}
											}

											@Override
											public void finished( BufferedImage result )
											{
												while( stars < maxStars )
												{
													stars++;
													System.out.print( "*" );
												}
												System.out.println( " complete" );
											}
										} );
						dff.run();

						ImageIO.write( dff.getResult(), "PNG", new File( "outTest" + ( i - 2 )
								+ ".png" ) );

						Image ri = buildImage( dff.getResult(), Format.LUMINANCE_ALPHA );
						ri.write( args[ i ].substring( 0, args[ i ].lastIndexOf( "." ) )
								+ ".ruglimg" );
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				for( int i = 0; i < args.length; i++ )
				{
					Image ri = loadImage( args[ i ], Format.RGBA );

					try
					{
						ri.write( args[ i ] + ".ruglimg" );
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Loads an image from a file
	 * 
	 * @param fileName
	 * @param format
	 *           The destination format of the {@link Image}
	 * @return The image
	 */
	public static Image loadImage( String fileName, Image.Format format )
	{
		try
		{
			return buildImage( ImageIO.read( new File( fileName ) ), format );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Loads an image from a stream
	 * 
	 * @param is
	 * @param format
	 *           The destination format of the {@link Image}
	 * @return The image
	 */
	public static Image loadImage( InputStream is, Image.Format format )
	{
		try
		{
			return buildImage( ImageIO.read( is ), format );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Loads an image from a {@link URL}
	 * 
	 * @param url
	 * @param format
	 *           The destination format of the {@link Image}
	 * @return The image
	 */
	public static Image loadImage( URL url, Image.Format format )
	{
		try
		{
			return buildImage( ImageIO.read( url ), format );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds an image
	 * 
	 * @param bi
	 *           the source data
	 * @param format
	 *           The destination format
	 * @return The image
	 */
	public static Image buildImage( BufferedImage bi, Image.Format format )
	{
		if( bi.getType() != BufferedImage.TYPE_INT_ARGB )
		{
			BufferedImage dest =
					new BufferedImage( bi.getWidth(), bi.getHeight(),
							BufferedImage.TYPE_INT_ARGB );

			dest.getGraphics().drawImage( bi, 0, 0, null );
			dest.getGraphics().dispose();
			bi = dest;
		}

		ByteBuffer buff = getData( bi, format );

		assert buff.capacity() == bi.getWidth() * bi.getHeight() * format.bytes;

		return new Image( bi.getWidth(), bi.getHeight(), format, buff );
	}

	/**
	 * Extracts the data from a {@link BufferedImage}, and converts
	 * from argb to rgba
	 * 
	 * @param bi
	 *           a {@link BufferedImage}, in argb format
	 * @param format
	 *           The destination format
	 * @return The image data
	 */
	public static ByteBuffer getData( BufferedImage bi, Image.Format format )
	{
		assert bi.getType() == BufferedImage.TYPE_INT_ARGB;

		ByteBuffer unpackedPixels =
				BufferUtils.createByteBuffer( bi.getWidth() * bi.getHeight() * format.bytes );

		for( int y = bi.getHeight() - 1; y >= 0; y-- )
		{
			for( int x = 0; x < bi.getWidth(); x++ )
			{
				int p = bi.getRGB( x, y );
				format.write( p, unpackedPixels );
			}
		}

		unpackedPixels.flip();

		return unpackedPixels;
	}
}
