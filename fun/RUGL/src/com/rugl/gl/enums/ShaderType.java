
package com.rugl.gl.enums;

import org.lwjgl.opengl.GL20;

/**
 * Shader object types
 * 
 * @author ryanm
 */
public enum ShaderType
{
	/***/
	Vertex( GL20.GL_VERTEX_SHADER ),

	/***/
	Fragment( GL20.GL_FRAGMENT_SHADER );

	/**
	 * Type flag to specify when creating the shader object
	 */
	public final int glFlag;

	private ShaderType( int t )
	{
		glFlag = t;
	}
}
