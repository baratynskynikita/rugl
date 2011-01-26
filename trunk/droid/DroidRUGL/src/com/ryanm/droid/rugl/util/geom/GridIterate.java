
package com.ryanm.droid.rugl.util.geom;

import android.graphics.Point;

/**
 * Allows us to iterate over a unit-grid over a line segment
 * 
 * @author ryanm
 */
public class GridIterate
{
	private enum Move
	{
		LEFT( -1, 0 ), RIGHT( 1, 0 ), UP( 0, 1 ), DOWN( 0, -1 ), COMPLETE( 0, 0 );

		private final int x;

		private final int y;

		private Move( int x, int y )
		{
			this.x = x;
			this.y = y;
		}
	}

	private enum Direction
	{
		UP_RIGHT
		{
			@Override
			public Move validate( Move m )
			{
				switch( m )
				{
					case DOWN:
						return Move.RIGHT;
					case LEFT:
						return Move.UP;
					default:
						return m;
				}
			}
		},
		UP_LEFT
		{
			@Override
			public Move validate( Move m )
			{
				switch( m )
				{
					case DOWN:
						return Move.LEFT;
					case RIGHT:
						return Move.UP;
					default:
						return m;
				}
			}
		},
		DOWN_RIGHT
		{
			@Override
			public Move validate( Move m )
			{
				switch( m )
				{
					case UP:
						return Move.RIGHT;
					case LEFT:
						return Move.DOWN;
					default:
						return m;
				}
			}
		},
		DOWN_LEFT
		{
			@Override
			public Move validate( Move m )
			{
				switch( m )
				{
					case UP:
						return Move.LEFT;
					case RIGHT:
						return Move.DOWN;
					default:
						return m;
				}
			}
		};

		/**
		 * Solves a really hideous corner-case problem. Literally a
		 * corner case : next move gets confused when the line exactly
		 * hits the corner of a box
		 * 
		 * @param m
		 * @return The correct next direction move
		 */
		public abstract Move validate( Move m );
	}

	private final Vector2f start = new Vector2f(), end = new Vector2f(),
			p = new Vector2f(), q = new Vector2f();

	private final Vector2f min = new Vector2f(), max = new Vector2f();

	private Direction dir;

	/**
	 * The coordinates of the last grid square
	 */
	public Point lastGridCoords = null;

	private Move lastGridExit = null;

	private boolean done = false;

	private final float invGridSize;

	/**
	 * @param gridSize
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 */
	public GridIterate( float gridSize, float minX, float minY, float maxX, float maxY )
	{
		invGridSize = 1.0f / gridSize;
		min.set( minX, minY );
		max.set( maxX, maxY );
	}

	/**
	 * @param startx
	 * @param starty
	 * @param endx
	 * @param endy
	 */
	public void setSeg( float startx, float starty, float endx, float endy )
	{
		start.set( startx, starty );
		end.set( endx, endy );

		start.scale( invGridSize );
		end.scale( invGridSize );

		if( start.x < end.x )
		{
			if( start.y < end.y )
			{
				dir = Direction.UP_RIGHT;
			}
			else
			{
				dir = Direction.DOWN_RIGHT;
			}
		}
		else
		{
			if( start.y < end.y )
			{
				dir = Direction.UP_LEFT;
			}
			else
			{
				dir = Direction.DOWN_LEFT;
			}
		}

		lastGridCoords = null;
		lastGridExit = null;
		done = false;
	}

	/**
	 * We are done with the line segment
	 * 
	 * @return <code>true</code> if the end has been reached
	 */
	public boolean isDone()
	{
		return done;
	}

	/**
	 * Advances to the next grid square
	 */
	public void next()
	{
		if( lastGridCoords == null )
		{
			lastGridCoords = new Point( ( int ) start.x, ( int ) start.y );
		}
		else
		{
			lastGridCoords.x = lastGridCoords.x + lastGridExit.x;
			lastGridCoords.y = lastGridCoords.y + lastGridExit.y;
		}

		p.set( start );
		q.set( end );

		// clip to grid
		lastGridExit = clip( p, q, lastGridCoords.x, lastGridCoords.y );

		lastGridExit = dir.validate( lastGridExit );

		if( lastGridExit == Move.COMPLETE || lastGridCoords.x < min.x
				|| lastGridCoords.y < min.y || lastGridCoords.x > max.x
				|| lastGridCoords.y > max.y )
		{
			done = true;
		}
	}

	private Move clip( Vector2f p, Vector2f q, int x, int y )
	{
		clipPoint( p, q, x, y );
		Move exit = clipPoint( q, p, x, y );

		return exit;
	}

	private Move clipPoint( Vector2f p, Vector2f q, int x, int y )
	{
		Move m = Move.COMPLETE;

		if( p.x < x )
		{
			p.y = p.y + ( q.y - p.y ) * ( x - p.x ) / ( q.x - p.x );
			p.x = x;

			m = Move.LEFT;
		}

		if( p.x > x + 1 )
		{
			p.y = p.y + ( q.y - p.y ) * ( x + 1 - p.x ) / ( q.x - p.x );
			p.x = x + 1;

			m = Move.RIGHT;
		}

		if( p.y < y )
		{
			p.x = p.x + ( q.x - p.x ) * ( y - p.y ) / ( q.y - p.y );
			p.y = y;

			m = Move.DOWN;
		}

		if( p.y > y + 1 )
		{
			p.x = p.x + ( q.x - p.x ) * ( y + 1 - p.y ) / ( q.y - p.y );
			p.y = y + 1;

			m = Move.UP;
		}

		return m;
	}

	// /**
	// * @param args
	// */
	// public static void main( String[] args )
	// {
	// int size = 500;
	// float box = 50;
	//
	// BufferedImage bi = new BufferedImage( size, size,
	// BufferedImage.TYPE_INT_ARGB );
	// Graphics2D g = bi.createGraphics();
	// g.setColor( Color.white );
	// g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
	//
	// g.setColor( Color.black );
	//
	// for( int i = 0; i < 500; i += box )
	// {
	// g.drawLine( i, 0, i, bi.getHeight() );
	// g.drawLine( 0, i, bi.getWidth(), i );
	// }
	//
	// Random rng = new Random();
	//
	// Vector2f start = new Vector2f( size * rng.nextFloat(), size *
	// rng.nextFloat() );
	// Vector2f end = new Vector2f( size * rng.nextFloat(), size *
	// rng.nextFloat() );
	//
	// g.setColor( Color.red );
	// // g.setStroke( new BasicStroke( 5, BasicStroke.CAP_ROUND,
	// // BasicStroke.JOIN_MITER ) );
	// g.drawLine( ( int ) start.x, ( int ) start.y, ( int ) end.x, (
	// int ) end.y );
	//
	// g.setColor( new Color( 0, 0, 0, 128 ) );
	//
	// System.out.println( start + " to " + end );
	//
	// GridIterate gi = new GridIterate( box );
	// gi.setSeg( start.x, start.y, end.x, end.y );
	//
	// while( !gi.isDone() )
	// {
	// gi.next();
	//
	// System.out.println( "( " + gi.lastGridCoords.x + ", " +
	// gi.lastGridCoords.y + " )\t" );
	//
	// g.fillRect( ( int ) ( gi.lastGridCoords.x * box ), ( int ) (
	// gi.lastGridCoords.y * box ),
	// ( int ) box, ( int ) box );
	// }
	//
	// try
	// {
	// ImageIO.write( bi, "png", new File( "grid.png" ) );
	// }
	// catch( IOException e )
	// {
	// e.printStackTrace();
	// }
	//
	// System.out.println( "done" );
	// }
}
