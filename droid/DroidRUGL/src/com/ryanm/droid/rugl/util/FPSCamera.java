
package com.ryanm.droid.rugl.util;

import android.opengl.GLES10;
import android.util.Log;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.gl.GLU;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Matrix4f;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.droid.rugl.util.geom.Vector4f;
import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.preflect.annote.Category;
import com.ryanm.preflect.annote.Order;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;

/**
 * A camera suitable for first-person stuff, assumes that the x-z
 * plane is horizontal
 * 
 * @author ryanm
 */
@Variable( "Camera" )
@Summary( "View and steering options" )
public class FPSCamera
{
	private final Vector3f position = new Vector3f();

	/**
	 * Invert mode - i.e.: pull down to look up
	 */
	@Variable( "Invert Y" )
	@Summary( "Invert mode FTW!" )
	@Order( 0 )
	public boolean invert = true;

	/**
	 * The forward vector
	 */
	public final Vector3f forward = new Vector3f();

	/**
	 * The up vector
	 */
	public final Vector3f up = new Vector3f();

	/**
	 * The right vector
	 */
	public final Vector3f right = new Vector3f();

	private final Matrix4f m = new Matrix4f();

	private Vector4f v4f = new Vector4f();

	/***/
	public float elevation = 0;

	/***/
	public float heading = 0;

	/***/
	@Variable( "Horizontal" )
	@Summary( "in degrees per second" )
	@Category( "Turn speed" )
	public float headingSpeed = 90;

	/***/
	@Variable( "Vertical" )
	@Summary( "in degrees per second" )
	@Category( "Turn speed" )
	public float pitchSpeed = 90;

	/**
	 * Aspect ratio of projection. Set to -1 to have it automatically
	 * update itself according to screen dimensions
	 */
	@Variable( "Aspect ratio" )
	@Summary( "width / height ratio\n<0 to set from screen size" )
	@Category( "Frustum control" )
	public float aspect = -1;

	/**
	 * Field of view, in degrees
	 */
	@Variable( "Field of vertical view" )
	@Summary( "In degrees" )
	@Category( "Frustum control" )
	public float fov = 70;

	/**
	 * Distance to near clipping plane
	 */
	@Variable( "Near" )
	@Summary( "Distance to near clip plane" )
	@Category( "Frustum control" )
	public float near = 0.01f;

	/**
	 * Distance to far clipping plane
	 */
	@Variable( "Far" )
	@Summary( "Distance to far clip plane" )
	@Category( "Frustum control" )
	public float far = 100f;

	/**
	 * Handy for culling purposes
	 */
	private final Frustum frustum = new Frustum();

	private boolean frustumDirty = true;

	private float[] projectionMatrix = new float[ 16 ];

	private float[] modelViewMatrix = new float[ 16 ];

	/**
	 * @param delta
	 *           time delta
	 * @param x
	 *           yaw control, in range -1 to 1
	 * @param y
	 *           pitch control, in range -1 to 1
	 */
	public void advance( float delta, float x, float y )
	{
		heading += -x * Math.toRadians( headingSpeed ) * delta;
		elevation += ( invert ? 1 : -1 ) * y * Math.toRadians( pitchSpeed ) * delta;
		heading = Range.wrap( heading, 0, Trig.TWO_PI );
		elevation = Range.limit( elevation, -Trig.HALF_PI * 0.99f, Trig.HALF_PI * 0.99f );

		m.setIdentity();
		m.rotate( heading, 0, 1, 0 );
		m.rotate( elevation, 1, 0, 0 );
		v4f.set( 0, 0, 1, 0 );
		Matrix4f.transform( m, v4f, v4f );

		forward.set( v4f.x, v4f.y, v4f.z );

		// right vector...
		right.set( forward.z, 0, -forward.x );
		right.normalise();

		// up is target x right
		Vector3f.cross( forward, right, up );

		if( x != 0 || y != 0 )
		{
			frustumDirty = true;
		}
	}

	/**
	 * Moves and applies the camera
	 * 
	 * @param eyeX
	 * @param eyeY
	 * @param eyeZ
	 */
	public void setPosition( float eyeX, float eyeY, float eyeZ )
	{
		if( position.x != eyeX || position.y != eyeY || position.z != eyeZ )
		{
			frustumDirty = true;
		}

		if( aspect == -1 )
		{
			aspect = ( float ) Game.width / Game.height;
		}

		GLES10.glMatrixMode( GLES10.GL_PROJECTION );
		GLES10.glLoadIdentity();
		GLU.gluPerspective( fov, aspect, near, far, projectionMatrix );

		GLES10.glMatrixMode( GLES10.GL_MODELVIEW );
		GLES10.glLoadIdentity();

		GLU.gluLookAt( eyeX, eyeY, eyeZ, eyeX + forward.x, eyeY + forward.y, eyeZ
				+ forward.z, up.x, up.y, up.z, modelViewMatrix );
	}

	/**
	 * Updates the {@link Frustum} as necessary, so call this every
	 * frame
	 * 
	 * @return The camera {@link Frustum}
	 */
	public Frustum getFrustum()
	{
		if( frustumDirty )
		{
			frustum.update( projectionMatrix, modelViewMatrix );
			frustumDirty = false;
		}

		return frustum;
	}

	/**
	 * @param x
	 *           x coordinate of point, in range -1 (for extreme left
	 *           of view) to 1 (extreme right)
	 * @param y
	 *           y coordinate of point, in range -1 (for bottom of
	 *           view) to 1 (top of view)
	 * @param dest
	 *           destination vector to store the result, or null for a
	 *           new {@link Vector3f}
	 * @return A unit vector pointing in the tapped direction
	 */
	public Vector3f unProject( float x, float y, Vector3f dest )
	{
		Log.i( Game.RUGL_TAG, "-" );
		Log.i( Game.RUGL_TAG, "unProject " + x + ", " + y );

		v4f.set( forward.x, forward.y, forward.z, 0 );

		Log.i( Game.RUGL_TAG, v4f.toString() );

		float yAngle = -y * fov / 2;
		float xAngle = -x * aspect * fov / 2;

		Log.i( Game.RUGL_TAG, xAngle + ", " + yAngle );

		m.setIdentity();
		m.rotate( Trig.toRadians( yAngle ), right.x, right.y, right.z );
		m.rotate( Trig.toRadians( xAngle ), 0, 1, 0 );

		Matrix4f.transform( m, v4f, v4f );

		if( dest == null )
		{
			dest = new Vector3f();
		}

		dest.set( v4f );

		Log.i( Game.RUGL_TAG, dest.toString() );

		return dest;
	}

	@Override
	public String toString()
	{
		return "e = " + elevation + "\nh = " + heading + "\nf = " + forward + "\nu = " + up
				+ "\nr = " + right;
	}
}
