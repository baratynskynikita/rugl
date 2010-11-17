
package com.ryanm.sage.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.SheevaSage;

/**
 * Starts and stop all torrents with "t start" and "t stop"
 * 
 * @author ryanm
 */
public class TorrentStarter extends Handler
{

	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		String s = null;
		if( m.getBody().startsWith( "t start" ) )
		{
			s = "torrent-start";
		}
		else if( m.getBody().startsWith( "t stop" ) )
		{
			s = "torrent-stop";
		}

		if( s != null )
		{
			boolean success = false;

			try
			{
				URL rpc = new URL( "http://127.0.0.1:9091/transmission/rpc?method=" + s );

				BufferedReader br = new BufferedReader( new InputStreamReader( rpc.openStream() ) );
				String line = null;
				do
				{
					line = br.readLine();
					System.out.println( line );

					if( line != null && line.contains( "success" ) )
					{
						success = true;
					}
				}
				while( line != null );
			}
			catch( IOException ioe )
			{
				ioe.printStackTrace();
			}

			SheevaSage.reply( m, success ? "ok" : "not so much", connection );

			return true;
		}

		return false;
	}
}
