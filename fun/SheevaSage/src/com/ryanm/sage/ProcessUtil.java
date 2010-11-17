
package com.ryanm.sage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * {@link SheevaSage} relies on other programs to do the hard stuff,
 * this class provides convenient ways to call external programs
 * 
 * @author ryanm
 */
public class ProcessUtil
{
	/**
	 * Executes the command
	 * 
	 * @param block
	 *           <code>true</code> to make the method call block untill
	 *           the process completes, <code>false</code> to return
	 *           immediately
	 * @param listener
	 *           Will be supplied with whatever is produced on the
	 *           process's out and err streams. Supply
	 *           <code>null</code> to simply dump to sysout
	 * @param directory
	 *           The working directory for the command
	 * @param command
	 *           The command array. Every element of the command should
	 *           be in its own string, e.g.: to execute
	 *           "rm -r foo.txt", pass the strings "rm", "-r" and
	 *           "foo.txt"
	 * @throws IOException
	 *            if something is wrong with the command
	 */
	public static void execute( boolean block, Listener listener, File directory, String... command )
			throws IOException
	{
		ProcessBuilder pb = new ProcessBuilder( command );
		pb.directory( directory );
		pb.redirectErrorStream( true );
		final Process p = pb.start();
		boolean done = false;

		final Listener l = listener != null ? listener : dumper;

		Thread outHandler = new Thread( "Output handler for " + command[ 0 ] ) {
			@Override
			public void run()
			{
				BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
				try
				{
					String line = null;
					do
					{

						line = br.readLine();
						l.line( line );
					}
					while( line != null );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		};

		outHandler.start();

		if( block )
		{
			while( !done )
			{
				try
				{
					p.waitFor();
					done = true;
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			try
			{
				outHandler.join();
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * For things interested in the output of a process
	 * 
	 * @author ryanm
	 */
	public static interface Listener
	{
		/**
		 * A line has been output
		 * 
		 * @param line
		 *           the output line, or null when the process has
		 *           finished
		 */
		public void line( String line );
	}

	private static Listener dumper = new Listener() {
		@Override
		public void line( String line )
		{
			System.out.println( line );
		}
	};
}