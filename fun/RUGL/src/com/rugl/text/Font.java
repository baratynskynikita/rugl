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

package com.rugl.text;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.TexturedShape;
import com.rugl.gl.enums.MagFilter;
import com.rugl.gl.enums.MinFilter;
import com.rugl.gl.facets.TextureState.Filters;
import com.rugl.renderer.RenderUtils;
import com.rugl.texture.Image.Format;
import com.rugl.texture.Texture;
import com.rugl.texture.TextureFactory;
import com.rugl.texture.TextureFactory.GLTexture;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;
import com.rugl.util.RectanglePacker;
import com.ryanm.util.geom.Rectanglef;

/**
 * Has some global characteristics, a number of {@link Glyph} s, and a
 * mapping of characters to {@link Glyph}s. A {@link Font} can be
 * constructed from various sources. This class started life in the
 * SPGL, but has been fairly extensively jiggered around - converted
 * to floating point etc
 */
public final class Font
{
	/** Handy bounds */
	private static final Rectanglef tempBounds = new Rectanglef();

	private static final Vector2f tempPoint = new Vector2f();

	private static final Vector2f tempOrigin = new Vector2f();

	private static final Vector2f tempExtent = new Vector2f();

	/**
	 * The source of kerning information that will be consulted
	 * whenever a glyph is added to the font. If <code>null</code>,
	 * kerning will be ignored
	 */
	public KerningSource kerningSource = null;

	/**
	 * The font's name
	 */
	public final String name;

	/**
	 * If the font is bold or not
	 */
	public final boolean bold;

	/**
	 * If the font is italic or not
	 */
	public final boolean italic;

	/**
	 * The distance between the line and the top of the font
	 */
	public final int ascent;

	/**
	 * The distance that this font descends below the line
	 */
	public final int descent;

	/**
	 * The spacing between one line's descent and the next line's
	 * ascent
	 */
	public final int leading;

	/**
	 * The distance between lines
	 */
	public final int size;

	/**
	 * The font's glyphs
	 */
	private List<Glyph> glyphs = new LinkedList<Glyph>();

	/**
	 * The font's images
	 */
	private Set<GlyphImage> glyphImages = new HashSet<GlyphImage>();

	/**
	 * Maps Unicode characters to glyphs. This approach is OK for ASCII
	 * characters, since they live at the bottom of the unicode tables,
	 * but something more clever needs to be done for higher characters
	 * to avoid allocating enormous mostly empty arrays
	 */
	private Glyph[] map;

	/**
	 * Indicates if this font uses distance field textures
	 */
	public final boolean distanceField;

	/**
	 * The texture in which the glyphs reside
	 */
	private transient Texture texture = null;

	/**
	 * Builds an initially empty font
	 * 
	 * @param name
	 *           The font name
	 * @param bold
	 *           if the font is bold or not
	 * @param italic
	 *           If the font is italic or not
	 * @param size
	 *           The size in points of the font
	 * @param ascent
	 *           The font's maximum ascent, in points
	 * @param descent
	 *           The font's maximum descent, in points
	 * @param leading
	 *           The distance between one line's descent and the next's
	 *           ascent
	 * @param distanceField
	 */
	public Font( String name, boolean bold, boolean italic, int size, int ascent,
			int descent, int leading, boolean distanceField )
	{
		this.name = name;
		this.bold = bold;
		this.italic = italic;
		this.size = size;
		this.ascent = ascent;
		this.descent = descent;
		this.leading = leading;
		this.distanceField = distanceField;
	}

	/**
	 * Reads a {@link Font} from a buffer
	 * 
	 * @param data
	 *           the buffer
	 */
	public Font( ByteBuffer data )
	{
		byte[] nd = new byte[ data.getInt() ];
		data.get( nd );
		name = new String( nd );

		bold = data.get() != 0;
		italic = data.get() != 0;
		size = data.getInt();
		ascent = data.getInt();
		descent = data.getInt();
		leading = data.getInt();
		distanceField = data.get() != 0;

		int gi = data.getInt();
		for( int i = 0; i < gi; i++ )
		{
			glyphImages.add( new GlyphImage( data ) );
		}

		GlyphImage[] imageArray =
				glyphImages.toArray( new GlyphImage[ glyphImages.size() ] );

		int g = data.getInt();
		for( int i = 0; i < g; i++ )
		{
			addGlyph( new Glyph( data, imageArray ) );
		}
	}

