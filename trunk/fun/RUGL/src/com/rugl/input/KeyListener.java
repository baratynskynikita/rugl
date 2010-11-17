
package com.rugl.input;

import org.lwjgl.input.Keyboard;

/**
 * Extends this class to be appraised of keyboard events
 * 
 * @author ryanm
 */
public class KeyListener
{
	/**
	 * Called when a key is pressed
	 * 
	 * @param keyCode
	 *           The index of the pressed key, see
	 *           {@link Keyboard#getKeyName(int)}
	 * @param keyChar
	 *           The character associated with the keypress. This takes
	 *           account of caps lock and shift
	 * @param repeat
	 *           <code>true</code> if this event is as a result of
	 *           OS-level keyboard repeat behaviour, <code>false</code>
	 *           if as a result of someone actually pressing a key
	 */
	public void keyDown( int keyCode, char keyChar, boolean repeat )
	{
	}

	/**
	 * Called when a key is released
	 * 
	 * @param keyCode
	 *           the index of the released key, see
	 *           {@link Keyboard#getKeyName(int)}
	 */
	public void keyUp( int keyCode )
	{
	}
}
