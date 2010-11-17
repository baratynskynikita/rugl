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
 * A 2D float point class, which looks remarkably like an lwjgl one.
 * 
 * @author $Author: matzon $
 * @version $Revision: 2286 $ $Id: Point.java 2286 2006-03-23
 *          19:32:21Z matzon $
 */
public final class Pointf implements ReadablePointf, WritablePointf, Serializable
{

	static final long serialVersionUID = 1L;

	/**
	 * The x coordinate
	 */
	public float x;

	/**
	 * The y coordinate
	 */
	public float y;

	/**
	 * Constructor for Point.
	 */
	public Pointf()
	{
		super();
	}

	/**
	 * Constructor for Point.
	 * 
	 * @param x
	 * @param y
	 */
	public Pointf( float x, float y )
	{
		setLocation( x, y );
	}

	/**
	 * Constructor for Point.
	 * 
	 * @param p
	 */
	public Pointf( ReadablePointf p )
	{
		setLocation( p );
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return The distance between two points
	 */
	public static float distance( float x1, float y1, float x2, float y2 )
	{
		return ( float ) Math.sqrt( distanceSq( x1, y1, x2, y2 ) );
	}

	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return The square of the distance between two points
	 */
	public static float distanceSq( float x1, float y1, float x2, float y2 )
	{
		float dx = x1 - x2;
		float dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	@Override
	public void setLocation( float x, float y )
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public void setLocation( ReadablePointf p )
	{
		x = p.getX();
		y = p.getY();
	}

	@Override
	public void setX( float x )
	{
		this.x = x;
	}

	@Override
	public void setY( float y )
	{
		this.y = y;
	}

	/**
	 * Translate a point.
	 * 
	 * @param dx
	 *           The translation to apply
	 * @param dy
	 *           The translation to apply
	 */
	public void translate( float dx, float dy )
	{
		x += dx;
		y += dy;
	}

	/**
	 * Translate a point.
	 * 
	 * @param p
	 *           The translation to apply
	 */
	public void translate( ReadablePointf p )
	{
		x += p.getX();
		y += p.getY();
	}

	/**
	 * Determines whether an instance of <code>Point2D</code> is equal
	 * to this point. Two instances of <code>Point2D</code> are equal
	 * if the values of their <code>x</code> and <code>y</code> member
	 * fields, representing their position in the coordinate space, are
	 * the same.
	 * 
	 * @param obj
	 *           an object to be compared with this point
	 * @return <code>true</code> if the object to be compared is an
	 *         instance of <code>Point</code> and has the same values;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean equals( Object obj )
	{
		if( obj instanceof Pointf )
		{
			Pointf pt = ( Pointf ) obj;
			return x == pt.x && y == pt.y;
		}
		return super.equals( obj );
	}

	/**
	 * Returns a string representation of this point and its location
	 * in the (<i>x</i>,&nbsp;<i>y</i>) coordinate space. This method
	 * is intended to be used only for debugging purposes, and the
	 * content and format of the returned string may vary between
	 * implementations. The returned string may be empty but may not be
	 * <code>null</code>.
	 * 
	 * @return a string representation of this point
	 */
	@Override
	public String toString()
	{
		return getClass().getName() + "[x=" + x + ",y=" + y + "]";
	}

	/**
	 * Returns the hash code for this <code>Point</code>.
	 * 
	 * @return a hash code for this <code>Point</code>
	 */
	@Override
	public int hashCode()
	{
		float sum = x + y;
		return ( int ) ( sum * ( sum + 1 ) / 2 + x );
	}

	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public void getLocation( WritablePointf dest )
	{
		dest.setLocation( x, y );
	}

}
