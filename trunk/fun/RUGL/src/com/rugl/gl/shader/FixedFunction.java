
package com.rugl.gl.shader;

/**
 * A dummy shader that represents the fixed-function pipeline.
 * 
 * @author ryanm
 */
public class FixedFunction extends Program
{
	/**
	 * Only need one instance
	 */
	public static FixedFunction instance = new FixedFunction();

	private FixedFunction()
	{
		super( null, null );
	}

	@Override
	public String toString()
	{
		return "Fixed-function pipeline";
	}

	@Override
	public final void setUniforms()
	{
	}
}
