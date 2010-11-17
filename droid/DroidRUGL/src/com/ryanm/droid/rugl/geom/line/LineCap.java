
package com.ryanm.droid.rugl.geom.line;

import java.util.List;

import com.ryanm.droid.rugl.util.geom.Vector2f;


/**
 * Interface for line cap decorations
 * 
 * @author ryanm
 */
public interface LineCap
{
	/**
	 * Compute the vertices and triangle indiecs needed for this
	 * {@link LineCap} decoration
	 * 
	 * @param endPoint
	 *           The endpoint of the line
	 * @param lineDirection
	 *           The direction that this line travels in. Will be a
	 *           normalised vector
	 * @param leftIndex
	 *           The index of the vertex on the left side of the line
	 * @param rightIndex
	 *           The index of the vertex on the right side of the line
	 * @param width
	 *           The width of the line
	 * @param verts
	 *           The list to add vertices to
	 * @param indices
	 *           The list to add triangle indices to
	 */
	public void createVerts( Vector2f endPoint, Vector2f lineDirection, short leftIndex,
			short rightIndex, float width, List<Vector2f> verts, List<Short> indices );
}
