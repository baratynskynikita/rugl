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

import android.content.res.Configuration;

import com.ryanm.droid.rugl.gl.enums.DrawMode;
import com.ryanm.droid.rugl.gl.facets.mutable.MutAlphaTest;
import com.ryanm.droid.rugl.gl.facets.mutable.MutBlend;
import com.ryanm.droid.rugl.gl.facets.mutable.MutDepthTest;
import com.ryanm.droid.rugl.gl.facets.mutable.MutFog;
import com.ryanm.droid.rugl.gl.facets.mutable.MutPolygonOffset;
import com.ryanm.droid.rugl.gl.facets.mutable.MutTextureState;
import com.ryanm.preflect.annote.DirtyFlag;
import com.ryanm.preflect.annote.Variable;

/**
 * Represents a mutable rendering state
 * 
 * @author ryanm
 */
@Variable( "GL State" )
public class MutableState
{
	/**
	 * This will be set to true when state variables are changed
	 * through {@link Configuration} changes
	 */
	@DirtyFlag( watchTree = true )
	public boolean dirty = false;

	/***/
	@Variable
	public DrawMode drawMode;

	/**
	 * Texture state
	 */
	@Variable
	public final MutTextureState texture;

	/**
	 * Alpha test function
	 */
	@Variable
	public final MutAlphaTest alphaTest;

	/**
	 * Blend function
	 */
	@Variable
	public final MutBlend blend;

	/**
	 * Depth test
	 */
	@Variable
	public final MutDepthTest depthTest;

	/**
	 * Polygon offset
	 */
	@Variable
	public final MutPolygonOffset polyOffset;

	/**
	 * Fog parameters
	 */
	@Variable
	public final MutFog fog;

	/**
	 * @param state
	 */
	public MutableState( State state )
	{
		drawMode = state.drawMode;
		texture = new MutTextureState( state.texture );
		alphaTest = new MutAlphaTest( state.alphaTest );
		blend = new MutBlend( state.blend );
		depthTest = new MutDepthTest( state.depthTest );
		polyOffset = new MutPolygonOffset( state.polyOffset );
		fog = new MutFog( state.fog );
	}

	/**
	 * @return A compiled state
	 */
	public State compile()
	{
		return new State( drawMode, texture.compile(), alphaTest.compile(),
				blend.compile(), depthTest.compile(), polyOffset.compile(), fog.compile() );
	}
}
