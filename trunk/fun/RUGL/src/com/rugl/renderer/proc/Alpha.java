
package com.rugl.renderer.proc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.rugl.renderer.Renderer.Processor;
import com.rugl.util.Colour;

/**
 * Processes colours to alter the alpha component
 * 
 * @author ryanm
 */
public class Alpha implements Processor
{
	/**
	 * Multiplier for the alpha component
	 */
	public float mult;

	/**
	 * @param mult
	 *           initial value
	 */
	public Alpha( float mult )
	{
		this.mult = mult;
	}

	@Override
	public void process( FloatBuffer verts, FloatBuffer texCoords, IntBuffer colours )
	{
		if( mult != 1 )
		{
			for( int i = 0; i < colours.remaining(); i++ )
			{
				int c = colours.get( i );
				c = Colour.withAlphai( c, ( int ) ( Colour.alphai( c ) * mult ) );
				colours.put( i, c );
			}
		}
	}
}
