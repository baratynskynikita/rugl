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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.lwjgl.util.vector.Vector2f;

import com.rugl.text.Font;
import com.rugl.text.Glyph;
import com.rugl.text.GlyphImage;
import com.rugl.text.KerningSource;
import com.rugl.texture.Image;

/**
 * Converts Java fonts into {@link Font}s. It's an enormous wedge of
 * software larceny, mostly lifted wholesale from SPGL with the
 * distance field generation from Slick, so mad props go to princec
 * and OrangyTang for the clever bits.
 */
public class FontFactory implements KerningSource
{
	/**
	 * A default character set, containing whatever looks useful on the
	 * keyboard in front of me, plus some others stuff
	 */
	public static final String defaultCharSet = "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"
			+ "`¬-=!\"£€$%^&*()_+\\[];'#,./|{}:@~<>? ";

	private static final DecimalFormat dc = new DecimalFormat( "00" );

	/**
	 * Utility to convert java fonts and save the results to disk,
	 * ready to be reconstituted with
	 * {@link Font#Font(java.nio.ByteBuffer)}. Specify the names of
	 * fonts to convert. The output files will be named
	 * <fontname>_<size>.ruglfont. If no argument is specified, a
	 * listing of available fonts will be printed
	 * 
	 * @param args
	 *           a sequence of font names, suitable for
	 *           {@link java.awt.Font#decode(String)} e.g.:
	 *           "Arial-BOLD-24", or nothing to see available fonts
	 */
	public static void main( String[] args )
	{
		System.out.println( Arrays.toString( args ) );
		if( args.length == 0 )
		{
			// print font listing
			String[] fonts =
					GraphicsEnvironment.getLocalGraphicsEnvironment()
							.getAvailableFontFamilyNames();
			System.out.println( fonts.length + " Available font families : " );
			for( String fontname : fonts )
			{
				System.out.println( "\t" + fontname );
			}

			System.out
					.println( "usage: \"java -jar FontFactory.jar (-d) <fontname>-<size>\"" );
		}
		else
		{
			int i = 0;
			boolean distanceField = false;

			if( args[ 0 ].equals( "-d" ) )
			{
				distanceField = true;
				i = 1;
			}

			for( ; i < args.length; i++ )
			{
				try
				{

					System.out.println( "Converting " + args[ i ] + "..." );
					java.awt.Font src = java.awt.Font.decode( args[ i ] );

					FontFactory fc =
							new FontFactory( src.getFontName() + "-" + src.getSize(),
									distanceField );

					fc.listener = new CommandLineListener();

					Font f = fc.buildFont( defaultCharSet );

					try
					{
						String outputName =
								src.getFontName() + "_" + src.getSize() + ".ruglfont";

						outputName = outputName.replaceAll( " ", "_" );
						outputName = outputName.toLowerCase();

						System.out.println( "\twriting to " + outputName );

						RandomAccessFile rf = new RandomAccessFile( outputName, "rw" );
						FileChannel ch = rf.getChannel();
						int fileLength = f.dataSize();
						rf.setLength( fileLength );
						MappedByteBuffer buffer =
								ch.map( FileChannel.MapMode.READ_WRITE, 0, fileLength );

						f.write( buffer );

						buffer.force();
						ch.close();

						System.out.println( "\tdone" );
					}
					catch( FileNotFoundException fnfe )
					{
						fnfe.printStackTrace();
					}
					catch( IOException ioe )
					{
						ioe.printStackTrace();
					}
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	private FontRenderContext frc = null;

	private FontMetrics metrics = null;

	/**
	 * The source java font
	 */
	public final java.awt.Font srcFont;

	private float fixedWidthAdvance = -1;

	private final boolean distanceField;

	/**
	 * The scaling factor for distance-field generation input images -
	 * larger will give more accurate distance fields but longer
	 * processing
	 */
	public int distFieldScale = 20;

	/**
	 * The proportion of the font height that is scanned and padded in
	 * the distance field - larger will give deeper border effects, but
	 * longer processing
	 */
	public float scanFactor = .1f;

	/**
	 * If set to true, a JFrame will open for every glyph generated,
	 * showing the texture.
	 */
	public boolean debug = false;

	/**
	 * Set this field to be updated with progress
	 */
	public Listener listener = null;

	/**
	 * For the detection of duplicate glyphs
	 */
	private List<ProtoGlyphImage> pgiList = new ArrayList<ProtoGlyphImage>();

	/**
	 * Constructs a new {@link FontFactory}.
	 * 
	 * @param fontName
	 *           The name of the font to build, suitable for passing to
	 *           {@link java.awt.Font#decode(String)}
	 * @param distanceField
	 *           <code>true</code> to generate distance field fonts
	 */
	public FontFactory( String fontName, boolean distanceField )
	{
		Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
		attributes.put( TextAttribute.KERNING, TextAttribute.KERNING_ON );
		srcFont = java.awt.Font.decode( fontName ).deriveFont( attributes );

		Graphics2D g =
				new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ).createGraphics();
		g.setFont( srcFont );

		frc = g.getFontRenderContext();
		metrics = g.getFontMetrics();

		debug = System.getProperty( "fontfactory.debug" ) != null;

		this.distanceField = distanceField;
	}

	@Override
	public float computeKerning( char prev, char next )
	{
		String text = new String( new char[] { prev, next } );
		TextLayout textLayout = new TextLayout( text, srcFont, frc );
		GLGraphics2D specialRenderer = new GLGraphics2D( srcFont );

		textLayout.draw( specialRenderer, 0, 0 );

		GlyphVector prevGlyphVector = srcFont.createGlyphVector( frc, new char[] { prev } );
		float glyphAdvance = prevGlyphVector.getGlyphMetrics( 0 ).getAdvance();

		float xdif = specialRenderer.xxx[ 1 ] - specialRenderer.xxx[ 0 ];
		float kerning = xdif - glyphAdvance;

		return kerning;
	}

	/**
	 * Builds a font
	 * 
	 * @param characterset
	 *           A string of characters that to be added to the font,
	 *           or null for the default
	 * @return A {@link Font}
	 */
	public Font buildFont( CharSequence characterset )
	{
		if( characterset == null )
		{
			characterset = defaultCharSet;
		}

		if( listener != null )
		{
			listener.fontStarted( srcFont, characterset );
		}

		String name = srcFont.getFontName() + "-" + srcFont.getSize();
		Font f =
				new Font( name, srcFont.isBold(), srcFont.isItalic(), srcFont.getSize(),
						metrics.getAscent(), metrics.getDescent(), metrics.getLeading(),
						distanceField );

		// make sure we have the WTF char
		buildGlyph( ( char ) 0 );

		// build the rest of the characters
		for( int i = 0; i < characterset.length(); i++ )
		{
			buildGlyph( characterset.charAt( i ) );
		}

		// tell the font where to get kerning information
		f.kerningSource = this;

		// add them to the font
		for( ProtoGlyphImage pgi : pgiList )
		{
			for( Glyph g : pgi.buildGlyphs() )
			{
				f.addGlyph( g );
			}
		}

		if( debug )
		{
			System.out.println( f );

			for( Glyph g : f.getGlyphs() )
			{
				System.out.println( "\t" + g );
			}
		}

		if( listener != null )
		{
			listener.fontComplete( f );
		}

		return f;
	}

	/**
	 * Builds a glyph and adds it to the {@link #pgiList} if it's not
	 * already there
	 * 
	 * @param c
	 *           The character
	 * @return A {@link ProtoGlyphImage}
	 */
	public ProtoGlyphImage buildGlyph( final char c )
	{
		if( listener != null )
		{
			listener.glyphStarted( c );
		}

		char[] charToMap = new char[] { c };
		GlyphVector glyphVector = srcFont.createGlyphVector( frc, charToMap );

		// check the list
		for( int i = 0; i < pgiList.size(); i++ )
		{
			ProtoGlyphImage pgi = pgiList.get( i );

			if( equal( glyphVector.getGlyphOutline( 0 ), pgi.gv.getGlyphOutline( 0 ) ) )
			{
				pgi.characters.add( new Character( c ) );

				if( listener != null )
				{
					listener.glyphComplete( c );
				}

				return pgi;
			}
		}

		Shape shape = glyphVector.getGlyphOutline( 0 );
		Rectangle bounds = shape.getBounds();
		Dimension imageSize = new Dimension( bounds.width, bounds.height );

		// compute metrics
		Vector2f origin = new Vector2f();
		origin.x = bounds.x;
		origin.y = -( bounds.y + bounds.height );

		// draw image
		int padScan = ( int ) ( metrics.getHeight() * scanFactor * distFieldScale );

		if( distanceField )
		{
			shape =
					AffineTransform.getScaleInstance( distFieldScale, distFieldScale )
							.createTransformedShape( shape );
			bounds = shape.getBounds();

			imageSize.width = bounds.width + 2 * padScan;
			imageSize.height = bounds.height + 2 * padScan;

			origin.x -= padScan / distFieldScale;
			origin.y -= padScan / distFieldScale;
		}

		BufferedImage image;
		Graphics2D g;
		if( !( bounds.width == 0 && bounds.height == 0 ) )
		{
			image =
					new BufferedImage( imageSize.width, imageSize.height,
							BufferedImage.TYPE_INT_ARGB );
			g = ( Graphics2D ) image.getGraphics();

			// set rendering hints
			g.setRenderingHint( RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY );
			g.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON );
			g.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE );

			if( !distanceField )
			{
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON );
				g.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );

			}
			else
			{
				g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_OFF );

				g.translate( padScan, padScan );
			}

