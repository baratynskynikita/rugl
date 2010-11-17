
package com.rugl.console.commands;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Command;
import com.rugl.console.Console;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.serial.ConfigurationSerialiser;

/**
 * Saves a configuration to a file
 * 
 * @author ryanm
 */
@ConfigurableType( "Save configuration" )
public class SaveConf extends Command
{
	/***/
	public SaveConf()
	{
		super( "saveconf" );
	}

	@Override
	public void execute( String command )
	{
		if( command.startsWith( "saveconf " ) )
		{
			String filepath = command.substring( 9 ).trim();

			try
			{
				File file = new File( GameBox.filebase, filepath ).getCanonicalFile();

				ConfigurationSerialiser.saveConfiguration( file, GameBox.configurators );
				Console.log( "Configuration written to " + file.getPath() );
			}
			catch( IOException e )
			{
				Console.error( e.getMessage() );
			}
			catch( SecurityException se )
			{
				Console.error( "Security Exception : Cannot write to " + filepath );
			}
		}
	}

	@Override
	public String getUsage()
	{
		return "saveconf <filename>\n\tSaves the current configuration to a file. File paths are relative to /RUGL/Console/Save base";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 9 && "saveconf ".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "saveconf ", 0 ) );
		}
		else if( current.startsWith( "saveconf " ) )
		{
			try
			{
				List<Suggestion> sl = new LinkedList<Suggestion>();

				suggestFile( current.substring( 9 ), sl, null );

				for( Suggestion s : sl )
				{
					s.offset += 9;
					suggestions.add( s );
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			catch( SecurityException se )
			{
				se.printStackTrace();
			}
		}
	}

}
