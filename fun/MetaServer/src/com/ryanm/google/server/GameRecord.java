
package com.ryanm.google.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates info on a game
 * 
 * @author ryanm
 */
public class GameRecord
{
	/***/
	static final String GAME_LONGITUDE_HEADER = "X-GameLongitude";

	/***/
	static final String GAME_LATITUDE_HEADER = "X-GameLatitude";

	/***/
	static final String GAME_MESSAGE_HEADER = "X-GameMessage";

	/***/
	static final String GAME_PORT_HEADER = "X-GamePort";

	/***/
	static final String GAME_IP_HEADER = "X-GameIP";

	private static final DecimalFormat df = new DecimalFormat( "000.00" );

	/***/
	public final int ip;

	/***/
	public final int port;

	/***/
	public final String message;

	/***/
	public final float lat;

	/***/
	public final float lon;

	private float distance = -1;

	/**
	 * @param ip
	 * @param port
	 * @param message
	 * @param lat
	 * @param lon
	 */
	public GameRecord( int ip, int port, String message, float lat, float lon )
	{
		this.ip = ip;
		this.port = port;
		this.message = message;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * @param ip
	 *           in int-encoded format
	 * @param port
	 * @param message
	 * @param lat
	 * @param lon
	 */
	public GameRecord( String ip, String port, String message, String lat, String lon )
	{
		this.ip = Integer.parseInt( ip );
		this.port = Integer.parseInt( port );
		this.message = message;
		this.lat = Float.parseFloat( lat );
		this.lon = Float.parseFloat( lon );
	}

	/**
	 * @param cacheKey
	 * @param cacheString
	 */
	public GameRecord( String cacheKey, String cacheString )
	{
		int i = cacheKey.indexOf( ":" );
		ip = Integer.parseInt( cacheKey.substring( 0, i ) );
		port = Integer.parseInt( cacheKey.substring( i + 1 ) );
		ParsePosition pp = new ParsePosition( 0 );
		lat = df.parse( cacheString, pp ).floatValue();
		lon = df.parse( cacheString, pp ).floatValue();

		message = cacheString.substring( pp.getIndex() );
	}

	/**
	 * @param encoded
	 * @return The decoded game object
	 */
	public static GameRecord decode( String encoded )
	{
		int i = encoded.indexOf( "|" );
		String ck = encoded.substring( 0, i );
		String cs = encoded.substring( i + 1 );
		return new GameRecord( ck, cs );
	}

	/**
	 * Adds this game to the server
	 * 
	 * @param url
	 *           url of the server
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise
	 */
	public boolean add( String url )
	{
		try
		{
			HttpURLConnection request = ( HttpURLConnection ) new URL( url ).openConnection();
			request.setRequestMethod( "POST" );
			request.setRequestProperty( GameRecord.GAME_IP_HEADER, String.valueOf( ip ) );
			request.setRequestProperty( GameRecord.GAME_PORT_HEADER, String.valueOf( port ) );
			request.setRequestProperty( GameRecord.GAME_MESSAGE_HEADER, message );
			request.setRequestProperty( GameRecord.GAME_LATITUDE_HEADER, df.format( lat ) );
			request.setRequestProperty( GameRecord.GAME_LONGITUDE_HEADER, df.format( lon ) );
			request.setRequestProperty( "Content-Length", "0" );
			request.connect();

			if( request.getResponseCode() != HttpURLConnection.HTTP_OK )
			{
				throw new IOException( "Unexpected response: " + request.getResponseCode() + " "
						+ request.getResponseMessage() );
			}

			return true;
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Removes this game from the server
	 * 
	 * @param url
	 *           url of the server
	 * @return <code>true</code> if successful, false otherwise
	 */
	public boolean delete( String url )
	{
		return delete( url, ip, port );
	}

	/**
	 * Deletes a game listing
	 * 
	 * @param url
	 * @param ip
	 * @param port
	 * @return <code>true</code> if successful
	 */
	public static boolean delete( String url, int ip, int port )
	{
		try
		{
			HttpURLConnection request = ( HttpURLConnection ) new URL( url ).openConnection();
			request.setRequestMethod( "DELETE" );
			request.setRequestProperty( GameRecord.GAME_IP_HEADER, String.valueOf( ip ) );
			request.setRequestProperty( GameRecord.GAME_PORT_HEADER, String.valueOf( port ) );
			request.connect();

			return request.getResponseCode() == HttpURLConnection.HTTP_OK;
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets a list of games from the server
	 * 
	 * @param url
	 *           the server's url
	 * @param lat
	 *           the query latitude
	 * @param lon
	 *           the query longitude
	 * @param count
	 *           the max number of games to retrieve
	 * @return A list of closest games, or null if the query failed
	 */
	public static GameRecord[] get( String url, float lat, float lon, int count )
	{
		try
		{
			HttpURLConnection req = ( HttpURLConnection ) new URL( url ).openConnection();
			req.setRequestMethod( "GET" );
			req.setRequestProperty( GameRecord.GAME_LATITUDE_HEADER, df.format( lat ) );
			req.setRequestProperty( GameRecord.GAME_LONGITUDE_HEADER, df.format( lon ) );
			req.setRequestProperty( "X-GameQueryCount", String.valueOf( count ) );
			req.connect();

			if( req.getResponseCode() == HttpURLConnection.HTTP_OK )
			{
				List<GameRecord> gl = new ArrayList<GameRecord>();

				BufferedReader br = new BufferedReader( new InputStreamReader( req.getInputStream() ) );
				String line;
				while( ( line = br.readLine() ) != null )
				{
					if( !line.startsWith( "#" ) )
					{
						gl.add( GameRecord.decode( line ) );
					}
				}

				return gl.toArray( new GameRecord[ gl.size() ] );
			}
			else
			{
				System.out.println( req.getResponseMessage() );
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return encoded for transit
	 */
	public String encode()
	{
		return getCacheKey() + "|" + getCacheString();
	}

	/**
	 * @return the cache key for this game record
	 */
	public String getCacheKey()
	{
		return ip + ":" + port;
	}

	/**
	 * @return string to be cached
	 */
	public String getCacheString()
	{
		return df.format( lat ) + df.format( lon ) + message;
	}

	/**
	 * @param plat
	 * @param plon
	 * @return The distance in kilometers to the point
	 */
	public float distance( float plat, float plon )
	{
		if( distance == -1 )
		{
			double rlat = Math.toRadians( lat );
			double rlon = Math.toRadians( lon );
			double altlat = Math.toRadians( plat );
			double altlon = Math.toRadians( plon );

			double p1 = Math.cos( rlat ) * Math.cos( rlon ) * Math.cos( altlat ) * Math.cos( altlon );
			double p2 = Math.cos( rlat ) * Math.sin( rlon ) * Math.cos( altlat ) * Math.sin( altlon );
			double p3 = Math.sin( rlat ) * Math.sin( altlat );

			distance = ( float ) ( Math.acos( p1 + p2 + p3 ) * 6371 );
		}

		return distance;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		int[] ipa = decodeIP( ip );
		sb.append( ipa[ 0 ] ).append( "." ).append( ipa[ 1 ] ).append( "." );
		sb.append( ipa[ 2 ] ).append( "." ).append( ipa[ 3 ] ).append( ":" );
		sb.append( port ).append( " " ).append( message );
		sb.append( " @ " ).append( lat ).append( " by " ).append( lon );

		return sb.toString();
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
	 * @param ip
	 * @return decoded ip
	 */
	public static int[] decodeIP( int ip )
	{
		int[] r = new int[ 4 ];
		r[ 3 ] = ip & 0xff;
		ip = ip >> 8;
		r[ 2 ] = ip & 0xff;
		ip = ip >> 8;
		r[ 1 ] = ip & 0xff;
		ip = ip >> 8;
		r[ 0 ] = ip & 0xff;

		return r;
	}
}
