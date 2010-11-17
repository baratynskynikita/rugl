
package com.rugl.gl.shader;

import org.lwjgl.opengl.GL20;

/***/
public class UniformVec4Variable
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	/***/
	private float var0, var1, var2, var3;

	/**
	 * @param program
	 * @param name
	 * @param location
	 */
	protected UniformVec4Variable( Program program, String name, int location )
	{
		this.program = program;

		this.name = name;
		this.location = location;
	}

	/**
	 * @param new0
	 * @param new1
	 * @param new2
	 * @param new3
	 */
	public void set( float new0, float new1, float new2, float new3 )
	{
		if( var0 != new0 || var1 != new1 || var2 != new2 || var3 != new3 )
		{
			GL20.glUniform4f( location, new0, new1, new2, new3 );

			var0 = new0;
			var1 = new1;
			var2 = new2;
			var3 = new3;
		}
	}
}
