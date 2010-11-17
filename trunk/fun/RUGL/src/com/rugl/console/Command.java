
package com.rugl.console;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Dimension;

import com.rugl.GameBox;
import com.rugl.input.KeyPress;
import com.ryanm.config.Configurator;
import com.ryanm.config.imp.ConfGet;
import com.ryanm.util.text.TextUtils;

/**
 * A console command
 * 
 * @author ryanm
 */
public abstract class Command
{
	/**
	 * The command name - what the user types to invoke. This string
	 * has been {@link String#intern()}ed
	 */
	public final String name;

	/**
	 * @param name
	 */
	protected Command( String name )
	{
		this.name = name.intern();
	}

	/**
	 * Suggests possible endings to the command
	 * 
	 * @param current
	 *           the start of the command
	 * @param suggestions
	 *           The list to add suggestions to
	 */
	public abstract void suggest( String current, List<Suggestion> suggestions );

	/**
	 * Executes the command
	 * 
	 * @param command
	 */
	public abstract void execute( String command );

	/**
	 * Gets a string describing usage
	 * 
	 * @return a usage hint
	 */
	public abstract String getUsage();

	private static String[] keyNames = null;

	/**
	 * Suggests commands
	 * 
	 * @param start
	 * @param suggestions
	 */
	public static void suggestCommand( String start, List<Suggestion> suggestions )
	{
		for( int i = 0; i < Console.commands.length; i++ )
		{
			Console.commands[ i ].suggest( start, suggestions );
		}
	}

	/**
	 * Suggests keypress encodings
	 * 
	 * @param start
	 * @param suggestions
	 */
	public static void suggestKeyPress( String start, List<Suggestion> suggestions )
	{
		if( start.length() == 0 )
		{
			suggestions.add( new Suggestion( "c:", 0 ) );
			suggestions.add( new Suggestion( "t:", 0 ) );
		}
		else if( start.length() == 1 )
		{
			if( start.charAt( 0 ) == 'c' || start.charAt( 0 ) == 't' )
			{
				suggestions.add( new Suggestion( ":", 1 ) );
			}
		}
		else
		{
			int i = 2;

			int li = start.lastIndexOf( '+' );

			if( li != -1 )
			{
				i = li + 1;
			}

			String k = start.substring( i );

			LinkedList<String> kn = new LinkedList<String>();
			suggestKeyName( k, kn );

			while( !kn.isEmpty() )
			{
				String s = kn.removeFirst();

				suggestions.add( new Suggestion( s, i ) );
			}
		}
	}

	/**
	 * Suggests possible key names based on the start of the name
	 * 
	 * @param start
	 *           the start of the name
	 * @param suggestions
	 *           the list to add suggestions to
	 */
	public static void suggestKeyName( String start, List<String> suggestions )
	{
		if( keyNames == null )
		{
			List<String> nl = new ArrayList<String>();

			Field[] field = Keyboard.class.getFields();

			for( int i = 0; i < field.length; i++ )
			{
				if( Modifier.isStatic( field[ i ].getModifiers() )
						&& Modifier.isPublic( field[ i ].getModifiers() )
						&& Modifier.isFinal( field[ i ].getModifiers() )
						&& field[ i ].getType().equals( int.class )
						&& field[ i ].getName().startsWith( "KEY_" )
						&& !field[ i ].getName().equals( "KEY_NONE" ) )
				{
					String name = field[ i ].getName().substring( 4 );
					nl.add( name );
				}
			}

			keyNames = new String[ nl.size() ];
			keyNames = nl.toArray( keyNames );
			Arrays.sort( keyNames );
		}

		int s = 0;

		while( s < keyNames.length
				&& !TextUtils.startsWithIgnoreCase( keyNames[ s ], start ) )
		{
			s++;
		}

		while( s < keyNames.length && TextUtils.startsWithIgnoreCase( keyNames[ s ], start ) )
		{
			suggestions.add( keyNames[ s ] );
			s++;
		}
	}

