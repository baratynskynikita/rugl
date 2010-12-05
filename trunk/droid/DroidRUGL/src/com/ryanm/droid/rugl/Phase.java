
package com.ryanm.droid.rugl;

import android.view.KeyEvent;

/**
 * Game phase
 * 
 * @author ryanm
 */
public abstract class Phase
{
	/**
	 * Set this to true when the phase is over
	 */
	protected boolean complete = false;

	/**
	 * Called just before this phase is used for the first time, and
	 * when the opengl state has been compromised e.g.: when the
	 * activity is backgrounded and restored. The upshot: any opengl
	 * calls you're making in {@link #init(Game)} should go in here
	 * instead, and you don't need to call this method yourself
	 */
	public void openGLinit()
	{
	}

	/**
	 * Called just before this phase is used for the first time. If
	 * this phase is reused, remember set to {@link #complete} to
	 * <code>false</code>
	 * 
	 * @param game
	 *           The {@link Game} that this phase is a part of. Gives
	 *           access to
	 */
	public abstract void init( Game game );

	/**
	 * Advance the phase
	 * 
	 * @param delta
	 *           time delta in seconds
	 */
	public abstract void advance( float delta );

	/**
	 * Draw the phase to the screen
	 */
	public abstract void draw();

	/**
	 * Gets the phase after this one
	 * 
	 * @return the next phase, or <code>null</code> to quit the game
	 */
	public abstract Phase next();

	/**
	 * Override to handle keys
	 * 
	 * @param keyCode
	 * @param event
	 */
	public void onKeyDown( int keyCode, KeyEvent event )
	{
	}

	/**
	 * Override to handle keys
	 * 
	 * @param keyCode
	 * @param event
	 */
	public void onKeyUp( int keyCode, KeyEvent event )
	{
	}

	/**
	 * Override to handle keys
	 * 
	 * @param keyCode
	 * @param event
	 */
	public void onKeyLongPress( int keyCode, KeyEvent event )
	{
	}
}