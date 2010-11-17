
package com.ryanm.sage.handlers;

import java.io.IOException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.ProcessUtil;
import com.ryanm.sage.SheevaSage;

/**
 * Restarts the plug
 * 
 * @author ryanm
 */
public class Restarter extends Handler
{
	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( "restart".equals( m.getBody().toLowerCase() ) )
		{
			try
			{
				ProcessUtil.execute( false, null, null, "shutdown", "-r", "now" );
				SheevaSage.reply( m, "I'm going away now, but I'll be back soon", connection );
			}
			catch( IOException e )
			{
				SheevaSage.reply( m, "wat", connection );
				e.printStackTrace();
			}

			return true;
		}
		return false;
	}
}
