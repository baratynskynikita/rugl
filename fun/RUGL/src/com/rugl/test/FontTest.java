
package com.rugl.test;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;

import com.rugl.Game;
import com.rugl.GameBox;
import com.rugl.console.Console;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.gl.enums.ComparisonFunction;
import com.rugl.gl.enums.MagFilter;
import com.rugl.gl.enums.MinFilter;
import com.rugl.gl.facets.AlphaTest;
import com.rugl.gl.facets.Blend;
import com.rugl.gl.facets.TextureState.Filters;
import com.rugl.gl.shader.DistanceFieldShader;
import com.rugl.input.KeyListener;
import com.rugl.input.KeyPress;
import com.rugl.renderer.RenderUtils;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.Font;
import com.rugl.text.TextShape;
import com.rugl.texture.Texture;
import com.rugl.texture.TextureFactory;
import com.rugl.util.Colour;
import com.rugl.util.FontFactory;
import com.rugl.util.FontFactory.ProtoGlyphImage;
import com.rugl.util.GLUtil;
import com.ryanm.config.Configurable;
import com.ryanm.config.Configurator;
import com.ryanm.config.ValueListener;
import com.ryanm.config.imp.AbstractConfigurator;
import com.ryanm.config.imp.AnnotatedConfigurator;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.Variable;

/**
 * @author ryanm
 */
