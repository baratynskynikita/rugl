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

package com.rugl.gl;

import java.util.Arrays;

import com.rugl.gl.facets.AlphaTest;
import com.rugl.gl.facets.Blend;
import com.rugl.gl.facets.DepthTest;
import com.rugl.gl.facets.PolygonMode;
import com.rugl.gl.facets.PolygonOffset;
import com.rugl.gl.facets.TextureState;
import com.rugl.gl.shader.FixedFunction;
import com.rugl.gl.shader.Program;

/**
 * Represents a mutable rendering state
 * 
 * @author ryanm
 */
public class State implements Comparable<State>
{
	/**
	 * The current state of openGL
	 */
	private static State currentState = new State();

	/**
	 * The {@link State} class mirrors the OpenGL state as much as
	 * possible, so don't change it outside of {@link State}, and call
	 * this when the OpenGL State is refreshed e.g.: when the display
	 * has been (re)created
	 */
	public static void stateReset()
	{
		currentState = new State();
	}

	/**
	 * How many batches of compiled {@link State}s have been produced
	 */
	private static int compilationBatchCount = 0;

	/**
	 * Compiles a set of states so that <code>compiledState.compareTo(
	 * otherCompiledState )</code> is fast: a simple int comparison
	 * rather than a traversal of the entire state tree.
	 * 
	 * @param states
	 */
	public static void compile( State... states )
	{
		// un-compile so we get the deepCompare()
		for( int i = 0; i < states.length; i++ )
		{
			states[ i ].compilationBatch = -1;
		}

		Arrays.sort( states );

		// identical states may have been supplied, we want them to
		// end up with the same index
		int compiledIndex = -1;
		State prev = null;

		for( int i = 0; i < states.length; i++ )
		{
			if( prev == null || !prev.equals( states[ i ] ) )
			{
				compiledIndex++;
				prev = states[ i ];
			}

			states[ i ].compiledIndex = compiledIndex;
			states[ i ].compilationBatch = compilationBatchCount;
		}

		compilationBatchCount++;
	}

	/**
	 * Shader program
	 */
	public final Program shader;

	/**
	 * Texture state
	 */
	public final TextureState texture;

	/**
	 * Alpha test function
	 */
	public final AlphaTest alphaTest;

	/**
	 * Blend function
	 */
	public final Blend blend;

	/**
	 * Depth test
	 */
	public final DepthTest depthTest;

	/**
	 * Polygon offset
	 */
	public final PolygonOffset polyOffset;

	/**
	 * Polygon fill mode
	 */
	public final PolygonMode polyMode;

	/**
	 * A list of state facets, in descending order of
	 * <ol>
	 * <li>change cost</li>
	 * <li>likelihood of difference</li>
	 * </ol>
	 */
	private final Facet[] facets;

	/**
	 * Only states that were compiled together can be compared based on
	 * their {@link #compiledIndex}
	 */
	private int compilationBatch = -1;

	/**
	 * The index of the state in its {@link #compilationBatch}
	 */
	private int compiledIndex = -1;

	/**
	 * Constructor for default OpenGL state
	 */
	public State()
	{
		shader = FixedFunction.instance;
		texture = TextureState.disabled;
		alphaTest = AlphaTest.disabled;
		blend = Blend.disabled;
		depthTest = DepthTest.disabled;
		polyOffset = PolygonOffset.disabled;
		polyMode = new PolygonMode();

		facets =
				new Facet[] { shader, texture, alphaTest, blend, depthTest, polyOffset,
						polyMode };
	}

	/**
	 * @param shader
	 * @param texture
	 * @param alphaTest
	 * @param blend
	 * @param depthTest
	 * @param polyOffset
	 * @param polyMode
	 */
	public State( Program shader, TextureState texture, AlphaTest alphaTest,
			Blend blend, DepthTest depthTest, PolygonOffset polyOffset, PolygonMode polyMode )
	{
		this.shader = shader;
		this.texture = texture;
		this.alphaTest = alphaTest;
		this.blend = blend;
		this.depthTest = depthTest;
		this.polyOffset = polyOffset;
		this.polyMode = polyMode;

		facets =
				new Facet[] { shader, texture, alphaTest, blend, depthTest, polyOffset,
						polyMode };
	}

	/**
	 * @param shader
	 * @return An altered clone
	 */
	public State with( Program shader )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param texture
	 * @return An altered clone
	 */
	public State with( TextureState texture )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param alphaTest
	 * @return An altered clone
	 */
	public State with( AlphaTest alphaTest )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param blend
	 * @return An altered clone
	 */
	public State with( Blend blend )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param depthTest
	 * @return An altered clone
	 */
	public State with( DepthTest depthTest )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param polyOffset
	 * @return An altered clone
	 */
	public State with( PolygonOffset polyOffset )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * @param polyMode
	 * @return An altered clone
	 */
	public State with( PolygonMode polyMode )
	{
		return new State( shader, texture, alphaTest, blend, depthTest, polyOffset,
				polyMode );
	}

	/**
	 * Applies this rendering state to OpenGL
	 */
	@SuppressWarnings( "unchecked" )
	public void apply()
	{
		for( int i = 0; i < facets.length; i++ )
		{
			if( facets[ i ] != null )
			{
				facets[ i ].transitionFrom( currentState.facets[ i ] );
			}
		}

		currentState = this;
	}

	/**
	 * Comparisons based on {@link #getCompiledIndex()} between states
	 * with different {@link #getCompilationBatch()} values are invalid
	 * 
	 * @return the batch number of the compilation of this
	 *         {@link State}, or -1 if this state has not been compiled
	 */
	public int getCompilationBatch()
	{
		return compilationBatch;
	}

	/**
	 * Gets the index that this state achieved in its compilation
	 * batch. Remember that states compiled in different batches can
	 * have the same compiled index
	 * 
	 * @return The index of this state in its compilation batch, or -1
	 *         if the state has not been compiled
	 */
	public int getCompiledIndex()
	{
		return compiledIndex;
	}

	@Override
	public int compareTo( State o )
	{
		if( compilationBatch >= 0 && compilationBatch == o.compilationBatch )
		{ // these two states were compiled together
			return compiledIndex - o.compiledIndex;
		}
		else
		{ // have to do a full tree comparison
			return deepCompare( o );
		}
	}

	@SuppressWarnings( "unchecked" )
	private int deepCompare( State o )
	{
		for( int i = 0; i < facets.length; i++ )
		{
			int d = facets[ i ].compareTo( o.facets[ i ] );

			if( d != 0 )
			{
				return d;
			}
		}

		return 0;
	}

	@Override
	public boolean equals( Object o )
	{
		if( o instanceof State )
		{
			return compareTo( ( State ) o ) == 0;
		}

		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "RenderState" );

		for( int i = 0; i < facets.length; i++ )
		{
			if( facets[ i ] != null )
			{
				buff.append( "\n\t" ).append( facets[ i ] );
			}
		}

		return buff.toString();
	}
}
