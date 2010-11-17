
package com.rugl.console.commands;

import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.Console;
import com.rugl.console.KeyBinds;
import com.rugl.console.KeyBinds.Binding;
import com.rugl.input.KeyPress;

/**
 * @author ryanm
 */
public class Unbind extends Command
{
	/***/
	public Unbind()
	{
		super( "unbind" );
	}

	@Override
	public void execute( String command )
	{
		String[] k = command.split( " " );

		if( k.length >= 2 )
		{
			if( k[ 1 ].compareToIgnoreCase( "all" ) == 0 )
			{
				KeyBinds.clear();
			}
			else
			{
				KeyPress kp = KeyPress.fromString( k[ 1 ] );

				if( kp != null )
				{
					boolean b = KeyBinds.removeBind( kp );
					if( b )
					{
						Console.log( "Binding for " + kp + " removed" );
					}
					else
					{
						Console.error( "No binding for " + kp + " found" );
					}
				}
				else
				{
					Console.error( "Could not parse keypress \"" + k[ 1 ]
							+ "\" Correct format is \"<t|c>:<key1>+...+<keyN>" );
				}
			}
		}
		else
		{
			Console.log( getUsage() );
		}
	}

	@Override
	public String getUsage()
	{
		return "unbind <<keypress>|all>\n\tDeletes one or all of the current set of key bindings";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 7 && "unbind ".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "unbind ", 0 ) );
		}
		else if( current.length() >= 7 && current.startsWith( "unbind " ) )
		{
			// suggest keypresses
			String kp = current.substring( 7 );

			if( "all".startsWith( kp ) )
			{
				suggestions.add( new Suggestion( "all", 7 ) );
			}

			for( Binding bind : KeyBinds.getBinds() )
			{
				String b = bind.kp.toString();

				if( b.startsWith( kp ) )
				{
					suggestions.add( new Suggestion( b, 7 ) );
				}
			}
		}
	}
}