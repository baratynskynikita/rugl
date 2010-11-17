
package com.rugl.gl.shader;

import org.lwjgl.opengl.GL20;

/**
 * A uniform variable with two float components.
 * 
 * @see UniformFloatVariable for a more complete description of
 *      uniform vars.
 * @author John Campbell
 */
public class UniformVec2Variable
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	/***/
	private float var0, var1;

	/**
	 * @param program
	 * @param name
	 * @param location
	 */
	protected UniformVec2Variable( Program program, String name, int location )
	{
		this.program = program;

		this.name = name;
		this.location = location;
	}

	/**
	 * @param new0
	 * @param new1
	 */
	public void set( float new0, float new1 )
	{
		if( var0 != new0 || var1 != new1 )
		{
			GL20.glUniform2f( location, new0, new1 );

			var0 = new0;
			var1 = new1;
		}
	}
}
