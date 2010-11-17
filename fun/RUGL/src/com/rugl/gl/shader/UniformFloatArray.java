
package com.rugl.gl.shader;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

/***/
public class UniformFloatArray
{
	/***/
	public final Program program;

	/***/
	public final String name;

	/***/
	public final int location;

	/***/
	private float[] dataArray;

	private FloatBuffer buffer;

	/**
	 * @param program
	 * @param name
	 * @param location
	 * @param size
	 */
	protected UniformFloatArray( Program program, String name, int location, int size )
	{
		this.program = program;
		this.name = name;
		this.location = location;

		dataArray = new float[ size ];
		buffer = BufferUtils.createFloatBuffer( size );
	}

	/**
	 * @param value
	 * @param index
	 */
	public void set( float value, int index )
	{
		dataArray[ index ] = value;

		submitData();
	}

	/**
	 * @param data
	 */
	public void set( float[] data )
	{
		assert data.length <= dataArray.length;

		for( int i = 0; i < data.length; i++ )
		{
			dataArray[ i ] = data[ i ];
		}

		submitData();
	}

	private void submitData()
	{
		buffer.rewind();
		buffer.put( dataArray );
		buffer.flip();

		GL20.glUniform1( location, buffer );
	}

	/**
	 * @return the size of the array
	 */
	public int getLength()
	{
		return dataArray.length;
	}
}
