
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * Comparison functions, used for alpha and depth tests
 * 
 * @author ryanm
 */
public enum ComparisonFunction
{
	/**
	 * Never passes
	 */
	NEVER( GLES10.GL_NEVER ),
	/**
	 * Passes if the incoming value is less than the reference value.
	 */
	LESS( GLES10.GL_LESS ),
	/**
	 * Passes if the incoming value is equal to the reference value.
	 */
	EQUAL( GLES10.GL_EQUAL ),
	/**
	 * Passes if the incoming value is less than or equal to the
	 * reference value.
	 */
	LEQUAL( GLES10.GL_LEQUAL ),
	/**
	 * Passes if the incoming value is greater than the reference
	 * value.
	 */
	GREATER( GLES10.GL_GREATER ),
	/**
	 * Passes if the incoming value is not equal to the reference
	 * value.
	 */
	NOTEQUAL( GLES10.GL_NOTEQUAL ),
	/**
	 * Passes if the incoming value is greater than or equal to the
	 * reference value.
	 */
	GEQUAL( GLES10.GL_GEQUAL ),
	/**
	 * Always passes.
	 */
	ALWAYS( GLES10.GL_ALWAYS );

	/**
	 * The value of the OpenGL constant
	 */
	public final int value;

	private ComparisonFunction( int value )
	{
		this.value = value;
	}
}