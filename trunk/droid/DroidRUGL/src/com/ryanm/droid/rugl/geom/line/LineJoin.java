
package com.ryanm.droid.rugl.geom.line;

import java.util.List;

import com.ryanm.droid.rugl.util.geom.Vector2f;


/**
 * Interface for line join decorations
 * 
 * @author ryanm
 */
public interface LineJoin
{
	/**
	 * Compute the vertices and triangle indices needed for the
	 * decoration. The last three elements of the vertex list are v1,
	 * join and v2
	 * 
	 * @param v1
	 *           An outer vertex of the join
	 * @param join
	 *           The inner vertex of the join
	 * @param v2
	 *           Another outer vertex of the join
	 * @param corner
	 *           The intersection of the underlying line segments
	 * @param verts
	 *           The list to add vertices to
	 * @param indices
	 *           The list to add triangle vertices to
	 */
	public void createVerts( Vector2f v1, Vector2f join, Vector2f v2, Vector2f corner,
			List<Vector2f> verts, List<Short> indices );
}
