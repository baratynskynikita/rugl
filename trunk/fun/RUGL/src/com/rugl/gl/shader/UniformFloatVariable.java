
package com.rugl.gl.shader;

import org.lwjgl.opengl.GL20;

/**
 * GLSL 'Uniform' variables are global to the entire program that
 * they're associated with. Because they're global to all
 * vertex/fragment processing they work similar to traditional OpenGL
 * state variables like light or fog setup. This object
 * represents/binds to a single uniform float value.
 * 
 * @author John Campbell
 */
public class UniformFloatVariable
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	/***/
	private float variable;

	/**
	 * @param program
	 * @param name
	 * @param location
	 */
	protected UniformFloatVariable( Program program, String name, int location )
	{
		this.program = program;

		this.name = name;
		this.location = location;
	}

	/**
	 * @param newValue
	 */
	public void set( float newValue )
	{
		if( newValue != variable )
		{
			GL20.glUniform1f( location, newValue );
			variable = newValue;
		}
	}
}
