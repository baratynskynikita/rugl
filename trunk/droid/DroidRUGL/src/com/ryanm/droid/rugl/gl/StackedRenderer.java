
package com.ryanm.droid.rugl.gl;

import android.opengl.Matrix;

/**
 * Adds opengl-style matrix operations
 * 
 * @author ryanm
 */
public class StackedRenderer extends Renderer
{
	/**
	 * Offset of where to push the current transform array
	 */
	private int pushOffset = 0;

	private float[] stack = new float[ 5 * 16 ];

	private int pushCount = 0;

	private int popCount = 0;

	/**
	 * Pops the current transform from the top of the stack
	 */
	public void popMatrix()
	{
		pushOffset -= 16;
		System.arraycopy( stack, pushOffset, transform, 0, 16 );

		popCount++;
	}

	/**
	 * Pushes the current transform onto the stack
	 */
	public void pushMatrix()
	{
		if( pushOffset + 16 >= stack.length )
		{
			float[] ns = new float[ stack.length * 2 ];
			System.arraycopy( stack, 0, ns, 0, pushOffset );
		}

		System.arraycopy( transform, 0, stack, pushOffset, 16 );
		pushOffset += 16;

		pushCount++;
	}

	/**
	 * Sets the current transform to the identity
	 */
	public void loadIdentity()
	{
		Matrix.setIdentityM( transform, 0 );
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
		Matrix.translateM( transform, 0, x, y, z );
	}

	/**
	 * Rotates the current transform
	 * 
	 * @param angle
	 *           The rotation angle, in degrees
	 * @param x
	 *           axis component
	 * @param y
	 *           axis component
	 * @param z
	 *           axis component
	 */
	public void rotate( float angle, float x, float y, float z )
	{
		Matrix.rotateM( transform, 0, angle, x, y, z );
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
		Matrix.scaleM( transform, 0, x, y, z );
	}

	@Override
	public void render()
	{
		super.render();

		if( pushCount != popCount )
		{
			throw new RuntimeException( "pushed " + pushCount + " and popped " + popCount
					+ " matrices between calls to render()" );
		}

		pushCount = 0;
		popCount = 0;
	}
}
