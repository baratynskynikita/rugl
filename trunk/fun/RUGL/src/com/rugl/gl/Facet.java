
package com.rugl.gl;

/**
 * Represents some part of the rendering state.
 * 
 * @author ryanm
 * @param <T>
 *           Set this as the class you are writing - makes
 *           implementation slightly simpler e.g.: {@code public class
 *           Foo implements Facet<Foo>}
 */
public abstract class Facet<T> implements Comparable<T>
{
	/**
	 * Alter the OpenGL state if necessary
	 * 
	 * @param facet
	 *           The current state.
	 */
	public abstract void transitionFrom( T facet );
}