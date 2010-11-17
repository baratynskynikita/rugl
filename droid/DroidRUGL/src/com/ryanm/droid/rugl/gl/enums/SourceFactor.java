
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * Alpha blending source factors
 * 
 * @author ryanm
 */
public enum SourceFactor
{
	/***/
	ZERO( GLES10.GL_ZERO ),
	/***/
	ONE( GLES10.GL_ONE ),
	/***/
	DST_COLOR( GLES10.GL_DST_COLOR ),
	/***/
	ONE_MINUS_DST_COLOR( GLES10.GL_ONE_MINUS_DST_COLOR ),
	/***/
	SRC_ALPHA( GLES10.GL_SRC_ALPHA ),
	/***/
	ONE_MINUS_SRC_ALPHA( GLES10.GL_ONE_MINUS_SRC_ALPHA ),
	/***/
	DST_ALPHA( GLES10.GL_DST_ALPHA ),
	/***/
	ONE_MINUS_DST_ALPHA( GLES10.GL_ONE_MINUS_DST_ALPHA ),
	/***/
	SRC_ALPHA_SATURATE( GLES10.GL_SRC_ALPHA_SATURATE );

	/***/
	public final int value;

	private SourceFactor( int value )
	{
		this.value = value;
	}
}