			g.translate( -bounds.x, -bounds.y );
			g.fill( shape );

			if( distanceField )
			{
				DistanceFieldFilter dff =
						new DistanceFieldFilter( image, distFieldScale, padScan,
								new DistanceFieldFilter.Listener() {

									@Override
									public void progress( float progress )
									{
										if( listener != null )
										{
											listener.glyphProgress( c, progress );
										}
									}

									@Override
									public void finished( BufferedImage result )
									{
									}
								} );

				dff.run();

				image = dff.getResult();
			}

			if( debug )
			{
				showDebug( "\'" + c + "\'", image );
			}
		}
		else
		{ // it's an invisible character
			image = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
		}

		Image glyphImage = ImageFactory.buildImage( image, Image.Format.LUMINANCE_ALPHA );

		ProtoGlyphImage glyph = new ProtoGlyphImage( glyphVector, glyphImage, origin, c );

		pgiList.add( glyph );

		if( listener != null )
		{
			listener.glyphComplete( c );
		}

		return glyph;
	}

	private float getFixedWidthAdvance()
	{
		if( fixedWidthAdvance < 0 )
		{
			char[] charToMap = new char[] { '0' };
			GlyphVector glyphVector = srcFont.createGlyphVector( frc, charToMap );

			fixedWidthAdvance = glyphVector.getGlyphMetrics( 0 ).getAdvance();
		}

		return fixedWidthAdvance;
	}

	/**
	 * Shows what Java2D is doing
	 * 
	 * @param s
	 *           The string to render
	 */
	public void showDebug( String s )
	{
		TextLayout tl = new TextLayout( s, srcFont, frc );
		Rectangle2D bounds = tl.getBounds();

		if( !( bounds.getWidth() == 0 && bounds.getHeight() == 0 ) )
		{
			BufferedImage image =
					new BufferedImage( ( int ) bounds.getWidth(), ( int ) bounds.getHeight(),
							BufferedImage.TYPE_INT_ARGB );
			Graphics2D g = ( Graphics2D ) image.getGraphics();
			g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON );
			g.setFont( srcFont );

			tl.draw( g, ( float ) -bounds.getX(), ( float ) -bounds.getY() );

			showDebug( s, image );
		}
	}

	private static boolean equal( Shape s, Shape t )
	{
		float[] sCoords = new float[ 6 ];
		PathIterator si = s.getPathIterator( null );
		float[] tCoords = new float[ 6 ];
		PathIterator ti = t.getPathIterator( null );

		do
		{
			if( si.isDone() != ti.isDone() )
			{
				return false;
			}

			int ss = si.currentSegment( sCoords );
			int ts = ti.currentSegment( tCoords );

			if( ss != ts )
			{
				return false;
			}

			if( !Arrays.equals( sCoords, tCoords ) )
			{
				return false;
			}

			si.next();
			ti.next();
		}
		while( !si.isDone() && !ti.isDone() );

		return true;
	}

	private void showDebug( String c, BufferedImage image )
	{

		final BufferedImage debugImage =
				new BufferedImage( image.getWidth(), image.getHeight(), image.getType() );
		debugImage.getGraphics().drawImage( image, 0, 0, null );

		final JFrame frame = new JFrame( srcFont.getFontName() + " \'" + c + "\'" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		JPanel panel = new JPanel() {
			{
				addMouseListener( new MouseAdapter() {
					@Override
					public void mouseClicked( MouseEvent e )
					{
						frame.setVisible( false );
					}
				} );
			}

			@Override
			public void paint( Graphics g )
			{
				Graphics2D g2d = ( Graphics2D ) g;
				g2d.setColor( java.awt.Color.black );
				g2d.fillRect( 0, 0, getWidth(), getHeight() );
				int x = ( getWidth() - debugImage.getWidth() ) / 2;
				int y = ( getHeight() - debugImage.getHeight() ) / 2;
				g2d.drawImage( debugImage, x, y, this );
				g2d.setColor( Color.RED );
				g2d.drawRect( x - 1, y - 1, debugImage.getWidth() + 1,
						debugImage.getHeight() + 1 );
			}
		};
		frame.getContentPane().add( panel );
		panel.setPreferredSize( new Dimension( debugImage.getWidth() + 30, debugImage
				.getHeight() + 30 ) );
		frame.pack();
		frame.setVisible( true );
	}

	/**
	 * Prints progress to stdOut
	 * 
	 * @author ryanm
	 */
	private static class CommandLineListener implements Listener
	{
		private static final int maxStars = 50;

		private int stars = 0;

		private int charSetSize = 1;

		private int currentChar = 0;

		private long startTime = System.currentTimeMillis();

		@Override
		public void fontStarted( java.awt.Font srcFont, CharSequence charSet )
		{
			startTime = System.currentTimeMillis();

			charSetSize = charSet.length();

			System.out.println( "Generating " + srcFont );
			System.out.println( "Character set : " + charSet );
		}

		@Override
		public void glyphStarted( char c )
		{
			System.out.print( "  " + dc.format( ( float ) currentChar / charSetSize * 100 )
					+ "% : Glyph " + c + " : " );
			currentChar++;
		}

		@Override
		public void glyphProgress( char c, float p )
		{
			while( stars < p * maxStars )
			{
				System.out.print( "*" );
				stars++;
			}
		}

		@Override
		public void glyphComplete( char arg0 )
		{
			while( stars < maxStars )
			{
				System.out.print( "*" );
				stars++;
			}

			System.out.println( " complete" );
			stars = 0;
		}

		@Override
		public void fontComplete( Font complete )
		{
			float dur = ( System.currentTimeMillis() - startTime ) / 1000.0f;
			System.out.println( "Generated in " + dur + " seconds" );
			currentChar = 0;
		}
	}

	/**
	 * Interface for monitoring generation progress
	 * 
	 * @author ryanm
	 */
	public interface Listener
	{
		/**
		 * Called when we start to generate a font
		 * 
		 * @param srcFont
		 *           The source font
		 * @param charSet
		 *           The characters to add to the generated font
		 */
		public void fontStarted( java.awt.Font srcFont, CharSequence charSet );

		/**
		 * Called when we start generating a new glyph
		 * 
		 * @param c
		 */
		public void glyphStarted( char c );

		/**
		 * Called to update progress on a glyph
		 * 
		 * @param c
		 *           The character
		 * @param p
		 *           0 <= p <= 1
		 */
		public void glyphProgress( char c, float p );

		/**
		 * Called when a glyph is complete
		 * 
		 * @param c
		 */
		public void glyphComplete( char c );

		/**
		 * Called when a font has been completed
		 * 
		 * @param complete
		 */
		public void fontComplete( Font complete );
	}

	/**
	 * An intermediate stage in font generation, allows us to find
	 * duplicate glyphs in, for example, an all-uppercase font
	 * 
	 * @author ryanm
	 */
	public class ProtoGlyphImage
	{
		/***/
		public final GlyphVector gv;

		/***/
		public final Image image;

		/***/
		public final Vector2f origin;

		/***/
		public final List<Character> characters = new ArrayList<Character>();

		private ProtoGlyphImage( GlyphVector gv, Image image, Vector2f origin, char c )
		{
			this.gv = gv;
			this.image = image;
			characters.add( new Character( c ) );
			this.origin = origin;
		}

		private Glyph[] buildGlyphs()
		{
			GlyphImage gi = buildGlyphImage();

			GlyphMetrics gmetrics = gv.getGlyphMetrics( 0 );
			float glyphAdvance;

			Glyph[] glyphs = new Glyph[ characters.size() ];
			for( int i = 0; i < glyphs.length; i++ )
			{
				char c = characters.get( i ).charValue();
				// we want numbers to be fixed width
				if( c >= '0' && c <= '9' )
				{
					glyphAdvance = getFixedWidthAdvance();
				}
				else
				{
					glyphAdvance = gmetrics.getAdvance();
				}

				glyphs[ i ] =
						new Glyph( c, gi, origin, glyphAdvance, new char[ 0 ], new float[ 0 ] );
			}

			return glyphs;
		}

		private GlyphImage buildGlyphImage()
		{
			char[] chars = new char[ characters.size() ];
			for( int i = 0; i < chars.length; i++ )
			{
				chars[ i ] = characters.get( i ).charValue();
			}

			return new GlyphImage( image, chars );
		}
	}

	/**
	 * Lord have mercy. A graphics2d which is implemented by OpenGL.
	 * Whatever next? This is just a hacked class to enable us to get
	 * at the glyphs being rendered by a TextLayout object.
	 */
	private static class GLGraphics2D extends Graphics2D
	{

		// Create temp dummy image which we can get a font rendering
		// context from
		private final BufferedImage image;

		private final Graphics2D g2d;

		private final FontRenderContext frc;

		private final FontMetrics metrics;

		private float[] xxx = new float[ 2 ];

		private float[] yyy = new float[ 2 ];

		// Used when rendering a font
		private int numGlyphsDrawn;

		private int glyphPos;

		private java.awt.Font srcFont;

		/**
		 * @param srcFont
		 */
		public GLGraphics2D( java.awt.Font srcFont )
		{
			this.srcFont = srcFont;
			image = new BufferedImage( 1, 1, BufferedImage.TYPE_4BYTE_ABGR );
			g2d = ( Graphics2D ) image.getGraphics();
			g2d.setFont( srcFont );
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setRenderingHint( RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY );
			frc = g2d.getFontRenderContext();
			metrics = g2d.getFontMetrics( srcFont );
		}

		/*
		 * These are the methods which are actually called when
		 * rendering a font with TextLayout
		 */
		@Override
		public java.awt.Font getFont()
		{
			return srcFont;
		}

		@Override
		public FontMetrics getFontMetrics( java.awt.Font f )
		{
			return metrics;
		}

		@Override
		public FontRenderContext getFontRenderContext()
		{
			return frc;
		}

		@Override
		public void drawGlyphVector( GlyphVector g, float x, float y )
		{
			final int n = g.getNumGlyphs();
			for( int i = 0; i < n && i < 2; i++ )
			{
				Point2D pos = g.getGlyphPosition( i );
				xxx[ glyphPos ] = ( float ) pos.getX() + x;
				yyy[ glyphPos++ ] = ( float ) pos.getY() + y;
				numGlyphsDrawn++;
			}
		}

		/*
		 * The following methods are just stubs to create a concrete
		 * class. During font rendering with a TextLayout, none of these
		 * methods are actually called, so we can get away with not
		 * implementing any of them properly.
		 */
		@Override
		public void addRenderingHints( java.util.Map hints )
		{
		}

		@Override
		public void clearRect( int x, int y, int width, int height )
		{
		}

		@Override
		public void clip( Shape s )
		{
		}

		@Override
		public void clipRect( int x, int y, int width, int height )
		{
		}

		@Override
		public void copyArea( int x, int y, int width, int height, int dx, int dy )
		{
		}

		@Override
		public Graphics create()
		{
			return this;
		}

		@Override
		public void dispose()
		{
		}

		@Override
		public void draw( Shape s )
		{
		}

		@Override
		public void drawArc( int x, int y, int width, int height, int startAngle,
				int arcAngle )
		{
		}

		@Override
		public void drawImage( java.awt.image.BufferedImage img,
				java.awt.image.BufferedImageOp op, int x, int y )
		{
		}

		@Override
		public boolean drawImage( java.awt.Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, java.awt.Color bgcolor,
				java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, int x, int y, int width, int height,
				java.awt.Color bgcolor, java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, int x, int y, int width, int height,
				java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, int x, int y, java.awt.Color bgcolor,
				java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, int x, int y,
				java.awt.image.ImageObserver observer )
		{
			return false;
		}

		@Override
		public boolean drawImage( java.awt.Image img, AffineTransform xform,
				java.awt.image.ImageObserver obs )
		{
			return false;
		}

		@Override
		public void drawLine( int x1, int y1, int x2, int y2 )
		{
		}

		@Override
		public void drawOval( int x, int y, int width, int height )
		{
		}

		@Override
		public void drawPolygon( int[] xPoints, int[] yPoints, int nPoints )
		{
		}

		@Override
		public void drawPolyline( int[] xPoints, int[] yPoints, int nPoints )
		{
		}

		@Override
		public void drawRenderableImage( java.awt.image.renderable.RenderableImage img,
				AffineTransform xform )
		{
		}

		@Override
		public void drawRenderedImage( java.awt.image.RenderedImage img,
				AffineTransform xform )
		{
		}

		@Override
		public void drawRoundRect( int x, int y, int width, int height, int arcWidth,
				int arcHeight )
		{
		}

		@Override
		public void drawString( String s, float x, float y )
		{
		}

		@Override
		public void drawString( String str, int x, int y )
		{
		}

		@Override
		public void drawString( java.text.AttributedCharacterIterator iterator, float x,
				float y )
		{
		}

		@Override
		public void drawString( java.text.AttributedCharacterIterator iterator, int x, int y )
		{
		}

		@Override
		public void fill( Shape s )
		{
		}

		@Override
		public void fillArc( int x, int y, int width, int height, int startAngle,
				int arcAngle )
		{
		}

		@Override
		public void fillOval( int x, int y, int width, int height )
		{
		}

		@Override
		public void fillPolygon( int[] xPoints, int[] yPoints, int nPoints )
		{
		}

		@Override
		public void fillRect( int x, int y, int width, int height )
		{
		}

		@Override
		public void fillRoundRect( int x, int y, int width, int height, int arcWidth,
				int arcHeight )
		{
		}

		@Override
		public java.awt.Color getBackground()
		{
			return null;
		}

		@Override
		public Shape getClip()
		{
			return null;
		}

		@Override
		public java.awt.Rectangle getClipBounds()
		{
			return null;
		}

		@Override
		public java.awt.Color getColor()
		{
			return null;
		}

		@Override
		public Composite getComposite()
		{
			return null;
		}

		@Override
		public GraphicsConfiguration getDeviceConfiguration()
		{
			return null;
		}

		@Override
		public Paint getPaint()
		{
			return null;
		}

		@Override
		public Object getRenderingHint( RenderingHints.Key hintKey )
		{
			return null;
		}

		@Override
		public RenderingHints getRenderingHints()
		{
			return null;
		}

		@Override
		public Stroke getStroke()
		{
			return null;
		}

		@Override
		public AffineTransform getTransform()
		{
			return null;
		}

		@Override
		public boolean hit( java.awt.Rectangle rect, Shape s, boolean onStroke )
		{
			return false;
		}

		@Override
		public void rotate( double theta )
		{
		}

		@Override
		public void rotate( double theta, double x, double y )
		{
		}

		@Override
		public void scale( double sx, double sy )
		{
		}

		@Override
		public void setBackground( java.awt.Color color )
		{
		}

		@Override
		public void setClip( int x, int y, int width, int height )
		{
		}

		@Override
		public void setClip( Shape clip )
		{
		}

		@Override
		public void setColor( java.awt.Color c )
		{
		}

		@Override
		public void setComposite( Composite comp )
		{
		}

		@Override
		public void setFont( java.awt.Font font )
		{
		}

		@Override
		public void setPaint( Paint paint )
		{
		}

		@Override
		public void setPaintMode()
		{
		}

		@Override
		public void setRenderingHint( RenderingHints.Key hintKey, Object hintValue )
		{
		}

		@Override
		public void setRenderingHints( java.util.Map hints )
		{
		}

		@Override
		public void setStroke( Stroke s )
		{
		}

		@Override
		public void setTransform( AffineTransform tx )
		{
		}

		@Override
		public void setXORMode( java.awt.Color c1 )
		{
		}

		@Override
		public void shear( double shx, double shy )
		{
		}

		@Override
		public void transform( AffineTransform tx )
		{
		}

		@Override
		public void translate( double tx, double ty )
		{
		}

		@Override
		public void translate( int x, int y )
		{
		}
	}

}
