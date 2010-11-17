
package com.rugl.console.commands;

import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.Console;
import com.rugl.console.KeyBinds;
import com.ryanm.util.text.TextUtils;

/**
 * @author ryanm
 */
public class LoadBinds extends Command
{
	/**
	 */
	public LoadBinds()
	{
		super( "loadbinds" );
	}

	@Override
	public void execute( String command )
	{
		String saveName = null;
		command = command.trim();

		if( command.startsWith( name ) )
		{
			saveName = command.substring( 9 ).trim();

			if( saveName.isEmpty() )
			{
				saveName = "default";
			}

			KeyBinds.loadBinds( saveName );
			KeyBinds.printBinds();
		}
		else
		{
			Console.error( getUsage() );
		}
	}

	@Override
	public String getUsage()
	{
		return "loadbinds <savename>\n\tLoads a named set of key bindings."
				+ " Leave the savename blank to save under \"default\"";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 10 && "loadbinds ".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "loadbinds ", 0 ) );
		}

		if( current.startsWith( "loadbinds " ) )
		{
			String[] sr = KeyBinds.listBindSaves();
			String s = current.substring( 10 );

			for( int i = 0; i < sr.length; i++ )
			{
				if( TextUtils.startsWithIgnoreCase( sr[ i ], s ) )
				{
					suggestions.add( new Suggestion( sr[ i ], 10 ) );
				}
			}
		}
	}
}