@ConfigurableType( "font" )
public class FontTest implements Game, Configurable
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		GameBox.startGame( new FontTest(), null );
	}

	private String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();
	{
		Arrays.sort( fontNames );
	}

	private StackedRenderer r = new StackedRenderer();

	private KeyPress space = new KeyPress( true, Keyboard.KEY_SPACE );

	private KeyPress up = new KeyPress( true, Keyboard.KEY_UP );

	private KeyPress down = new KeyPress( true, Keyboard.KEY_DOWN );

	private KeyPress left = new KeyPress( true, Keyboard.KEY_LEFT );

	private KeyPress right = new KeyPress( true, Keyboard.KEY_RIGHT );

	private KeyPress ctrl = new KeyPress( true, Keyboard.KEY_LCONTROL );

	private int mode = 0;

	/***/
	@Variable
	@Description( "Inpt font size" )
	public int size = 72;

	private int fontIndex = 0;

	/***/
	@Variable
	@Description( "Font family name" )
	public String fontName = fontNames[ fontIndex ];

	/***/
	@Variable
	@Description( "Scale factor for computing distance fields - higher = more accurate but slower" )
	public int dfScale = 20;

	/***/
	@Variable
	@Description( "Spread factor for computing distance fields - higher = deeper borders/glows but slower" )
	public float dfPad = 0.1f;

	/***/
	@Variable
	@Description( "Distance field or standard fonts" )
	public boolean distanceField = true;

	private FontFactory ff = null;

	private ProtoGlyphImage glyph = null;

	private Texture texture = null;

	private char character = 'a';

	private String status;

	private float progressBar = 0;

	private FontFactory.Listener progress = new FontFactory.Listener() {

		@Override
		public void glyphStarted( char c )
		{
		}

		@Override
		public void glyphProgress( char c, float p )
		{
			progressBar = p;
		}

		@Override
		public void glyphComplete( char c )
		{
		}

		@Override
		public void fontStarted( java.awt.Font srcFont, CharSequence charSet )
		{
		}

		@Override
		public void fontComplete( Font complete )
		{
		}
	};

	private KeyListener kl = new KeyListener() {
		@Override
		public void keyDown( int keyCode, char keyChar, boolean repeat )
		{
			if( keyChar != Keyboard.CHAR_NONE && keyCode != Keyboard.KEY_GRAVE
					&& keyCode != Keyboard.KEY_SPACE )
			{
				character = keyChar;
				clearGlyph();
			}
		};
	};

	private Thread glyphGen = null;

	/***/
	public FontTest()
	{
		GameBox.dispConf.setResolution( new Dimension( 800, 600 ) );
		GameBox.dispConf.setFullScreen( false );

		GameBox.addKeyListener( kl );
	}

	@Override
	public String getName()
	{
		return "Font test";
	}

	@Override
	public void advance( float delta )
	{
		if( space.isActive() )
		{
			mode++;
			mode %= 3;
		}
		if( up.isActive() )
		{
			size++;
			clearFontFactory();
		}
		if( down.isActive() )
		{
			size--;
			clearFontFactory();
		}
		if( left.isActive() )
		{
			fontIndex = ( fontIndex - 1 + fontNames.length ) % fontNames.length;
			clearFontFactory();
		}
		if( right.isActive() )
		{
			fontIndex = ( fontIndex + 1 ) % fontNames.length;
			clearFontFactory();
		}
		if( ctrl.isActive() )
		{
			distanceField = !distanceField;
			clearFontFactory();
		}

	}

	private void clearFontFactory()
	{
		ff = null;
		clearGlyph();
	}

	private void clearGlyph()
	{
		glyph = null;

		if( texture != null )
		{
			TextureFactory.deleteTexture( texture );
			texture = null;
		}
	}

	@Override
	public void draw()
	{
		GLUtil.scaledOrtho( 800, 600 );
		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

		if( ff == null )
		{
			ff = new FontFactory( fontNames[ fontIndex ] + "-" + size, distanceField );

			ff.listener = progress;
			ff.distFieldScale = dfScale;
			ff.scanFactor = dfPad;
		}

		if( glyph == null && glyphGen == null )
		{
			status = "generating";

			glyphGen = new Thread( "Glyph generator" ) {
				@Override
				public void run()
				{
					glyph = ff.buildGlyph( character );

					glyphGen = null;
				}
			};

			glyphGen.setDaemon( true );
			glyphGen.start();
		}

		if( glyph != null )
		{
			progressBar = 1;

			if( texture == null )
			{
				texture = TextureFactory.buildTexture( glyph.image, false, true );
			}

			status =
					ff.srcFont.getFontName() + "-" + size + " " + glyph.image.getWidth() + "x"
							+ glyph.image.getHeight();

			TexturedShape ts =
					new TexturedShape( new ColouredShape( ShapeUtil.filledQuad( 0, 0,
							glyph.image.getWidth(), glyph.image.getHeight(), 0 ), Colour.white,
							null ), RenderUtils.getQuadTexCoords( 1 ), texture );

			if( mode == 1 )
			{
				ts.state = RenderUtils.distanceFieldRendering( ts.state );
				if( ts.state.shader instanceof DistanceFieldShader )
				{
					status += " with shader";
				}
				else
				{
					status += " shaders not supported";
				}

			}
			else if( mode == 2 )
			{
				ts.state =
						ts.state.with( Blend.disabled ).with(
								new AlphaTest( ComparisonFunction.GEQUAL, 0.5f ) );
				status += " with alpha test";
			}
			else
			{
				ts.state =
						ts.state.with( ts.state.texture.with( new Filters(
								MinFilter.NEAREST_MIPMAP_NEAREST, MagFilter.NEAREST ) ) );

				status += " raw texture";
			}

			float y = 580;

			for( int i = -2; i <= 2; i++ )
			{
				float scale = ( float ) Math.pow( 2, i );
				TexturedShape s = ts.clone();
				s.scale( scale, scale, 1 );

				y -= s.getBounds().getHeight() + 20;

				s.translate( 10, y, 0 );
				s.render( r );

			}

			// big
			float scale =
					Math.min( 600 / ts.getBounds().getWidth(), 580 / ts.getBounds()
							.getHeight() );
			ts.scale( scale, scale, 1 );
			ts.translate( 780 - ts.getBounds().getWidth(),
					300 - ts.getBounds().getCenter().y, 0 );

			ts.render( r );
		}

		Console.font.init( true );
		TextShape s = Console.font.buildTextShape( status, Colour.white );
		s.translate( 5, 5, 0 );
		s.render( r );

		ColouredShape pb =
				new ColouredShape( ShapeUtil.filledQuad( 1, 1, 1 + 798 * progressBar, 2, 0 ),
						Colour.white, null );
		pb.render( r );

		r.render();
	}

	@Override
	public void exit()
	{
	}

	@Override
	public void loadResources()
	{
	}

	@Override
	public void resourcesLoaded()
	{
	}

	private Configurator conf = null;

	@Override
	public Configurator getConfigurator()
	{
		if( conf == null )
		{
			conf = AnnotatedConfigurator.buildConfigurator( this );

			( ( AbstractConfigurator ) conf ).setRange( "fontName", fontNames );
			conf.addValueListener( new ValueListener() {

				@Override
				public void valueChanged( String name )
				{
					if( name.equals( "fontName" ) )
					{
						for( int i = 0; i < fontNames.length; i++ )
						{
							if( fontName.equals( fontNames[ i ] ) )
							{
								fontIndex = i;
								break;
							}
						}
					}

					clearFontFactory();
				}
			} );
		}

		return conf;
	}
}
