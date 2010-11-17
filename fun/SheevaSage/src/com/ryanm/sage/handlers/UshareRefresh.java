
package com.ryanm.sage.handlers;

import java.io.IOException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.ProcessUtil;
import com.ryanm.sage.SheevaSage;

/**
 * Refreshes uShare
 * 
 * @author ryanm
 */
public class UshareRefresh extends Handler
{
	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( m.getBody().toLowerCase().startsWith( "ushare" ) )
		{
			try
			{
				refresh();
				SheevaSage.reply( m, "I've restarted ushare", connection );
			}
			catch( IOException e )
			{
				e.printStackTrace();
				SheevaSage.reply( m, "Something went a bit wonky there", connection );
			}

			return true;
		}
		return false;
	}

	/**
	 * Kills and restarts uShare - rpc call doesn't seem to work
	 * reliably
	 * 
	 * @throws IOException
	 */
	public static void refresh() throws IOException
	{
		ProcessUtil.execute( true, null, null, "killall", "ushare" );

		SpinDisk.spinDisks();

		ProcessUtil.execute( true, null, null, "ushare", "-t", "-x", "-D", "-f", "/etc/ushare.conf" );
	}
}
