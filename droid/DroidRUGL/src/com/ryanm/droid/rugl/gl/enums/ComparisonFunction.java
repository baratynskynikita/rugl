
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES11;

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
	NEVER( GLES11.GL_NEVER ),
	/**
	 * Passes if the incoming value is less than the reference value.
	 */
	LESS( GLES11.GL_LESS ),
	/**
	 * Passes if the incoming value is equal to the reference value.
	 */
	EQUAL( GLES11.GL_EQUAL ),
	/**
	 * Passes if the incoming value is less than or equal to the
	 * reference value.
	 */
	LEQUAL( GLES11.GL_LEQUAL ),
	/**
	 * Passes if the incoming value is greater than the reference
	 * value.
	 */
	GREATER( GLES11.GL_GREATER ),
	/**
	 * Passes if the incoming value is not equal to the reference
	 * value.
	 */
	NOTEQUAL( GLES11.GL_NOTEQUAL ),
	/**
	 * Passes if the incoming value is greater than or equal to the
	 * reference value.
	 */
	GEQUAL( GLES11.GL_GEQUAL ),
	/**
	 * Always passes.
	 */
	ALWAYS( GLES11.GL_ALWAYS );

	/**
	 * The value of the OpenGL constant
	 */
	public final int value;

	private ComparisonFunction( int value )
	{
		this.value = value;
	}
}