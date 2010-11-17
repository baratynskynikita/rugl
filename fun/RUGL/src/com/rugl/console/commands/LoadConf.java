
package com.rugl.console.commands;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Command;
import com.rugl.console.Console;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;

/**
 * Loads a saved configuration
 * 
 * @author ryanm
 */
public class LoadConf extends Command
{
	/***/
	public LoadConf()
	{
		super( "loadconf" );
	}

	@Override
	public void execute( String command )
	{
		if( command.startsWith( "loadconf " ) )
		{
			String filepath = command.substring( 9 ).trim();

			try
			{
				File file = new File( GameBox.filebase, filepath ).getCanonicalFile();

				if( !file.isAbsolute() )
				{
					file = new File( GameBox.filebase, filepath ).getCanonicalFile();
				}

				try
				{
					ConfigurationSerialiser.loadConfiguration( file, GameBox.configurators );
					Console.log( "Configuration loaded from " + file.getPath() );
				}
				catch( IOException ioe )
				{
					Console.error( "Problem reading file : " + ioe.getMessage() );
				}
				catch( ParseException pe )
				{
					Console.error( "Could not parse file " + file.getAbsolutePath() );
				}
			}
			catch( IOException e )
			{
				Console.error( e.getMessage() );
			}
			catch( SecurityException se )
			{
				Console.error( "Security Exception : Cannot read " + filepath );
			}
		}
	}

	@Override
	public String getUsage()
	{
		return "loadconf <filename>\n\tLoads a configuration file. File paths are relative to /RUGL/Console/Save base";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 9 && "loadconf ".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "loadconf ", 0 ) );
		}
		else if( current.startsWith( "loadconf " ) )
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
