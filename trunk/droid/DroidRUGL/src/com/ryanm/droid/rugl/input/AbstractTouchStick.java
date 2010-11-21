
package com.ryanm.droid.rugl.input;

import java.util.ArrayList;

import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.TouchListener;

/**
 * Remember to register these with
 * {@link Touch#addListener(TouchListener)} before use, and to
 * {@link Touch#removeListener(TouchListener)} when you're done
 * 
 * @author ryanm
 */
public abstract class AbstractTouchStick implements Touch.TouchListener
{
	/**
	 * Current x value, in range -1 (left) to 1 (right)
	 */
	public float x;

	/**
	 * Current y value, in range -1 (bottom) to 1 (top)
	 */
	public float y;

	/**
	 * The maximum touch time for a click to be registered
	 */
	public long tapTime = 150;

	/**
	 * Use this to track the current touch
	 */
	protected Touch.Pointer touch = null;

	/**
	 * Listeners to notify of a click
	 */
	private ArrayList<ClickListener> listeners = new ArrayList<ClickListener>();

	/**
	 * @author ryanm
	 */
	public static abstract class ClickListener
	{
		/**
		 * The stick has been tapped
		 */
		public abstract void onClick();
	}

	/**
	 * Update the {@link #x} and {@link #y} values according to input
	 */
	public abstract void advance();

	/**
	 * @param sr
	 */
	public abstract void draw( StackedRenderer sr );

	/**
	 * Call this to notify your listeners of a click event
	 */
	protected void notifyClick()
	{
		for( int i = 0; i < listeners.size(); i++ )
		{
			listeners.get( i ).onClick();
		}
	}

	/**
	 * @param l
	 */
	public void addListener( ClickListener l )
	{
		listeners.add( l );
	}

	/**
	 * @param l
	 */
	public void removeListener( ClickListener l )
	{
		listeners.remove( l );
	}
}