
package com.ryanm.droid.rugl.util;

import java.nio.ByteOrder;

/**
 * Utility for working with natively-ordered integer-packed
 * RGBA-format colours. Nice and easy to squirt to OpenGL. Beware that
 * byte order can change from machine to machine, so if you're saving
 * the packed values to disk, use {@link #toBigEndian(int)} to write
 * and {@link #fromBigEndian(int)} to read
 * 
 * @author ryanm
 */
public class Colour
{
	private static final int redOffset, greenOffset, blueOffset, alphaOffset;
	static
	{
		boolean big = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
		redOffset = big ? 24 : 0;
		greenOffset = big ? 16 : 8;
		blueOffset = big ? 8 : 16;
		alphaOffset = big ? 0 : 24;
	}

	/***/
	public static final int white = packFloat( 1, 1, 1, 1 );

	/***/
	public static final int black = packFloat( 0, 0, 0, 1 );

	/***/
	public static final int grey = packFloat( 0.5f, 0.5f, 0.5f, 1 );

	/***/
	public static final int darkgrey = packFloat( 0.25f, 0.25f, 0.25f, 1 );

	/***/
	public static final int lightgrey = packFloat( 0.75f, 0.75f, 0.75f, 1 );

	/***/
	public static final int red = packFloat( 1, 0, 0, 1 );

	/***/
	public static final int green = packFloat( 0, 1, 0, 1 );

	/***/
	public static final int blue = packFloat( 0, 0, 1, 1 );

	/***/
	public static final int yellow = packFloat( 1, 1, 0, 1 );

	/***/
	public static final int cyan = packFloat( 0, 1, 1, 1 );

	/***/
	public static final int magenta = packFloat( 1, 0, 1, 1 );

	/***/
	public static final int orange = packFloat( 1, 0.5f, 0, 1 );

	/***/
	public static final int springGreen = packFloat( 0.5f, 1, 0, 1 );

	/***/
	public static final int turquoise = packFloat( 0, 1, 0.5f, 1 );

	/***/
	public static final int ocean = packFloat( 0, 0.5f, 1, 1 );

	/***/
	public static final int violet = packFloat( 0.5f, 0, 1, 1 );

	/***/
	public static final int raspberry = packFloat( 1, 0, 0.5f, 1 );

	/**
	 * Converts from a native-order packed colour int to a big-endian
	 * packed colour int
	 * 
	 * @param rgba
	 * @return The big-endian int
	 */
	public static int toBigEndian( int rgba )
	{
		return redi( rgba ) << 24 | greeni( rgba ) << 16 | bluei( rgba ) << 8
				| alphai( rgba );
	}

	/**
	 * Converts from a big-endian packed colouor int to a native-order
	 * packed colour int
	 * 
	 * @param rgba
	 * @return the native-order int
	 */
	public static int fromBigEndian( int rgba )
	{
		int r = rgba >> 24 & 0xff;
		int g = rgba >> 16 & 0xff;
		int b = rgba >> 8 & 0xff;
		int a = rgba >> 0 & 0xff;
		return packInt( r, g, b, a );
	}

	/**
	 * @param rgba
	 *           packed colour int
	 * @param array
	 *           destination array, or <code>null</code> to allocate a
	 *           new array
	 * @return a float[]{ r, g, b, a } array, in ranges 0-1
	 */
	public static float[] toArray( int rgba, float[] array )
	{
		if( array == null )
		{
			array = new float[ 4 ];
		}

		array[ 0 ] = redf( rgba );
		array[ 1 ] = bluef( rgba );
		array[ 2 ] = greenf( rgba );
		array[ 3 ] = alphaf( rgba );

		return array;
	}

	/**
	 * Packs colour components into an integer
	 * 
	 * @param r
	 *           range 0-255
	 * @param g
	 *           range 0-255
	 * @param b
	 *           range 0-255
	 * @param a
	 *           range 0-255
	 * @return a packed colour integer
	 */
	public static int packInt( int r, int g, int b, int a )
	{
		r = ( r & 0xff ) << redOffset;
		g = ( g & 0xff ) << greenOffset;
		b = ( b & 0xff ) << blueOffset;
		a = ( a & 0xff ) << alphaOffset;

		return r | g | b | a;
	}

	/**
	 * Packs colour components into an integer
	 * 
	 * @param r
	 *           range 0-1
	 * @param g
	 *           range 0-1
	 * @param b
	 *           range 0-1
	 * @param a
	 *           range 0-1
	 * @return a packed colour integer
	 */
	public static int packFloat( float r, float g, float b, float a )
	{
		return packInt( ( int ) ( r * 255f ), ( int ) ( g * 255f ), ( int ) ( b * 255f ),
				( int ) ( a * 255f ) );
	}

	/**
	 * Extracts the red component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-1
	 */
	public static float redf( int rgba )
	{
		return redi( rgba ) / 255f;
	}

	/**
	 * Extracts the green component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-1
	 */
	public static float greenf( int rgba )
	{
		return greeni( rgba ) / 255f;
	}

	/**
	 * Extracts the blue component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-1
	 */
	public static float bluef( int rgba )
	{
		return bluei( rgba ) / 255f;
	}

	/**
	 * Extracts the alpha component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-1
	 */
	public static float alphaf( int rgba )
	{
		return alphai( rgba ) / 255f;
	}

	/**
	 * Extracts the red component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-255
	 */
	public static int redi( int rgba )
	{
		return rgba >> redOffset & 0xff;
	}

	/**
	 * Extracts the green component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-255
	 */
	public static int greeni( int rgba )
	{
		return rgba >> greenOffset & 0xff;
	}

	/**
	 * Extracts the blue component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-255
	 */
	public static int bluei( int rgba )
	{
		return rgba >> blueOffset & 0xff;
	}

	/**
	 * Extracts the alpha component
	 * 
	 * @param rgba
	 *           packed colour value
	 * @return The component 0-255
	 */
	public static int alphai( int rgba )
	{
		return rgba >> alphaOffset & 0xff;
	}

	/**
	 * Mask to get only the alpha bits
	 */
	private static int alphaMask = 0xff << alphaOffset;

	/**
	 * Mask to get only the colour bits
	 */
	private static int colourmask = ~alphaMask;

	/**
	 * Amends a colour by changing the alpha component
	 * 
	 * @param colour
	 *           source colour
	 * @param alpha
	 *           new alpha component (0-255)
	 * @return new colour
	 */
	public static int withAlphai( int colour, int alpha )
	{
		colour = colour & colourmask;
		alpha = ( alpha & 0xff ) << alphaOffset;
		colour |= alpha;
		return colour;
	}

	/**
	 * Amends an array of colour ints by changing the alpha component
	 * 
	 * @param colours
	 *           source colours
	 * @param alpha
	 *           new alpha component (0-255)
	 */
	public static void withAlphai( int[] colours, int alpha )
	{
		for( int i = 0; i < colours.length; i++ )
		{
			colours[ i ] = withAlphai( colours[ i ], alpha );
		}
	}

	/**
	 * @param rgba
	 * @return a string in r:g:b:a format
	 */
	public static String toString( int rgba )
	{
		return redi( rgba ) + ":" + greeni( rgba ) + ":" + bluei( rgba ) + ":"
				+ alphai( rgba );
	}
}
