/*
 * Copyright (c) 2007, Ryan McNally All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <ORGANIZATION> nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.ryanm.droid.rugl.gl;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.opengl.GLES10;
import android.opengl.Matrix;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.CompiledShape;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.util.FastFloatBuffer;
import com.ryanm.droid.rugl.util.geom.MatrixUtils;

/**
 * Batches up triangles so as to avoid expensive state changes
 * 
 * @author ryanm
 */
public class Renderer
{
	/**
	 * Holds vertices
	 */
	private FastFloatBuffer vertices;

	/**
	 * Holds texture coordinates
	 */
	private FastFloatBuffer texCoords;

	/**
	 * Holds vertex colours
	 */
	public IntBuffer colours;

	/**
	 * Holds triangle indices, before rendering
	 */
	private ShortBuffer tris;

	/**
	 * Used to transform vertices before being rendered
	 */
	protected final float[] transform = new float[ 16 ];

	private float[] t = new float[] { 0, 0, 0, 1 };

	/**
	 * Array of triangle lists, indexed by compiled
	 * {@link State#getCompiledIndex()}
	 */
	private IndexList[] indices = new IndexList[ 0 ];

	/**
	 * A list of states that have been used with this renderer
	 */
	private List<State> interned = new ArrayList<State>();

	/**
	 * Incremented every time the interned list is altered
	 */
	private static int internedListVersion = 0;

	private int vertexCount = 0;

	private int triangleCount = 0;

	/**
	 * When <code>true</code>, {@link #clear()} will be called for you
	 * at the end of {@link #render()}
	 */
	public boolean automaticallyClear = true;

	/**
	 * Builds a new Renderer that can handle 1000 vertices at the
	 * outset. The buffers will grow as needed to accommodate more
	 * vertices.
	 */
	public Renderer()
	{
		this( 1000 );
	}

	/**
	 * Builds a new Renderer
	 * 
	 * @param verts
	 *           The initial number of vertices expected
	 */
	public Renderer( int verts )
	{
		vertices = new FastFloatBuffer( verts * 3 );
		texCoords = new FastFloatBuffer( verts * 2 );
		colours = BufferUtils.createIntBuffer( verts );

		Matrix.setIdentityM( transform, 0 );
	}

	/**
	 * Compares this state to the internal pool, and returns an
	 * equivalent state if one exists, or else adds it to the pool.
	 * It's good practice to do this for all new states. This is done
	 * for you in {@link ColouredShape#render(Renderer)},
	 * {@link TexturedShape#render(Renderer)} and
	 * {@link CompiledShape#render(Renderer)}
	 * 
	 * @param s
	 *           The state to add to the pool
	 * @return The same state, or the pooled equivalent
	 */
	public State intern( State s )
	{
		if( indices.length > 0
				&& indices[ 0 ].state.getCompilationBatch() == s.getCompilationBatch() )
		{
			// the state has been compiled in the same batch as our pool,
			// it must already be in there
			return s;
		}

		int index = Collections.binarySearch( interned, s );
		if( index >= 0 )
		{
			// there is an equivalent state in the pool
			return interned.get( index );
		}
		else
		{
			// no equivalent, add it to the pool
			index += 1;
			index = -index;
			interned.add( index, s );
			internedListVersion++;

			for( int i = 0; i < interned.size(); i++ )
			{
				State t = interned.get( i );
				t.compiledIndex = i;
				t.compilationBatch = internedListVersion;
			}

			// redo the triangle lists
			IndexList[] newTL = new IndexList[ interned.size() ];
			newTL[ s.getCompiledIndex() ] = new IndexList( s );
			for( int i = 0; i < indices.length; i++ )
			{
				newTL[ indices[ i ].state.getCompiledIndex() ] = indices[ i ];
			}

			indices = newTL;

			return s;
		}
	}

