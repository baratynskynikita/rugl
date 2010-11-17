
package com.ryanm.ubiety;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

/**
 * @author ryanm
 */
public class Friend
{
	private final Roster r;

	private final RosterEntry re;

	Friend( Roster r, RosterEntry re )
	{
		assert r != null;
		assert re != null;

		this.r = r;
		this.re = re;
	}

	/**
	 * Gets the google ID of this {@link Friend}
	 * 
	 * @return The googleID
	 */
	public String googleID()
	{
		return re.getUser();
	}

	/**
	 * Determines if the {@link Friend} is online or not
	 * 
	 * @return <code>true</code> if online, <code>false</code>
	 *         otherwise
	 */
	public boolean isOnline()
	{
		return r.getPresence( re.getUser() ).isAvailable();
	}

	/**
	 * Determines the subscription status of the friend
	 * 
	 * @return <code>true</code> if our friend request is still pending
	 */
	public boolean isPending()
	{
		return re.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING;
	}

	/**
	 * Gets the readable name of this {@link Friend}
	 * 
	 * @return The readable name, or <code>null</code> if offline or if
	 *         the {@link Friend} is not logged in with {@link Ubiety}
	 */
	public String name()
	{
		Presence p = r.getPresence( re.getUser() );

		if( p.getType() != Presence.Type.unavailable )
		{
			return ( String ) p.getProperty( Ubiety.NAME_PROP );
		}

		return null;
	}

	/**
	 * Gets the name of the game that this {@link Friend} is playing
	 * 
	 * @return the game name, or <code>null</code> if offline or if the
	 *         Friend is not logged in with {@link Ubiety}
	 */
	public String game()
	{
		Presence p = r.getPresence( re.getUser() );

		if( p.getType() != Presence.Type.unavailable )
		{
			return ( String ) p.getProperty( Ubiety.GAMENAME_PROP );
		}

		return null;
	}

	/**
	 * Gets the status string of this {@link Friend}
	 * 
	 * @return the status string, or <code>null</code> if offline or if
	 *         the Friend is not logged in with {@link Ubiety}
	 */
	public String status()
	{
		Presence p = r.getPresence( re.getUser() );

		if( p.getType() != Presence.Type.unavailable )
		{
			return ( String ) p.getProperty( Ubiety.STATUS_PROP );
		}

		return null;
	}

	/**
	 * Gets a property of the {@link Friend}'s presence
	 * 
	 * @param name
	 * @return the named property, or <code>null</code>
	 */
	public Object getProperty( String name )
	{
		Presence p = r.getPresence( re.getUser() );

		if( p.getType() != Presence.Type.unavailable )
		{
			return p.getProperty( name );
		}

		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( googleID() );
		Presence p = r.getPresence( re.getUser() );
		sb.append( isOnline() ? " online " : " offline " );
		if( isPending() )
		{
			sb.append( "pending " );
		}

		sb.append( "groups : " );
		for( RosterGroup rg : re.getGroups() )
		{
			sb.append( rg.getName() );
		}

		for( String pn : p.getPropertyNames() )
		{
			sb.append( "\n\t" ).append( pn );
			sb.append( " = " ).append( p.getProperty( pn ) );
		}

		return sb.toString();
	}

	/**
	 * A handy way to define which {@link Friend}s you want to see
	 * 
	 * @author ryanm
	 */
	public interface Filter
	{
		/**
		 * @param f
		 * @return <code>true</code> to accept
		 */
		public boolean accept( Friend f );
	}
}
