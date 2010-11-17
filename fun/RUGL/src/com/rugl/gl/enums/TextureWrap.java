
package com.rugl.gl.enums;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

/**
 * Texture wrapping parameters
 * 
 * @author ryanm
 */
public enum TextureWrap
{
	/**
	 * Causes texture coordinates to be clamped to the range [0,1] and
	 * is useful for preventing wrapping artifacts when mapping a
	 * single image onto an object.
	 */
	CLAMP( GL11.GL_CLAMP ),
	/**
	 * Causes the texture coordinate to be clamped to the range [
	 * -1/2N, 1 + 1/2N ], where N is the size of the texture in the
	 * direction of clamping.
	 */
	CLAMP_TO_BORDER( GL13.GL_CLAMP_TO_BORDER ),
	/**
	 * Causes the texture coordinate to be clamped to the range [ 1/2N,
	 * 1 - 1/2N ], where N is the size of the texture in the direction
	 * of clamping.
	 */
	CLAMP_TO_EDGE( GL12.GL_CLAMP_TO_EDGE ),
	/**
	 * Causes the texture coordinate s to be set to the fractional part
	 * of the texture coordinate if the integer part of s is even; if
	 * the integer part of s is odd, then the s texture coordinate is
	 * set to 1 - frac(⁡ s ), where frac( ⁡s ) represents the
	 * fractional part of s
	 */
	MIRRORED_REPEAT( GL14.GL_MIRRORED_REPEAT ),
	/**
	 * Causes the integer part of the texture coordinate to be ignored;
	 * the GL uses only the fractional part, thereby creating a
	 * repeating pattern.
	 */
	REPEAT( GL11.GL_REPEAT );

	/***/
	public final int value;

	private TextureWrap( int value )
	{
		this.value = value;
	}
}