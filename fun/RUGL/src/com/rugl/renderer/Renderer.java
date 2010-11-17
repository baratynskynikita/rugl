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

package com.rugl.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.rugl.gl.State;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;
import com.ryanm.util.geom.MatrixUtils;

/**
 * Batches up triangles so as to avoid expensive state changes
 * 
 * @author ryanm
 */
public class Renderer
{
	/**
	 * Holds vertex coordinates
	 */
	public FloatBuffer vertices;

	/**
	 * Holds texture coordinates
	 */
	public FloatBuffer texCoords;

	private ByteBuffer colourBytes;

	/**
	 * Holds vertex colours
	 */
	public IntBuffer colours;

	/**
	 * Used to transform vertices before being rendered
	 */
	protected Matrix4f transform;

	private Vector4f t = new Vector4f( 0, 0, 0, 1 );

	/**
	 * Holds triangle indices, before rendering
	 */
	private IntBuffer tris;

	/**
	 * Array of triangle lists, indexed by compiled
	 * {@link State#getCompiledIndex()}
	 */
	private TriangleList[] triangles = new TriangleList[ 0 ];

	private List<State> interned = new ArrayList<State>();

	private int vertexCount = 0;

	private int triangleCount = 0;

	private LinkedList<Processor> processorStack = new LinkedList<Processor>();

	/**
	 * Pushes a processor onto the top of the stack
	 * 
	 * @param p
	 */
	public void push( Processor p )
	{
		processorStack.addFirst( p );
	}

	/**
	 * Pops the processor from the top of the stack
	 */
	public void popProcessor()
	{
		processorStack.removeFirst();
	}

