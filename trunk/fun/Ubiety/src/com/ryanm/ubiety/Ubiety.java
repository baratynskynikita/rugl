
package com.ryanm.ubiety;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

/**
 * Uses Google Talk to supply an online presence/chat/friend system
 * 
 * @author ryanm
 */
public abstract class Ubiety
{
	private static final String UBIETY_GROUP = "Ubiety";

	static final String NAME_PROP = "name";

	static final String GAMENAME_PROP = "gamename";

	static final String STATUS_PROP = "activity";

	/**
	 * The name of the current game
	 */
	public final String gameName;

	private String name = "";

	private String status = "";

	private Map<String, Serializable> properties = new HashMap<String, Serializable>();

	private final XMPPConnection connection =
			new XMPPConnection(
					new ConnectionConfiguration( "talk.google.com", 5222, "googlemail.com" ) );

	private PacketFilter pf = new PacketFilter() {
		@Override
		public boolean accept( Packet packet )
		{
			return packet instanceof Presence || packet instanceof Message;
		}
	};

	private PacketListener pl = new PacketListener() {

		@Override
		public void processPacket( Packet p )
		{
			Roster r = connection.getRoster();

			if( p.getError() == null && p instanceof Presence )
			{
				Presence pre = ( Presence ) p;

				if( pre.getType() == Presence.Type.subscribe )
				{ // someone wants to be our friend

					handleFriendRequest( StringUtils.parseBareAddress( p.getFrom() ), ( String ) pre
							.getProperty( NAME_PROP ), ( String ) pre.getProperty( "message" ) );
				}
				else if( pre.getType() == Presence.Type.subscribed )
				{ // our friend request has been accepted
					try
					{
						r.createEntry( StringUtils.parseBareAddress( pre.getFrom() ), ( String ) pre
								.getProperty( NAME_PROP ), new String[] { UBIETY_GROUP } );
					}
					catch( XMPPException e )
					{
						e.printStackTrace();
					}
				}
				else if( pre.getType() == Presence.Type.unsubscribe )
				{ // was it something I said?
					try
					{
						r.removeEntry( r.getEntry( StringUtils.parseBareAddress( pre.getFrom() ) ) );
					}
					catch( XMPPException e )
					{
						e.printStackTrace();
					}
				}
			}
			else if( p instanceof Message )
			{
				Message m = ( Message ) p;

				if( m.getError() != null && m.getError().getCode() == 503 )
				{
					try
					{
						r.removeEntry( r.getEntry( m.getFrom() ) );
					}
					catch( XMPPException e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					RosterEntry re = r.getEntry( StringUtils.parseBareAddress( p.getFrom() ) );

					Friend f = new Friend( r, re );

					// this will be called on some thread inside of smack,
					// which will swallow any exception - let's print this
					// bitch so failure in our code isn't silent
					try
					{
						handleMessage( f, m.getBody() );
					}
					catch( Throwable t )
					{
						t.printStackTrace();
					}
				}
			}
		}

	};

	/**
	 * @param gameName
	 *           the current game. Friends will be able to see what you
	 *           are playing by checking {@link Friend#game()}
	 */
	protected Ubiety( String gameName )
	{
		this.gameName = gameName;
	}

	/**
	 * Connect to the google talk servers
	 * 
	 * @param username
	 *           your google id
	 * @param password
	 *           your google password
	 * @return <code>true</code> if successful
	 */
	public boolean connect( String username, String password )
	{
		try
		{
			connection.connect();
			connection.login( username, password );

			connection.addPacketListener( pl, pf );

			connection.getRoster().createGroup( UBIETY_GROUP );
			connection.getRoster().setSubscriptionMode( SubscriptionMode.manual );

			updatePresence();

			return true;
		}
		catch( XMPPException e )
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Sets your nickname. Friends will be able to see this by calling
	 * {@link Friend#name()}
	 * 
	 * @param name
	 *           a more readable name than your googleID
	 */
	public void setName( String name )
	{
		if( name == null )
		{
			name = "";
		}

		this.name = name;

		updatePresence();
	}

	/**
	 * Sets your current status. Friends will be able to see this by
	 * calling {@link Friend#status()}
	 * 
	 * @param status
	 */
	public void setStatus( String status )
	{
		if( status == null )
		{
			status = "";
		}

		this.status = status;

		updatePresence();
	}

	/**
	 * Adds a custom property to your status. Friends can retrieve it
	 * with {@link Friend#getProperty(String)}
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty( String name, Serializable value )
	{
		if( value != null )
		{
			properties.put( name, value );
		}
		else
		{
			properties.remove( name );
		}

		updatePresence();
	}

	private void updatePresence()
	{
		if( connection.isConnected() )
		{
			Presence p = new Presence( Presence.Type.available );
			p.setProperty( NAME_PROP, name );
			p.setProperty( GAMENAME_PROP, gameName );
			p.setProperty( STATUS_PROP, status );

			for( Map.Entry<String, Serializable> e : properties.entrySet() )
			{
				p.setProperty( e.getKey(), e.getValue() );
			}

			p.setStatus( "Currently \"" + status + "\" in \"" + gameName + "\"" );

			connection.sendPacket( p );
		}
	}

	/**
	 * Disconnects from the google talk servers
	 */
	public void logout()
	{
		connection.disconnect();
	}

	/**
	 * Sends a message to a set of friends
	 * 
	 * @param message
	 * @param recipients
	 */
	public void send( String message, Friend... recipients )
	{
		for( Friend f : recipients )
		{
			Message m = new Message( f.googleID(), Message.Type.chat );
			m.setBody( message );
			connection.sendPacket( m );
		}
	}

	/**
	 * Use this to respond to a friend request
	 * 
	 * @param googleID
	 *           The ID of the request sender
	 * @param name
	 *           The readable name of the sender
	 * @param accept
	 *           <code>true</code> to accept, <code>false</code> to
	 *           deny
	 */
	public void respondToFriendRequest( String googleID, String name, boolean accept )
	{
		if( accept )
		{
			Roster r = connection.getRoster();
			try
			{
				r.createEntry( googleID, name, new String[] { UBIETY_GROUP } );
			}
			catch( XMPPException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			Presence p = new Presence( Presence.Type.unsubscribed );
			p.setTo( googleID );
			connection.sendPacket( p );
		}
	}

	/**
	 * Query your {@link Friend} roster
	 * 
	 * @param filter
	 *           <code>null</code> to accept all
	 * @return an array of {@link Friend}s that pass the filter
	 */
	public Friend[] getFriends( Friend.Filter filter )
	{
		List<Friend> l = new ArrayList<Friend>();

		Roster r = connection.getRoster();
		for( RosterEntry re : r.getEntries() )
		{
			Friend p = new Friend( r, re );

			if( filter == null || filter.accept( p ) )
			{
				l.add( p );
			}
		}

		return l.toArray( new Friend[ l.size() ] );
	}

	/**
	 * Sends a friend request
	 * 
	 * @param googleID
	 *           The ID to send the request to
	 * @param message
	 *           Your message
	 */
	public void addFriend( String googleID, String message )
	{
		Presence p = new Presence( Presence.Type.subscribe );
		p.setTo( googleID );
		p.setProperty( NAME_PROP, name );
		p.setProperty( "message", message );

		connection.sendPacket( p );
	}

	/**
	 * Removes a friend from your roster. Note that you will still
	 * appear on their roster, but you will appear to be permanently
	 * unavailable. If they try to send you a message, an error will be
	 * returned from the server and you will be removed from their
	 * roster automatically. Note that this call will block until we
	 * get a response from the server
	 * 
	 * @param idiotHole
	 *           I hate that guy
	 */
	public void removeFriend( Friend idiotHole )
	{
		Roster r = connection.getRoster();
		RosterEntry re = r.getEntry( idiotHole.googleID() );

		try
		{
			r.removeEntry( re );
		}
		catch( XMPPException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Called when a friend request is received
	 * 
	 * @param googleID
	 *           The id of the request sender
	 * @param reqName
	 *           The name of the request sender
	 * @param message
	 *           A message accompanying the request
	 */
	public abstract void handleFriendRequest( String googleID, String reqName, String message );

	/**
	 * Called when a message is received
	 * 
	 * @param f
	 *           The sender
	 * @param message
	 *           The message
	 */
	public abstract void handleMessage( Friend f, String message );
}
