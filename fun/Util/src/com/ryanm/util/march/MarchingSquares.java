
package com.ryanm.util.march;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple implementation of the marching squares algorithm that can
 * identify perimeters in an supplied byte array. The array of data
 * over which this instances of this class operate is not cloned by
 * this class's constructor (for obvious efficiency reasons) and
 * should therefore not be modified while the object is in use. It is
 * expected that the data elements supplied to the algorithm have
 * already been thresholded. The algorithm only distinguishes between
 * zero and non-zero values. Altered by ryanm to do an exhaustive
 * search for all paths
 * 
 * @author Tom Gibara
 */
public class MarchingSquares
{

	/**
	 * The data over which we are operating
	 */
	private final BufferedImage data;

	private final boolean[][] found;

	/**
	 * The value to test for
	 */
	private final int value;

	/**
	 * Creates a new object that can locate perimeter paths in the
	 * supplied data. The length of the supplied data array must exceed
	 * width * height, with the data elements in row major order and
	 * the top-left-hand data element at index zero.
	 * 
	 * @param data
	 *           the data elements
	 * @param value
	 *           The value to test for
	 */
	public MarchingSquares( BufferedImage data, Color value )
	{
		this.data =
				new BufferedImage( data.getWidth() + 2, data.getHeight() + 2,
						BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = this.data.createGraphics();
		g.setColor( value.getRGB() == Color.white.getRGB() ? Color.black : Color.white );
		g.fillRect( 0, 0, this.data.getWidth(), this.data.getHeight() );
		g.drawImage( data, 1, 1, null );

		this.value = value.getRGB();
		found = new boolean[ this.data.getWidth() ][ this.data.getHeight() ];

		for( int i = 0; i < found.length; i++ )
		{
			Arrays.fill( found[ i ], false );
		}
	}

	/**
	 * Finds the perimeter between a set of zero and non-zero values
	 * which begins at the specified data element. If no initial point
	 * is known, consider using the convenience method supplied. The
	 * paths returned by this method are always closed.
	 * 
	 * @param initialX
	 *           the column of the data matrix at which to start
	 *           tracing the perimeter
	 * @param initialY
	 *           the row of the data matrix at which to start tracing
	 *           the perimeter
	 * @return a closed, anti-clockwise path that is a perimeter of
	 *         between a set of zero and non-zero values in the data.
	 * @throws IllegalArgumentException
	 *            if there is no perimeter at the specified initial
	 *            point.
	 */
	public Path identifyPerimeter( int initialX, int initialY )
	{
		if( initialX < 0 )
		{
			initialX = 0;
		}
		if( initialX > data.getWidth() )
		{
			initialX = data.getWidth();
		}
		if( initialY < 0 )
		{
			initialY = 0;
		}
		if( initialY > data.getHeight() )
		{
			initialY = data.getHeight();
		}

		int initialValue = value( initialX, initialY );
		if( initialValue == 0 || initialValue == 15 )
		{
			throw new IllegalArgumentException( "Supplied initial coordinates (" + initialX + ", "
					+ initialY + ") do not lie on a perimeter. " + initialValue );
		}

		ArrayList<Direction> directions = new ArrayList<Direction>();

		int x = initialX;
		int y = initialY;
		Direction previous = null;

		do
		{
			final Direction direction;
			switch( value( x, y ) )
			{
				case 1:
					direction = Direction.N;
					break;
				case 2:
					direction = Direction.E;
					break;
				case 3:
					direction = Direction.E;
					break;
				case 4:
					direction = Direction.W;
					break;
				case 5:
					direction = Direction.N;
					break;
				case 6:
					direction = previous == Direction.N ? Direction.W : Direction.E;
					break;
				case 7:
					direction = Direction.E;
					break;
				case 8:
					direction = Direction.S;
					break;
				case 9:
					direction = previous == Direction.E ? Direction.N : Direction.S;
					break;
				case 10:
					direction = Direction.S;
					break;
				case 11:
					direction = Direction.S;
					break;
				case 12:
					direction = Direction.W;
					break;
				case 13:
					direction = Direction.N;
					break;
				case 14:
					direction = Direction.W;
					break;
				default:
					throw new IllegalStateException();
			}

			directions.add( direction );
			x += direction.screenX;
			y += direction.screenY; // accommodate change of basis
			previous = direction;
		}
		while( x != initialX || y != initialY );

		return new Path( initialX, -initialY, directions );
	}

	/**
	 * A convenience method that locates at least one perimeter in the
	 * data with which this object was constructed. If there is no
	 * perimeter (i.e.: if all elements of the supplied array are
	 * identically zero) then null is returned.
	 * 
	 * @return a perimeter path obtained from the data, or null
	 */
	public Path identifyPerimeter()
	{
		for( int x = 0; x < data.getWidth(); x++ )
		{
			for( int y = 0; y < data.getHeight(); y++ )
			{
				if( !found[ x ][ y ] )
				{
					int v = value( x, y );
					if( v != 0 && v != 15 )
					{
						return identifyPerimeter( x, y );
					}
				}
			}
		}

		return null;
	}

	private int value( int x, int y )
	{
		int sum = 0;
		if( isSet( x, y ) )
		{
			sum |= 1;
		}
		if( isSet( x + 1, y ) )
		{
			sum |= 2;
		}
		if( isSet( x, y + 1 ) )
		{
			sum |= 4;
		}
		if( isSet( x + 1, y + 1 ) )
		{
			sum |= 8;
		}

		if( sum != 0 && sum != 15 )
		{
			found[ x ][ y ] = true;
		}

		return sum;
	}

	private boolean isSet( int x, int y )
	{
		return x < 0 || x >= data.getWidth() || y < 0 || y >= data.getHeight() ? false : data.getRGB(
				x, y ) == value;
	}

	/**
	 * Finds all perimeters in the data
	 * 
	 * @return An array of the perimeter paths
	 */
	public Path[] identifyPerimeters()
	{
		List<Path> paths = new ArrayList<Path>();
		Path p = null;

		do
		{
			p = identifyPerimeter();

			if( p != null )
			{
				paths.add( p );
			}
		}
		while( p != null );

		return paths.toArray( new Path[ paths.size() ] );
	}
}
