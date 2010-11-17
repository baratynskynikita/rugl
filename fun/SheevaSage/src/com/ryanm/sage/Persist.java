
package com.ryanm.sage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;

/**
 * Attempts to repeatedly launch the sage, logging errors that ended
 * the last 5 sessions. Sessions are launched in a separate process
 * for robustness
 * 
 * @author ryanm
 */
public class Persist
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		int run = 0;
		int limit = 5;

		while( true )
		{
			// find the location of the jar
			URL jarLoc = SheevaSage.class.getProtectionDomain().getCodeSource().getLocation();

			ProcessBuilder pb =
					new ProcessBuilder( "java", "-cp", jarLoc.getPath(), SheevaSage.class.getName(),
							args[ 0 ] );
			try
			{
				System.out.println( "Starting at " + new Date().toString() );
				final Process p = pb.start();

				OutPipe out = new OutPipe( p.getInputStream() );
				ErrPipe err =
						new ErrPipe( run, p.getErrorStream(), new File( jarLoc.getFile() ).getParent() );

				out.start();
				err.start();

				p.waitFor();

				out.shouldStop = true;
				out.interrupt();
				out.join();

				err.shouldStop = true;
				err.interrupt();
				err.join();

				System.out.println( "Process ended at " + new Date().toString() + " with code "
						+ p.exitValue() );
			}
			catch( IOException e )
			{
				// command is wrong
				System.err.println( "Exiting due to" );
				e.printStackTrace();
				System.exit( 1 );
			}
			catch( InterruptedException ie )
			{
				// waitfor or join have been interrupted
				System.err.println( "That shouldn't have happened!" );
				ie.printStackTrace();
				System.exit( 1 );
			}

			try
			{
				Thread.sleep( 10000 );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}

			run++;
			run %= limit;
		}
	}

	private static class OutPipe extends Thread
	{
		public boolean shouldStop = false;

		private final BufferedReader br;

		private OutPipe( InputStream is )
		{
			br = new BufferedReader( new InputStreamReader( is ) );
		}

		@Override
		public void run()
		{
			try
			{
				String line = br.readLine();
				while( line != null && !shouldStop )
				{
					System.out.print( "\t" );
					System.out.println( line );
					line = br.readLine();
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	private static class ErrPipe extends Thread
	{
		public boolean shouldStop = false;

		private final int runNumber;

		private BufferedReader br;

		private final String runDir;

		private ErrPipe( int runNumber, InputStream is, String runDir )
		{
			this.runNumber = runNumber;
			this.runDir = runDir;
			br = new BufferedReader( new InputStreamReader( is ) );
		}

		@Override
		public void run()
		{
			try
			{
				PrintStream ps = new PrintStream( runDir + "/err" + runNumber + ".txt" );
				ps.print( new Date().toString() + "\n\n" );

				String line = br.readLine();
				while( line != null && !shouldStop )
				{
					try
					{
						ps.print( line );
						ps.print( "\n" );
						line = br.readLine();
					}
					catch( IOException e )
					{
						e.printStackTrace();
						line = null;
					}
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
}
