
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * Alpha blending destination factors
 * 
 * @author ryanm
 */
public enum DestinationFactor
{
	/***/
	ZERO( GLES10.GL_ZERO ),
	/***/
	ONE( GLES10.GL_ONE ),
	/***/
	SRC_COLOR( GLES10.GL_SRC_COLOR ),
	/***/
	ONE_MINUS_SRC_COLOR( GLES10.GL_ONE_MINUS_SRC_COLOR ),
	/***/
	SRC_ALPHA( GLES10.GL_SRC_ALPHA ),
	/***/
	ONE_MINUS_SRC_ALPHA( GLES10.GL_ONE_MINUS_SRC_ALPHA ),
	/***/
	DST_ALPHA( GLES10.GL_DST_ALPHA ),
	/***/
	ONE_MINUS_DST_ALPHA( GLES10.GL_ONE_MINUS_DST_ALPHA );

	/***/
	public final int value;

	private DestinationFactor( int value )
	{
		this.value = value;
	}
}