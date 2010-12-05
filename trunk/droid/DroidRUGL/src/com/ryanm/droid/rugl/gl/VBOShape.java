
package com.ryanm.droid.rugl.gl;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES10;
import android.opengl.GLES11;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.util.FastFloatBuffer;

/**
 * @author ryanm
 */
public class VBOShape
{
	/**
	 * Increment this to indicate that VBO state may have been
	 * invalidated, i.e. in
	 * {@link Game#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)}
	 * This should cause all {@link VBOShape}s to re-upload their data
	 */
	public static int contextID = 0;

	/***/
	public State state;

	/**
	 * The {@link #contextID} when the data was uploaded. If this is
	 * ever different from {@link #contextID}, we know that the VBOs
	 * may have been squelched
	 */
	private int uploadedContextID;

	private int vertBufferID = -1;

	private final FastFloatBuffer vertBuffer;

	private int colourBufferID = -1;

	private final IntBuffer colourBuffer;

	private int texCoordBufferID = -1;

	private final FastFloatBuffer texCoordBuffer;

	private int indexBufferID = -1;

	private final ShortBuffer indexBuffer;

	private final int vertexCount;

	private final int triangleIndexCount;

	/**
	 * @param ts
	 */
	public VBOShape( TexturedShape ts )
	{
		state = ts.state;
		vertexCount = ts.vertexCount();
		triangleIndexCount = ts.triangles.length;

		vertBuffer = new FastFloatBuffer( ts.vertices.length );
		vertBuffer.put( ts.vertices );
		vertBuffer.flip();

		colourBuffer = BufferUtils.createIntBuffer( ts.colours.length );
		colourBuffer.put( ts.colours );
		colourBuffer.flip();

		texCoordBuffer = new FastFloatBuffer( ts.texCoords.length );
		texCoordBuffer.put( ts.getTextureCoords() );
		texCoordBuffer.flip();

		indexBuffer = BufferUtils.createShortBuffer( ts.triangles.length );
		indexBuffer.put( ts.triangles );
		indexBuffer.flip();
	}

	/**
	 * @param cs
	 */
	public VBOShape( ColouredShape cs )
	{
		state = cs.state;
		vertexCount = cs.vertexCount();
		triangleIndexCount = cs.triangles.length;

		vertBuffer = new FastFloatBuffer( cs.vertices.length );
		vertBuffer.put( cs.vertices );
		vertBuffer.flip();

		colourBuffer = BufferUtils.createIntBuffer( cs.colours.length );
		colourBuffer.put( cs.colours );
		colourBuffer.flip();

		texCoordBuffer = new FastFloatBuffer( 2 * vertexCount );

		indexBuffer = BufferUtils.createShortBuffer( cs.triangles.length );
		indexBuffer.put( cs.triangles );
		indexBuffer.flip();
	}

	/***/
	public void draw()
	{
		if( uploadedContextID != contextID )
		{ // the context may have changed - we need to refresh our
			// buffer handles
			IntBuffer ib = GLUtil.intScratch( 4 );
			ib.put( 0, vertBufferID );
			ib.put( 1, colourBufferID );
			ib.put( 2, texCoordBufferID );
			ib.put( 3, indexBufferID );
			GLES11.glDeleteBuffers( 4, ib );

			vertBufferID = -1;
			colourBufferID = -1;
			texCoordBufferID = -1;
			indexBufferID = -1;
		}

		if( vertBufferID == -1 )
		{
			IntBuffer ib = GLUtil.intScratch( 4 );
			GLES11.glGenBuffers( 4, ib );
			vertBufferID = ib.get( 0 );
			colourBufferID = ib.get( 1 );
			texCoordBufferID = ib.get( 2 );
			indexBufferID = ib.get( 3 );

			GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, vertBufferID );
			GLES11.glBufferData( GLES11.GL_ARRAY_BUFFER, vertexCount * 3 * 4,
					vertBuffer.bytes, GLES11.GL_STATIC_DRAW );

			GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, colourBufferID );
			GLES11.glBufferData( GLES11.GL_ARRAY_BUFFER, vertexCount * 4, colourBuffer,
					GLES11.GL_STATIC_DRAW );

			GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, texCoordBufferID );
			GLES11.glBufferData( GLES11.GL_ARRAY_BUFFER, vertexCount * 2 * 4,
					texCoordBuffer.bytes, GLES11.GL_STATIC_DRAW );

			GLES11.glBindBuffer( GLES11.GL_ELEMENT_ARRAY_BUFFER, indexBufferID );
			GLES11.glBufferData( GLES11.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.limit() * 2,
					indexBuffer, GLES11.GL_STATIC_DRAW );

			GLUtil.checkGLError();

			uploadedContextID = contextID;
		}

		state.apply();

		GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, vertBufferID );
		GLES11.glVertexPointer( 3, GLES10.GL_FLOAT, 0, 0 );

		GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, colourBufferID );
		GLES11.glColorPointer( 4, GLES10.GL_UNSIGNED_BYTE, 0, 0 );

		GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, texCoordBufferID );
		GLES11.glTexCoordPointer( 2, GLES10.GL_FLOAT, 0, 0 );

		GLES11.glBindBuffer( GLES11.GL_ELEMENT_ARRAY_BUFFER, indexBufferID );
		GLES11.glDrawElements( GLES10.GL_TRIANGLES, triangleIndexCount,
				GLES10.GL_UNSIGNED_SHORT, 0 );

		GLES11.glBindBuffer( GLES11.GL_ARRAY_BUFFER, 0 );
		GLES11.glBindBuffer( GLES11.GL_ELEMENT_ARRAY_BUFFER, 0 );
	}

	/***/
	public void delete()
	{
		if( vertBufferID != -1 )
		{
			IntBuffer ib = GLUtil.intScratch( 4 );
			ib.put( 0, vertBufferID );
			ib.put( 1, colourBufferID );
			ib.put( 2, texCoordBufferID );
			ib.put( 3, indexBufferID );
			GLES11.glDeleteBuffers( 4, ib );

			vertBufferID = -1;
			colourBufferID = -1;
			texCoordBufferID = -1;
			indexBufferID = -1;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buff =
				new StringBuilder( "VBOShape " + vertexCount + " verts\n\tbufferIDs: v="
						+ vertBufferID + " c=" + colourBufferID + " tx=" + texCoordBufferID
						+ " i=" + indexBufferID + "\n" + state );

		return buff.toString();
	}
}
