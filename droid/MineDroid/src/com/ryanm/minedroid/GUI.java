
package com.ryanm.minedroid;

import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;

import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.input.Touch;
import com.ryanm.droid.rugl.input.TouchStickArea;
import com.ryanm.droid.rugl.res.FontLoader;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.text.Font;
import com.ryanm.droid.rugl.text.Readout;
import com.ryanm.droid.rugl.text.TextShape;
import com.ryanm.droid.rugl.util.Colour;

/**
 * Holds the touchsticks
 * 
 * @author ryanm
 */
@Variable( "Interface" )
@Summary( "GUI options" )
public class GUI
{
	private static final float radius = 100;

	/***/
	@Variable( "Left" )
	@Category( "Sticks" )
	public final TouchStickArea left = new TouchStickArea( 0, 0, 400, 480, radius );

	/***/
	@Variable( "Right" )
	@Category( "Sticks" )
	public final TouchStickArea right = new TouchStickArea( 400, 0, 400, 480, radius );

	/***/
	@Variable( "Print queue sizes" )
	@Summary( "Show the number of chunks awaiting loading and chunklets "
			+ "awaiting geometry generation" )
	public boolean printQueues = true;

	private Readout loadQueue, geomQueue, chunkletCount;

	private StackedRenderer r = new StackedRenderer();

	private TextShape notification;

	private float notifyTime = 0;

	private Font font;

	/***/
	public GUI()
	{
		Touch.addListener( left );
		Touch.addListener( right );

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

		Touch.setScreenSize( 800, 480, Game.width, Game.height );
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		left.advance();
		right.advance();

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
		GLUtil.scaledOrtho( 800, 480, Game.width, Game.height );
		glClear( GL_DEPTH_BUFFER_BIT );

		left.draw( r );
		right.draw( r );

		if( notification != null )
		{
			notification.render( r );
		}

		if( printQueues && loadQueue != null )
		{
			loadQueue.updateValue( ResourceLoader.queueSize() );
			loadQueue.render( r );

			geomQueue.updateValue( Chunklet.getChunkletQueueSize() );
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
		if( font != null )
		{
			notification = font.buildTextShape( string, Colour.black );
			notification.translate(
					( Game.width - notification.getBounds().x.getSpan() ) / 2, 100, 0 );
			notifyTime = 1.5f;
		}
	}
}
