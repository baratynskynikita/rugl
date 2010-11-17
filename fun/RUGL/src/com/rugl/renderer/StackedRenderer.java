
package com.rugl.renderer;

import java.util.Deque;
import java.util.LinkedList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Adds opengl-style matrix operations
 * 
 * @author ryanm
 */
public class StackedRenderer extends Renderer
{
	private Deque<Matrix4f> stack = new LinkedList<Matrix4f>();

	private static Vector3f tempVec = new Vector3f();

	/**
	 * The maximum size of the matrix stack. push/pop disparities,
	 * assertions must be enabled for effect
	 */
	public int stackLimit = 100;

	/**
	 * Pops the current transform from the top of the stack
	 */
	public void popMatrix()
	{
		transform = stack.pop();
	}

	/**
	 * Pushes the current transform onto the stack
	 */
	public void pushMatrix()
	{
		Matrix4f m;

		if( transform == null )
		{
			m = null;
		}
		else
		{
			m = new Matrix4f();
			m.load( transform );
		}

		stack.push( m );

		assert stack.size() <= stackLimit : "Stack overflow!";
	}

	/**
	 * Sets the current transform to the identity
	 */
	public void loadIdentity()
	{
		transform = null;
	}

	/**
	 * Translates the current transform
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translate( float x, float y, float z )
	{
		checkTransform();

		tempVec.set( x, y, z );
		transform.translate( tempVec );
	}

	/**
	 * Rotates the current transform
	 * 
	 * @param angle
	 *           The rotation angle, in radians
	 * @param x
	 *           axis component
	 * @param y
	 *           axis component
	 * @param z
	 *           axis component
	 */
	public void rotate( float angle, float x, float y, float z )
	{
		checkTransform();

		tempVec.set( x, y, z );
		transform.rotate( angle, tempVec );
	}

	/**
	 * Scales the current transform
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void scale( float x, float y, float z )
	{
		checkTransform();

		tempVec.set( x, y, z );
		transform.scale( tempVec );
	}

	private void checkTransform()
	{
		if( transform == null )
		{
			transform = new Matrix4f();
		}
	}
}
