
package com.rugl.console.commands;

import java.util.LinkedList;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Command;
import com.rugl.console.Console;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.ConfGet;
import com.ryanm.config.serial.ConfigurationSerialiser;

/**
 * Prints the contents of configurators to the console
 * 
 * @author ryanm
 */
public class PrintConfig extends Command
{
	/**
	 */
	public PrintConfig()
	{
		super( "printconf" );
	}

	@Override
	public void execute( String command )
	{
		command = command.trim();

		if( command.equals( "printconf" ) )
		{
			// print all
			for( int i = 0; i < GameBox.configurators.length; i++ )
			{
				printConf( GameBox.configurators[ i ], 0, true );
			}
		}
		else if( command.startsWith( "printconf " ) )
		{
			boolean descend = true;

			// print conf
			String path = command.substring( 10 ).trim();

			if( path.endsWith( "*" ) )
			{
				descend = true;
				path = path.substring( 0, path.length() - 2 );
			}

			Configurator conf = ConfGet.forPath( path, GameBox.configurators );

			if( conf != null )
			{
				printConf( conf, 0, descend );
			}
			else
			{
				Console.error( "Could not find configurator \"" + path + "\"" );
			}
		}
	}

	private void printConf( Configurator conf, int depth, boolean descend )
	{
		if( conf != null )
		{
			String indent;
			{
				StringBuilder buff = new StringBuilder();
				for( int i = 0; i < depth; i++ )
				{
					buff.append( "   " );
				}
				indent = buff.toString();
			}

			Console.log( indent + ( depth == 0 ? conf.getPath() : conf.getName() ) + "/" );

			indent += "  ";

			for( Object o : conf.getNames() )
			{
				if( o instanceof Configurator )
				{
					Configurator sc = ( Configurator ) o;
					if( descend )
					{
						printConf( sc, depth + 1, descend );
					}
					else
					{
						Console.log( indent + sc.getName() + "/" );
					}
				}
				else
				{
					String varName = ( String ) o;

					StringBuilder b = new StringBuilder();

					b.append( indent );
					b.append( varName );
					Class type = conf.getType( varName );

					if( type != void.class )
					{
						b.append( " : " );
						b.append( type.getSimpleName() );
						b.append( " = " );
						b.append( ConfigurationSerialiser.encode( conf, varName ) );
					}

					String d = conf.getDescription( varName );
					if( d != null )
					{
						b.append( " -" ).append( d );
					}

					Console.log( b.toString() );
				}
			}
		}
	}

	@Override
	public String getUsage()
	{
		return "printconf <configurator path>\n\t"
				+ "Prints the values of some portion of the configuration tree";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 9 && "printconf".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "printconf", 0 ) );
		}
		else if( current.equals( "printconf " ) )
		{
			suggestions.add( new Suggestion( "/", 10 ) );
		}
		else if( current.startsWith( "printconf " ) )
		{
			String path = current.substring( 10 );
			path = path.trim();

			LinkedList<Suggestion> cs = new LinkedList<Suggestion>();
			suggestConfiguratorName( path, cs );

			while( !cs.isEmpty() )
			{
				Suggestion s = cs.removeFirst();
				s.offset += 10;
				suggestions.add( s );
			}
		}
	}
}
