
package com.ryanm.trace;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Command;
import com.rugl.console.Console;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.ConfGet;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;

/**
 * @author ryanm
 */
public class LoadGame extends Command
{

	/***/
	public static final String dirName = "games";

	/***/
	public LoadGame()
	{
		this( "loadgametype " );
	}

	/**
	 * @param name
	 */
	protected LoadGame( String name )
	{
		super( name );
	}

	@Override
	public void execute( String command )
	{
		if( command.startsWith( name ) )
		{
			if( GameBox.secureEnvironment )
			{
				Console.error( "Cannot load or save gametype in sandbox" );
				return;
			}

			String typeName = command.substring( name.length() ).trim();
			if( !typeName.isEmpty() )
			{
				File dir = new File( GameBox.filebase, dirName );
				dir.mkdirs();
				File f = new File( dir, typeName );

				Configurator conf = ConfGet.getConfigurator( TraceGame.game );
				operate( f, conf );
			}
			else
			{
				Console.error( getUsage() );
			}
		}
	}

	/**
	 * @param f
	 * @param conf
	 */
	protected void operate( File f, Configurator conf )
	{
		if( f.exists() )
		{
			try
			{
				ConfigurationSerialiser.loadConfiguration( f, conf );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			catch( ParseException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			Console.error( "gametype not found" );
		}
	}

	@Override
	public String getUsage()
	{
		return name + "<typename>";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( GameBox.secureEnvironment )
		{
			return;
		}

		if( name.startsWith( current ) && current.length() < name.length() )
		{
			suggestions.add( new Suggestion( name, 0 ) );
		}
		else if( current.startsWith( name ) )
		{
			final String sn = current.substring( name.length() ).trim();
			FilenameFilter fnf = new FilenameFilter() {
				@Override
				public boolean accept( File dir, String n )
				{
					return sn.isEmpty() || n.startsWith( sn );
				}
			};
			File dir = new File( GameBox.filebase, dirName );
			dir.mkdirs();
			File[] possibles = dir.listFiles( fnf );

			for( File f : possibles )
			{
				suggestions.add( new Suggestion( f.getName(), name.length() ) );
			}
		}
	}

}
