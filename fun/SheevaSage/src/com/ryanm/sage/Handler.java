
package com.ryanm.sage;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

/**
 * @author ryanm
 */
public abstract class Handler
{
	/**
	 * Handles an incoming message
	 * 
	 * @param m
	 * @param connection
	 *           the means to reply. Check out
	 *           {@link SheevaSage#reply(Message, String, XMPPConnection)}
	 * @return <code>true</code> if this handler can handle the
	 *         message, <code>false</code> otherwise
	 */
	public abstract boolean handle( Message m, XMPPConnection connection );

	/**
	 * Gets a status string if the handler is doing anything
	 * 
	 * @return A status string, or null if you don't have anything
	 *         interesting to say
	 */
	public String status()
	{
		return null;
	}

	/**
	 * Someone has come online
	 * 
	 * @param person
	 *           the person
	 * @param connection
	 *           the connection
	 */
	public void online( String person, XMPPConnection connection )
	{
	}
}
