
package com.ryanm.sage.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import com.ryanm.sage.Handler;
import com.ryanm.sage.SheevaSage;

/**
 * Scrapes a page of random quotations, and dishes them out one at a
 * time
 * 
 * @author ryanm
 */
public class Quoter extends Handler
{
	private List<String> quotes = new LinkedList<String>();

	private Thread getter = null;

	@Override
	public boolean handle( Message m, XMPPConnection connection )
	{
		if( quotes.size() < 3 )
		{ // time to get some more quotes
			getQuotes();
		}

		String reply = "I'll have to think about that...";
		if( !quotes.isEmpty() )
		{
			reply = quotes.remove( 0 );
		}

		SheevaSage.reply( m, reply, connection );

		return true;
	}

	private void getQuotes()
	{
		if( getter == null )
		{
			getter = new Thread( "Quote getter" ) {
				@Override
				public void run()
				{
					try
					{
						URL quoteSource = new URL( "http://www.quotationspage.com/random.php3" );
						URLConnection uc = quoteSource.openConnection();
						BufferedReader br =
								new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
						StringBuilder buff = new StringBuilder();

						String line = null;
						do
						{
							line = br.readLine();
							buff.append( line );
						}
						while( line != null );

						br.close();
						String page = buff.toString();

						// extract quotes
						int dtstart = page.indexOf( "<dt class=\"quote\">" );

						while( dtstart > 0 )
						{
							int qstart = page.indexOf( ".html\">", dtstart ) + 7;
							int qend = page.indexOf( "</a>", qstart );

							String quote = new String( page.substring( qstart, qend ) );
							quotes.add( quote );

							dtstart = page.indexOf( "<dt class=\"quote\">", dtstart + 1 );
						}

					}
					catch( MalformedURLException e )
					{
						e.printStackTrace();
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}

					getter = null;
				}
			};
			getter.start();
		}
	}
}
