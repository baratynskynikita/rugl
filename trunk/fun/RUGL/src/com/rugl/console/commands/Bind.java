
package com.rugl.console.commands;

import java.util.LinkedList;
import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.Console;
import com.rugl.console.KeyBinds;
import com.rugl.input.KeyPress;

/**
 * @author ryanm
 */
public class Bind extends Command
{
	/**
	 */
	public Bind()
	{
		super( "bind" );
	}

	@Override
	public void execute( String command )
	{
		int ki = command.indexOf( " ", 5 );

		if( ki != -1 )
		{
			CharSequence kps = command.subSequence( 5, ki );
			KeyPress kp = KeyPress.fromString( kps );

			if( kp != null )
			{
				String c = command.substring( ki + 1 ).trim();

				if( c.startsWith( "(" ) && c.endsWith( ")" ) )
				{
					KeyBinds.addBind( kp, c.substring( 1, c.length() - 1 ) );
				}
				else
				{
					Console.error( "Bound command must be in brackets" );
				}
			}
			else
			{
				Console.error( "Could not parse keypress \"" + kps
						+ "\" Correct format is \"<t|c>:<key1>+...+<keyN>" );
			}
		}
		else
		{
			Console.error( getUsage() );
		}
	}

	@Override
	public String getUsage()
	{
		return "bind <keypress> (<command>)\n\tBinds a keypress to execute some command";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 5 && "bind ".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "bind ", 0 ) );
		}
		else if( current.length() >= 5 && current.startsWith( "bind " ) )
		{
			if( current.lastIndexOf( " " ) == 4 )
			{
				// suggest keypresses
				String kp = current.substring( 5 );

				LinkedList<Suggestion> ks = new LinkedList<Suggestion>();
				suggestKeyPress( kp, ks );

				while( !ks.isEmpty() )
				{
					Suggestion s = ks.removeFirst();
					s.offset += 5;
					suggestions.add( s );
				}
			}
			else if( current.indexOf( " ", 5 ) == current.length() - 1 )
			{
				suggestions.add( new Suggestion( "(", current.length() ) );
			}
			else
			{
				List<Suggestion> cs = new LinkedList<Suggestion>();

				int start = current.indexOf( "(" ) + 1;
				if( start != 0 )
				{
					Command.suggestCommand( current.substring( start ), cs );

					for( Suggestion s : cs )
					{
						s.offset += start;
						suggestions.add( s );
					}
				}
			}
		}
	}
}
