
package com.ryanm.sage.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.ProcessUtil;
import com.ryanm.sage.SheevaSage;

/**
 * Makes sure that the hard disk is spun up
 * 
 * @author ryanm
 */
public class SpinDisk extends Handler
{
	private static File file = new File( System.getProperty( "sheevasage.spindiskfile" ) );

	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( m.getBody().toLowerCase().startsWith( "wake" ) )
		{
			SheevaSage.reply( m, "Spinning the disk up", connection );

			try
			{
				spinDisks();

				SheevaSage.reply( m, "Done", connection );
			}
			catch( IOException e )
			{
				e.printStackTrace();

				SheevaSage.reply( m, "Well that didn't go very well", connection );
			}
			return true;
		}

		return false;
	}

	/**
	 * Ensures that the disk is spinning
	 * 
	 * @throws IOException
	 */
	public static void spinDisks() throws IOException
	{
		try
		{
			FileWriter fw = new FileWriter( file );
			fw.write( "The disk was spun up at" );
			fw.write( new Date().toString() );
			fw.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		try
		{
			ProcessUtil.execute( true, null, null, "sync" );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

}
