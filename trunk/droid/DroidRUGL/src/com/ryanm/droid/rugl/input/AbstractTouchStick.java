
package com.ryanm.droid.rugl.input;

import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.TouchListener;
import com.ryanm.preflect.annote.Category;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;

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
	@Variable( "Tap time" )
	@Summary( "The maximum touch time for a click to be registered, in milliseconds" )
	@Category( "Interaction" )
	public long tapTime = 150;

	/**
	 * 
	 */
	@Variable( "Tap-hold delay" )
	@Summary( "The maximum delay between a tap and a long hold, in milliseconds" )
	@Category( "Interaction" )
	public long clickHoldDelay = 150;

	/**
	 * Use this to track the current touch
	 */
	protected Touch.Pointer touch = null;

	/**
	 * Listener to notify of a click
	 */
	public ClickListener listener = null;

	/**
	 * @author ryanm
	 */
	public static abstract class ClickListener
	{
		/**
		 * The stick has been tapped
		 */
		public abstract void onClick();

		/**
		 * The stick has been clicked, and then long-held
		 * 
		 * @param active
		 *           <code>true</code> when we start the click-hold,
		 *           <code>false</code> when we end it
		 */
		public abstract void onClickHold( boolean active );
	}

	/**
	 * Update the {@link #x} and {@link #y} values according to input
	 */
	public abstract void advance();

	/**
	 * @param sr
	 */
	public abstract void draw( StackedRenderer sr );
}