	/**
	 * Adds a set of triangles to be rendered
	 * 
	 * @param verts
	 *           The vertex coordinates, in [x1,y1,z1,x2,y2,z2...]
	 *           form.
	 * @param textureCoordinates
	 *           The texture coordinates, in [s1,t1,s2,t2,...] form.
	 *           Can be null, for (0,0) coords
	 * @param vertexColours
	 *           The vertex colours. Can be null for all-white
	 * @param geomIndices
	 *           The indices of geometry primitives.
	 * @param state
	 *           The rendering state for this set of triangles. Can be
	 *           null for the default state
	 */
	public void addGeometry( float[] verts, float[] textureCoordinates,
			int[] vertexColours, short[] geomIndices, State state )
	{
		int vc = verts.length / 3;

		if( state == null )
		{
			state = GLUtil.typicalState;
		}

		state = intern( state );

		short indexOffset = ( short ) ( vertices.position() / 3 );

		while( vertices.remaining() < verts.length )
		{ // need to get bigger buffers
			growArrays();
		}

		boolean doTransform = !MatrixUtils.isIdentity( transform );

		// write vertex data to buffers
		if( !doTransform )
		{
			vertices.put( verts );
		}
		else
		{
			for( int i = 0; i < vc; i++ )
			{
				System.arraycopy( verts, 3 * i, t, 0, 3 );
				t[ 3 ] = 1;
				Matrix.multiplyMV( t, 0, transform, 0, t, 0 );
				vertices.put( t[ 0 ] );
				vertices.put( t[ 1 ] );
				vertices.put( t[ 2 ] );
			}
		}

		colours.put( vertexColours );

		if( textureCoordinates != null )
		{
			texCoords.put( textureCoordinates );
		}
		else
		{
			texCoords.position( texCoords.position() + 2 * vc );
		}

		// insert triangle indices
		indices[ state.getCompiledIndex() ].add( geomIndices, indexOffset );
	}

	/**
	 * Adds a set of triangles to be rendered. For use with
	 * {@link FastFloatBuffer#convert(float...)}-ed vertex and texcoord
	 * data - note that having a non-identity transform will ruin any
	 * performance benefit
	 * 
	 * @param verts
	 *           The vertex coordinates, in [x1,y1,z1,x2,y2,z2...]
	 *           form.
	 * @param textureCoordinates
	 *           The texture coordinates, in [s1,t1,s2,t2,...] form.
	 *           Can be null, for (0,0) coords
	 * @param vertexColours
	 *           The vertex colours. Can be null for all-white
	 * @param geomIndices
	 *           The indices of geometry primitives.
	 * @param state
	 *           The rendering state for this set of triangles. Can be
	 *           null for the default state
	 */
	public void addGeometry( int[] verts, int[] textureCoordinates, int[] vertexColours,
			short[] geomIndices, State state )
	{
		int vc = verts.length / 3;

		if( state == null )
		{
			state = GLUtil.typicalState;
		}

		state = intern( state );

		short indexOffset = ( short ) ( vertices.position() / 3 );

		while( vertices.remaining() < verts.length )
		{ // need to get bigger buffers
			growArrays();
		}

		boolean doTransform = !MatrixUtils.isIdentity( transform );

		// write vertex data to buffers
		if( !doTransform )
		{
			vertices.put( verts );
		}
		else
		{
			for( int i = 0; i < vc; i++ )
			{
				t[ 0 ] = Float.intBitsToFloat( verts[ 3 * i ] );
				t[ 1 ] = Float.intBitsToFloat( verts[ 3 * i + 1 ] );
				t[ 2 ] = Float.intBitsToFloat( verts[ 3 * i + 2 ] );
				t[ 3 ] = 1;

				Matrix.multiplyMV( t, 0, transform, 0, t, 0 );

				vertices.put( t[ 0 ] );
				vertices.put( t[ 1 ] );
				vertices.put( t[ 2 ] );
			}
		}

		colours.put( vertexColours );

		if( textureCoordinates != null )
		{
			texCoords.put( textureCoordinates );
		}
		else
		{
			texCoords.position( texCoords.position() + 2 * vc );
		}

		// insert triangle indices
		indices[ state.getCompiledIndex() ].add( geomIndices, indexOffset );
	}

