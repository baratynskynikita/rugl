
package com.ryanm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility for working with the xrandr commmand-line utility. Assumes
 * xrandr v1.2 or higher.
 * 
 * @author ryanm
 */
public class XRandR
{
	private static Screen[] current;

	private static Map<String, Screen[]> screens;

	private static String xrandrOutput;

	private static void populate()
	{
		AccessController.doPrivileged( new PrivilegedAction<Object>() {
			@Override
			public Object run()
			{
				if( screens == null )
				{
					screens = new HashMap<String, Screen[]>();

					StringBuilder sb = new StringBuilder();

					// declare this out here so we can log it if a parsing
					// problem occurs
					String line = null;

					ProcessBuilder pb = new ProcessBuilder( "xrandr", "-q" );
					pb.redirectErrorStream();
					try
					{
						Process p = pb.start();

						List<Screen> currentList = new ArrayList<Screen>();
						List<Screen> possibles = new ArrayList<Screen>();
						String name = null;

						// just get all the input
						List<String> sl = new ArrayList<String>();
						BufferedReader br =
								new BufferedReader( new InputStreamReader( p.getInputStream() ) );
						while( ( line = br.readLine() ) != null )
						{
							sb.append( line ).append( "\n" );
							sl.add( line );
						}

						// we've got the output even if the parsing fails
						xrandrOutput = sb.toString();

						for( int i = 0; i < sl.size(); i++ )
						{
							// now parse it
							line = sl.get( i );
							line = line.trim();
							String[] sa = line.split( "\\s+" );

							if( sa[ 1 ].equals( "connected" ) )
							{
								// found a new screen block
								if( name != null )
								{
									screens.put( name, possibles.toArray( new Screen[ possibles.size() ] ) );
									possibles.clear();
								}
								name = sa[ 0 ];

								// record the current config
								currentList.add( new Screen( name, sa[ 2 ] ) );
							}
							else if( Pattern.matches( "\\d*x\\d*", sa[ 0 ] ) )
							{
								// found a new mode line
								possibles.add( new Screen( name, sa[ 0 ] ) );
							}
						}

						screens.put( name, possibles.toArray( new Screen[ possibles.size() ] ) );

						current = currentList.toArray( new Screen[ currentList.size() ] );
					}
					catch( IOException ioe )
					{
						ioe.printStackTrace();
						current = new Screen[ 0 ];
						screens.clear();
					}
					catch( Exception e )
					{ // might be NFE or AIOOBE
						e.printStackTrace();
						current = new Screen[ 0 ];
						screens.clear();
					}
				}
				return null;
			}
		} );
	}

	/**
	 * Gets the raw output from "xrandr -q"
	 * 
	 * @return the raw output xrandr
	 */
	public static String getXrandrOutput()
	{
		populate();
		return xrandrOutput;
	}

	/**
	 * @return The current screen configuration, or an empty array if
	 *         xrandr is not supported
	 */
	public static Screen[] getConfiguration()
	{
		populate();
		return current.clone();
	}

	/**
	 * @param screens
	 *           The desired screen set, no <code>null</code> elements
	 *           please
	 */
	public static void setConfiguration( final Screen... screens )
	{
		AccessController.doPrivileged( new PrivilegedAction<Object>() {
			@Override
			public Object run()
			{
				if( screens.length == 0 )
				{
					throw new IllegalArgumentException( "Must specify at least one screen" );
				}

				List<String> cmd = new ArrayList<String>();
				cmd.add( "xrandr" );

				// switch off those in the current set not in the new set
				for( int i = 0; i < current.length; i++ )
				{
					boolean found = false;
					for( int j = 0; j < screens.length; j++ )
					{
						if( screens[ j ].name.equals( current[ i ].name ) )
						{
							found = true;
							break;
						}
					}

					if( !found )
					{
						cmd.add( "--output" );
						cmd.add( current[ i ].name );
						cmd.add( "--off" );
					}
				}

				// set up new screen set
				for( int i = 0; i < screens.length; i++ )
				{
					screens[ i ].getArgs( cmd );
				}

				try
				{
					ProcessBuilder pb = new ProcessBuilder( cmd );
					pb.redirectErrorStream();
					Process p = pb.start();
					// no output is expected, but check anyway
					BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
					String line;
					while( ( line = br.readLine() ) != null )
					{
						System.out
								.println( "Unexpected output from xrandr when setting config : " + line );
					}

					// clone the array to prevent changes to argument
					// objects
					// altering held config objects
					current = screens.clone();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				return null;
			}
		} );
	}

	/**
	 * @return the name of connected screens, or an empty array if
	 *         xrandr is not supported
	 */
	public static String[] getScreenNames()
	{
		populate();
		return screens.keySet().toArray( new String[ screens.size() ] );
	}

	/**
	 * @param name
	 * @return the possible resolutions of the named screen, or
	 *         <code>null</code> if there is no such screen
	 */
	public static Screen[] getResolutions( String name )
	{
		populate();
		// clone the array to prevent held copies being altered
		return screens.get( name ).clone();
	}

	/**
	 * Encapsulates the configuration of a monitor. Resolution is
	 * fixed, position is mutable
	 * 
	 * @author ryanm
	 */
	public static class Screen implements Cloneable
	{
		/**
		 * Name for this output
		 */
		public final String name;

		/**
		 * Width in pixels
		 */
		public final int width;

		/**
		 * Height in pixels
		 */
		public final int height;

		/**
		 * Position on the x-axis, in pixels
		 */
		public int xPos = 0;

		/**
		 * Position on the y-axis, in pixels
		 */
		public int yPos = 0;

		/**
		 * @param name
		 *           name of the screen
		 * @param conf
		 *           config string, format either widthxheight or
		 *           widthxheight+xPos+yPos
		 */
		private Screen( String name, String conf )
		{
			this.name = name;

			String[] sa = conf.split( "\\D" );
			width = Integer.parseInt( sa[ 0 ] );
			height = Integer.parseInt( sa[ 1 ] );

			if( sa.length > 2 )
			{
				xPos = Integer.parseInt( sa[ 2 ] );
				yPos = Integer.parseInt( sa[ 3 ] );
			}
		}

		private void getArgs( List<String> argList )
		{
			argList.add( "--output" );
			argList.add( name );
			argList.add( "--mode" );
			argList.add( width + "x" + height );
			argList.add( "--pos" );
			argList.add( xPos + "x" + yPos );
		}

		@Override
		public String toString()
		{
			return name + " " + width + "x" + height + " @ " + xPos + "x" + yPos;
		}
	}
}
