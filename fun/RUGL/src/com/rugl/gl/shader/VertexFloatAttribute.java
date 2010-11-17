
package com.rugl.gl.shader;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.ARBVertexProgram;

/**
 * GLSL Vertex 'Attributes' allow the app to pass custom per-vertex
 * data along with the regular position/colour/etc. data Custom vertex
 * attributes can be whatever the application (and its shader code)
 * need on a per-vertex basis. Attributes are sets of float data with
 * an app-specified size. Eg. temperature data (1f), tangent vectors
 * (3f) or quats (4f). Attributes can't have a size greater than 4
 * though ('cos the largest primative in shader code is a vec4).
 * 
 * @author John Campbell
 */
public class VertexFloatAttribute
{
	/***/
	public final String name;

	/***/
	public final int location;

	/**
	 * @param name
	 * @param location
	 */
	protected VertexFloatAttribute( String name, int location )
	{
		this.name = name;
		this.location = location;
	}

	/***/
	public void enable()
	{
		ARBVertexProgram.glEnableVertexAttribArrayARB( location );
	}

	/**
	 * Bind a buffer of vertex data for use in a vertex shader. 'size'
	 * indicates how many floats per vertex. Assumes values are tightly
	 * packed in the buffer (stride of 0).
	 * 
	 * @param vertexData
	 * @param size
	 * @param normalised
	 */
	public void bind( FloatBuffer vertexData, int size, boolean normalised )
	{
		ARBVertexProgram.glVertexAttribPointerARB( location, size, normalised, 0,
				vertexData );
	}

	/**
 * 
 */
	public void disable()
	{
		ARBVertexProgram.glDisableVertexAttribArrayARB( location );
	}
}
