
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
	protected final float[] correctedTexCoords;

	/**
	 * Set to true if you change the texture coordinates
	 */
	public boolean texCoordsDirty = true;

	private void sanity() throws IllegalArgumentException, IllegalStateException
	{
		if( texCoords.length != vertexCount() * 2 )
		{
			throw new IllegalArgumentException( "Texture coordinate count mismatch\n"
					+ toString() );
		}

		if( texCoords.length != correctedTexCoords.length )
		{
			throw new IllegalStateException( "wat" );
		}

		if( texCoords == correctedTexCoords )
		{
			throw new IllegalStateException( "this is a poor idea" );
		}
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

		sanity();
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
	}

	/**
	 * Gets the texture coordinates, corrected with respect to the
	 * texture's range
	 * 
	 * @return corrected texture coordinates
	 */
	public float[] getTextureCoords()
	{
		if( texCoordsDirty )
		{
			System.arraycopy( texCoords, 0, correctedTexCoords, 0, texCoords.length );

			if( texture != null )
			{
				texture.correctTexCoords( correctedTexCoords );
			}
			texCoordsDirty = false;
		}

		return correctedTexCoords;
	}

	@Override
	public void render( Renderer r )
	{
		state = texture.applyTo( state );

		r.addGeometry( vertices, getTextureCoords(), colours, indices, state );
	}

	@Override
	public int bytes()
	{
		return super.bytes() + texCoords.length * 4;
	}

	@Override
	public TexturedShape clone()
	{
		ColouredShape cs = super.clone();
		float[] ntc = texCoords.clone();

		return new TexturedShape( cs, ntc, texture );
	}
}
