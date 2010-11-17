
package com.ryanm.sage.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.SheevaSage;

/**
 * Grabs a file from a url, and then does something with it
 * 
 * @author ryanm
 */
public class URLGrabber extends Handler
{
	/**
	 * Add your custom ContentGrabber implementations to this array
	 */
	private ContentGrabber[] grabbers = new ContentGrabber[] { new TorrentGrabber() };

	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( m.getBody().toLowerCase().startsWith( "http://" ) )
		{
			try
			{
				URL url = new URL( m.getBody() );

				URLConnection urlconn = url.openConnection();
				urlconn.connect();
				String type = urlconn.getContentType();

				boolean found = false;
				for( ContentGrabber h : grabbers )
				{
					if( type.equals( h.contentType ) )
					{
						found = true;
						SheevaSage.reply( m, "Give me a moment here...", connection );
						h.handle( urlconn );
						SheevaSage.reply( m, "Done", connection );
						break;
					}
				}

				if( !found )
				{
					SheevaSage.reply( m, "I have no idea what to do with that", connection );
				}
			}
			catch( MalformedURLException e )
			{
				SheevaSage.reply( m, "That is one ass-ugly URL. What in the name of "
						+ "shuddering fuck do you expect me to do with it?", connection );

				e.printStackTrace();
			}
			catch( IOException e )
			{
				SheevaSage.reply( m, "Something's fucked up with that URL. You sure it works?",
						connection );

				e.printStackTrace();
			}

			return true;
		}

		return false;
	}

	@Override
	public String status()
	{
		StringBuffer buff = new StringBuffer();
		for( ContentGrabber cg : grabbers )
		{
			String s = cg.getStatus();
			if( s != null )
			{
				buff.append( s );
				buff.append( "\n" );
			}
		}
		if( buff.length() > 0 )
		{
			buff.insert( 0, "Torrents:\n" );
			return buff.toString();
		}

		return null;
	}

	/**
	 * Extend to handle a particular content type
	 * 
	 * @author ryanm
	 */
	public static abstract class ContentGrabber
	{
		/**
		 * The type of content that can be handled
		 */
		public final String contentType;

		/**
		 * @param ct
		 */
		protected ContentGrabber( String ct )
		{
			contentType = ct;
		}

		/**
		 * @param con
		 * @throws IOException
		 */
		public abstract void handle( URLConnection con ) throws IOException;

		/**
		 * Override this to return interesting status
		 * 
		 * @return A status string
		 */
		public String getStatus()
		{
			return null;
		}
	}
}
