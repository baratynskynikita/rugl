
package com.ryanm.droid.rugl.util;

/**
 * Where should these go?
 * 
 * @author ryanm
 */
public class Util
{
	/**
	 * Extracts a decimal digit from a number
	 * 
	 * @param value
	 *           The number to extract from
	 * @param place
	 *           The index of the digit to extract: 0 for units, 1 for
	 *           tens, 2 for hundreds, -1 for tenths, and so on
	 * @return The designated digit
	 */
	public static int extractDigit( float value, int place )
	{
		float pow = ( float ) Math.pow( 10, place );
		float powp = pow * ( place < 0 ? -10 : 10 );

		value %= powp;

		return ( int ) ( value / pow );
	}
}
