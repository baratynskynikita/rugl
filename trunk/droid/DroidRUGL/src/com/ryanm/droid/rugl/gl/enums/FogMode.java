
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * Fog modes
 * 
 * @author ryanm
 */
public enum FogMode
{
	/**
	 * f= (end-z)/(end-start)
	 */
	LINEAR( GLES10.GL_LINEAR ),
	/**
	 * f= e^(-(density-z))
	 */
	EXP( GLES10.GL_EXP ),
	/**
	 * f= e^(-(density-z)^2)
	 */
	EXP2( GLES10.GL_EXP2 );

	/**
	 * GL mode value
	 */
	public final int mode;

	private FogMode( int mode )
	{
		this.mode = mode;
	}
}
