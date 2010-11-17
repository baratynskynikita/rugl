
package com.ryanm.util;

/**
 * @author ryanm
 */
public class ArrayUtil
{
	/**
	 * Doubles the size of an array
	 * 
	 * @param in
	 * @return The new array
	 */
	public static String[] grow( String[] in )
	{
		String[] na = new String[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}

	/**
	 * Doubles the size of an array
	 * 
	 * @param in
	 * @return The new array
	 */
	public static long[] grow( long[] in )
	{
		long[] na = new long[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}

	/**
	 * Doubles the size of an array
	 * 
	 * @param in
	 * @return The new array
	 */
	public static int[] grow( int[] in )
	{
		int[] na = new int[ in.length * 2 ];
		System.arraycopy( in, 0, na, 0, in.length );
		return na;
	}
}
