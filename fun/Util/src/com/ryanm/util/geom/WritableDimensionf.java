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

/**
 * Write interface for Dimensions
 * 
 * @author $Author: matzon $
 * @version $Revision: 2286 $ $Id: WritableDimension.java 2286
 *          2006-03-23 19:32:21Z matzon $
 */
public interface WritableDimensionf
{
	/**
	 * Set the size
	 * 
	 * @param w
	 *           the new width
	 * @param h
	 *           the new height
	 */
	public void setSize( float w, float h );

	/**
	 * Sets the size
	 * 
	 * @param d
	 *           Thge new size to copy
	 */
	public void setSize( ReadableDimensionf d );

	/**
	 * Sets the height.
	 * 
	 * @param height
	 *           The height to set
	 */
	public void setHeight( float height );

	/**
	 * Sets the width.
	 * 
	 * @param width
	 *           The width to set
	 */
	public void setWidth( float width );
}