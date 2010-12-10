
package com.ryanm.droid.rugl.gl.enums;

import android.opengl.GLES10;

/**
 * OpenGL drawing primitives
 * 
 * @author ryanm
 */
public enum DrawMode
{
	/***/
	Points( GLES10.GL_POINTS ),
	/***/
	LineStrip( GLES10.GL_LINE_STRIP ),
	/***/
	LineLoop( GLES10.GL_LINE_LOOP ),
	/***/
	Lines( GLES10.GL_LINES ),
	/***/
	TriangleStrip( GLES10.GL_TRIANGLE_STRIP ),
	/***/
	TriangleFan( GLES10.GL_TRIANGLE_FAN ),
	/***/
	Triangles( GLES10.GL_TRIANGLES );

	/**
	 * The value to pass to OpenGL
	 */
	public final int glValue;

	private DrawMode( int glValue )
	{
		this.glValue = glValue;
	}
}
