
package com.ryanm.droid.rugl.input;

import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.ShapeUtil;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.Pointer;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.BoundingRectangle;

/**
 * A tapable rectangle on the screen
 * 
 * @author ryanm
 */
@Variable( "Tap pad" )
public class TapPad implements Touch.TouchListener
{
	/***/
	@Variable( "Draw" )
	@Summary( "Outline the sensitive area" )
	public boolean draw = false;

	/***/
	@Variable( "Max tap time" )
	@Summary( "The maximum time between press and release "
			+ "for a tap to be registered, in seconds" )
	public float tapTime = 0.15f;

	/***/
	@Variable( "Long press time" )
	@Summary( "The time between press and release "
			+ "for a long press to be registered, in seconds" )
	public float longPressTime = 0.5f;

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
			if( tapped )
			{
				listener.onTap( this );
				tapped = false;
			}
			else if( touch != null )
			{
				long delta = System.currentTimeMillis() - downTime;
				if( delta > longPressTime * 1000 && !longPressed )
				{
					listener.onLongPress( this );
					longPressed = true;
				}
			}
		}
	}

	@Override
	public void pointerAdded( Pointer p )
	{
		if( pad.contains( p.x, p.y ) )
		{
			touch = p;
			downTime = System.currentTimeMillis();
		}
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
								pad.x.getMax(), pad.y.getMax(), 5, 0 ), Colour.packFloat( 1, 1,
								1, 0.25f ), GLUtil.typicalState );
			}

			outline.render( sr );
		}
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
		 * Called when the pad has been held for longer than
		 * {@link TapPad#longPressTime}
		 * 
		 * @param pad
		 *           The pad that has been held
		 */
		public abstract void onLongPress( TapPad pad );
	}
}
