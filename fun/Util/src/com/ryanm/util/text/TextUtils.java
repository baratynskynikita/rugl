
package com.ryanm.util.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for dealing with text
 * 
 * @author ryanm
 */
public class TextUtils
{
	/**
	 * Tests if s starts with t, ignoring the case of the characters
	 * 
	 * @param s
	 * @param t
	 * @return <code>true</code> if s.toLowerCase().equals(
	 *         t.toLowerCase() ), but more efficiently
	 */
	public static boolean startsWithIgnoreCase( CharSequence s, CharSequence t )
	{
		if( s.length() < t.length() )
		{
			return false;
		}

		for( int i = 0; i < t.length(); i++ )
		{
			char slc = Character.toLowerCase( s.charAt( i ) );
			char tlc = Character.toLowerCase( t.charAt( i ) );
			if( slc != tlc )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * See {@link String#compareToIgnoreCase(String)}
	 * 
	 * @param s
	 * @param t
	 * @return See {@link String#compareToIgnoreCase(String)}
	 */
	public static int compareToIgnoreCase( CharSequence s, CharSequence t )
	{
		int i = 0;

		while( i < s.length() && i < t.length() )
		{
			char a = Character.toLowerCase( s.charAt( i ) );
			char b = Character.toLowerCase( t.charAt( i ) );

			int diff = a - b;

			if( diff != 0 )
			{
				return diff;
			}

			i++;
		}

		return s.length() - t.length();
	}

	/**
	 * See {@link String#compareTo(String)}
	 * 
	 * @param s
	 * @param t
	 * @return See {@link String#compareTo(String)}
	 */
	public static int compareTo( CharSequence s, CharSequence t )
	{
		int i = 0;

		while( i < s.length() && i < t.length() )
		{
			char a = s.charAt( i );
			char b = t.charAt( i );

			int diff = a - b;

			if( diff != 0 )
			{
				return diff;
			}

			i++;
		}

		return s.length() - t.length();
	}

	/**
	 * Splits a string
	 * 
	 * @param composite
	 *           The composite string
	 * @param leftBracket
	 *           the opening parenthesis character
	 * @param rightBracket
	 *           the closing parenthesis character
	 * @param separator
	 *           The character that separates tokens. Separators that
	 *           lie between at least one pair of parenthesis are
	 *           ignored
	 * @return An array of individual tokens
	 */
	public static String[] split( String composite, char leftBracket, char rightBracket,
			char separator )
	{
		List<String> c = new ArrayList<String>();

		int start = 0;
		int i;
		int lbcount = 0;

		for( i = 0; i < composite.length(); i++ )
		{
			if( composite.charAt( i ) == leftBracket )
			{
				lbcount++;
			}
			else if( composite.charAt( i ) == rightBracket )
			{
				lbcount--;
			}
			else if( composite.charAt( i ) == separator && lbcount == 0 )
			{
				c.add( composite.substring( start, i ).trim() );
				start = i + 1;
			}
		}

		c.add( composite.substring( start, i ).trim() );

		return c.toArray( new String[ c.size() ] );
	}

	/**
	 * Wraps the input string in {@code <html></html>} and breaks it up
	 * into lines with {@code <br>} elements. Useful for making
	 * multi-line tootips and the like.
	 * 
	 * @param s
	 *           The input String
	 * @param lineLength
	 *           The desired length of the output lines.
	 * @return The HTMLised string
	 */
	public static String HTMLiseString( String s, int lineLength )
	{
		if( s != null )
		{
			StringBuilder buff = new StringBuilder( s );

			int lineStart = 0;

			while( lineStart + lineLength < s.length() )
			{
				// find the first whitespace after the linelength
				int firstSpaceIndex = buff.indexOf( " ", lineStart + lineLength );
				// replace it with a <br>
				if( firstSpaceIndex != -1 )
				{
					buff.deleteCharAt( firstSpaceIndex );
					buff.insert( firstSpaceIndex, "<br>" );
					lineStart = firstSpaceIndex + 4;
				}
				else
				{
					lineStart = s.length();
				}
			}

			buff.insert( 0, "<html>" );
			buff.append( "</html>" );

			return buff.toString();
		}

		return null;
	}

}
