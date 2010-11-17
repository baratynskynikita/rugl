
package com.rugl.console;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

import com.rugl.DisplayConfigurable;
import com.rugl.GameBox;
import com.rugl.console.commands.Add;
import com.rugl.console.commands.Bind;
import com.rugl.console.commands.InspectTexture;
import com.rugl.console.commands.LoadBinds;
import com.rugl.console.commands.LoadConf;
import com.rugl.console.commands.PrintBinds;
import com.rugl.console.commands.PrintConfig;
import com.rugl.console.commands.SaveBinds;
import com.rugl.console.commands.SaveConf;
import com.rugl.console.commands.Set;
import com.rugl.console.commands.Unbind;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.gl.State;
import com.rugl.gl.enums.MagFilter;
import com.rugl.gl.enums.MinFilter;
import com.rugl.gl.facets.TextureState.Filters;
import com.rugl.renderer.RenderUtils;
import com.rugl.renderer.StackedRenderer;
import com.rugl.sound.SoundSystem;
import com.rugl.sound.Source;
import com.rugl.text.Font;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.texture.Texture;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.Variable;
import com.ryanm.util.geom.Rectanglef;
import com.ryanm.util.text.TextUtils;

/**
 * @author ryanm
 */
@ConfigurableType( "console" )
public class Console
{
	private static boolean enabled = false;

	private static float bottom = 10000;

	/**
	 * The time taken for the console to fully appear or disappear
	 */
	@Variable( "growth" )
	@Description( "The time taken to hide and show the console" )
	public static float growthDuration = 0.3f;

	/**
	 * The space between the edge of the screen and the console, and
	 * between the edge of the console and the contents
	 */
	public static float border = 5;

	/**
	 * 
	 */
	@Variable( "top colour" )
	@Description( "The colour at the top of the console" )
	public static Color topColour = new Color( ReadableColor.BLACK );

	/**
	 * 
	 */
	@Variable( "bottom colour" )
	@Description( "The colour at the bottom of the console" )
	public static Color bottomColour = new Color( ReadableColor.DKGREY );

	private static ColouredShape backGround = null;

	/**
	 * 
	 */
	@Variable( "text colour" )
	@Description( "The colour of the text" )
	public static Color textColour = new Color( ReadableColor.LTGREY );

	/**
	 * 
	 */
	@Variable( "error colour" )
	@Description( "The colour of error text" )
	public static Color errorColour = new Color( ReadableColor.RED );

	/**
	 * 
	 */
	@Variable( "suggestion colour" )
	@Description( "The colour of command completion suggestions" )
	public static Color suggestColour = new Color( ReadableColor.GREEN );

	/**
	 * 
	 */
	@Variable( "command colour" )
	@Description( "The colour of commands" )
	public static Color commandColour = new Color( ReadableColor.WHITE );

	/**
	 * A texture to display over the log messages
	 */
	public static Texture inspectedTexture;

	private static TexturedShape textureInspector = null;

	private static ConsoleInput input = new ConsoleInput();

	private static int[] vertexColours = new int[ 4 ];

	static StackedRenderer r = new StackedRenderer();

	private static LinkedList<LogMessage> messages = new LinkedList<LogMessage>();

	/**
	 * The maximum number of log messages to keep
	 */
	@Variable( "maximum entries" )
	@Description( "The maximum number of stored messages" )
	public static int maxLog = 100;

	/**
	 * The font
	 */
	public static Font font = null;

	/**
	 * Determines if logging is also written to a file
	 */
	@Variable( "file logging" )
	@Description( "Write logging to file \"log.txt\"" )
	public static boolean logFile = false;

	private static FileWriter fw;

	static Command[] commands = new Command[] { new Add(), new Bind(), new Unbind(),
			new Set(), new LoadBinds(), new SaveBinds(), new PrintBinds(),
			new PrintConfig(), new InspectTexture(), new SaveConf(), new LoadConf() };

	private static Source[] pausedSources = null;

