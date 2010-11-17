/*
 * Created on 07-Sep-2005 by Ryan McNally
 */

package com.ryanm.util.math;

/**
 * Contains utility methods to express a number at varying levels of
 * precision
 * 
 * @author ryanm
 */
public class FloatRep
{
	/**
	 * Returns the value of input as if it were expressed with the
	 * given precision
	 * 
	 * @param input
	 *           The input value
	 * @param bits
	 *           The number of bits to use to express the input value
	 * @return Input value, at the specified precision
	 */
	public static double express( double input, int bits )
	{
		assert bits > 0;

		if( bits >= 64 )
		{
			return input;
		}

		// work out how many bits are needed for the integer part
		int intPart = ( int ) input;

		int intBits = ( intPart == 0 ? -1 : ( int ) ( Math.log( intPart ) / Math.log( 2 ) ) ) + 1;

		if( intBits >= bits )
		{
			return Math.pow( 2, intBits - 1 );
		}

		double floatPart = input - intPart;

		assert floatPart > -1 && floatPart < 1 : floatPart;

		double div = Math.pow( 0.5, bits - intBits - 1 );

		int d = ( int ) Math.round( floatPart / div );

		floatPart = d * div;

		return intPart + floatPart;
	}

	/**
	 * Rounds a value to the specified number of decimal places
	 * 
	 * @param f
	 * @param places
	 * @return a rounded value
	 */
	public static float round( float f, int places )
	{
		float fracs = f % 1;

		float m = ( float ) Math.pow( 10, places );

		fracs *= m;

		fracs = Math.round( fracs );

		fracs /= m;

		return ( int ) f + fracs;
	}

}
