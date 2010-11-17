
package com.ryanm.sage.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ryanm.sage.handlers.URLGrabber.ContentGrabber;

/**
 * @author ryanm
 */
public class TorrentGrabber extends ContentGrabber
{
	/**
	 * 
	 */
	public TorrentGrabber()
	{
		super( "application/x-bittorrent" );
	}

	@Override
	public void handle( URLConnection con ) throws IOException
	{
		URL rpc =
				new URL( "http://127.0.0.1:9091/transmission/rpc?method=torrent-add&filename="
						+ con.getURL().toString() );

		BufferedReader br = new BufferedReader( new InputStreamReader( rpc.openStream() ) );
		String line = null;
		do
		{
			line = br.readLine();
			System.out.println( line );
		}
		while( line != null );
	}

	@Override
	public String getStatus()
	{
		return getTorrentStatus();
	}

	private static String getTorrentStatus()
	{
		StringBuffer buff = new StringBuffer();
		try
		{
			JSONArray fields =
					new JSONArray( new String[] { "name", "percentDone", "eta", "rateDownload" } );
			JSONObject args = new JSONObject();
			args.put( "fields", fields );
			JSONObject json = new JSONObject();
			json.put( "method", "torrent-get" );
			json.put( "arguments", args );

			String request = json.toString();

			URL rpc = new URL( "http://127.0.0.1:9091/transmission/rpc" );
			URLConnection urlc = rpc.openConnection();
			urlc.setDoOutput( true );
			OutputStreamWriter out = new OutputStreamWriter( urlc.getOutputStream() );
			out.write( request );
			out.close();

			BufferedReader br = new BufferedReader( new InputStreamReader( urlc.getInputStream() ) );
			StringBuilder responce = new StringBuilder();
			String line = null;
			while( ( line = br.readLine() ) != null )
			{
				responce.append( line );
			}
			br.close();

			// have to be careful here, might not get everything we asked
			// for
			JSONObject resp = new JSONObject( responce.toString() );
			JSONArray torrents =
					( JSONArray ) ( ( JSONObject ) resp.get( "arguments" ) ).get( "torrents" );
			for( int i = 0; i < torrents.length(); i++ )
			{
				JSONObject t = ( JSONObject ) torrents.get( i );

				String timeLeft = "?";
				if( t.has( "eta" ) )
				{
					int seconds = Integer.parseInt( t.getString( "eta" ) );
					int minutes = seconds / 60;
					int hours = minutes / 60;
					minutes %= 60;
					timeLeft = hours + ":" + ( minutes < 10 ? "0" : "" ) + minutes;
				}

				String rate = "?";
				if( t.has( "rateDownload" ) )
				{
					int bps = Integer.parseInt( t.getString( "rateDownload" ) );
					float kbps = bps / 1024;
					rate = String.valueOf( kbps );
				}

				String done = "?";
				if( t.has( "percentDone" ) )
				{
					done = t.getString( "percentDone" );
				}

				buff.append( t.getString( "name" ) + " : " + done + "% " + rate + "kBps, Time left: "
						+ timeLeft + "\n" );
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return buff.toString();
	}
}