	/**
	 * Doubles the number of vertices that can be handled by this
	 * renderer
	 */
	private void growArrays()
	{
		FastFloatBuffer nv = new FastFloatBuffer( vertices.capacity() * 2 );
		vertices.flip();
		nv.put( vertices );
		vertices = nv;

		FastFloatBuffer ntc = new FastFloatBuffer( texCoords.capacity() * 2 );
		texCoords.flip();
		ntc.put( texCoords );
		texCoords = ntc;

		IntBuffer bc = BufferUtils.createIntBuffer( colours.capacity() * 2 );
		colours.flip();
		bc.put( colours );
		colours = bc;
	}

	/**
	 * Render the triangles and optionally {@link #clear()} the data
	 * 
	 * @see #automaticallyClear
	 */
	public void render()
	{
		// enable the buffers for openGL
		vertices.flip();
		texCoords.flip();
		colours.flip();

		vertexCount = vertices.limit() / 3;
		triangleCount = 0;

		GLES10.glVertexPointer( 3, GLES10.GL_FLOAT, 0, vertices.bytes );
		GLES10.glColorPointer( 4, GLES10.GL_UNSIGNED_BYTE, 0, colours );
		GLES10.glTexCoordPointer( 2, GLES10.GL_FLOAT, 0, texCoords.bytes );

		// render
		for( int i = 0; i < indices.length; i++ )
		{
			IndexList tl = indices[ i ];

			if( tl.count > 0 )
			{
				// apply the state
				tl.state.apply();

				triangleCount += tl.count;

				if( tris == null || tris.capacity() < tl.indices.length )
				{
					tris = BufferUtils.createShortBuffer( tl.indices.length );
				}

				tris.clear();

				tris.put( tl.indices );
				tris.position( tl.count );

				tris.flip();

				// render
				GLES10.glDrawElements( tl.state.drawMode.glValue, tl.count,
						GLES10.GL_UNSIGNED_SHORT, tris );
			}
		}

		triangleCount /= 3;

		if( automaticallyClear )
		{
			clear();
		}
	}

	/**
	 * Clears the data held in the buffers.
	 * 
	 * @see Renderer#automaticallyClear
	 */
	public void clear()
	{
		// clear the buffers
		vertices.clear();
		texCoords.clear();
		colours.clear();

		for( int i = 0; i < indices.length; i++ )
		{
			indices[ i ].count = 0;
		}
	}

	/**
	 * Gets the number of vertices submitted to OpenGL in the last call
	 * to {@link #render()}
	 * 
	 * @return The number of vertices rendered
	 */
	public int countVertices()
	{
		return vertexCount;
	}

	/**
	 * Gets the number of triangles rendered in the last call to
	 * {@link #render()}
	 * 
	 * @return The number of triangles rendered
	 */
	public int countTriangles()
	{
		return triangleCount;
	}

	/**
	 * Sets the transform that is applied to vertices as they are added
	 * to the renderer
	 * 
	 * @param m
	 *           The new transform, or null for no transformation
	 */
	public void setTransform( float[] m )
	{
		if( m != null )
		{
			System.arraycopy( m, 0, transform, 0, m.length );
		}
		else
		{
			Matrix.setIdentityM( transform, 0 );
		}
	}

	/**
	 * Gets the currently set transform
	 * 
	 * @return The transform
	 */
	public float[] getTransform()
	{
		return transform;
	}

	/**
	 * Keeps track of added element indices
	 * 
	 * @author ryanm
	 */
	private static class IndexList
	{
		/**
		 * The GL state for these indices
		 */
		private final State state;

		/**
		 * The number of indices
		 */
		private int count = 0;

		/**
		 * The triangle indices
		 */
		private short[] indices = new short[ 100 ];

		private IndexList( State state )
		{
			this.state = state;
		}

		private void add( short[] ti, int indexOffset )
		{
			if( count + ti.length > indices.length )
			{
				short[] nInd = new short[ count + ti.length ];
				System.arraycopy( indices, 0, nInd, 0, indices.length );
				indices = nInd;
			}

			for( int i = 0; i < ti.length; i++ )
			{
				indices[ count + i ] = ( short ) ( ti[ i ] + indexOffset );
			}

			count += ti.length;
		}
	}
}
