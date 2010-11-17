
package com.ryanm.util;

import java.util.NoSuchElementException;

/**
 * Call it an unordered list or a multiset, this collection is defined
 * by oxymorons. Find the original <a
 * href=http://riven8192.blogspot.com/
 * 2009/08/bag-unordered-list-fast-remove.html>over here</a>
 * 
 * @author Riven
 * @param <T>
 */
public class Bag<T>
{
	private T[] data;

	private int size;

	/***/
	public Bag()
	{
		this( 4 );
	}

	/**
	 * @param space
	 *           initial size of bag
	 */
	@SuppressWarnings( "unchecked" )
	public Bag( int space )
	{
		this.data = ( T[] ) new Object[ space ];
	}

	/**
	 * Add an element
	 * 
	 * @param t
	 */
	public void put( T t )
	{
		data = ensure( data, size + 1, 1.75f );
		data[ size++ ] = t;
	}

	/**
	 * Adds a lot of elements
	 * 
	 * @param bag
	 */
	public void putAll( Bag<T> bag )
	{
		if( bag.size == 0 )
		{
			return;
		}

		data = ensure( data, this.size + bag.size, 1.75f );
		System.arraycopy( bag.data, 0, this.data, this.size, bag.size );
		this.size += bag.size;
	}

	/**
	 * Retrieves an element
	 * 
	 * @param index
	 * @return the indexed element
	 */
	public T get( int index )
	{
		if( index >= size )
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return data[ index ];
	}

	/**
	 * Retrieves and removes an element
	 * 
	 * @param index
	 * @return The indexed element
	 */
	public T take( int index )
	{
		if( index >= size )
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		T took = data[ index ];
		data[ index ] = data[ --size ];
		data[ size ] = null;
		return took;
	}

	/**
	 * Attempts to remove an element
	 * 
	 * @param t
	 *           The element to remove
	 * @return t
	 * @throws NoSuchElementException
	 *            if the element was not found
	 */
	public T take( T t )
	{
		int i = this.indexOf( t );
		if( i == -1 )
		{
			throw new NoSuchElementException();
		}
		return this.take( i );
	}

	/**
	 * Tests if an element is present
	 * 
	 * @param t
	 * @return <code>true</code> if t is present, <code>false</code>
	 *         otherwise
	 */
	public boolean contains( T t )
	{
		return this.indexOf( t ) != -1;
	}

	/**
	 * Finds the index of an element
	 * 
	 * @param t
	 * @return the index of t, or -1 if not found
	 */
	public int indexOf( T t )
	{
		for( int i = 0; i < size; i++ )
		{
			if( data[ i ] == t )
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Tries to shrink the internal array. If your {@link Bag} holds a
	 * lot of elements temporarily and you're concerned with memory
	 * usage, it might be worthwhile to call this after a sequence of
	 * {@link #take(int)} calls
	 */
	@SuppressWarnings( "unchecked" )
	public void shrink()
	{
		if( this.data.length > 8 )
		{
			int factor = 4;

			if( this.size < this.data.length / factor )
			{
				int newSize = Math.max( 4, this.size );
				T[] newData = ( T[] ) new Object[ newSize ];
				System.arraycopy( this.data, 0, newData, 0, this.size );
				this.data = newData;
			}
		}
	}

	/**
	 * Removes all elements
	 */
	public void clear()
	{
		for( int i = 0; i < size; i++ )
		{
			data[ i ] = null;
		}
		this.size = 0;
	}

	/**
	 * The maximum number of elements allowed before the internal array
	 * will be grown
	 * 
	 * @return The size of the internal array
	 */
	public int capacity()
	{
		return this.data.length;
	}

	/**
	 * @return The number of held elements
	 */
	public int size()
	{
		return size;
	}

	@SuppressWarnings( "unchecked" )
	private static final <T> T[] ensure( T[] src, int minCapacity, float factor )
	{
		if( src.length >= minCapacity )
		{
			return src;
		}
		int newCapacity = src.length + 1;
		do
		{
			newCapacity *= factor;
		}
		while( newCapacity < minCapacity );

		T[] dst = ( T[] ) new Object[ newCapacity ];
		System.arraycopy( src, 0, dst, 0, src.length );
		return dst;
	}
}
