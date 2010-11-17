
package com.ryanm.google.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author ryanm
 */
public class MetaServlet extends HttpServlet
{

	/***/
	private static final String listCacheKey = "iplist";

	/**
	 * Inserts/updates
	 */
	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp )
			throws ServletException, IOException
	{
		try
		{
			String ip = req.getHeader( GameRecord.GAME_IP_HEADER );
			String port = req.getHeader( GameRecord.GAME_PORT_HEADER );
			String mess = req.getHeader( GameRecord.GAME_MESSAGE_HEADER );
			String lat = req.getHeader( GameRecord.GAME_LATITUDE_HEADER );
			String lon = req.getHeader( GameRecord.GAME_LONGITUDE_HEADER );

			GameRecord g = new GameRecord( ip, port, mess, lat, lon );

			MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

			String ck = g.getCacheKey();

			if( !cache.contains( ck ) )
			{
				// need to add to the ipList
				String s = ( String ) cache.get( listCacheKey );
				cache.put( listCacheKey, s == null ? ck : s + " " + ck );
			}

			cache.put( ck, g.getCacheString(), Expiration.byDeltaSeconds( 300 ) );

			resp.setStatus( HttpServletResponse.SC_OK );
		}
		catch( NumberFormatException nfe )
		{
			nfe.printStackTrace();
			resp.setStatus( HttpServletResponse.SC_BAD_REQUEST );
		}
		catch( IllegalArgumentException iae )
		{
			iae.printStackTrace();
			resp.setStatus( HttpServletResponse.SC_BAD_REQUEST );
		}
	}

	/**
	 * Removes
	 */
	@Override
	protected void doDelete( HttpServletRequest req, HttpServletResponse resp )
			throws ServletException, IOException
	{
		String ip = req.getHeader( GameRecord.GAME_IP_HEADER );

		MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
		cache.delete( ip );

		resp.setStatus( HttpServletResponse.SC_OK );
	}

	/**
	 * Queries
	 */
	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp )
			throws IOException
	{
		try
		{
			String latString = req.getHeader( GameRecord.GAME_LATITUDE_HEADER );
			String lonString = req.getHeader( GameRecord.GAME_LONGITUDE_HEADER );
			String countString = req.getHeader( "X-GameQueryCount" );

			final float lat = latString == null ? 0 : Float.parseFloat( latString );
			final float lon = lonString == null ? 0 : Float.parseFloat( lonString );
			int count = countString == null ? 100 : Integer.parseInt( countString );

			MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

			List<GameRecord> games = new ArrayList<GameRecord>();
			String gameList = ( String ) cache.get( listCacheKey );
			if( gameList != null )
			{
				String[] gla = gameList.split( " " );
				Collection<Object> c = new ArrayList<Object>();
				for( int i = 0; i < gla.length; i++ )
				{
					c.add( gla[ i ] );
				}

				Map<Object, Object> gm = cache.getAll( c );

				// build games
				for( Map.Entry<Object, Object> entry : gm.entrySet() )
				{
					games.add( new GameRecord( ( String ) entry.getKey(), ( String ) entry
							.getValue() ) );
				}

				if( games.size() < gla.length )
				{
					// some stale keys, refresh the list
					StringBuilder sb = new StringBuilder();
					for( int i = 0; i < games.size(); i++ )
					{
						sb.append( games.get( i ).getCacheKey() );
						sb.append( " " );
					}

					cache.put( listCacheKey, sb.toString() );
				}

				// sort by distance
				Collections.sort( games, new Comparator<GameRecord>() {
					public int compare( GameRecord o1, GameRecord o2 )
					{
						float d = o1.distance( lat, lon ) - o2.distance( lat, lon );

						return ( int ) Math.signum( d );
					}
				} );

				// write out the first however many
				resp.setStatus( HttpServletResponse.SC_OK );
				resp.setContentType( "text/plain" );
				PrintWriter w = resp.getWriter();
				w.println( "# games" );
				for( int i = 0; i < games.size() && i < count; i++ )
				{
					w.println( games.get( i ).encode() );
				}
			}
			else
			{
				resp.setStatus( HttpServletResponse.SC_NO_CONTENT );
			}
		}
		catch( NumberFormatException nfe )
		{
			nfe.printStackTrace();
			resp.setStatus( HttpServletResponse.SC_BAD_REQUEST );
		}
	}
}