	static
	{
		GameBox.dispConf.addListener( new DisplayConfigurable.Listener() {

			@Override
			public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
					boolean fsaa )
			{
				if( res )
				{
					backGround = null;
					// need to lay out the messages again
					for( LogMessage lm : messages )
					{
						lm.shape = null;
					}

					// need to resize the inspector
					textureInspector = null;
				}
			}
		} );
	}

	/**
	 * Adds a command to the console
	 * 
	 * @param c
	 */
	public static void addCommand( Command c )
	{
		Command[] nc = new Command[ commands.length + 1 ];

		System.arraycopy( commands, 0, nc, 0, commands.length );
		nc[ nc.length - 1 ] = c;

		commands = nc;
	}

	/**
	 * Logs a message to the console
	 * 
	 * @param message
	 */
	public static void log( CharSequence message )
	{
		LogMessage lm = new LogMessage( message, LogMessage.Type.LOG );
		log( lm );
	}

	/**
	 * Log an error message to the console
	 * 
	 * @param message
	 */
	public static void error( CharSequence message )
	{
		LogMessage lm = new LogMessage( message, LogMessage.Type.ERROR );
		log( lm );
	}

	/**
	 * Executes a command
	 * 
	 * @param composite
	 */
	public static void execute( String composite )
	{
		String[] c = TextUtils.split( composite, '(', ')', ';' );

		for( String command : c )
		{
			LogMessage lm = new LogMessage( command, LogMessage.Type.COMMAND );
			log( lm );

			command = command.trim();
			boolean found = false;

			for( int i = 0; i < commands.length && !found; i++ )
			{
				if( command.startsWith( commands[ i ].name ) )
				{
					commands[ i ].execute( command );
					found = true;
				}
			}

			if( !found )
			{
				error( "Command \"" + command + "\" not recognised" );
			}
		}
	}

	static void suggest( CharSequence message )
	{
		if( message == null )
		{
			if( messages.size() > 0 && messages.getFirst().type == LogMessage.Type.SUGGEST )
			{ // remove the suggest
				messages.removeFirst();
			}
		}
		else
		{
			LogMessage lm = new LogMessage( message, LogMessage.Type.SUGGEST );
			log( lm );
		}
	}

	static void clear()
	{
		inspectedTexture = null;
		messages.clear();
	}

	private static void log( LogMessage lm )
	{
		if( messages.size() > 0 && messages.getFirst().type == LogMessage.Type.SUGGEST )
		{ // remove the suggest
			messages.removeFirst();
		}

		messages.addFirst( lm );

		while( messages.size() > maxLog )
		{
			messages.removeLast();
		}

		if( logFile && lm.type != LogMessage.Type.SUGGEST )
		{
			if( fw == null )
			{
				try
				{
					fw = new FileWriter( "log.txt" );
				}
				catch( IOException e )
				{
					System.err.println( "Failed to open log file writer" );
					e.printStackTrace();
				}
			}

			if( fw != null )
			{
				try
				{
					fw.append( lm.toString() );
					fw.write( "\n" );
					fw.flush();
				}
				catch( IOException e )
				{
					System.err.println( "Error writing to log, abandoning file" );
					e.printStackTrace();

					fw = null;
					logFile = false;
				}
			}
		}
	}

	/**
	 * Toggles displaying the console
	 */
	public static void toggle()
	{
		enabled = !enabled;

		if( enabled )
		{
			GameBox.addKeyListener( input.kl );

			// pause sounds
			pausedSources = SoundSystem.getPlayingSources();
			for( int i = 0; i < pausedSources.length; i++ )
			{
				pausedSources[ i ].pause();
			}
		}
		else
		{
			GameBox.removeKeyListener( input.kl );

			// resume paused sounds
			for( int i = 0; i < pausedSources.length; i++ )
			{
				pausedSources[ i ].play();
			}
			pausedSources = null;
		}
	}

	/**
	 * Determines if the console is currently visible
	 * 
	 * @return <code>true</code> if the console is currently showing,
	 *         false otherwise
	 */
	public static boolean isVisible()
	{
		return bottom < Display.getDisplayMode().getHeight();
	}

	/**
	 * Loads the font file
	 */
	public static void loadFont()
	{
		InputStream fontstream =
				Thread.currentThread().getContextClassLoader()
						.getResourceAsStream( "font/console.ruglfont" );
		if( fontstream != null )
		{
			try
			{
				font = new Font( fontstream );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}

		if( font == null )
		{
			System.out.println( "Couldn't load console font" );
		}
	}

	/**
	 * Advances the console animation
	 * 
	 * @param delta
	 * @return The multiplier that should be used on the game's advance
	 *         time - allows the game to slow to a stop while the
	 *         console is deploying
	 */
	public float advance( float delta )
	{
		float h = Display.getDisplayMode().getHeight();

		float step = h / growthDuration;

		float g = ( enabled ? 1 : -1 ) * step * delta;

		bottom -= g;

		bottom = Math.max( 0, bottom );
		bottom = Math.min( h, bottom );

		return 1;
	}

	/**
	 * Draws the console if needed
	 */
	public static void draw()
	{
		if( isVisible() )
		{
			font.init( true );

			GLUtil.standardOrtho();

			GL11.glClear( GL11.GL_DEPTH_BUFFER_BIT );

			r.loadIdentity();

			r.translate( 0, bottom, 0 );

			drawBackground();

			r.translate( border, border, 0 );

			input.draw( r );

			r.translate( 0, input.getHeight(), 0 );

			r.pushMatrix();
			drawMessages();
			r.popMatrix();

			drawTexture();

			r.render();
		}
	}

	private static void drawBackground()
	{
		int w = Display.getDisplayMode().getWidth();
		int h = Display.getDisplayMode().getHeight();

		if( backGround == null )
		{
			for( int i = 0; i < vertexColours.length; i++ )
			{
				vertexColours[ i ] = Colour.white;
			}

			backGround =
					new ColouredShape( ShapeUtil.filledQuad( 0, 0, w - 2 * border, h, -1 ),
							vertexColours, null );
		}

		float a = 1 - bottom / h;

		int ba = ( int ) ( a * bottomColour.getAlpha() );
		int ta = ( int ) ( a * topColour.getAlpha() );

		vertexColours[ 0 ] = Colour.withAlphai( toInt( bottomColour ), ba );
		vertexColours[ 1 ] = Colour.withAlphai( toInt( topColour ), ta );
		vertexColours[ 2 ] = Colour.withAlphai( toInt( bottomColour ), ba );
		vertexColours[ 3 ] = Colour.withAlphai( toInt( topColour ), ta );

		r.translate( border, border, 0 );

		backGround.render( r );
	}

	private static void drawMessages()
	{
		if( font != null )
		{
			Iterator<LogMessage> iter = messages.iterator();

			float height = 0;

			while( iter.hasNext() )
			{
				LogMessage lm = iter.next();

				int screenHeight = Display.getDisplayMode().getHeight();
				if( height > screenHeight )
				{
					iter.remove();
				}
				else
				{
					if( lm.shape == null && lm.message.length() > 0 )
					{
						lm.shape =
								new TextLayout( lm.message, font, Alignment.LEFT, Display
										.getDisplayMode().getWidth() - 4 * border,
										lm.type.getColour() );

						lm.shape.textShape.translate( 0, ( lm.shape.lines.length - 1 )
								* font.size, 0 );
					}

					if( lm.shape != null )
					{
						lm.shape.textShape.render( r );
						float heightInc = lm.shape.lines.length * font.size + font.leading + 2;
						height += heightInc;
						r.translate( 0, heightInc, 0 );
					}
				}
			}
		}
	}

	private static void drawTexture()
	{
		if( inspectedTexture != null && textureInspector == null
				|| textureInspector != null && textureInspector.texture != inspectedTexture )
		{ // need to refresh

			if( inspectedTexture == null )
			{
				textureInspector = null;
			}
			else
			{
				Rectanglef rect =
						new Rectanglef( border, border, inspectedTexture.getWidth(),
								inspectedTexture.getHeight() );

				// the max size we have
				final float width = Display.getDisplayMode().getWidth() - 6 * border;
				final float height =
						Display.getDisplayMode().getHeight() - 2 * border - input.getHeight();

				float hScale = 1;
				float vScale = 1;
				if( rect.getWidth() > width )
				{
					hScale = width / rect.getWidth();
				}
				if( rect.getHeight() > height )
				{
					vScale = height / rect.getHeight();
				}

				float scale = Math.min( hScale, vScale );

				rect.setWidth( scale * rect.getWidth() );
				rect.setHeight( scale * rect.getHeight() );

				rect.setX( width - rect.getWidth() );
				rect.setY( height - rect.getHeight() - border );

				textureInspector =
						new TexturedShape(
								new ColouredShape( ShapeUtil.filledQuad( rect.getX(),
										rect.getY(), rect.getX() + rect.getWidth(), rect.getY()
												+ rect.getHeight(), 1 ), Colour.white, ( State ) null ),
								RenderUtils.getQuadTexCoords( 1 ), inspectedTexture );

				textureInspector.state =
						textureInspector.state.with( textureInspector.state.texture
								.with( new Filters( MinFilter.NEAREST, MagFilter.NEAREST ) ) );
			}
		}

		if( textureInspector != null )
		{
			textureInspector.render( r );
		}
	}

	private static class LogMessage
	{
		private String message;

		private TextLayout shape = null;

		private final Type type;

		private static enum Type
		{
			/***/
			LOG( textColour ),
			/***/
			ERROR( errorColour ),
			/***/
			SUGGEST( suggestColour ),
			/***/
			COMMAND( commandColour );

			public final Color color;

			private Type( Color color )
			{
				this.color = color;
			}

			/**
			 * @return The packed int colour
			 */
			public int getColour()
			{
				return toInt( color );
			}
		};

		private LogMessage( CharSequence m, Type type )
		{
			message = m.toString();
			this.type = type;
		}

		@Override
		public String toString()
		{
			return message;
		}
	}

	/**
	 * Closes the log file amd saves the command history
	 */
	public static void exit()
	{
		input.saveHistory();

		if( fw != null )
		{
			try
			{
				fw.flush();
				fw.close();
			}
			catch( IOException e )
			{
				System.err.println( "Error closing log file" );
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads the console command history from {@link Preferences}
	 */
	public static void loadCommandHistory()
	{
		input.loadHistory();
	}

	/**
	 * Notifies the console of input
	 * 
	 * @param key
	 * @param down
	 * @param repeat
	 * @param c
	 */
	public static void input( int key, boolean down, boolean repeat, char c )
	{
		if( down )
		{
			input.kl.keyDown( key, c, repeat );
		}
		else
		{
			input.kl.keyUp( key );
		}
	}

	/**
	 * @param c
	 * @return packed int colour
	 */
	public static int toInt( Color c )
	{
		return Colour.packInt( c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() );
	}
}
