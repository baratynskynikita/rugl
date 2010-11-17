
package com.rugl.console.commands;

import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.KeyBinds;

/**
 * @author ryanm
 */
public class PrintBinds extends Command
{
	/**
	 */
	public PrintBinds()
	{
		super( "printbinds" );
	}

	@Override
	public void execute( String command )
	{
		command = command.trim();

		if( command.equals( "printbinds" ) )
		{
			KeyBinds.printBinds();
		}
	}

	@Override
	public String getUsage()
	{
		return "printbinds\n\tPrints the current set of keybindings";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 10 && "printbinds".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "printbinds", 0 ) );
		}
	}

}
