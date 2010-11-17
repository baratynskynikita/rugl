
package com.rugl.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.Timer;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.GameBox;
import com.rugl.console.Command.Suggestion;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.input.KeyListener;
import com.rugl.renderer.StackedRenderer;
import com.rugl.sound.Sound;
import com.rugl.sound.SoundSystem;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.util.Colour;
import com.ryanm.soundgen.SoundSpec;
import com.ryanm.soundgen.imp.Constant;
import com.ryanm.soundgen.imp.SawtoothWave;

/**
 * @author ryanm
 */
class ConsoleInput
{
	private int textColour = Colour.white;

	private StringBuilder text = new StringBuilder();

	private TextLayout textLayout = null;

	private int caretPosition = 0;

	private ColouredShape caret;

	// private float caretBlink = 0.5f;

	private Timer blinkTimer = new Timer();

	private String[] history = new String[ 20 ];

	private int historyCursor = -1;

	private String historySave = "";

	private Sound noComplete = null;

	private float caretBlink = 0.5f;

	/**
	 * Number of pixels around the edge
	 */
	private float border = 5;

	KeyListener kl = new KeyListener() {
		@Override
		public void keyDown( int keyCode, char keyChar, boolean repeat )
		{
			boolean consumed = true;

			String command = text.toString();
			switch( keyCode )
			{
				case Keyboard.KEY_TAB:
					suggest();
					break;
				case Keyboard.KEY_RETURN:
					if( text.length() > 0 )
					{
						addToHistory( command );
						Console.execute( command );
						text.delete( 0, text.length() );
						textLayout = null;
						caretPosition = 0;
						historyCursor = -1;
						historySave = "";
					}
					break;
				case Keyboard.KEY_RIGHT:
					caretPosition++;
					caretPosition = Math.min( caretPosition, text.length() );
					textLayout = null;
					break;
				case Keyboard.KEY_LEFT:
					caretPosition--;
					caretPosition = Math.max( caretPosition, 0 );
					textLayout = null;
					break;
				case Keyboard.KEY_UP:
					if( historyCursor == -1 )
					{
						historySave = command;
					}

					if( historyCursor < history.length - 1
							&& history[ historyCursor + 1 ] != null )
					{
						historyCursor++;
						text.delete( 0, text.length() );
						text.append( history[ historyCursor ] );

						textLayout = null;
					}
					caretPosition = text.length();
					break;
				case Keyboard.KEY_DOWN:
					if( historyCursor > -1 )
					{
						historyCursor--;

						if( historyCursor == -1 )
						{
							text.delete( 0, text.length() );
							text.append( historySave );
						}
						else
						{
							text.delete( 0, text.length() );
							text.append( history[ historyCursor ] );
						}
						textLayout = null;
					}
					caretPosition = text.length();
					break;
				case Keyboard.KEY_END:
					caretPosition = text.length();
					textLayout = null;
					break;
				case Keyboard.KEY_HOME:
					caretPosition = 0;
					textLayout = null;
					break;
				case Keyboard.KEY_BACK:
					if( caretPosition >= 1 )
					{
						caretPosition--;
						text.deleteCharAt( caretPosition );
						textLayout = null;
					}
					break;
				case Keyboard.KEY_DELETE:
					if( caretPosition < text.length() )
					{
						text.deleteCharAt( caretPosition );
						textLayout = null;
					}
					break;
				case Keyboard.KEY_L:
					if( Keyboard.isKeyDown( Keyboard.KEY_LCONTROL )
							|| Keyboard.isKeyDown( Keyboard.KEY_RCONTROL ) )
					{
						Console.clear();
					}
					else
					{
						consumed = false;
					}
					break;
				case Keyboard.KEY_SPACE:
					if( Keyboard.isKeyDown( Keyboard.KEY_LCONTROL )
							|| Keyboard.isKeyDown( Keyboard.KEY_RCONTROL ) )
					{
						String cmd = text.substring( 0, caretPosition );
						text.delete( 0, caretPosition );
						caretPosition = 0;

						for( int i = 0; i < cmd.length(); i++ )
						{
							text.insert( caretPosition++, cmd.charAt( i ) );

							suggest();
						}
					}
					else
					{
						consumed = false;
					}
					break;
				default:
					consumed = false;
			}

			if( !consumed && keyChar != Keyboard.CHAR_NONE
					&& !GameBox.consoleTrigger.contains( keyCode ) )
			{
				text.insert( caretPosition++, keyChar );
				textLayout = null;
			}
		}
	};

	ConsoleInput()
	{

		SoundSpec ss = new SoundSpec();
		ss.length = 0.01f;
		ss.volumeEnvelope = new Constant( 0.3f );
		ss.waveform = new SawtoothWave();
		ss.waveform.frequency = new Constant( 12000 );
		ss.superSamples = 8;

		noComplete = new Sound( ss, 44100 );
	}

