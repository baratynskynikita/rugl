
package com.ryanm.config;

import java.io.File;

import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector3f;

import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.StringRange;
import com.ryanm.config.imp.Variable;

/**
 * @author ryanm
 */
@ConfigurableType( "ConfTest" )
public class ConfTest
{
	/**
	 * @author ryanm
	 */
	public static enum FooEnum
	{
		/***/
		ZERO,
		/***/
		ONE,
		/***/
		TWO,
		/***/
		THREE,
		/***/
		FOUR,
		/***/
		FIVE;
	};

	/**
	 * 
	 */
	@Variable( "Bool" )
	public boolean b;

	/**
	 * 
	 */
	@Variable( "Colour" )
	public Color c = new Color();

	/**
	 * 
	 */
	@Variable( "Dimension" )
	public Dimension d = new Dimension();

	/**
	 * 
	 */
	@Variable( "Enumeration" )
	public FooEnum e = FooEnum.ZERO;

	/**
	 * 
	 */
	@Variable( "Float" )
	public float f;

	/**
	 * 
	 */
	@Variable( "Integer" )
	public int i;

	/**
	 * 
	 */
	@Variable( "String" )
	@StringRange( { "Hey", "there", "brett", "I", "see", "you", "looking", "down" } )
	public String s = "Hello";

	/**
	 * 
	 */
	@Variable( "String list" )
	@StringRange( { "I", "don't", "want", "to", "see", "my", "little", "buddy", "there",
			"with", "a", "frown" } )
	public String[] sa = new String[ 0 ];

	/**
	 * 
	 */
	@Variable( "Vector" )
	public Vector3f v = new Vector3f();

	/***/
	@Variable( "File" )
	public File file = new File( System.getProperty( "user.home" ) );
}