	/**
	 * Reads a {@link Font} from a stream
	 * 
	 * @param is
	 * @throws IOException
	 */
	public Font( InputStream is ) throws IOException
	{
		DataInputStream dis = new DataInputStream( is );

		int nd = dis.readInt();
		byte[] nb = new byte[ nd ];
		dis.readFully( nb );
		name = new String( nb );

		bold = dis.readByte() != 0;
		italic = dis.readByte() != 0;
		size = dis.readInt();
		ascent = dis.readInt();
		descent = dis.readInt();
		leading = dis.readInt();
		distanceField = dis.readByte() != 0;

		int gi = dis.readInt();
		for( int i = 0; i < gi; i++ )
		{
			glyphImages.add( new GlyphImage( is ) );
		}

		GlyphImage[] imageArray =
				glyphImages.toArray( new GlyphImage[ glyphImages.size() ] );

		int g = dis.readInt();
		for( int i = 0; i < g; i++ )
		{
			addGlyph( new Glyph( is, imageArray ) );
		}
	}

	/**
	 * Reads a font from a file
	 * 
	 * @param fileName
	 * @return A {@link Font}
	 * @throws IOException
	 */
	public static Font readFont( String fileName ) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile( fileName, "r" );
		FileChannel ch = raf.getChannel();
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_ONLY, 0, raf.length() );

		Font f = new Font( buffer );

		ch.close();

		return f;
	}

	/**
	 * Writes a Font to a buffer
	 * 
	 * @param data
	 *           the buffer to write to
	 */
	public void write( ByteBuffer data )
	{
		byte[] nd = name.getBytes();
		data.putInt( nd.length );
		data.put( nd );

		data.put( ( byte ) ( bold ? 1 : 0 ) );
		data.put( ( byte ) ( italic ? 1 : 0 ) );
		data.putInt( size );
		data.putInt( ascent );
		data.putInt( descent );
		data.putInt( leading );
		data.put( ( byte ) ( distanceField ? 1 : 0 ) );

		data.putInt( glyphImages.size() );
		for( GlyphImage gi : glyphImages )
		{
			gi.write( data );
		}

		data.putInt( glyphs.size() );
		for( Glyph g : glyphs )
		{
			g.write( data );
		}
	}

	/**
	 * Write the {@link Font} to a file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void write( String fileName ) throws IOException
	{
		RandomAccessFile rf = new RandomAccessFile( fileName, "rw" );
		FileChannel ch = rf.getChannel();
		int fileLength = dataSize();
		rf.setLength( fileLength );
		MappedByteBuffer buffer = ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );

		write( buffer );

		buffer.force();
		ch.close();
	}

	/**
	 * Calculates the size of buffer needed to store this {@link Font}
	 * 
	 * @return The number of bytes used to store this font
	 */
	public int dataSize()
	{
		int bytes = 0;

		// name length
		bytes += 4;

		// name
		bytes += name.getBytes().length;

		// bold/italic
		bytes += 2;

		// size, ascent, descent, leading
		bytes += 4 * 4;

		// glyph image count
		bytes += 4;
		for( GlyphImage gi : glyphImages )
		{
			bytes += gi.dataSize();
		}

		// glyph count
		bytes += 4;
		for( Glyph g : glyphs )
		{
			// glyphs
			bytes += g.dataSize();
		}

		// distance field
		bytes += 1;

		return bytes;
	}

	/**
	 * Adds a glyph to this font
	 * 
	 * @param g
	 *           the glyph to add
	 */
	public void addGlyph( Glyph g )
	{
		if( map == null || g.character >= map.length )
		{
			Glyph[] newMap = new Glyph[ ( g.character + 10 ) ];

			if( map != null )
			{
				System.arraycopy( map, 0, newMap, 0, map.length );
			}

			map = newMap;
		}

		if( map[ g.character ] == null )
		{
			map[ g.character ] = g;

			// update kerning?
			if( kerningSource != null )
			{
				for( Glyph gl : glyphs )
				{
					gl.updateKerning( g.character,
							kerningSource.computeKerning( g.character, gl.character ) );

					g.updateKerning( gl.character,
							kerningSource.computeKerning( gl.character, g.character ) );
				}
			}

			// add glyph
			glyphs.add( g );

			glyphImages.add( g.image );
		}
	}

	/**
	 * Loads the image into an OpenGL texture and initialises the glyph
	 * texcoords
	 * 
	 * @param mipmap
	 *           If you're going to be zooming text, you might want to
	 *           build mipmaps
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise
	 */
	public boolean init( boolean mipmap )
	{
		if( texture == null )
		{
			List<GlyphImage> gImages = new ArrayList<GlyphImage>();
			gImages.addAll( glyphImages );

			// sort the glyphs into descending order of size
			Collections.sort( gImages, new Comparator<GlyphImage>() {
				@Override
				public int compare( GlyphImage o1, GlyphImage o2 )
				{
					int left = o1.image.getWidth() * o1.image.getHeight();
					int right = o2.image.getWidth() * o2.image.getHeight();

					// remember we want descending order
					return -( left - right );
				}
			} );

			// determine how big the texture should be
			Dimension d = calculateTextureSize();

			GLTexture glt =
					TextureFactory.createTexture( Format.LUMINANCE_ALPHA, d, mipmap, 1 );

			if( glt != null )
			{
				texture = glt.getTexture();

				boolean success = true;

				for( GlyphImage gi : gImages )
				{
					if( !gi.init( glt ) )
					{
						success = false;
					}
				}

				for( Glyph g : glyphs )
				{
					if( !g.image.init( glt ) )
					{ // a glyph has failed to init, remap it to the WTF
						map[ g.character ] = map[ '0' ];
						success = false;
					}
				}

				// this will be done for us when we render, but it doesn't
				// hurt to do it in init time
				glt.regenerateMipmaps();

				return success;
			}
		}

		return texture != null;
	}

	private Dimension calculateTextureSize()
	{
		// mean glyph size
		float mean = 0;
		for( GlyphImage g : glyphImages )
		{
			mean += g.image.getWidth();
			mean += g.image.getHeight();
		}
		mean /= glyphs.size() * 2;

		// initial guess
		int dim = GLUtil.nextPowerOf2( ( int ) ( mean * Math.sqrt( glyphs.size() ) ) ) / 2;
		dim = Math.max( 2, dim );

		Dimension d = new Dimension( dim / 2, dim );
		boolean growWidth = true;

		boolean success = false;

		do
		{
			// alternate expanding horizontally and vertically
			if( growWidth )
			{
				d.setWidth( d.getWidth() * 2 );
			}
			else
			{
				d.setHeight( d.getHeight() * 2 );
			}
			growWidth = !growWidth;

			RectanglePacker<GlyphImage> packer =
					new RectanglePacker<GlyphImage>( d.getWidth(), d.getHeight(), 1 );
			Iterator<GlyphImage> iter = glyphImages.iterator();
			boolean fit = true;
			while( fit && iter.hasNext() )
			{
				GlyphImage g = iter.next();

				fit &= packer.insert( g.image.getWidth(), g.image.getHeight(), g ) != null;
			}

			success = fit;
		}
		while( !success );

		return d;
	}

	/**
	 * Map a character to a glyph
	 * 
	 * @param c
	 *           The character
	 * @return The corresponding {@link Glyph}, or null
	 */
	public Glyph map( char c )
	{
		if( c >= map.length )
		{ // we don't have this char, use the WTF instead
			c = 0;
		}
		if( c == '\t' )
		{ // convert tabs to spaces
			c = ' ';
		}

		Glyph g = map[ c ];

		if( g != null )
		{
			return g;
		}
		else
		{
			assert map[ ( char ) 0 ] != null;

			return map[ ( char ) 0 ];
		}
	}

	/**
	 * Calculate the bounding box of a string if it was drawn at (0,0)
	 * 
	 * @param text
	 *           The text to measure
	 * @param dest
	 *           A destination rectangle for the bounds, or null
	 * @return The string bounds
	 */
	public Rectanglef getStringBounds( CharSequence text, Rectanglef dest )
	{
		if( dest == null )
		{
			dest = new Rectanglef();
		}

		dest.setBounds( 0, 0, 0, 0 );

		Glyph last = null;
		int penX = 0;
		for( int i = 0; i < text.length(); i++ )
		{
			Glyph next = map( text.charAt( i ) );

			next.image.getSize( tempBounds );
			next.getGlyphOffset( tempPoint );

			float kerning = last == null ? 0 : next.getKerningAfter( last.character );

			tempBounds.setLocation( tempPoint.getX() + penX - kerning, tempPoint.getY() );
			tempBounds.setWidth( Math.max( tempBounds.getWidth(), next.advance ) );

			dest.add( tempBounds );
			penX += next.advance + kerning;
			last = next;
		}

		return dest;
	}

	/**
	 * Calculates the rendered length of a string
	 * 
	 * @param text
	 *           The text to measure
	 * @return The length of the rendered text
	 */
	public float getStringLength( CharSequence text )
	{
		Glyph last = null;
		float length = 0;
		for( int i = 0; i < text.length(); i++ )
		{
			Glyph next = map( text.charAt( i ) );
			float kerning = last == null ? 0 : next.getKerningAfter( last.character );

			length += next.advance + kerning;

			last = next;
		}

		return length;
	}

	/**
	 * Calculates the glyph vertices if the string is being rendered at
	 * the origin. The dest array will have 4 * text.length elements
	 * written to it, in bl tl br tr format
	 * 
	 * @param text
	 *           The text to render
	 * @param dest
	 *           A destination vertex array, or null. If non-null, all
	 *           elements must also be non-null
	 * @param start
	 *           The element to start writing to in the dest array.
	 *           Ignored in dest is null;
	 * @return an array of glyph vertices
	 */
	public Vector3f[] getVertices( CharSequence text, Vector3f[] dest, int start )
	{
		int index = start;
		if( dest == null )
		{
			dest = new Vector3f[ 4 * text.length() ];
			for( int i = 0; i < dest.length; i++ )
			{
				dest[ i ] = new Vector3f();
			}
			index = 0;
		}

		Glyph last = null;
		float penX = 0;
		float penY = 0;
		for( int i = 0; i < text.length(); i++ )
		{
			char c = text.charAt( i );
			boolean nl = c == '\n';
			if( nl )
			{
				penY -= size;
				c = ' ';
				penX = 0;
			}

			Glyph next = map( c );

			next.image.getSize( tempBounds );
			next.getGlyphOffset( tempPoint );
			float kerning = last == null ? 0 : next.getKerningAfter( last.character );

			tempBounds.setLocation( tempPoint.getX() + penX + kerning, tempPoint.getY()
					+ penY );

			dest[ index++ ].set( tempBounds.getX(), tempBounds.getY(), 0 );
			dest[ index++ ].set( tempBounds.getX(),
					tempBounds.getY() + tempBounds.getHeight(), 0 );
			dest[ index++ ].set( tempBounds.getX() + tempBounds.getWidth(),
					tempBounds.getY(), 0 );
			dest[ index++ ].set( tempBounds.getX() + tempBounds.getWidth(),
					tempBounds.getY() + tempBounds.getHeight(), 0 );

			if( !nl )
			{
				penX += next.advance + kerning;
			}
			last = next;

		}

		return dest;
	}

	/**
	 * Builds a textured shape that will render some text. Text is
	 * rendered starting at the origin and advancing along the x-axis.
	 * See {@link TextLayout} for line-wrapping and such
	 * 
	 * @param text
	 *           The text to render
	 * @param c
	 *           The {@link Colour} of the text
	 * @return A {@link TexturedShape} that will render the text
	 */
	public TextShape buildTextShape( CharSequence text, int c )
	{
		assert texture != null : "Font " + name + " not initialised";
		assert text.length() != 0 : "Empty string";

		Vector3f[] verts = getVertices( text, null, 0 );
		Vector2f[] texcoords = getTexCoords( text, null, 0 );
		int[] indices = RenderUtils.makeQuads( verts.length, 0, null, 0 );

		TexturedShape ts =
				new TexturedShape( new ColouredShape( new Shape( Shape.extract( verts ),
						indices ), c, null ), Shape.extract( texcoords ), texture );

		if( distanceField )
		{
			RenderUtils.distanceFieldRendering( ts.state );
		}
		else
		{
			ts.state =
					ts.state.with( ts.state.texture.with( new Filters(
							MinFilter.LINEAR_MIPMAP_LINEAR, MagFilter.LINEAR ) ) );
		}

		return new TextShape( ts, this, text.toString() );
	}

	/**
	 * Calculates the glyph texture coordinates. The dest array will
	 * have 4 * text.length elements written to it, in bl tl br tr
	 * format
	 * 
	 * @param text
	 *           The text to render
	 * @param dest
	 *           A destination texcoord array, or null. If non-null,
	 *           all elements must also be non-null
	 * @param start
	 *           The element to start writing to in the dest array.
	 *           Ignored in dest is null;
	 * @return an array of glyph texture coordinates
	 */
	public Vector2f[] getTexCoords( CharSequence text, Vector2f[] dest, int start )
	{
		int index = start;
		if( dest == null )
		{
			dest = new Vector2f[ 4 * text.length() ];
			for( int i = 0; i < dest.length; i++ )
			{
				dest[ i ] = new Vector2f();
			}
			index = 0;
		}

		for( int i = 0; i < text.length(); i++ )
		{
			Glyph g = map( text.charAt( i ) );

			g.image.getOrigin( tempOrigin );
			g.image.getExtent( tempExtent );

			dest[ index++ ].set( tempOrigin.x, tempOrigin.y );
			dest[ index++ ].set( tempOrigin.x, tempExtent.y );
			dest[ index++ ].set( tempExtent.x, tempOrigin.y );
			dest[ index++ ].set( tempExtent.x, tempExtent.y );
		}

		return dest;
	}

	/**
	 * @return The font's texture
	 */
	public Texture getTexture()
	{
		return texture;
	}

	/**
	 * @return true if this font is not bold or italic
	 */
	public boolean isPlain()
	{
		return !( bold || italic );
	}

	/**
	 * Gets an unmodifiable list of the glyphs in this font
	 * 
	 * @return A list of glyphs
	 */
	public List<Glyph> getGlyphs()
	{
		return Collections.unmodifiableList( glyphs );
	}

	@Override
	public String toString()
	{
		return "Font \"" + name + "\" " + size + " " + ( bold ? "bold" : "" ) + " "
				+ ( italic ? "italic" : "" ) + " ascent = " + ascent + " descent = "
				+ descent + " leading = " + leading;
	}
}
