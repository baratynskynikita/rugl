
package com.ryanm.droid.rugl.util;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.gl.GLU;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Matrix4f;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.droid.rugl.util.geom.Vector4f;
import com.ryanm.droid.rugl.util.math.Range;

import android.opengl.GLES10;

/**
 * A camera suitable for first-person stuff, assumes that the x-z
 * plane is horizontal
 * 
 * @author ryanm
 */
public class FPSCamera
{
	/**
	 * Invert mode - i.e.: pull down to look up
	 */
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

	/**
	 * Aspect ratio of projection. Set to -1 to have it automatically
	 * update itself according to screen dimensions
	 */
	public float aspect = -1;

	/**
	 * Field of view, in degrees
	 */
	public float fov = 70;

	/**
	 * Distance to near clipping plane
	 */
	public float near = 0.01f;

	/**
	 * Distance to far clipping plane
	 */
	public float far = 100f;

	/**
	 * Handy for culling purposes
	 */
	public final Frustum frustum = new Frustum();

	private boolean frustumDirty = true;

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
		float turn = Trig.PI * 2 / 2;
		heading += -x * turn * delta;
		elevation += ( invert ? 1 : -1 ) * y * turn * delta;
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
	 * @param eyeX
	 * @param eyeY
	 * @param eyeZ
	 */
	public void apply( float eyeX, float eyeY, float eyeZ )
	{
		if( aspect == -1 )
		{
			aspect = ( float ) Game.width / Game.height;
		}

		GLES10.glMatrixMode( GLES10.GL_PROJECTION );
		GLES10.glLoadIdentity();
		GLU.gluPerspective( fov, aspect, near, far );

		GLES10.glMatrixMode( GLES10.GL_MODELVIEW );
		GLES10.glLoadIdentity();

		GLU.gluLookAt( eyeX, eyeY, eyeZ, eyeX + forward.x, eyeY + forward.y, eyeZ
				+ forward.z, up.x, up.y, up.z );

		if( frustumDirty )
		{
			frustum.extractFromOGL();
			frustumDirty = false;
		}
	}

	@Override
	public String toString()
	{
		return "e = " + elevation + "\nh = " + heading + "\nf = " + forward + "\nu = " + up
				+ "\nr = " + right;
	}
}
