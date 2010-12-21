
package com.ryanm.minedroid;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import android.opengl.GLES10;
import android.util.FloatMath;
import android.view.KeyEvent;

import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.config.annote.WidgetHint;
import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.Phase;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.FPSCamera;
import com.ryanm.droid.rugl.util.geom.BoundingCuboid;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.minedroid.BlockFactory.Block;

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
	@Variable( "Sky colour" )
	@WidgetHint( Colour.class )
	public int skyColour = Colour.packFloat( 0.7f, 0.7f, 0.9f, 1 );

	/***/
	@Variable( "Speed" )
	@Summary( "Speed of camera, in blocks per second" )
	public float speed = 4f;

	/***/
	@Variable( "World collide" )
	@Summary( "Fly free or suffer a mundane corporeal existence" )
	@Category( "Clipping" )
	public boolean worldCollide = true;

	/***/
	@Variable( "Width" )
	@Summary( "Width of character, in block units" )
	@Category( "Clipping" )
	public float width = 0.75f;

	/***/
	@Variable( "Height" )
	@Summary( "Height of character, in block units" )
	@Category( "Clipping" )
	public float height = 1.8f;

	/***/
	@Variable( "Eye level" )
	@Summary( "The height of the camera, in terms of character height" )
	@Category( "Clipping" )
	public float eyeLevel = 0.9f;

	private Frustum savedFrustum;

	private Vector3f savedPosition = new Vector3f();

	private Vector3f position = new Vector3f();

	private Vector3f collideCorrection = new Vector3f();

	private BoundingCuboid playerBounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private BoundingCuboid blockBounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private BoundingCuboid intersection = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

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

		cam.far = 80;

		if( gui == null )
		{
			gui = new GUI();
		}

		BlockFactory.loadTexture();
	}

	@Override
	public void openGLinit()
	{
		GLES10.glClearColor( Colour.redf( skyColour ), Colour.greenf( skyColour ),
				Colour.bluef( skyColour ), Colour.alphaf( skyColour ) );
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

		if( worldCollide )
		{
			float w = width / 2;
			float feet = height * eyeLevel;
			float head = height - feet;
			playerBounds.set( position.x - w, position.y - feet, position.z - w, position.x
					+ w, position.y + head, position.z + w );

			for( float x = FloatMath.floor( playerBounds.x.getMin() ); x < playerBounds.x
					.getMax(); x++ )
			{
				for( float z = FloatMath.floor( playerBounds.z.getMin() ); z < playerBounds.z
						.getMax(); z++ )
				{
					for( float y = FloatMath.floor( playerBounds.y.getMin() ); y < playerBounds.y
							.getMax(); y++ )
					{
						collideCorrection.set( 0, 0, 0 );

						collide( x, y, z, playerBounds, collideCorrection );

						playerBounds.translate( collideCorrection.x, collideCorrection.y,
								collideCorrection.z );
						Vector3f.add( position, collideCorrection, position );
					}
				}
			}
		}

		world.advance( position.x, position.z );
	}

	/**
	 * Collides a point against the world's blocks, computes the
	 * smallest correction to rectify any collision
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param correction
	 */
	private void collide( float x, float y, float z, BoundingCuboid player,
			Vector3f correction )
	{
		byte bt = world.blockType( x, y, z );
		Block b = BlockFactory.getBlock( bt );

		if( !( b == null || b == Block.Water || b == Block.StillWater ) )
		{
			x = FloatMath.floor( x );
			y = FloatMath.floor( y );
			z = FloatMath.floor( z );
			blockBounds.set( x, y, z, x + 1, y + 1, z + 1 );

			if( playerBounds.intersection( blockBounds, intersection ) )
			{
				correction( intersection, collideCorrection );

				// Log.i( Game.RUGL_TAG, "Eye at " + position );
				// Log.i( Game.RUGL_TAG, "Player bounds " + player );
				// Log.i( Game.RUGL_TAG, "Block " + blockBounds.toString()
				// );
				// Log.i( Game.RUGL_TAG, "intersection " +
				// intersection.toString() );
				// Log.i( Game.RUGL_TAG, "Correction " +
				// collideCorrection.toString() );
			}
		}
	}

	/**
	 * Calculates the minimum correction vector to move the point out
	 * of the unit cube
	 * 
	 * @param intersection
	 * @param correction
	 */
	private void correction( BoundingCuboid intersection, Vector3f correction )
	{
		float mx = intersection.x.getSpan();
		float my = intersection.y.getSpan();
		float mz = intersection.z.getSpan();

		if( mx < my && mx < mz )
		{
			correction.set( intersection.x.toValue( 0.5f ) < position.x ? mx : -mx, 0, 0 );
		}
		else if( my < mz )
		{
			correction.set( 0, intersection.y.toValue( 0.5f ) < position.y ? my : -my, 0 );
		}
		else
		{
			correction.set( 0, 0, intersection.z.toValue( 0.5f ) < position.z ? mz : -mz );
		}
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
