/*
 * Copyright (c) 2002-2004 LWJGL Project All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. * Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of 'LWJGL' nor the names
 * of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written
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

package com.ryanm.util.geom;

import java.io.Serializable;

/**
 * A 2D float Dimension class, which looks remarkably like an lwjgl.
 * 
 * @author $Author: matzon $
 * @version $Revision: 2286 $ $Id: Dimension.java 2286 2006-03-23
 *          19:32:21Z matzon $
 */
public final class Dimensionf implements Serializable, ReadableDimensionf, WritableDimensionf
{

	static final long serialVersionUID = 1L;

	/** The dimensions! */
	private float width, height;

	/**
	 * Constructor for Dimension.
	 */
	public Dimensionf()
	{
		super();
	}

	/**
	 * Constructor for Dimension.
	 * 
	 * @param w
	 * @param h
	 */
	public Dimensionf( float w, float h )
	{
		width = w;
		height = h;
	}

	/**
	 * Constructor for Dimension.
	 * 
	 * @param d
	 */
	public Dimensionf( ReadableDimensionf d )
	{
		setSize( d );
	}

	@Override
	public void setSize( float w, float h )
	{
		width = w;
		height = h;
	}

	@Override
	public void setSize( ReadableDimensionf d )
	{
		width = d.getWidth();
		height = d.getHeight();
	}

	/*
	 * (Overrides)
	 * @see
	 * com.shavenpuppy.jglib.ReadableDimension#getSize(com.shavenpuppy
	 * .jglib.Dimension)
	 */
	@Override
	public void getSize( WritableDimensionf dest )
	{
		dest.setSize( this );
	}

	/**
	 * Checks whether two dimension objects have equal values.
	 */
	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof ReadableDimensionf )
		{
			ReadableDimensionf d = ( ReadableDimensionf ) obj;
			return width == d.getWidth() && height == d.getHeight();
		}
		return false;
	}

	/**
	 * Returns the hash code for this <code>Dimension</code>.
	 * 
	 * @return a hash code for this <code>Dimension</code>
	 */
	@Override
	public int hashCode()
	{
		float sum = width + height;
		return ( int ) ( sum * ( sum + 1 ) / 2 + width );
	}

	/**
	 * Returns a string representation of the values of this
	 * <code>Dimension</code> object's <code>height</code> and
	 * <code>width</code> fields. This method is intended to be used
	 * only for debugging purposes, and the content and format of the
	 * returned string may vary between implementations. The returned
	 * string may be empty but may not be <code>null</code>.
	 * 
	 * @return a string representation of this <code>Dimension</code>
	 *         object
	 */
	@Override
	public String toString()
	{
		return getClass().getName() + "[width=" + width + ",height=" + height + "]";
	}

	/**
	 * Gets the height.
	 * 
	 * @return Returns a float
	 */
	@Override
	public float getHeight()
	{
		return height;
	}

	/**
	 * Sets the height.
	 * 
	 * @param height
	 *           The height to set
	 */
	@Override
	public void setHeight( float height )
	{
		this.height = height;
	}

	/**
	 * Gets the width.
	 * 
	 * @return Returns a float
	 */
	@Override
	public float getWidth()
	{
		return width;
	}

	/**
	 * Sets the width.
	 * 
	 * @param width
	 *           The width to set
	 */
	@Override
	public void setWidth( float width )
	{
		this.width = width;
	}

}
