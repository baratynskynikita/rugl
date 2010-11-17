
package com.ryanm.sage.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import com.ryanm.sage.Handler;
import com.ryanm.sage.SheevaSage;

/**
 * Tells someone something
 * 
 * @author ryanm
 */
public class Teller extends Handler
{
	private Map<String, List<String>> messages = new TreeMap<String, List<String>>();

	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		// do tells
		String from = StringUtils.parseBareAddress( m.getFrom() );
		online( from, connection );

		// check for new tells
		if( m.getBody().toLowerCase().startsWith( "tell " ) )
		{
			String responce = "I have no idea who you're talking about";

			try
			{
				int firstspace = m.getBody().indexOf( " " ) + 1;
				int secondspace = m.getBody().indexOf( " ", firstspace );

				String who = m.getBody().substring( firstspace, secondspace ).toLowerCase();
				String what = m.getBody().substring( secondspace + 1 );

				Roster r = connection.getRoster();

				for( RosterEntry re : r.getEntries() )
				{
					String name = re.getUser().toLowerCase();

					if( name.contains( who ) )
					{
						if( r.getPresence( re.getUser() ).getType() == Presence.Type.available )
						{
							// tell them right now
							Message mess = new Message( re.getUser(), Type.chat );
							mess.setBody( what );
							connection.sendPacket( mess );
						}
						else
						{
							// save it for next time they're on
							tellLater( re.getUser(), what );
						}

						responce = "Will do";
					}
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();

				responce = "tell who what? Speak english!";
			}

			SheevaSage.reply( m, responce, connection );

			return true;
		}

		return false;
	}

	@Override
	public String status()
	{
		return "Holding " + messages.size() + " messages for people";
	}

	@Override
	public void online( String person, XMPPConnection connection )
	{
		if( messages.containsKey( person ) )
		{
			for( String t : messages.get( person ) )
			{
				Message m = new Message( person, Type.chat );
				m.setBody( t );
				connection.sendPacket( m );
			}

			messages.remove( person );
		}
	}

	private void tellLater( String who, String what )
	{
		if( !messages.containsKey( who ) )
		{
			messages.put( who, new LinkedList<String>() );
		}

		messages.get( who ).add( what );
	}
}
