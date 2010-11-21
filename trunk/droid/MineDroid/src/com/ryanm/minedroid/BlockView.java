
package com.ryanm.minedroid;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import android.opengl.GLES10;
import android.view.KeyEvent;

import com.ryanm.droid.rugl.Phase;
import com.ryanm.droid.rugl.input.AbstractTouchStick.ClickListener;
import com.ryanm.droid.rugl.util.FPSCamera;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Vector3f;

/**
 * @author ryanm
 */
public class BlockView extends Phase
{
	private GUI gui;

	private FPSCamera cam = new FPSCamera();

	private Frustum savedFrustum;

	private Vector3f savedPosition = new Vector3f();

	private Vector3f position = new Vector3f();

	private final World world;

	/**
	 * @param world
	 */
	public BlockView( World world )
	{
		this.world = world;
		position.set( world.startPosition );
	}

	@Override
	public void init()
	{
		cam.far = 2.0f;

		if( gui == null )
		{
			gui = new GUI();

			gui.right.addListener( new ClickListener() {
				@Override
				public void onClick()
				{
					if( savedFrustum != null )
					{
						savedFrustum = null;
						gui.notify( "Back to camera" );
					}
					else
					{
						savedFrustum = new Frustum( cam.frustum );
						savedPosition.set( position );
						gui.notify( "Freezing frustum" );
					}
				}
			} );
		}

		BlockFactory.loadTexture();
	}

	@Override
	public void openGLinit()
	{
		GLES10.glClearColor( 0.9f, 0.9f, 0.9f, 1 );
		GLES10.glEnable( GLES10.GL_FOG );
		GLES10.glFogx( GLES10.GL_FOG_MODE, GLES10.GL_LINEAR );
		GLES10.glFogf( GLES10.GL_FOG_START, 1.8f );
		GLES10.glFogf( GLES10.GL_FOG_END, 2f );
		GLES10.glFogfv( GLES10.GL_FOG_COLOR, new float[] { 1, 1, 1, 1 }, 0 );
	}

	@Override
	public void advance( float delta )
	{
		gui.advance( delta );

		cam.advance( delta, gui.right.x, gui.right.y );

		float speed = 0.25f;

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

		cam.apply( position.x, position.y, position.z );

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
	}
}
