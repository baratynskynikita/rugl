
package com.rugl.console.commands;

import java.util.LinkedList;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.console.Command;
import com.rugl.console.Console;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.ConfGet;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ParseException;
import com.ryanm.util.text.TextUtils;

/**
 * @author ryanm
 */
public class Set extends Command
{
	/***/
	public Set()
	{
		super( "set" );
	}

	@Override
	public void execute( String command )
	{
		String[] c = TextUtils.split( command, '(', ')', ' ' );

		if( c.length >= 2 )
		{
			if( c[ 0 ].equals( "set" ) )
			{
				String path = c[ 1 ];

				if( path.startsWith( "(" ) && path.endsWith( ")" ) )
				{
					path = path.substring( 1, path.length() - 1 );

					StringBuilder sb = new StringBuilder();
					for( int i = 2; i < c.length; i++ )
					{
						sb.append( c[ i ] ).append( " " );
					}
					String value = sb.toString().trim();

					if( value.startsWith( "(" ) && value.endsWith( ")" ) )
					{
						value = value.substring( 1, path.length() - 1 );
					}

					Configurator conf = ConfGet.forPath( path, GameBox.configurators );

					if( conf != null )
					{
						String cp = conf.getPath();

						String var = path.substring( cp.length() + 1 );

						// look for a case-insensitive match
						boolean found = false;
						for( int i = 0; i < conf.getNames().length && !found; i++ )
						{
							if( conf.getNames()[ i ] instanceof String )
							{
								String v = ( String ) conf.getNames()[ i ];
								if( v.compareToIgnoreCase( var ) == 0 )
								{
									var = v;
									found = true;
								}
							}
						}

						if( found )
						{
							try
							{
								Object nv = value;

								if( conf.getType( var ) != void.class )
								{
									nv = ConfigurationSerialiser.decode( conf, var, value );
								}

								conf.setValue( var, nv );

								if( conf.getType( var ) != void.class )
								{
									Console.log( "\"" + conf.getPath() + "/" + var + "\" set to "
											+ ConfigurationSerialiser.encode( conf, var ) );
								}
							}
							catch( ParseException e )
							{
								Console.error( "Could not parse value \"" + value + "\"" );
								Console.error( ConfigurationSerialiser.getFormatDescription(
										conf, var ) );
							}
						}
						else
						{
							Console.error( "Could not find variable \"" + var
									+ "\" in configurator \"" + conf.getPath() + "\"" );
						}
					}
					else
					{
						if( path.startsWith( "/" ) )
						{
							Console.error( "Configurator \"" + path + "\" not found" );
						}
						else
						{
							Console.error( "Configurator paths must start with \"/\"" );
						}
					}
				}
				else
				{
					Console.error( "Parenthesise the path : " + getUsage() );
				}
			}
		}
	}

	@Override
	public String getUsage()
	{
		return "set (<configurator path>) <value>\n\tSets the value of some variable";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 6 && "set (/".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "set (/", 0 ) );
		}
		else if( current.startsWith( "set (/" ) )
		{
			int close = current.indexOf( ") " );
			if( close == -1 )
			{
				String path = current.substring( 5 );

				LinkedList<Suggestion> cs = new LinkedList<Suggestion>();
				suggestConfiguratorName( path, cs );

				while( !cs.isEmpty() )
				{
					Suggestion s = cs.removeFirst();
					s.offset += 5;
					suggestions.add( s );
				}
			}
			else
			{
				LinkedList<Suggestion> vs = new LinkedList<Suggestion>();

				String conf = current.substring( 5, close );
				String value = current.substring( close + 2 );

				suggestValue( conf, value, vs );

				while( !vs.isEmpty() )
				{
					Suggestion s = vs.removeFirst();
					s.offset += close + 2;
					suggestions.add( s );
				}
			}
		}
	}
}