	/**
	 * Suggests possible {@link Configurator} names
	 * 
	 * @param start
	 *           A {@link Configurator} path
	 * @param suggestions
	 */
	public static void suggestConfiguratorName( String start, List<Suggestion> suggestions )
	{
		if( start.length() >= 1 && start.charAt( 0 ) == '/'
				&& start.lastIndexOf( '/' ) == 0 )
		{
			for( Configurator conf : GameBox.configurators )
			{
				if( conf != null && TextUtils.startsWithIgnoreCase( conf.getPath(), start ) )
				{
					suggestions.add( new Suggestion( conf.getName() + "/", 1 ) );
				}
			}
		}
		else
		{
			Configurator conf = ConfGet.forPath( start, GameBox.configurators );

			if( conf != null )
			{
				String path = conf.getPath();

				if( start.equals( path ) )
				{
					suggestions.add( new Suggestion( conf.getName() + "/", path.length()
							- conf.getName().length() ) );

					// check the parent conf for similarly-named variables
					if( conf.getParent() != null )
					{
						String pp = conf.getParent().getPath();

						String varSearch = start.substring( pp.length() + 1 );

						for( Object o : conf.getParent().getNames() )
						{
							if( o instanceof String )
							{
								String var = ( String ) o;

								if( var.startsWith( varSearch ) )
								{
									suggestions.add( new Suggestion( var + ") ", pp.length() ) );
								}
							}
						}
					}
				}
				else
				{
					path += "/";
					if( start.startsWith( path ) )
					{
						String var = start.substring( path.length() );

						for( Object o : conf.getNames() )
						{
							if( o instanceof Configurator )
							{
								Configurator sc = ( Configurator ) o;

								if( TextUtils.startsWithIgnoreCase( sc.getName(), var ) )
								{
									suggestions.add( new Suggestion( sc.getName() + "/", path
											.length() ) );
								}
							}
							else
							{
								if( TextUtils.startsWithIgnoreCase( ( String ) o, var ) )
								{
									suggestions.add( new Suggestion( ( String ) o + ") ", path
											.length() ) );
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Tries to suggest a value completion
	 * 
	 * @param conf
	 *           The variable to suggest for
	 * @param v
	 *           The current value string
	 * @param suggestion
	 *           The list of suggestions to add to
	 */
	public static void suggestValue( String conf, String v, List<Suggestion> suggestion )
	{
		Configurator c = ConfGet.forPath( conf, GameBox.configurators );

		if( c != null )
		{
			String varName = conf.substring( conf.lastIndexOf( "/" ) + 1 );
			Class type = c.getType( varName );
			Object range = c.getRange( varName );

			if( ( type == Dimension.class || type == String.class ) && range != null )
			{
				String[] sr = ( String[] ) range;
				for( int i = 0; i < sr.length; i++ )
				{
					if( TextUtils.startsWithIgnoreCase( sr[ i ], v ) )
					{
						suggestion.add( new Suggestion( sr[ i ], 0 ) );
					}
				}
			}
			else if( type == String[].class && range != null )
			{
				int l = v.lastIndexOf( "|" );
				l = l < 0 ? 0 : l;
				String last = v.substring( l );

				String[] sr = ( String[] ) range;
				for( int i = 0; i < sr.length; i++ )
				{
					if( TextUtils.startsWithIgnoreCase( sr[ i ], last ) )
					{
						suggestion.add( new Suggestion( sr[ i ], l ) );
					}
				}
			}
			else if( type == boolean.class )
			{
				if( TextUtils.startsWithIgnoreCase( "true", v ) )
				{
					suggestion.add( new Suggestion( "true", 0 ) );
				}
				if( TextUtils.startsWithIgnoreCase( "false", v ) )
				{
					suggestion.add( new Suggestion( "false", 0 ) );
				}
			}
			else if( type == KeyPress.class )
			{
				suggestKeyPress( v, suggestion );
			}
			else if( type.isEnum() )
			{
				Object[] e = type.getEnumConstants();
				for( int i = 0; i < e.length; i++ )
				{
					Enum en = ( Enum ) e[ i ];
					if( TextUtils.startsWithIgnoreCase( en.toString(), v ) )
					{
						suggestion.add( new Suggestion( en.toString(), 0 ) );
					}
				}
			}
			else if( type == File.class )
			{
				try
				{
					suggestFile( v, suggestion, ( String[] ) range );
				}
				catch( IOException e )
				{
				}
			}
		}
	}

	/**
	 * Suggests completions for filenames
	 * 
	 * @param current
	 *           The current command line contents
	 * @param suggest
	 *           A list to add the suggestions to
	 * @param suffixes
	 *           An array of acceptable file suffixes, or null to
	 *           accept all
	 * @throws IOException
	 *            We inspect the hard drive, so this could happen
	 * @throws SecurityException
	 *            or this
	 */
	public static void suggestFile( String current, List<Suggestion> suggest,
			final String[] suffixes ) throws IOException
	{
		if( GameBox.secureEnvironment )
		{
			return;
		}

		String filePath = current;
		int lastSlash = filePath.lastIndexOf( File.separator );
		File basedir = GameBox.filebase;
		int off = 0;

		if( new File( current ).isAbsolute() )
		{
			for( File root : File.listRoots() )
			{
				if( current.startsWith( root.getPath() ) )
				{
					basedir = root;
					off = root.getPath().length();
					filePath = filePath.substring( off );
					lastSlash = filePath.lastIndexOf( File.separator );

					break;
				}
			}
		}

		if( lastSlash != -1 )
		{
			basedir = new File( basedir, filePath.substring( 0, lastSlash ) );

			filePath = filePath.substring( lastSlash + 1 );
		}

		File[] files = basedir.listFiles( new FileFilter() {

			@Override
			public boolean accept( File file )
			{
				if( suffixes == null )
				{
					return true;
				}
				else
				{
					for( int i = 0; i < suffixes.length; i++ )
					{
						if( file.isDirectory() || file.getName().endsWith( suffixes[ i ] ) )
						{
							return true;
						}
					}

					return false;
				}
			}
		} );

		if( files != null )
		{
			for( File f : files )
			{
				if( !f.isHidden() && f.getName().startsWith( filePath ) )
				{
					suggest.add( new Suggestion( f.getName()
							+ ( f.isDirectory() ? File.separator : "" ), lastSlash + 1 + off ) );
				}
			}
		}
	}

	/**
	 * Encapsulates a command completion suggestion
	 * 
	 * @author ryanm
	 */
	public static class Suggestion implements Comparable<Suggestion>
	{
		/**
		 * The text of the suggestion
		 */
		public String suggestion;

		/**
		 * The index in the command at which the suggestion should be
		 * inserted
		 */
		public int offset;

		/**
		 * @param suggestion
		 * @param offset
		 */
		public Suggestion( String suggestion, int offset )
		{
			this.suggestion = suggestion;
			this.offset = offset;
		}

		@Override
		public String toString()
		{
			return suggestion + " at " + offset;
		}

		@Override
		public int compareTo( Suggestion o )
		{
			return suggestion.compareTo( o.suggestion );
		}
	}
}
