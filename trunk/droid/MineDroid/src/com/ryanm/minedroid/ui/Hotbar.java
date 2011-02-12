
package com.ryanm.minedroid.ui;

import java.util.Arrays;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.Shape;
import com.ryanm.droid.rugl.geom.ShapeUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch.Pointer;
import com.ryanm.droid.rugl.input.Touch.TouchListener;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.BoundingRectangle;
import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.minedroid.Player;
import com.ryanm.preflect.annote.DirtyFlag;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;
import com.ryanm.preflect.annote.WidgetHint;

/**
 * On-screen inventory
 * 
 * @author ryanm
 */
@Variable( "Hotbar" )
@Summary( "Controls for size, placement and zoom behaviour" )
public class Hotbar implements TouchListener
{
	private final Player player;

	/***/
	@Variable
	public BoundingRectangle bounds = new BoundingRectangle( 150, 0, 500, 80 );

	private ColouredShape boundsShape;

	/***/
	@Variable
	@WidgetHint( Colour.class )
	public int boundsColour = Colour.packFloat( 1, 1, 1, 0.25f );

	/**
	 * How much to zoom roll-overed items
	 */
	@Variable
	@Summary( "For rolled-over items" )
	public float maxZoom = 2;

	/***/
	@Variable
	@Summary( "Zoom duration in seconds" )
	public float zoomTime = 0.15f;

	private float[] currentZooms = new float[ 9 ];

	private float[] targetZooms = new float[ 9 ];

	private Pointer touch;

	private int selection;

	// to notify when we drag from the hotbar
	private final Interaction interaction;

	/**
	 * @param player
	 * @param interaction
	 */
	public Hotbar( Player player, Interaction interaction )
	{
		this.player = player;
		this.interaction = interaction;
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		// set target zooms
		Arrays.fill( targetZooms, 0 );
		if( touch != null )
		{
			float d = bounds.x.toRatio( touch.x );
			d = Range.limit( d, 0, 0.99f );
			d *= 9;
			selection = ( int ) d;
			targetZooms[ selection ] = 1;

			// check for swipe off
			if( !bounds.contains( touch.x, touch.y ) )
			{
				interaction.swipeFromHotBar( player.hotbar[ selection ], touch );
				touch = null;
			}
		}

		// lerp zooms
		for( int i = 0; i < currentZooms.length; i++ )
		{
			if( currentZooms[ i ] < targetZooms[ i ] )
			{
				currentZooms[ i ] += delta / zoomTime;
			}
			else if( currentZooms[ i ] > targetZooms[ i ] )
			{
				currentZooms[ i ] -= delta / zoomTime;
			}

			currentZooms[ i ] = Range.limit( currentZooms[ i ], 0, 1 );
		}
	}

	/**
	 * @param sr
	 */
	public void draw( StackedRenderer sr )
	{
		if( boundsShape == null )
		{
			Shape bs =
					ShapeUtil.innerQuad( bounds.x.getMin(), bounds.y.getMin(),
							bounds.x.getMax(), bounds.y.getMax(), 5, 0.5f );
			boundsShape = new ColouredShape( bs, boundsColour, null );
		}

		boundsShape.render( sr );

		float off = ( bounds.x.getSpan() - 10 ) / 9;
		float size = Math.min( bounds.y.getSpan() - 10, off );

		for( int i = 0; i < player.hotbar.length; i++ )
		{
			if( player.hotbar[ i ] != null )
			{
				sr.pushMatrix();
				sr.translate(
						bounds.x.getMin() + 5 + off / 2 + i * off,
						Range.toValue( currentZooms[ i ], bounds.y.toValue( 0.5f ),
								bounds.y.toValue( 1.5f ) ), 0 );

				float zoom = Range.smooth( currentZooms[ i ], 1, maxZoom );

				sr.scale( size * zoom, size * zoom, 1 );

				player.hotbar[ i ].itemShape.render( sr );

				sr.popMatrix();
			}
		}
	}

	/***/
	@DirtyFlag
	public void boundsDirty()
	{
		boundsShape = null;
	}

	@Override
	public boolean pointerAdded( Pointer p )
	{
		if( touch == null && bounds.contains( p.x, p.y ) )
		{
			touch = p;

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

			player.inHand = player.hotbar[ selection ];
		}
	}

	@Override
	public void reset()
	{
		touch = null;
	}
}
