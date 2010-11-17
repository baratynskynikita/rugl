
package com.ryanm.trace.game.ai;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.rugl.console.Console;

/**
 * @author ryanm
 */
public class BotManager
{
	private static final Map<String, Class<? extends Bot>> bots =
			new HashMap<String, Class<? extends Bot>>();

	private static final Map<String, String> descriptions = new HashMap<String, String>();

	static
	{
		register( MeatBag.class.getName() );
		register( WanderBot.class.getName() );
		register( GreedyBot.class.getName() );
		register( HunterBot.class.getName() );
		register( TimidBot.class.getName() );
		register( GapHoundBot.class.getName() );
	}

	/**
	 * Adds a bot class
	 * 
	 * @param botClassName
	 */
	@SuppressWarnings( "unchecked" )
	public static void register( String botClassName )
	{
		try
		{
			Class c = Thread.currentThread().getContextClassLoader().loadClass( botClassName );

			if( Bot.class.isAssignableFrom( c ) )
			{
				Bot instance = ( Bot ) c.newInstance();

				bots.put( instance.getName(), c );
				descriptions.put( instance.getName(), instance.getDescription() );
				Console.log( "loaded bot " + instance.getName() );
			}
			else
			{
				Console.log( c + " is not a bot class" );
			}
		}
		catch( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		catch( InstantiationException e )
		{
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return {@link Bot} names
	 */
	public static String[] getNames()
	{
		String[] names = bots.keySet().toArray( new String[ bots.keySet().size() ] );
		Arrays.sort( names );

		// put meatbag at the start
		for( int i = 0; i < names.length; i++ )
		{
			if( names[ i ] == MeatBag.NAME )
			{
				names[ i ] = names[ 0 ];
				names[ 0 ] = MeatBag.NAME;
			}
		}

		return names;
	}

	/**
	 * @param name
	 * @return a named {@link Bot} instance
	 */
	public static Bot forName( String name )
	{
		try
		{
			Class<? extends Bot> c = bots.get( name );
			if( c != null )
			{
				return c.newInstance();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @param name
	 * @return a description for that bot name
	 */
	public static String getDescription( String name )
	{
		return descriptions.get( name );
	}
}
