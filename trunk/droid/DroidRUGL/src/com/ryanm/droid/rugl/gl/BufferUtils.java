
package com.ryanm.droid.rugl.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author ryanm
 */
public class BufferUtils
{
	/**
	 * @param length
	 * @return A direct, native-order buffer
	 */
	public static ByteBuffer createByteBuffer( int length )
	{
		return ByteBuffer.allocateDirect( length ).order( ByteOrder.nativeOrder() );
	}

	/**
	 * @param i
	 * @return A direct, native-order buffer
	 */
	public static IntBuffer createIntBuffer( int i )
	{
		return createByteBuffer( 4 * i ).asIntBuffer();
	}

	/**
	 * @param i
	 * @return A direct, native-order buffer
	 */
	public static FloatBuffer createFloatBuffer( int i )
	{
		return createByteBuffer( 4 * i ).asFloatBuffer();
	}

	/**
	 * @param i
	 * @return A direct, native-order buffer
	 */
	public static ShortBuffer createShortBuffer( int i )
	{
		return createByteBuffer( 2 * i ).asShortBuffer();
	}
}
