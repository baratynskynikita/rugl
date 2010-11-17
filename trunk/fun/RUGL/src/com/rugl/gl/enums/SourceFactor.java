
package com.rugl.gl.enums;

import org.lwjgl.opengl.GL11;

/**
 * Alpha blending source factors
 * 
 * @author ryanm
 */
public enum SourceFactor
{
	/***/
	ZERO( GL11.GL_ZERO ),
	/***/
	ONE( GL11.GL_ONE ),
	/***/
	SRC_COLOR( GL11.GL_SRC_COLOR ),
	/***/
	ONE_MINUS_SRC_COLOR( GL11.GL_ONE_MINUS_SRC_COLOR ),
	/***/
	DST_COLOR( GL11.GL_DST_COLOR ),
	/***/
	ONE_MINUS_DST_COLOR( GL11.GL_ONE_MINUS_DST_COLOR ),
	/***/
	SRC_ALPHA( GL11.GL_SRC_ALPHA ),
	/***/
	ONE_MINUS_SRC_ALPHA( GL11.GL_ONE_MINUS_SRC_ALPHA ),
	/***/
	DST_ALPHA( GL11.GL_DST_ALPHA ),
	/***/
	ONE_MINUS_DST_ALPHA( GL11.GL_ONE_MINUS_DST_ALPHA ),
	/***/
	CONSTANT_COLOR( GL11.GL_CONSTANT_COLOR ),
	/***/
	ONE_MINUS_CONSTANT_COLOR( GL11.GL_ONE_MINUS_CONSTANT_COLOR ),
	/***/
	CONSTANT_ALPHA( GL11.GL_CONSTANT_ALPHA ),
	/***/
	ONE_MINUS_CONSTANT_ALPHA( GL11.GL_ONE_MINUS_CONSTANT_ALPHA ),
	/***/
	SRC_ALPHA_SATURATE( GL11.GL_SRC_ALPHA_SATURATE );

	/***/
	public final int value;

	private SourceFactor( int value )
	{
		this.value = value;
	}
}