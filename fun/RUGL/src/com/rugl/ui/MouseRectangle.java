
package com.rugl.ui;

import com.ryanm.util.geom.Rectanglef;

/**
 * It's a rectangle that keeps track of mouse interactions
 * 
 * @author ryanm
 */
public class MouseRectangle extends Rectanglef
{
	private boolean over = false;

	private boolean primed = false;

	private boolean clicked = false;

	private boolean wasDown = false;

	/**
	 * Update with the latest mouse state
	 * 
	 * @param x
	 * @param y
	 * @param down
	 */
	public void advance( float x, float y, boolean down )
	{
		over = contains( x, y );

		if( over && primed && !down )
		{
			clicked = true;
		}

		if( over && down )
		{
			if( !wasDown )
			{
				primed = true;
			}
		}

		wasDown = down;

		if( !down )
		{
			primed = false;
		}
	}

	/**
	 * Determines if the mouse is currently over the area
	 * 
	 * @return <code>true</code> if the mouse is over
	 */
	public boolean isOver()
	{
		return over;
	}

	/**
	 * @return if the area is primed to click
	 */
	public boolean isPrimed()
	{
		return primed;
	}

	/**
	 * @return <code>true</code> if the area has been clicked in the
	 *         last {@link #advance(float, float, boolean)} call
	 */
	public boolean isClicked()
	{
		boolean c = clicked;
		clicked = false;
		return c;
	}
}
