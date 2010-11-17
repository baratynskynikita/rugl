
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.texture.Texture;

/**
 * @author ryanm
 */
public class TexturedShape extends ColouredShape
{
	/**
	 * The vertices' texture coordinates in the normal 0-1 range
	 */
	public final float[] texCoords;

	/**
	 * The texture to apply to the {@link Shape}
	 */
	public final Texture texture;

	/**
	 * The texture coordinates in terms of the sub-texture range.
	 * Generated as needed
	 */
	private final float[] correctedTexCoords;

	/**
	 * Set to true if you change the texture coordinates
	 */
	public boolean texCoordsDirty = true;

	private boolean sanity()
	{
		assert texCoords.length == vertexCount() * 2;
		return true;
	}

	/**
	 * @param shape
	 * @param texCoords
	 * @param texture
	 */
	public TexturedShape( ColouredShape shape, float[] texCoords, Texture texture )
	{
		super( shape );
		this.texCoords = texCoords;
		this.texture = texture;
		correctedTexCoords = texCoords.clone();

		if( texture != null )
		{
			state = texture.applyTo( state );
		}

		assert sanity();
	}

	/**
	 * Shallow copy constructor
	 * 
	 * @param ts
	 */
	public TexturedShape( TexturedShape ts )
	{
		super( ts );
		texCoords = ts.texCoords;
		texture = ts.texture;
		correctedTexCoords = ts.correctedTexCoords;

		if( texture != null )
		{
			state = texture.applyTo( state );
		}

		assert sanity();
	}

	@Override
	public void render( Renderer r )
	{
		if( texCoordsDirty )
		{
			System.arraycopy( texCoords, 0, correctedTexCoords, 0, texCoords.length );
			texture.correctTexCoords( correctedTexCoords );
			texCoordsDirty = false;
		}
		state = texture.applyTo( state );

		r.addTriangles( vertices, correctedTexCoords, colours, triangles, state );
	}

	@Override
	public TexturedShape clone()
	{
		ColouredShape cs = super.clone();
		float[] ntc = texCoords.clone();

		return new TexturedShape( cs, ntc, texture );
	}
}
