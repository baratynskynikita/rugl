
package com.rugl.gl.shader;

import org.lwjgl.opengl.GL20;

/**
 * A uniform variable with three float components.
 * 
 * @author John Campbell
 */
public class UniformVec3Variable
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	/***/
	private float var0, var1, var2;

	/**
	 * @param program
	 * @param name
	 * @param location
	 */
	protected UniformVec3Variable( Program program, String name, int location )
	{
		this.program = program;

		this.name = name;
		this.location = location;
	}

	/**
	 * @param new0
	 * @param new1
	 * @param new2
	 */
	public void set( float new0, float new1, float new2 )
	{
		if( var0 != new0 || var1 != new1 || var2 != new2 )
		{
			GL20.glUniform3f( location, new0, new1, new2 );

			var0 = new0;
			var1 = new1;
			var2 = new2;
		}
	}
}
