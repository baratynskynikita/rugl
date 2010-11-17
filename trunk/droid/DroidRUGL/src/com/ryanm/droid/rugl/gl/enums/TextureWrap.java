
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * Texture wrapping parameters
 * 
 * @author ryanm
 */
public enum TextureWrap
{
	/**
	 * Causes the texture coordinate to be clamped to the range [ 1/2N,
	 * 1 - 1/2N ], where N is the size of the texture in the direction
	 * of clamping.
	 */
	CLAMP_TO_EDGE( GLES10.GL_CLAMP_TO_EDGE ),
	/**
	 * Causes the integer part of the texture coordinate to be ignored;
	 * the GL uses only the fractional part, thereby creating a
	 * repeating pattern.
	 */
	REPEAT( GLES10.GL_REPEAT );

	/***/
	public final int value;

	private TextureWrap( int value )
	{
		this.value = value;
	}
}