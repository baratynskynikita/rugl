
package com.ryanm.minedroid.gui;

import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import android.util.Log;

import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.AbstractTouchStick.ClickListener;
import com.ryanm.droid.rugl.input.TapPad;
import com.ryanm.droid.rugl.input.Touch;
import com.ryanm.droid.rugl.input.TouchStickArea;
import com.ryanm.droid.rugl.res.FontLoader;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.text.Font;
import com.ryanm.droid.rugl.text.Readout;
import com.ryanm.droid.rugl.text.TextShape;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.minedroid.Player;
import com.ryanm.minedroid.World;
import com.ryanm.minedroid.chunk.GeometryGenerator;

/**
 * Holds the touchsticks
 * 
 * @author ryanm
 */
@Variable( "Interface" )
@Summary( "GUI options" )
public class GUI
{
	private static final float radius = 50;

	private static final float size = 150;

	/***/
	@Variable( "Left stick" )
	@Summary( "Controls motion" )
	@Category( "Controls" )
	public final TouchStickArea left = new TouchStickArea( 0, 0, size, size, radius );

	/***/
	@Variable( "Right stick" )
	@Summary( "Controls view direction" )
	@Category( "Controls" )
	public final TouchStickArea right = new TouchStickArea( 800 - size, 0, size, size,
			radius );

	/***/
	@Variable( "Right tap pad" )
	@Summary( "Tap to jump, long press to crouch" )
	@Category( "Controls" )
	public final TapPad rightTap = new TapPad( 800 - size, right.pad.y.getMax(), size,
			size / 2 );

	/***/
	@Variable( "Chunklet info display" )
	@Summary( "Show chunks awaiting loading, chunklets "
			+ "awaiting geometry generation, and rendered chunklet count" )
	public boolean printQueues = true;

	/***/
	@Variable
	public final Hotbar hotbar;

	/***/
	@Variable
	public final Hand hand;

	private Readout loadQueue, geomQueue, chunkletCount;

	private StackedRenderer r = new StackedRenderer();

	private TextShape notification;

	private float notifyTime = 0;

	private Font font;

	/**
	 * @param player
	 */
	public GUI( Player player )
	{
		hotbar = new Hotbar( player );
		hand = new Hand( player );
		rightTap.listener = player.jumpCrouchListener;

		Touch.addListener( left );
		Touch.addListener( right );
		Touch.addListener( rightTap );

		ClickListener strikey = new ClickListener() {
			@Override
			public void onClick()
			{
				hand.strike();
			}
		};

		right.listener = strikey;
		left.listener = strikey;

		Touch.setScreenSize( 800, 480, Game.width, Game.height );

		ResourceLoader.load( new FontLoader( com.ryanm.droid.rugl.R.raw.font, false ) {
			@Override
			public void fontLoaded()
			{
				font = resource;

				loadQueue = new Readout( font, Colour.black, "Load queue = ", false, 2, 0 );
				loadQueue.translate( 10,
						Game.height - 10 - loadQueue.getBounds().y.getSpan(), 0 );

				geomQueue = new Readout( font, Colour.black, "Geom queue = ", false, 3, 0 );
				geomQueue.translate( 10,
						Game.height - 20 - 2 * geomQueue.getBounds().y.getSpan(), 0 );

				chunkletCount = new Readout( font, Colour.black, "Chunklets = ", false, 3, 0 );
				chunkletCount.translate( 10, Game.height - 40 - 3
						* chunkletCount.getBounds().y.getSpan(), 0 );

			}
		} );
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		left.advance();
		right.advance();
		rightTap.advance();

		hotbar.advance( delta );
		hand.advance( delta );

		notifyTime -= delta;
		if( notifyTime < 0 )
		{
			notification = null;
		}
	}

	/**
	 * Note that the projection matrix will be changed and the depth
	 * buffer cleared in here
	 */
	public void draw()
	{
		GLUtil.scaledOrtho( 800, 480, Game.width, Game.height, -1, 1 );
		glClear( GL_DEPTH_BUFFER_BIT );

		hand.draw( r );

		r.render();

		left.draw( r );
		right.draw( r );
		rightTap.draw( r );

		hotbar.draw( r );

		if( notification != null )
		{
			notification.render( r );
		}

		if( printQueues && loadQueue != null )
		{
			loadQueue.updateValue( ResourceLoader.queueSize() );
			loadQueue.render( r );

			geomQueue.updateValue( GeometryGenerator.getChunkletQueueSize() );
			geomQueue.render( r );

			chunkletCount.updateValue( World.renderedChunklets );

			chunkletCount.render( r );
		}

		r.render();
	}

	/**
	 * @param string
	 */
	public void notify( String string )
	{
		Log.i( Game.RUGL_TAG, "Notification: " + string );
		if( font != null )
		{
			notification = font.buildTextShape( string, Colour.black );
			notification.translate( ( 800 - notification.getBounds().x.getSpan() ) / 2, 100,
					0 );
			notifyTime = 1.5f;
		}
	}
}
