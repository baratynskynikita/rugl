
package com.ryanm.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.rugl.GameBox;
import com.rugl.util.Colour;
import com.ryanm.trace.game.ai.Bot;
import com.ryanm.trace.game.ai.BotManager;
import com.ryanm.trace.game.ai.MeatBag;

/**
 * Encapsulates player details
 * 
 * @author ryanm
 */
public class Player
{
	/**
	 * Player name
	 */
	public final String name;

	/**
	 * Left keycode
	 */
	public int leftKey = -1;

	/**
	 * Right keycode
	 */
	public int rightKey = -1;

	/**
	 * Trace {@link Colour}
	 */
	public int colour = Colour.white;

	/**
	 * The score
	 */
	public int score = 0;

	/**
	 * Life status
	 */
	public boolean dead = false;

	/**
	 * Controls steering
	 */
	public Bot bot = null;

	/**
	 * @param name
	 */
	public Player( String name )
	{
		this.name = name.toLowerCase();
	}

	/**
	 * @param s
	 * @return A player built from the string
	 */
	public static Player fromString( String s )
	{
		Player p = new Player( s.substring( 0, 3 ) );
		s = s.substring( 3 );
		String[] sa = s.split( ":" );
		p.leftKey = Integer.parseInt( sa[ 0 ] );
		p.rightKey = Integer.parseInt( sa[ 1 ] );
		p.colour = Integer.parseInt( sa[ 2 ] );

		p.bot = sa.length < 6 ? null : BotManager.forName( sa[ 5 ] );

		if( p.bot == null )
		{
			p.bot = new MeatBag();
		}

		return p;
	}

	@Override
	public String toString()
	{
		return name + leftKey + ":" + rightKey + ":" + colour + ":" + bot.getName();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof Player )
		{
			Player p = ( Player ) obj;
			return p.name.equals( name );
		}

		return false;
	}

	/**
	 * Loads players from a file into the list
	 * 
	 * @param filename
	 * @param list
	 */
	public static void load( String filename, List<Player> list )
	{
		if( GameBox.secureEnvironment )
		{
			return;
		}

		try
		{
			BufferedReader br =
					new BufferedReader(
							new FileReader( new File( GameBox.filebase, filename ) ) );
			String line = br.readLine();
			while( line != null )
			{
				Player p = Player.fromString( line );
				if( p != null )
				{
					list.add( p );
				}
				line = br.readLine();
			}
			br.close();
		}
		catch( FileNotFoundException fnfe )
		{

		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * Saves a list of players to a file
	 * 
	 * @param filename
	 * @param list
	 */
	public static void save( String filename, List<Player> list )
	{
		if( GameBox.secureEnvironment )
		{
			return;
		}

		try
		{
			FileWriter fr = new FileWriter( new File( GameBox.filebase, filename ) );
			for( Player p : list )
			{
				fr.write( p.toString() );
				fr.write( "\n" );
			}
			fr.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

}