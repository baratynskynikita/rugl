
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.util.FastFloatBuffer;

/**
 * A shape that cannot be altered, but that will be very quick to add
 * to a renderer
 * 
 * @author ryanm
 */
public class CompiledShape
{
	private final int[] vertices;

	private final short[] triangles;

	private final int[] colours;

	private final int[] texCoords;

	/**
	 * Rendering state
	 */
	public State state;

	/**
	 * @param ts
	 */
	public CompiledShape( TexturedShape ts )
	{
		vertices = FastFloatBuffer.convert( ts.vertices );
		triangles = ts.indices.clone();
		colours = ts.colours.clone();
		texCoords = FastFloatBuffer.convert( ts.texCoords );
		state = ts.state;
	}

	/**
	 * @param cs
	 */
	public CompiledShape( ColouredShape cs )
	{
		vertices = FastFloatBuffer.convert( cs.vertices );
		triangles = cs.indices.clone();
		colours = cs.colours.clone();
		texCoords = null;
		state = cs.state;
	}

	/**
	 * @param r
	 */
	public void render( Renderer r )
	{
		state = r.intern( state );
		r.addGeometry( vertices, texCoords, colours, triangles, state );
	}

	/**
	 * @return count of bytes used for data storage
	 */
	public int bytes()
	{
		return 4 * ( vertices.length + colours.length + texCoords.length ) * 2
				* triangles.length;
	}
}
