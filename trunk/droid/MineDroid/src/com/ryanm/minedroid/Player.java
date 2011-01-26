
package com.ryanm.minedroid;

import android.util.FloatMath;

import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.input.TapPad;
import com.ryanm.droid.rugl.util.FPSCamera;
import com.ryanm.droid.rugl.util.geom.BoundingCuboid;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.minedroid.BlockFactory.Block;
import com.ryanm.minedroid.ItemFactory.Item;
import com.ryanm.minedroid.gui.GUI;

/**
 * @author ryanm
 */
public class Player
{
	private final World world;

	/***/
	@Variable( "Speed" )
	@Summary( "In blocks per second" )
	@Category( "Motion" )
	public float speed = 4f;

	/***/
	@Variable( "Crouched speed" )
	@Summary( "In blocks per second" )
	@Category( "Motion" )
	public float crouchedSpeed = 2f;

	/***/
	@Variable( "Jump speed" )
	@Summary( "Vertical speed on jumping, in blocks per second" )
	@Category( "Motion" )
	public float jumpSpeed = 6f;

	/***/
	@Variable( "Gravity" )
	@Summary( "Acceleration due to gravity, in blocks per second per second" )
	@Category( "Motion" )
	public float gravity = -10;

	/***/
	@Variable( "Ghost mode" )
	@Summary( "Fly free or suffer a mundane corporeal existence" )
	public boolean ghost = false;

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

	/***/
	@Variable( "Crouched eye level" )
	@Summary( "The height of the camera when crouched, in terms of character height" )
	@Category( "Clipping" )
	public float crouchedEyeLevel = 0.65f;

	/***/
	public boolean onGround = false;

	/***/
	private boolean crouched = false;

	/***/
	public Vector3f position = new Vector3f();

	/***/
	public Vector3f velocity = new Vector3f();

	// handy data structures for collision detection
	private Vector3f collideCorrection = new Vector3f();

	private BoundingCuboid playerBounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private BoundingCuboid blockBounds = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private BoundingCuboid intersection = new BoundingCuboid( 0, 0, 0, 0, 0, 0 );

	private Vector3f forward = new Vector3f();

	/**
	 * Items in hotbar
	 */
	public Item[] hotbar = new Item[ 9 ];

	/**
	 * Item in hand
	 */
	public Item inHand = null;

	/***/
	public TapPad.Listener jumpCrouchListener = new TapPad.Listener() {
		@Override
		public void onTap( TapPad pad )
		{
			if( crouched )
			{
				crouched = false;
			}
			else if( onGround )
			{
				velocity.y = jumpSpeed;
			}
		}

		@Override
		public void onFlick( TapPad pad, int horizontal, int vertical )
		{
			if( vertical == 1 )
			{
				onTap( pad );
			}
			else if( vertical == -1 )
			{
				crouched = true;
			}
		}

		@Override
		public void onLongPress( TapPad pad )
		{
			crouched = true;
		}
	};

	/**
	 * @param world
	 */
	public Player( World world )
	{
		this.world = world;
		resetLocation();
	}

	/***/
	@Variable( "Reset location" )
	@Summary( "Lost? Go back to your starting location" )
	public void resetLocation()
	{
		position.set( world.startPosition );
		velocity.set( 0, 0, 0 );
	}

	/**
	 * @param delta
	 * @param cam
	 * @param gui
	 */
	public void advance( float delta, FPSCamera cam, GUI gui )
	{
		forward.set( cam.forward );

		float s = crouched ? crouchedSpeed : speed;

		if( ghost )
		{
			position.x += gui.left.y * delta * forward.x * s;
			position.z += gui.left.y * delta * forward.z * s;

			position.x += -gui.left.x * delta * cam.right.x * s;
			position.z += -gui.left.x * delta * cam.right.z * s;

			position.y += gui.left.y * delta * cam.forward.y * s;
			position.y += -gui.left.x * delta * cam.right.y * s;

			velocity.y = 0;
		}
		else
		{
			// make sure the chunk we're in is loaded first
			if( world.getChunklet( position.x, position.y, position.z ) != null )
			{
				// we still walk forward at top speed, even if we're
				// looking
				// at the floor
				forward.y = 0;
				forward.normalise();

				position.x += gui.left.y * delta * forward.x * s;
				position.z += gui.left.y * delta * forward.z * s;

				position.x += -gui.left.x * delta * cam.right.x * s;
				position.z += -gui.left.x * delta * cam.right.z * s;

				// gravity
				velocity.y += gravity * delta;
				position.y += velocity.y * delta;

				// world collide
				float w = width / 2;
				float feet = height * ( crouched ? crouchedEyeLevel : eyeLevel );
				float head = height - feet;
				playerBounds.set( position.x - w, position.y - feet, position.z - w,
						position.x + w, position.y + head, position.z + w );

				boolean groundHit = false;

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

							if( collideCorrection.y != 0
									&& Math.signum( collideCorrection.y ) != Math
											.signum( velocity.y ) )
							{ // hit the ground or roof
								velocity.y = 0;
							}

							groundHit |= collideCorrection.y > 0;
						}
					}
				}

				onGround = groundHit;
			}
		}
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
}
