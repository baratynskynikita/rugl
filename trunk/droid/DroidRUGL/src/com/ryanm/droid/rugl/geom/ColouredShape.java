
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.Matrix4f;

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

	private void sanity() throws IllegalArgumentException
	{
		if( colours.length != vertexCount() )
		{
			throw new IllegalArgumentException( "Colour count mismatch\n" + toString() );
		}
	}

	/**
	 * @param s
	 *           The geometry to share
	 * @param colour
	 *           The colour for every vertex to share, or null for
	 *           white
	 * @param state
	 *           The render state to share, or null for typical state.
	 *           See {@link GLUtil#typicalState}
	 */
	public ColouredShape( Shape s, int colour, State state )
	{
		this( s, ShapeUtil.expand( colour, s.vertexCount() ), state );
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

		sanity();
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
		state = renderer.intern( state );
		renderer.addGeometry( vertices, null, colours, indices, state );
	}

	@Override
	public ColouredShape clone()
	{
		return new ColouredShape( super.clone(), colours.clone(), state );
	}

	@Override
	public int bytes()
	{
		return super.bytes() + colours.length * 4;
	}

	@Override
	public ColouredShape transform( Matrix4f m )
	{
		super.transform( m );
		return this;
	}

	@Override
	public ColouredShape translate( float x, float y, float z )
	{
		super.translate( x, y, z );
		return this;
	}

	@Override
	public ColouredShape scale( float x, float y, float z )
	{
		super.scale( x, y, z );
		return this;
	}

	@Override
	public ColouredShape rotate( float angle, float ax, float ay, float az )
	{
		super.rotate( angle, ax, ay, az );
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( super.toString() );

		if( colours != null )
		{ // this can get called through the super-constructor, so need
			// to check if colours are there yet
			buff.append( "\ncolours" );
			for( int i = 0; i < colours.length; i++ )
			{
				buff.append( "\n\t" );
				buff.append( Colour.toString( colours[ i ] ) );
			}
		}

		return buff.toString();
	}
}
