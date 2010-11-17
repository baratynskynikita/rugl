
package com.rugl.gl.enums;

import org.lwjgl.opengl.GL11;

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
	NEVER( GL11.GL_NEVER ),
	/**
	 * Passes if the incoming value is less than the reference value.
	 */
	LESS( GL11.GL_LESS ),
	/**
	 * Passes if the incoming value is equal to the reference value.
	 */
	EQUAL( GL11.GL_EQUAL ),
	/**
	 * Passes if the incoming value is less than or equal to the
	 * reference value.
	 */
	LEQUAL( GL11.GL_LEQUAL ),
	/**
	 * Passes if the incoming value is greater than the reference
	 * value.
	 */
	GREATER( GL11.GL_GREATER ),
	/**
	 * Passes if the incoming value is not equal to the reference
	 * value.
	 */
	NOTEQUAL( GL11.GL_NOTEQUAL ),
	/**
	 * Passes if the incoming value is greater than or equal to the
	 * reference value.
	 */
	GEQUAL( GL11.GL_GEQUAL ),
	/**
	 * Always passes.
	 */
	ALWAYS( GL11.GL_ALWAYS );

	/**
	 * The value of the OpenGL constant
	 */
	public final int value;

	private ComparisonFunction( int value )
	{
		this.value = value;
	}
}