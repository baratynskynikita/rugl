
package com.rugl.gl.shader;

import org.lwjgl.opengl.GL20;

/**
 * GLSL 'Samplers' allow vertex or fragment shaders to sample values
 * from a texture. What we actually do is bind to an integer uniform
 * location. Then we poke the number of the texture unit we want this
 * sampler associated with into that location. Remember that this
 * class only takes care of the binding of a texture unit to a shader
 * sampler. Therefore you have to load in the textures and enable/bind
 * them to traditional multitexturing units as before.
 * 
 * @author John Campbell
 */
public class UniformSampler2D
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	private int boundTextureUnit;

	/**
	 * @param program
	 * @param name
	 * @param location
	 */
	protected UniformSampler2D( Program program, String name, int location )
	{
		this.program = program;

		this.name = name;
		this.location = location;
	}

	/**
	 * Specify what texture unit we want this sampler to be associated
	 * with.
	 * 
	 * @param textureUnit
	 */
	public void set( int textureUnit )
	{
		if( boundTextureUnit != textureUnit )
		{
			GL20.glUniform1i( textureUnit, textureUnit );
			boundTextureUnit = textureUnit;
		}
	}
}
