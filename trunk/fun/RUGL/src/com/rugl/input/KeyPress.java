
package com.rugl.input;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

/**
 * Encapsulates a composite key press
 * 
 * @author ryanm
 */
public class KeyPress implements Comparable<KeyPress>
{

	/**
	 * The key codes that must be down for this KeyPress to be active
	 */
	private int[] keyCodes;

	private transient boolean triggerPrimed = true;

	private boolean triggerable = false;

	private static Pattern splitter = Pattern.compile( "\\+" );

	/**
	 * @param keyCodes
	 * @param trigger
	 *           <code>false</code> for continuous or <code>true</code>
	 *           if it needs to be let off before reactivation
	 */
	public KeyPress( boolean trigger, int... keyCodes )
	{
		this.keyCodes = keyCodes;
		triggerable = trigger;

		Arrays.sort( keyCodes );
	}

	/**
	 * Adds a key to this keypress
	 * 
	 * @param keyCode
	 */
	public void addKey( int keyCode )
	{
		int[] nkc = new int[ keyCodes.length + 1 ];

		System.arraycopy( keyCodes, 0, nkc, 0, keyCodes.length );

		nkc[ nkc.length ] = keyCode;

		keyCodes = nkc;
		Arrays.sort( keyCodes );
	}

	/**
	 * Sets the trigger status of this keypress. Triggerable keypresses
	 * activate once and must be released before being activated again
	 * 
	 * @param trigger
	 */
	public void setTriggerable( boolean trigger )
	{
		triggerable = trigger;
		triggerPrimed = true;
	}

	/**
	 * Determines if this {@link KeyPress} is triggerable or continuous
	 * 
	 * @return <code>true</code> if triggerable
	 */
	public boolean isTriggerable()
	{
		return triggerable;
	}

	/**
	 * Determines if this keyPress contains the specified key
	 * 
	 * @param keyCode
	 * @return <code>true</code> if this {@link KeyPress} contains the
	 *         specified code
	 */
	public boolean contains( int keyCode )
	{
		return Arrays.binarySearch( keyCodes, keyCode ) >= 0;
	}

	/**
	 * Gets the number of keys in this {@link KeyPress}
	 * 
	 * @return The number of keys in this {@link KeyPress}
	 */
	public int countKeys()
	{
		return keyCodes.length;
	}

	/**
	 * Determines if this {@link KeyPress} is active
	 * 
	 * @return <code>true</code> if all the keycodes held in the
	 *         {@link KeyPress} are down, <code>false</code> otherwise
	 */
	public boolean isActive()
	{
		boolean active = keyCodes.length > 0;

		for( int i = 0; i < keyCodes.length && active; i++ )
		{
			active &= Keyboard.isKeyDown( keyCodes[ i ] );
		}

		if( active && triggerable )
		{
			if( triggerPrimed )
			{
				triggerPrimed = false;
			}
			else
			{
				active = false;
			}
		}
		else
		{
			triggerPrimed = true;
		}

		return active;
	}

	/**
	 * Builds a {@link KeyPress} from a string
	 * 
	 * @param s
	 *           The string to parse
	 * @return A {@link KeyPress}, or null if the string could not be
	 *         parsed
	 */
	public static KeyPress fromString( CharSequence s )
	{
		if( s.length() >= 2 )
		{
			boolean trigger = s.charAt( 0 ) == 't';
			s = s.subSequence( 2, s.length() );

			List<Integer> keyCodes = new LinkedList<Integer>();

			for( String keyName : splitter.split( s ) )
			{
				keyName = keyName.trim();
				keyName = keyName.toUpperCase();

				int kc = Keyboard.getKeyIndex( keyName );

				if( kc != Keyboard.KEY_NONE )
				{
					keyCodes.add( new Integer( kc ) );
				}
			}

			int[] kc = new int[ keyCodes.size() ];

			int i = 0;
			for( Integer integer : keyCodes )
			{
				kc[ i++ ] = integer.intValue();
			}

			return new KeyPress( trigger, kc );
		}

		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( triggerable ? "t" : "c" );
		buff.append( ":" );

		if( keyCodes.length > 0 )
		{
			buff.append( Keyboard.getKeyName( keyCodes[ 0 ] ) );
			for( int i = 1; i < keyCodes.length; i++ )
			{
				buff.append( " + " );
				buff.append( Keyboard.getKeyName( keyCodes[ i ] ) );
			}
		}
		return buff.toString();

	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof KeyPress )
		{
			KeyPress kp = ( KeyPress ) obj;

			boolean same = triggerable == kp.triggerable;

			same &= keyCodes.length == kp.keyCodes.length;

			for( int i = 0; i < keyCodes.length && same; i++ )
			{
				same &= keyCodes[ i ] == kp.keyCodes[ i ];
			}

			return same;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hc = triggerable ? 257 : 258;

		for( int i = 0; i < keyCodes.length; i++ )
		{
			hc *= keyCodes[ i ];
		}

		return hc;
	}

	@Override
	public int compareTo( KeyPress o )
	{
		int hash = hashCode();
		int otherhash = o.hashCode();

		if( hash < otherhash )
		{
			return -1;
		}
		else if( hash > otherhash )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}
