
package com.ryanm.ubiety;

/**
 * Exercises the {@link Ubiety} system
 * 
 * @author ryanm
 */
public class Test
{
	/**
	 * Tests {@link Ubiety}. Call with [id pass duration] to spawn an
	 * instance that will accept all friend requests and print all
	 * received messages for the specified duration, and then clear its
	 * friend roster. Call with [id pass targetID duration] to spawn an
	 * instance that will accept no friend requests, pester the target
	 * with messages for the specified duration, and then clear its
	 * friend roster
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		if( args.length == 3 )
		{
			listeny( args[ 0 ], args[ 1 ], 1000 * Float.parseFloat( args[ 2 ] ) );
		}
		else if( args.length == 4 )
		{
			talky( args[ 0 ], args[ 1 ], args[ 2 ], 1000 * Float.parseFloat( args[ 3 ] ) );
		}
		else
		{
			System.out.println( "usage:" );
			System.out.println( "\t\"java -jar Ubiety.jar id pass duration\" for listening instance" );
			System.out
					.println( "\t\"java -jar Ubiety.jar id pass targetid duration\" for talking instance" );
		}
	}

	private static void listeny( String user, String pass, float duration )
	{
		System.out.println( "starting listening instance \"" + user + "\" \"" + pass + "\"" );

		Ubiety u = new Ubiety( "testy" ) {
			@Override
			public void handleMessage( Friend f, String message )
			{
				System.out.println( f + "\n\tsays " + message );
			}

			@Override
			public void handleFriendRequest( String googleID, String name, String message )
			{
				// be friendly
				System.out.println( "friend request from " + googleID + ", " + name + ", " + message );

				respondToFriendRequest( googleID, name, true );
			}
		};

		u.setName( "max listeny" );
		u.setStatus( "listening intently" );
		u.setProperty( "grunka", "lunka" );
		boolean b = u.connect( user, pass );

		if( b )
		{
			System.out.println( "connected" );

			long t = System.currentTimeMillis();
			while( System.currentTimeMillis() - t < duration )
			{
				try
				{
					Thread.sleep( 1000 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}

			Friend[] fa = u.getFriends( null );
			for( Friend f : fa )
			{
				System.out.println( "unfriending" );
				System.out.println( "\t" + f );
				u.removeFriend( f );
			}

			System.out.println( "I've got " + u.getFriends( null ).length + " friends" );

			u.logout();

			System.out.println( "disconnected" );
		}
		else
		{
			System.out.println( "failed to connect" );
		}
	}

	private static void talky( String user, String pass, String target, float duration )
	{
		System.out.println( "starting talking instance \"" + user + "\" \"" + pass + "\" \"" + target
				+ "\"" );

		Ubiety u = new Ubiety( "testy" ) {

			@Override
			public void handleMessage( Friend f, String message )
			{
				System.out.println( f + "\n\tsays " + message );
			}

			@Override
			public void handleFriendRequest( String googleID, String name, String message )
			{
				System.out.println( "friend request from " + googleID + ", " + name + ", " + message );

				respondToFriendRequest( googleID, name, false );
			}
		};

		u.setName( "talky mctalktalk" );
		u.setStatus( "talking incessantly" );
		u.setProperty( "flargle", "flurp" );
		boolean b = u.connect( user, pass );

		if( b )
		{
			System.out.println( "connected" );

			System.out.println( "requesting friend" );
			u.addFriend( target, "Can I talk at you?" );

			long t = System.currentTimeMillis();
			while( System.currentTimeMillis() - t < duration )
			{
				try
				{
					Thread.sleep( 5000 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}

				Friend[] fa = u.getFriends( null );
				System.out.println( "saying hello to " + fa.length );
				for( Friend f : fa )
				{
					System.out.println( f );
				}
				u.send( "hello", fa );
			}

			Friend[] fa = u.getFriends( null );
			for( Friend f : fa )
			{
				System.out.println( "unfriending" );
				System.out.println( "\t" + f );
				u.removeFriend( f );
			}

			System.out.println( "I've got " + u.getFriends( null ).length + " friends" );

			u.logout();

			System.out.println( "disconnected" );
		}
		else
		{
			System.out.println( "failed to connect" );
		}
	}
}
