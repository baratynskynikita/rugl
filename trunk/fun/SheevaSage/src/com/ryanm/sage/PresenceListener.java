
package com.ryanm.sage;

import org.jivesoftware.smack.XMPPConnection;

/**
 * @author ryanm
 */
public interface PresenceListener
{
	/**
	 * Someone has come online
	 * 
	 * @param person
	 *           the person
	 * @param connection
	 *           the connection
	 */
	public void online( String person, XMPPConnection connection );
}