	/**
	 * Defines some processing that is applied to vertex data as it is
	 * added to the renderer
	 * 
	 * @author ryanm
	 */
	public interface Processor
	{
		/**
		 * Make alterations to newly added vertex data
		 * 
		 * @param verts
		 *           vertex coordinates, in x,y,z,... order
		 * @param texCoords
		 *           texture coordinates, in s,t,... order
		 * @param colours
		 *           colour components, in rgba packed int format
		 */
		public void process( FloatBuffer verts, FloatBuffer texCoords, IntBuffer colours );
	}

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
		vertices = BufferUtils.createFloatBuffer( verts * 3 );
		texCoords = BufferUtils.createFloatBuffer( verts * 2 );
		colourBytes = BufferUtils.createByteBuffer( verts * 4 );
		colours = colourBytes.asIntBuffer();
	}

	/**
	 * Compares this state to the internal pool, and returns an
	 * equivalent state if one exists, or else adds it to the pool. All
	 * pooled states are compiled together. It's good practise to do
	 * this for all new states, just don't go changing them afterwards
	 * 
	 * @param s
	 *           The state to add to the pool
	 * @return The same state, or the pooled equivalent
	 */
	public State intern( State s )
	{
		if( triangles.length > 0
				&& triangles[ 0 ].state.getCompilationBatch() == s.getCompilationBatch() )
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

			// need to recompile pooled states
			State.compile( interned.toArray( new State[ interned.size() ] ) );

			// redo the triangle lists
			TriangleList[] newTL = new TriangleList[ interned.size() ];
			newTL[ s.getCompiledIndex() ] = new TriangleList( s );
			for( int i = 0; i < triangles.length; i++ )
			{
				newTL[ triangles[ i ].state.getCompiledIndex() ] = triangles[ i ];
			}

			triangles = newTL;

			return s;
		}
	}

	/**
	 * Convenience method for rendering triangles with a single colour
	 * 
	 * @param verts
	 *           The vertex coordinates, in [x1,y1,z1,x2,y2,z2...]
	 *           form.
	 * @param textureCoordinates
	 *           The texture coordinates, in [s1,t1,s2,t2,...] form.
	 *           Can be null, for (0,0) coords
	 * @param triangleIndices
	 *           The indices of triangle vertices.
	 * @param vertexColour
	 *           The {@link Colour} for all vertices.
	 * @param state
	 *           The rendering state for this set of triangles. Can be
	 *           null for the default state
	 */
	public void addTriangles( float[] verts, float[] textureCoordinates,
			int[] triangleIndices, int vertexColour, State state )
	{
		int[] vertexColours = new int[ verts.length / 3 ];
		Arrays.fill( vertexColours, vertexColour );

		addTriangles( verts, textureCoordinates, vertexColours, triangleIndices, state );
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
	 *           The vertex {@link Colour}s.
	 * @param triangleIndices
	 *           The indices of triangle vertices.
	 * @param state
	 *           The rendering state for this set of triangles. Can be
	 *           null for the default state
	 */
	public void addTriangles( float[] verts, float[] textureCoordinates,
			int[] vertexColours, int[] triangleIndices, State state )
	{
		int vc = verts.length / 3;

		assert vertexColours != null;
		assert vertexColours.length == vc : vc + " vertices but " + vertexColours.length
				+ " colours";
		assert textureCoordinates == null || textureCoordinates.length / 2 == vc : vc
				+ " vertices but " + textureCoordinates.length / 2 + " texCoords";
		assert triangleIndices != null && triangleIndices.length > 0 : "You do need to specify some triangles";

		if( state == null )
		{
			state = GLUtil.typicalState;
		}

		state = intern( state );

		int indexOffset = vertices.position() / 3;

		while( vertices.remaining() < verts.length )
		{ // need to get bigger buffers
			growArrays();
		}

		int vertPos = vertices.position();
		int texCoordPos = texCoords.position();
		int colourPos = colours.position();

		boolean doTransform = transform != null && !MatrixUtils.isidentity( transform );

		// write vertex data to buffers
		if( transform == null )
		{
			vertices.put( verts );
		}
		else
		{
			for( int i = 0; i < vc; i++ )
			{
				t.set( verts[ 3 * i ], verts[ 3 * i + 1 ], verts[ 3 * i + 2 ], 1 );

				if( doTransform )
				{
					Matrix4f.transform( transform, t, t );
				}

				vertices.put( t.x );
				vertices.put( t.y );
				vertices.put( t.z );
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

		if( !processorStack.isEmpty() )
		{
			int p = vertices.position();
			vertices.position( vertPos );
			FloatBuffer sv = vertices.slice();
			sv.limit( vc * 3 );
			vertices.position( p );

			p = texCoords.position();
			texCoords.position( texCoordPos );
			FloatBuffer stc = texCoords.slice();
			stc.limit( vc * 2 );
			texCoords.position( p );

			p = colours.position();
			colours.position( colourPos );
			IntBuffer sc = colours.slice();
			sc.limit( vc );
			colours.position( p );

			for( Processor proc : processorStack )
			{
				proc.process( sv, stc, sc );
			}
		}

		// insert triangle indices
		triangles[ state.getCompiledIndex() ].add( triangleIndices, indexOffset );
	}

	/**
	 * Doubles the number of vertices that can be handled by this
	 * renderer
	 */
	private void growArrays()
	{
		FloatBuffer bVerts = BufferUtils.createFloatBuffer( vertices.capacity() * 2 );
		vertices.flip();
		bVerts.put( vertices );
		vertices = bVerts;

		FloatBuffer btc = BufferUtils.createFloatBuffer( texCoords.capacity() * 2 );
		texCoords.flip();
		btc.put( texCoords );
		texCoords = btc;

		ByteBuffer bcb = BufferUtils.createByteBuffer( colourBytes.capacity() * 2 );
		IntBuffer bc = bcb.asIntBuffer();
		colours.flip();
		bc.put( colours );
		colourBytes = bcb;
		colours = bc;
	}

	/**
	 * Render the triangles and clear the data
	 */
	public void render()
	{
		// enable the buffers for openGL
		vertices.flip();
		texCoords.flip();
		colours.flip();

		vertexCount = vertices.limit() / 3;
		triangleCount = 0;

		colourBytes.position( 0 );
		colourBytes.limit( colours.limit() * 4 );

		GL11.glEnableClientState( GL11.GL_VERTEX_ARRAY );
		GL11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		GL11.glEnableClientState( GL11.GL_COLOR_ARRAY );

		GL11.glVertexPointer( 3, 0, vertices );
		GL11.glTexCoordPointer( 2, 0, texCoords );
		GL11.glColorPointer( 4, true, 0, colourBytes );

		// sorting is already done by the treemap

		// render
		for( int i = 0; i < triangles.length; i++ )
		{
			TriangleList tl = triangles[ i ];

			if( tl.count > 0 )
			{
				// apply the state
				tl.state.apply();

				triangleCount += tl.count;

				if( tris == null || tris.capacity() < tl.tris.length )
				{
					tris = BufferUtils.createIntBuffer( tl.tris.length );
				}

				tris.clear();

				tris.put( tl.tris );
				tris.position( tl.count );

				tris.flip();

				// render
				GL11.glDrawElements( GL11.GL_TRIANGLES, tris );
			}
		}

		triangleCount /= 3;

		// disable and clear the buffers
		GL11.glDisableClientState( GL11.GL_VERTEX_ARRAY );
		GL11.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
		GL11.glDisableClientState( GL11.GL_COLOR_ARRAY );

		vertices.clear();
		texCoords.clear();
		colours.clear();
		colourBytes.clear();

		for( int i = 0; i < triangles.length; i++ )
		{
			triangles[ i ].count = 0;
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
	public void setTransform( Matrix4f m )
	{
		transform = m;
	}

	/**
	 * Gets the currently set transform
	 * 
	 * @return The transform, or null for no transformation
	 */
	public Matrix4f getTransform()
	{
		return transform;
	}

	/**
	 * Keeps track of added triangle indices
	 * 
	 * @author ryanm
	 */
	private static class TriangleList
	{
		/**
		 * The GL state for these triangles
		 */
		private final State state;

		/**
		 * The number of indices
		 */
		private int count = 0;

		/**
		 * The triangle indices
		 */
		private int[] tris = new int[ 100 ];

		private TriangleList( State state )
		{
			this.state = state;
		}

		private void add( int[] ti, int indexOffset )
		{
			if( count + ti.length >= tris.length )
			{
				int[] nTris = new int[ count + ti.length ];
				System.arraycopy( tris, 0, nTris, 0, tris.length );
				tris = nTris;
			}

			for( int i = 0; i < ti.length; i++ )
			{
				tris[ count + i ] = ti[ i ] + indexOffset;
			}

			count += ti.length;
		}
	}

}
