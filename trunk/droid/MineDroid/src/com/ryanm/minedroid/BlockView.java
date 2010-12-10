
package com.ryanm.minedroid;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import android.opengl.GLES10;
import android.view.KeyEvent;

import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.Phase;
import com.ryanm.droid.rugl.util.FPSCamera;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * @author ryanm
 */
@Variable
@Summary( "Explore minecraft worlds" )
public class BlockView extends Phase
{
	/***/
	@Variable
	public GUI gui;

	/***/
	@Variable
	public FPSCamera cam = new FPSCamera();

	/***/
	@Variable( "Speed" )
	@Summary( "Speed of camera, in units per second" )
	public float speed = 0.25f;

	private Frustum savedFrustum;

	private Vector3f savedPosition = new Vector3f();

	private Vector3f position = new Vector3f();

	/***/
	@Variable
	public final World world;

	private Game game;

	/**
	 * @param world
	 */
	public BlockView( World world )
	{
		this.world = world;
		position.set( world.startPosition );
	}

	@Override
	public void init( Game game )
	{
		Game.setConfigurationRoots( game, this );
		this.game = game;

		cam.far = 4.5f;

		if( gui == null )
		{
			gui = new GUI();
		}

		BlockFactory.loadTexture();
	}

	@Override
	public void openGLinit()
	{
		GLES10.glClearColor( 0.7f, 0.7f, 0.9f, 1 );
	}

	@Override
	public void advance( float delta )
	{
		gui.advance( delta );

		cam.advance( delta, gui.right.x, gui.right.y );

		position.x += gui.left.y * delta * cam.forward.x * speed;
		position.y += gui.left.y * delta * cam.forward.y * speed;
		position.z += gui.left.y * delta * cam.forward.z * speed;

		position.x += -gui.left.x * delta * cam.right.x * speed;
		position.y += -gui.left.x * delta * cam.right.y * speed;
		position.z += -gui.left.x * delta * cam.right.z * speed;

		world.advance( position.x, position.z );
	}

	@Override
	public void draw()
	{
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		cam.setPosition( position.x, position.y, position.z );

		if( savedFrustum != null )
		{
			world.draw( savedPosition, savedFrustum );
		}
		else
		{
			world.draw( position, cam.frustum );
		}

		gui.draw();
	}

	@Override
	public Phase next()
	{
		return null;
	}

	@Override
	public void onKeyDown( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			complete = true;
		}
		else if( keyCode == KeyEvent.KEYCODE_MENU )
		{
			game.launchConfiguration();
		}
	}
}
