
package com.ryanm.util;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * I can't think of anywhere else to put them
 * 
 * @author ryanm
 */
public class Util
{
	/**
	 * Deep-copies the values from one object to the other
	 * 
	 * @param <T>
	 * @param from
	 *           the source of the copied data
	 * @param to
	 *           The destination of the copied data
	 */
	public static <T> void copyFields( T from, T to )
	{
		for( Field f : from.getClass().getFields() )
		{
			try
			{
				if( isPrimitivish( f.getType() ) )
				{
					f.set( to, f.get( from ) );
				}
				else
				{
					copyFields( f.get( from ), f.get( to ) );
				}
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}
	}

	private static boolean isPrimitivish( Class c )
	{
		return c.isPrimitive() || c == String.class || c == Boolean.class
				|| c == Byte.class || c == Short.class || c == Character.class
				|| c == Integer.class || c == Float.class || c == Double.class
				|| c == Long.class;
	}

	/**
	 * Shuffles the array
	 * 
	 * @param array
	 * @param rng
	 */
	public static void shuffle( int[] array, Random rng )
	{
		for( int i = array.length - 1; i >= 0; i-- )
		{
			int index = rng.nextInt( i + 1 );
			int a = array[ index ];
			array[ index ] = array[ i ];
			array[ i ] = a;
		}
	}

	/**
	 * Shuffles the array
	 * 
	 * @param array
	 * @param rng
	 */
	public static void shuffle( Object[] array, Random rng )
	{
		for( int i = array.length - 1; i >= 0; i-- )
		{
			int index = rng.nextInt( i + 1 );
			Object a = array[ index ];
			array[ index ] = array[ i ];
			array[ i ] = a;
		}
	}
}
