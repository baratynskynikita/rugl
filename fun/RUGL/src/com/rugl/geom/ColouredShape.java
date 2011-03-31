
package com.rugl.geom;

import com.rugl.gl.State;
import com.rugl.renderer.Renderer;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;

/**
 * A {@link Shape} with associated vertex colours
 * 
 * @author ryanm
 */
public class ColouredShape extends Shape
{
	/**
	 * The {@link Shape}'s colour
	 */
	public final int[] colours;

	/**
	 * The state that this shape will be rendered with
	 */
	public State state;

	private boolean sanity()
	{
		assert state != null : toString();
		assert colours.length == vertexCount() : toString();

		return true;
	}

	/**
	 * @param s
	 *           The geometry to share
	 * @param colour
	 *           The colour for every vertex to share
	 * @param state
	 *           The render state to share, or null for typical state.
	 *           See {@link GLUtil#typicalState}
	 */
	public ColouredShape( Shape s, int colour, State state )
	{
		this( s, expand( colour, s.vertexCount() ), state );
	}

	/**
	 * Shallow copy constructor
	 * 
	 * @param s
	 *           The geometry to share
	 * @param colours
	 *           The vertex colours to use
	 * @param state
	 *           The render state to share, or null for typical state.
	 *           See {@link GLUtil#typicalState}
	 */
	public ColouredShape( Shape s, int[] colours, State state )
	{
		super( s );

		this.colours = colours;

		this.state = state != null ? state : GLUtil.typicalState;

		assert sanity();
	}

	/**
	 * Shallow copy constructor
	 * 
	 * @param cs
	 */
	public ColouredShape( ColouredShape cs )
	{
		super( cs );
		colours = cs.colours;
		state = cs.state;
	}

	/**
	 * Submits the shape for rendering
	 * 
	 * @param renderer
	 */
	public void render( Renderer renderer )
	{
		renderer.addTriangles( vertices, null, colours, triangles, state );
	}

	@Override
	public ColouredShape clone()
	{
		return new ColouredShape( super.clone(), colours.clone(), state );
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( super.toString() );

		if( colours != null )
		{ // this can get called through the super-constructor, so need
			// to check if colours are there yet
			for( int i = 0; i < colours.length; i++ )
			{
				buff.append( "\n\t" ).append( Colour.toString( colours[ i ] ) );
			}
		}

		return buff.toString();
	}
}