	private void suggest()
	{
		List<Suggestion> suggest = new ArrayList<Suggestion>();

		int si = text.substring( 0, caretPosition ).lastIndexOf( ";" );
		si++;
		String start = text.substring( si, caretPosition );

		Command.suggestCommand( start, suggest );

		for( Suggestion s : suggest )
		{
			s.offset += si;
		}

		if( suggest.size() == 1 )
		{
			Suggestion s = suggest.get( 0 );

			text.delete( s.offset, caretPosition );

			text.insert( s.offset, s.suggestion );
			caretPosition = s.offset + s.suggestion.length();
			textLayout = null;
		}
		else if( suggest.size() > 1 )
		{
			// look for a common prefix
			boolean common = true;
			int i = -1;

			while( common )
			{
				i++;

				if( i >= suggest.get( 0 ).suggestion.length() )
				{
					common = false;
				}
				else
				{
					char c = suggest.get( 0 ).suggestion.charAt( i );

					for( int j = 0; j < suggest.size() && common; j++ )
					{
						if( i >= suggest.get( j ).suggestion.length() )
						{
							common = false;
						}
						else
						{
							common &= suggest.get( j ).suggestion.charAt( i ) == c;
						}
					}
				}
			}

			if( i > 0 )
			{
				Suggestion s = suggest.get( 0 );
				text.delete( s.offset, caretPosition );

				text.insert( s.offset, s.suggestion.subSequence( 0, i ) );
				caretPosition = s.offset + i;
				textLayout = null;
			}

			// print out the possibilities
			Collections.sort( suggest );
			StringBuilder buff = new StringBuilder();

			for( Suggestion s : suggest )
			{
				buff.append( "   " );
				buff.append( s.suggestion );
			}

			Console.suggest( buff );
		}
		else
		{
			Console.suggest( null );
			SoundSystem.getSource().bindSound( noComplete ).play();
		}
	}

	/**
	 * Ignores dups
	 * 
	 * @param command
	 */
	private void addToHistory( String command )
	{
		if( !command.equals( history[ 0 ] ) )
		{
			// shuffle
			for( int i = history.length - 1; i > 0; i-- )
			{
				history[ i ] = history[ i - 1 ];
			}

			history[ 0 ] = command;
		}
	}

	void saveHistory()
	{
		if( !GameBox.secureEnvironment )
		{
			return;
		}

		Preferences prefs = Preferences.userNodeForPackage( KeyBinds.class );
		prefs = prefs.node( GameBox.game.getName() );
		prefs = prefs.node( "console history" );

		for( int i = 0; i < history.length; i++ )
		{
			if( history[ i ] == null )
			{
				break;
			}

			prefs.put( String.valueOf( i ), history[ i ] );
		}

		try
		{
			prefs.flush();
		}
		catch( BackingStoreException e )
		{
			e.printStackTrace();
		}
	}

	void loadHistory()
	{
		if( !GameBox.secureEnvironment )
		{
			return;
		}

		Preferences prefs = Preferences.userNodeForPackage( KeyBinds.class );
		prefs = prefs.node( GameBox.game.getName() );
		prefs = prefs.node( "console history" );

		try
		{
			for( String key : prefs.keys() )
			{
				int index = Integer.valueOf( key ).intValue();

				history[ index ] = prefs.get( key, null );
			}
		}
		catch( NumberFormatException e )
		{
			Console.error( "Unexpected key when loading command history " + e.getMessage() );
		}
		catch( BackingStoreException e )
		{
			Console.error( "Error loading command history" );
		}
	}

	void draw( StackedRenderer r )
	{
		r.pushMatrix();

		r.translate( 0, border, 0 );

		if( textLayout == null && text.length() > 0 )
		{
			int width =
					( int ) ( Display.getDisplayMode().getWidth() - 4 * Console.border - 2 * border );
			textLayout =
					new TextLayout( text, Console.font, Alignment.LEFT, width,
							Console.toInt( Console.commandColour ) );
		}

		if( textLayout != null )
		{
			// draw text
			r.translate( 0, ( textLayout.lines.length - 1 ) * Console.font.size, 0 );
			textLayout.textShape.render( r );
		}

		// draw caret
		int t = ( int ) ( blinkTimer.getTime() / caretBlink );
		if( t % 2 == 0 )
		{
			if( textLayout != null )
			{
				Vector2f cr = textLayout.getCaretPosition( caretPosition, null );

				r.translate( cr.x, cr.y, 0 );
			}

			if( caret == null )
			{
				caret =
						new ColouredShape( ShapeUtil.filledQuad( 0, -Console.font.descent, 1,
								Console.font.ascent, 0 ), textColour, null );
			}

			caret.render( r );
		}

		r.popMatrix();
	}

	float getHeight()
	{
		return border * 2 + Console.font.size
				* ( textLayout != null ? textLayout.lines.length : 1 );
	}
}