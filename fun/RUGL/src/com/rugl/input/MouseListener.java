
package com.rugl.input;

/**
 * For things interested in being notified of mouse actions
 * 
 * @author ryanm
 */
public interface MouseListener
{
	/**
	 * Called when the mouse has moved since the last frame
	 * 
	 * @param x
	 *           The new x-coordinate of the pointer
	 * @param y
	 *           The new y-coordinate of the pointer
	 * @param dx
	 *           The change in x-coordinate since the last frame
	 * @param dy
	 *           The change in y-coordinate since the last frame
	 */
	public void mouseMoved( int x, int y, int dx, int dy );

	/**
	 * Called when a button is released
	 * 
	 * @param button
	 *           The button ID
	 * @param down
	 *           <code>true</code> if the button has been pressed,
	 *           <code>false</code> if released
	 */
	public void mouseButton( int button, boolean down );

	/**
	 * Called when the mouse wheel is moved
	 * 
	 * @param dz
	 *           +ve for up, -ve for down, I think it's the number of
	 *           pixels to scroll by. In any case, you can probably
	 *           safely assume that you'll get one click per frame
	 */
	public void mouseWheel( int dz );
}
