
package com.ryanm.droid.rugl.input;

import java.util.ArrayList;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.ShapeUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.Pointer;
import com.ryanm.droid.rugl.input.Touch.TouchListener;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.Trig;
import com.ryanm.droid.rugl.util.math.Range;


/**
 * Simulates a thumbstick
 * 
 * @author ryanm
 */
public class TouchStick
{
	private ColouredShape limit;

	private ColouredShape stick;

	private float xPos, yPos, radius;

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

	private boolean touchLeft = false;

	/**
	 * Indicates that the touch has been lifted
	 */
	private Touch.Pointer touch = null;

	private long touchTime = -1;

	private ArrayList<ClickListener> listeners = new ArrayList<ClickListener>();

	private TouchListener l = new TouchListener() {
		@Override
		public void pointerRemoved( Pointer p )
		{
			if( p == touch )
			{
				touchLeft = true;
			}
		}

		@Override
		public void pointerAdded( Pointer p )
		{
			if( touch == null && Math.hypot( p.x - xPos, p.y - yPos ) < radius )
			{
				touch = p;
				touchTime = System.currentTimeMillis();
			}
		}
	};

	/**
	 * @param x
	 *           position, in screen coordinates
	 * @param y
	 *           position, in screen coordinates
	 * @param limitRadius
	 *           radius, in screen coordinates
	 */
	public TouchStick( float x, float y, float limitRadius )
	{
		xPos = x;
		yPos = y;
		radius = limitRadius;

		limit =
				new ColouredShape( ShapeUtil.innerCircle( 0, 0, limitRadius, 10, 30, 0 ),
						Colour.white, null );

		stick = new ColouredShape( limit.clone(), Colour.white, null );
		stick.scale( 0.5f, 0.5f, 1 );
		Colour.withAlphai( stick.colours, 128 );

		limit.translate( x, y, 0 );
		stick.translate( x, y, 0 );

		for( int i = 0; i < limit.colours.length; i += 2 )
		{
			limit.colours[ i ] = Colour.withAlphai( limit.colours[ i ], 0 );
		}

		Touch.addListener( l );
	}

	/**
	 * @param x
	 * @param y
	 */
	public void setPosition( float x, float y )
	{
		limit.translate( x - xPos, y - yPos, 0 );
		stick.translate( x - xPos, y - yPos, 0 );

		xPos = x;
		yPos = y;
	}

	/**
	 * Updates the {@link #x} and {@link #y} values
	 */
	public void advance()
	{
		if( touchLeft )
		{
			touch = null;
			touchLeft = false;

			long tapDuration = System.currentTimeMillis() - touchTime;

			if( tapDuration < tapTime )
			{
				for( int i = 0; i < listeners.size(); i++ )
				{
					listeners.get( i ).onClick();
				}
			}
		}

		if( touch != null )
		{
			float dx = touch.x - xPos;
			float dy = touch.y - yPos;

			float a = Trig.atan2( dy, dx );

			float r = ( float ) Math.sqrt( dx * dx + dy * dy ) / radius;
			r = Range.limit( r, 0, 1 );

			x = r * Trig.cos( a );
			y = r * Trig.sin( a );
		}
		else
		{
			x = 0;
			y = 0;
		}
	}

	/**
	 * @param r
	 */
	public void draw( StackedRenderer r )
	{
		limit.render( r );

		r.pushMatrix();
		{
			r.translate( x * radius, y * radius, 0 );
			stick.render( r );
		}
		r.popMatrix();
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
	public void removeLIstener( ClickListener l )
	{
		listeners.remove( l );
	}

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
}
