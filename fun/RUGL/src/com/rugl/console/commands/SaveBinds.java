
package com.rugl.console.commands;

import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.KeyBinds;

/**
 * @author ryanm
 */
public class SaveBinds extends Command
{
	/***/
	public SaveBinds()
	{
		super( "savebinds" );
	}

	@Override
	public void execute( String command )
	{
		String saveName = null;
		command = command.trim();

		if( command.equals( name ) )
		{
			saveName = "default";
		}
		else
		{
			saveName = command.substring( 10 );
			saveName = saveName.trim();
		}

		KeyBinds.saveBinds( saveName );
	}

	@Override
	public String getUsage()
	{
		return "savebinds <savename>\n\tSaves the current set of key bindings under the specified name."
				+ " Leave the savename blank to save under \"default\"";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < name.length() && name.startsWith( current ) )
		{
			suggestions.add( new Suggestion( name + " ", 0 ) );
		}
	}

}
