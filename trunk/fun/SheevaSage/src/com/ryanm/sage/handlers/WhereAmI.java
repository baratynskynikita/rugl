
package com.ryanm.sage.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedList;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.SheevaSage;

/**
 * Reports the local IP address
 * 
 * @author ryanm
 */
public class WhereAmI extends Handler
{
	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( m.getBody().toLowerCase().equals( "where" ) )
		{
			LinkedList<InetAddress> addrList = getNetworkAddresses();

			StringBuilder buff = new StringBuilder( "Locally, I'm at " );

			String addr = "";

			while( !addrList.isEmpty() )
			{
				addr = addrList.removeFirst().getHostAddress();
				buff.append( addr ).append( addrList.isEmpty() ? "" : "\nor " );
			}

			SheevaSage.reply( m, buff.toString(), connection );

			String wa = getWorldAddress();

			if( wa != null )
			{
				SheevaSage.reply( m, "Globally, I'm at " + wa, connection );
			}

			return true;
		}

		return false;
	}

	private String getWorldAddress()
	{
		try
		{
			URLConnection uc = new URL( "http://www.whatismyip.org/" ).openConnection();
			BufferedReader br = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
			return br.readLine();
		}
		catch( MalformedURLException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	private LinkedList<InetAddress> getNetworkAddresses()
	{
		LinkedList<InetAddress> addrList = new LinkedList<InetAddress>();

		try
		{
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

			while( nis.hasMoreElements() )
			{
				NetworkInterface ni = nis.nextElement();

				if( !ni.isLoopback() )
				{
					Enumeration<InetAddress> addresses = ni.getInetAddresses();

					while( addresses.hasMoreElements() )
					{
						InetAddress addr = addresses.nextElement();
						if( !addr.isLoopbackAddress() && addr instanceof Inet4Address )
						{
							addrList.add( addr );
						}
					}
				}
			}
		}
		catch( SocketException e )
		{
			e.printStackTrace();
		}

		return addrList;
	}
}
