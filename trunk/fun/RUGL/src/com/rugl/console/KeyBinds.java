
package com.rugl.console;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.rugl.GameBox;
import com.rugl.input.KeyPress;

/**
 * Maintains bindings between {@link KeyPress} and {@link Console}
 * commands
 * 
 * @author ryanm
 */
public class KeyBinds
{
	private static Binding[] binds = new Binding[ 0 ];

	private static String[] bindSaves = null;

	/**
	 * Gets the current set of keybinds
	 * 
	 * @return the binds
	 */
	public static Binding[] getBinds()
	{
		return binds;
	}

	/**
	 * Checks all {@link Binding}s and executes active ones
	 */
	public static void checkBinds()
	{
		for( int i = 0; i < binds.length; i++ )
		{
			binds[ i ].check();
		}
	}

	/**
	 * Adds a bind
	 * 
	 * @param kp
	 * @param command
	 */
	public static void addBind( KeyPress kp, String command )
	{
		Binding b = null;
		for( int i = 0; i < binds.length && b == null; i++ )
		{
			if( binds[ i ].kp.equals( kp ) )
			{
				b = binds[ i ];
			}
		}

		if( b != null )
		{
			b.append( command );
		}
		else
		{
			Binding[] nkb = new Binding[ binds.length + 1 ];
			System.arraycopy( binds, 0, nkb, 0, binds.length );

			b = new Binding( kp, command );

			nkb[ nkb.length - 1 ] = new Binding( kp, command );

			binds = nkb;
		}

		Console.log( b.toString() );
	}

	/**
	 * Removes a bind
	 * 
	 * @param kp
	 * @return <code>true</code> if a bind was removed
	 */
	public static boolean removeBind( KeyPress kp )
	{
		int index = -1;
		for( int i = 0; i < binds.length; i++ )
		{
			if( kp.equals( binds[ i ].kp ) )
			{
				index = i;
			}
		}

		if( index != -1 )
		{
			if( binds.length == 1 )
			{
				binds = new Binding[ 0 ];
			}
			else
			{
				Binding[] nkb = new Binding[ binds.length - 1 ];

				System.arraycopy( binds, 0, nkb, 0, index );

				if( index < binds.length - 1 )
				{
					System.arraycopy( binds, index, nkb, index, binds.length - index );
				}

				binds = nkb;
			}

			return true;
		}

		Console.error( "Could not find binding for " + kp + " to unbind" );

		return false;
	}

	/**
	 * Removes all bindings
	 */
	public static void clear()
	{
		binds = new Binding[ 0 ];

		Console.log( "Key bindings cleared" );
	}

	/**
	 * Saves the current set of keybinds to {@link Preferences}
	 * 
	 * @param saveName
	 *           the name under which to save
	 */
	public static void saveBinds( String saveName )
	{
		Console.log( "Saving keybinds to " + saveName );

		if( GameBox.secureEnvironment )
		{
			Console.error( "Cannot use prefs in sandbox" );
			return;
		}

		Preferences prefs = getPreferencesRoot();
		prefs = prefs.node( saveName );

		try
		{
			prefs.clear();
		}
		catch( BackingStoreException e1 )
		{
			Console.error( "Error clearing preferences when preparing to save keybinds" );
		}

		for( Binding b : binds )
		{
			prefs.put( b.kp.toString(), b.command );
		}

		try
		{
			prefs.flush();
		}
		catch( BackingStoreException e )
		{
			Console.error( "Error flushing preferences when saving keybinds" );
		}

		bindSaves = null;
	}

	private static Preferences getPreferencesRoot()
	{
		Preferences prefs = Preferences.userNodeForPackage( GameBox.game.getClass() );
		prefs = prefs.node( KeyBinds.class.getName() );
		return prefs;
	}

	/**
	 * Prints the current set of bindings to the console
	 */
	public static void printBinds()
	{
		Console.log( binds.length + " bind" + ( binds.length == 1 ? "ing" : "s" )
				+ " active" );
		for( Binding b : binds )
		{
			Console.log( "  " + b );
		}
	}

	/**
	 * Lists the available saved keybind sets
	 * 
	 * @return An array of saved keybinding names
	 */
	public static String[] listBindSaves()
	{
		if( GameBox.secureEnvironment )
		{
			bindSaves = new String[ 0 ];
		}

		if( bindSaves == null )
		{
			Preferences prefs = getPreferencesRoot();

			try
			{
				bindSaves = prefs.childrenNames();
			}
			catch( BackingStoreException e )
			{
				Console.error( "Error inspecting saved keybinds" );
				bindSaves = new String[ 0 ];
			}
		}

		return bindSaves;
	}

	/**
	 * Replaces the current set of binds from the {@link Preferences}
	 * 
	 * @param saveName
	 *           the named set to load
	 */
	public static void loadBinds( String saveName )
	{
		clear();

		if( GameBox.secureEnvironment )
		{
			Console.error( "Cannot use prefs in sandbox" );
			return;
		}

		Console.log( "Loading keybinds from " + saveName );

		Preferences prefs = getPreferencesRoot();
		prefs = prefs.node( saveName );

		try
		{
			for( String key : prefs.keys() )
			{
				KeyPress kp = KeyPress.fromString( key );
				String command = prefs.get( key, null );

				if( kp != null )
				{
					assert command != null;
					addBind( kp, command );
				}
				else
				{
					Console.error( "Could not parse keypress \"" + key + "\"" );
				}
			}
		}
		catch( BackingStoreException e )
		{
			Console.error( "Could not access preferences to load keybinds" );
		}

	}

	/**
	 * @author ryanm
	 */
	public static class Binding
	{
		/**
		 * The keypress that activates this binding
		 */
		public final KeyPress kp;

		private String command;

		/**
		 * @param kp
		 * @param command
		 */
		public Binding( KeyPress kp, String command )
		{
			this.kp = kp;
			this.command = command;
		}

		/**
		 * @param c
		 */
		public void append( String c )
		{
			command = command + ";" + c;
		}

		/**
		 * 
		 */
		public void check()
		{
			if( kp.isActive() )
			{
				Console.execute( command );
			}
		}

		/**
		 * Gets the command
		 * 
		 * @return the command
		 */
		public String command()
		{
			return command;
		}

		@Override
		public String toString()
		{
			return kp.toString() + " invokes " + command;
		}
	}
}
