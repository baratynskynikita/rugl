
package com.ryanm.droid.rugl.geom;

import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.State;
import com.ryanm.droid.rugl.gl.enums.DrawMode;

/**
 * Utility for making wireframe shapes
 * 
 * @author ryanm
 */
public class WireUtil
{
	/**
	 * Typical render state for wireframe rendering
	 */
	public static State state = GLUtil.typicalState.with( DrawMode.Lines );

	/**
	 * @return a wireframe unit cube i.e.: from (0,0,0) to (1,1,1)
	 */
	public static Shape unitCube()
	{
		float[] verts = new float[] {
				// 0 nbl
				0, 0, 0,
				// 1 ntl
				0, 1, 0,
				// 2 nbr
				1, 0, 0,
				// 3 ntr
				1, 1, 0,
				// 4 fbl
				0, 0, 1,
				// 5 ftl
				0, 1, 1,
				// 6 fbr
				1, 0, 1,
				// 7 ftr
				1, 1, 1 };

		short[] li = new short[] {
				// x dir
				0, 2, 1, 3, 4, 6, 5, 7,
				// y dir
				0, 1, 2, 3, 4, 5, 6, 7,
				// z dir
				0, 4, 1, 5, 2, 6, 3, 7 };

		return new Shape( verts, li );
	}
}
