
package com.ryanm.droid.rugl.input;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.ShapeUtil;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.Pointer;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.BoundingRectangle;
import com.ryanm.preflect.annote.DirtyFlag;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;
import com.ryanm.preflect.annote.WidgetHint;

/**
 * A tapable, flickable rectangle on the screen
 * 
 * @author ryanm
 */
@Variable( "Tap pad" )
public class TapPad implements Touch.TouchListener
{
	/***/
	@Variable( "Draw" )
	@Summary( "Outline the sensitive area" )
	public boolean draw = true;

	/***/
	@Variable( "Max tap time" )
	@Summary( "The maximum time between press and release "
			+ "for a tap to be registered, in seconds" )
	public float tapTime = 0.15f;

	/***/
	@Variable( "Long press time" )
	@Summary( "Minimum touch time for a long press to be registered, in seconds" )
	public float longPressTime = 0.5f;

	/***/
	@Variable( "Bounds colour" )
	@Summary( "Colour of pad area outline" )
	@WidgetHint( Colour.class )
	public int boundsColour = Colour.packFloat( 1, 1, 1, 0.3f );

	private BoundingRectangle pad = new BoundingRectangle();

	private Pointer touch;

	private long downTime = -1;

	private boolean tapped = false;

	private boolean longPressed = false;

	private ColouredShape outline;

	/**
	 * Set this to respond to taps and presses
	 */
	public Listener listener = null;

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public TapPad( float x, float y, float width, float height )
	{
		pad.set( x, x + width, y, y + height );
	}

	/**
	 * Advance pad state
	 */
	public void advance()
	{
		if( listener != null )
		{
			if( touch != null )
			{
				if( !pad.contains( touch.x, touch.y ) )
				{
					int horizontal = 0;
					if( touch.x < pad.x.getMin() )
					{
						horizontal = -1;
					}
					else if( touch.x > pad.x.getMax() )
					{
						horizontal = 1;
					}

					int vertical = 0;
					if( touch.y < pad.y.getMin() )
					{
						vertical = -1;
					}
					else if( touch.y > pad.y.getMax() )
					{
						vertical = 1;
					}

					listener.onFlick( this, horizontal, vertical );
				}

				long delta = System.currentTimeMillis() - downTime;
				if( delta > longPressTime * 1000 && !longPressed )
				{
					listener.onLongPress( this );
					longPressed = true;
				}
			}

			if( tapped )
			{
				listener.onTap( this );
				tapped = false;
			}
		}
	}

	@Override
	public boolean pointerAdded( Pointer p )
	{
		if( pad.contains( p.x, p.y ) )
		{
			touch = p;
			downTime = System.currentTimeMillis();

			return true;
		}
		return false;
	}

	@Override
	public void pointerRemoved( Pointer p )
	{
		if( touch == p )
		{
			touch = null;

			long delta = System.currentTimeMillis() - downTime;
			if( delta < tapTime * 1000 )
			{
				tapped = true;
			}

			longPressed = false;
		}
	}

	/**
	 * @return the sensitive pad area
	 */
	@Variable( "Pad area" )
	@Summary( "Position and size of sensitive area" )
	public BoundingRectangle getPad()
	{
		return pad;
	}

	/**
	 * @param pad
	 *           The new sensitive pad area
	 */
	@Variable( "Pad area" )
	public void setPad( BoundingRectangle pad )
	{
		this.pad.set( pad );
		outline = null;
	}

	/**
	 * @param sr
	 */
	public void draw( StackedRenderer sr )
	{
		if( draw && touch == null )
		{
			if( outline == null )
			{
				outline =
						new ColouredShape( ShapeUtil.innerQuad( pad.x.getMin(), pad.y.getMin(),
								pad.x.getMax(), pad.y.getMax(), 5, 0 ), boundsColour,
								GLUtil.typicalState );
			}

			outline.render( sr );
		}
	}

	/***/
	@DirtyFlag
	public void outLineDirty()
	{
		outline = null;
	}

	/**
	 * @author ryanm
	 */
	public static abstract class Listener
	{
		/**
		 * Called when the pad has been tapped for less than
		 * {@link TapPad#tapTime}
		 * 
		 * @param pad
		 *           The pad that has been tapped
		 */
		public abstract void onTap( TapPad pad );

		/**
		 * Called when a touch is made within, and then slid out of the
		 * bounds of the pad before the touch become a long-press
		 * 
		 * @param pad
		 *           The pad that has been flicked
		 * @param horizontal
		 *           1 if the touch is now to the right of the pad, -1
		 *           if to the left, or 0 if still within bounds on the
		 *           x-axis
		 * @param vertical
		 *           1 if the touch is now above the pad, -1 if below,
		 *           or 0 if still within bounds on the y-axis
		 */
		public abstract void onFlick( TapPad pad, int horizontal, int vertical );

		/**
		 * Called when the pad has been held for longer than
		 * {@link TapPad#longPressTime}
		 * 
		 * @param pad
		 *           The pad that has been held
		 */
		public abstract void onLongPress( TapPad pad );
	}
}
