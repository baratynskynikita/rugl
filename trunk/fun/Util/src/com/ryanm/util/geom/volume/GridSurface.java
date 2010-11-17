
package com.ryanm.util.geom.volume;

import org.lwjgl.util.vector.Vector2f;

/**
 * Represents a surface topographically similar to a rectangular grid
 * of vertices. Note that intersection paths obtained from this will
 * be doubled i.e.: what look like simple paths will actually be
 * degenerate loops that double back on themselves, and two identical
 * instances will be returned for loops
 * 
 * @author ryanm
 */
public class GridSurface extends IntersectionVolume
{
	/**
	 * @param columns
	 * @param rows
	 */
	public GridSurface( int columns, int rows )
	{
		super( columns * rows, 6 * ( columns - 1 ) * ( rows - 1 ), 4 * ( columns - 1 ) * ( rows - 1 ) );

		// let's default to flat
		int vi = 0;
		for( int i = 0; i < columns; i++ )
		{
			for( int j = 0; j < rows; j++ )
			{
				verts[ vi++ ].set( i, j, 0 );
			}
		}

		Quad[][] front = makeSheet( columns - 1, rows - 1 );
		Quad[][] back = makeSheet( columns - 1, rows - 1 );

		// add the edges and faces
		int edgeIndex = 0;
		int faceIndex = 0;
		for( int i = 0; i < front.length; i++ )
		{
			for( int j = 0; j < front[ i ].length; j++ )
			{
				// stitch front and back sheets together if we can
				front[ i ][ j ].stitch( back[ i ][ j ] );
				back[ i ][ j ].stitch( front[ i ][ j ] );

				edgeIndex = front[ i ][ j ].add( edgeIndex, faceIndex );
				faceIndex += 2;
				edgeIndex = back[ i ][ j ].add( edgeIndex, faceIndex );
				faceIndex += 2;
			}
		}
	}

	/**
	 * Sets this surface to be a heightmap terrain. Points are assumed
	 * to be evenly distributed on an axis-aligned grid between two
	 * points
	 * 
	 * @param min
	 *           One corner of the map
	 * @param max
	 *           The opposite corner
	 * @param heightmap
	 *           The height values of the points
	 */
	public void setTerrain( Vector2f min, Vector2f max, float[][] heightmap )
	{
		int vi = 0;

		float xDelta = ( max.x - min.x ) / ( heightmap.length - 1 );
		float yDelta = ( max.y - min.y ) / ( heightmap[ 0 ].length - 1 );

		for( int i = 0; i < heightmap.length; i++ )
		{
			for( int j = 0; j < heightmap[ i ].length; j++ )
			{
				verts[ vi++ ].set( min.x + i * xDelta, min.y + j * yDelta, heightmap[ i ][ j ] );
			}
		}
	}

	/**
	 * Makes a sheet of quads. Edge references will be null
	 * 
	 * @param columns
	 * @param rows
	 * @return a sheet of quads
	 */
	private Quad[][] makeSheet( int columns, int rows )
	{
		Quad[][] sheet = new Quad[ columns ][ rows ];

		for( int i = 0; i < columns; i++ )
		{
			for( int j = 0; j < rows; j++ )
			{
				int vi = j * ( columns + 1 ) + i;
				sheet[ i ][ j ] = new Quad( vi, vi + 1, vi + columns + 1, vi + columns + 2 );
			}
		}

		// add references to neighbour quads, or null if quad is on the
		// edge of the sheet
		for( int i = 0; i < columns; i++ )
		{
			for( int j = 0; j < rows; j++ )
			{
				Quad q = sheet[ i ][ j ];

				q.left = i > 0 ? sheet[ i - 1 ][ j ] : null;
				q.right = i < columns - 1 ? sheet[ i + 1 ][ j ] : null;
				q.below = j > 0 ? sheet[ i ][ j - 1 ] : null;
				q.above = j < rows - 1 ? sheet[ i ][ j + 1 ] : null;
			}
		}

		return sheet;
	}

	private class Quad
	{
		/**
		 * The quad below this one
		 */
		private Quad below;

		/**
		 * The index of the edge between this quad and the one below, or
		 * -1 if it has not been added yet
		 */
		private int belowEdgeIndex = -1;

		private Quad right;

		private int rightEdgeIndex = -1;

		private Quad left;

		private int leftEdgeIndex = -1;

		private Quad above;

		private int aboveEdgeIndex = -1;

		/**
		 * Vertex indices
		 */
		private final int bl, br, tl, tr;

		private Quad( int bl, int br, int tl, int tr )
		{
			this.bl = bl;
			this.br = br;
			this.tl = tl;
			this.tr = tr;
		}

		private void stitch( Quad quad )
		{
			below = below == null ? quad : below;
			right = right == null ? quad : right;
			left = left == null ? quad : left;
			above = above == null ? quad : above;
		}

		private int add( int edgeIndex, int faceIndex )
		{
			// check if our neighbour has already added the edge between
			// us. If not, add it ourselves
			// watch out for the edge cases
			belowEdgeIndex = below.below == this ? below.belowEdgeIndex : below.aboveEdgeIndex;
			if( belowEdgeIndex == -1 )
			{
				belowEdgeIndex = edgeIndex;
				edges[ edgeIndex ] = new Edge( bl, br );
				edgeIndex++;
			}

			rightEdgeIndex = right.right == this ? right.rightEdgeIndex : right.leftEdgeIndex;
			if( rightEdgeIndex == -1 )
			{
				rightEdgeIndex = edgeIndex;
				edges[ edgeIndex ] = new Edge( br, tr );
				edgeIndex++;
			}

			leftEdgeIndex = left.left == this ? left.leftEdgeIndex : left.rightEdgeIndex;
			if( leftEdgeIndex == -1 )
			{
				leftEdgeIndex = edgeIndex;
				edges[ edgeIndex ] = new Edge( bl, tl );
				edgeIndex++;
			}

			aboveEdgeIndex = above.above == this ? above.aboveEdgeIndex : above.belowEdgeIndex;
			if( aboveEdgeIndex == -1 )
			{
				aboveEdgeIndex = edgeIndex;
				edges[ edgeIndex ] = new Edge( tl, tr );
				edgeIndex++;
			}

			// diagonal
			int diagonalEdgeIndex = edgeIndex;
			edges[ edgeIndex ] = new Edge( bl, tr );
			edgeIndex++;

			faces[ faceIndex ] = new Face( belowEdgeIndex, rightEdgeIndex, diagonalEdgeIndex );

			faces[ faceIndex + 1 ] = new Face( leftEdgeIndex, aboveEdgeIndex, diagonalEdgeIndex );

			return edgeIndex;
		}
	}
}
