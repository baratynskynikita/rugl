
package com.ryanm.util.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author ryanm
 */
public class NetUtils
{
	/**
	 * Sends a string to an address
	 * 
	 * @param args
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void main( String[] args ) throws NumberFormatException, UnknownHostException,
			IOException
	{
		if( args.length < 4 )
		{
			System.out.println( "Arguments: <tcp|udp> <ip> <port> <message>" );
			System.exit( 0 );
		}

		String protocol = args[ 0 ];
		String address = args[ 1 ];
		int port = Integer.parseInt( args[ 2 ] );
		StringBuilder sb = new StringBuilder();
		for( int i = 3; i < args.length; i++ )
		{
			sb.append( args[ i ] ).append( " " );
		}
		String message = sb.toString();

		if( protocol.equals( "tcp" ) )
		{
			System.out.println( "sending via TCP" );
			Socket sock = new Socket( address, port );
			DataOutputStream dos = new DataOutputStream( sock.getOutputStream() );
			dos.writeUTF( message );
			dos.close();
			System.out.println( "done" );
		}
		else if( protocol.equals( "udp" ) )
		{
			System.out.println( "sending via UDP" );
			SocketAddress sa = new InetSocketAddress( address, port );
			DatagramSocket dgs = new DatagramSocket();
			byte[] data = message.getBytes();
			DatagramPacket dgp = new DatagramPacket( data, data.length, sa );
			dgs.send( dgp );
			dgs.close();
			System.out.println( "done" );
		}
	}

	/**
	 * Finds a local, non-loopback, IPv4 address
	 * 
	 * @return The first non-loopback IPv4 address found, or
	 *         <code>null</code> if no such addresses found
	 * @throws SocketException
	 *            If there was a problem querying the network
	 *            interfaces
	 */
	public static InetAddress getLocalAddress() throws SocketException
	{
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while( ifaces.hasMoreElements() )
		{
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();

			while( addresses.hasMoreElements() )
			{
				InetAddress addr = addresses.nextElement();
				if( addr instanceof Inet4Address && !addr.isLoopbackAddress() )
				{
					return addr;
				}
			}
		}

		return null;
	}

	/**
	 * Finds this computer's global IP address
	 * 
	 * @return The global IP address, or null if a problem occurred
	 */
	public static Inet4Address getGlobalAddress()
	{
		try
		{
			URLConnection uc = new URL( "http://www.whatismyip.org/" ).openConnection();
			BufferedReader br = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
			return ( Inet4Address ) InetAddress.getByName( br.readLine() );
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

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return the encoded ip
	 */
	public static int encodeIP( int a, int b, int c, int d )
	{
		int ip = 0;
		ip |= a << 24;
		ip |= b << 16;
		ip |= c << 8;
		ip |= d;

		return ip;
	}

	/**
	 * Encodes an IP as an int
	 * 
	 * @param ip
	 * @return the encoded IP
	 */
	public static int encode( Inet4Address ip )
	{
		int i = 0;

		byte[] b = ip.getAddress();

		i |= b[ 0 ] << 24;
		i |= b[ 1 ] << 16;
		i |= b[ 2 ] << 8;
		i |= b[ 3 ] << 0;

		return i;
	}
}
