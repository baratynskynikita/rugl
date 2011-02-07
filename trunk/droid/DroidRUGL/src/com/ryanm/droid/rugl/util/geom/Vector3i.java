
package com.ryanm.droid.rugl.util.geom;

/**
 * Integer-based vector
 * 
 * @author ryanm
 */
public class Vector3i
{
	/***/
	public int x;

	/***/
	public int y;

	/***/
	public int z;

	/**
	 * @param v
	 */
	public void set( Vector3i v )
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set( int x, int y, int z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString()
	{
		return "( " + x + ", " + y + ", " + z + " )";
	}
}
