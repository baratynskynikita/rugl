
package com.ryanm.droid.configtest;

import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Description;
import com.ryanm.droid.config.annote.Order;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.config.annote.WidgetHint;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.Vector2f;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.droid.rugl.util.math.Range;

/**
 * @author ryanm
 */
@Variable( "Testy" )
@Description( "Cold shriveled testsicles" )
public class ConfTest
{
	/***/
	public enum TestEnum
	{
		/***/
		Foo,
		/***/
		Bar,
		/***/
		Baz,
		/***/
		Blah,
		/***/
		Flooble
	};

	/***/
	@Variable( "A colour" )
	@Description( "It'd be nice if google would release more widgets" )
	@WidgetHint( Colour.class )
	public int colour = Colour.raspberry;

	/***/
	@Variable( "An enumeration" )
	@Description( "Mutually exclusive" )
	public TestEnum enumeration = TestEnum.Foo;

	/***/
	@Variable( "A boolean" )
	@Description( "true or false" )
	public boolean aBoolean = true;

	/***/
	@Variable( "A float" )
	@Description( "floaty light" )
	@Category( "Numbers" )
	public float aFloat = 1;

	/***/
	@Variable( "A string" )
	@Description( "The story of one man's downfall, and another's redemption\nDo linebreaks work?" )
	public String aString = "hello";

	/***/
	@Variable( "A range" )
	@Description( "So these turn out to be really useful" )
	public Range range = new Range( 20, 40 );

	/***/
	@Variable( "A 2D Vector" )
	@Description( "X and Y" )
	@Category( "Vectors" )
	public Vector2f vector2 = new Vector2f( 1, 2 );

	/***/
	@Variable( "A 3D Vector" )
	@Description( "X, Y and Z" )
	@Category( "Vectors" )
	public Vector3f vector3 = new Vector3f( 1, 2, 3 );

	private int encap = 4;

	/***/
	@Variable
	@Order( 0 )
	public SubTest sub = new SubTest();

	/***/
	@Variable( "Named Subconf" )
	@Description( "I've overridden the description!" )
	public SubTest namedSub = new SubTest();

	/**
	 * @return a number
	 */
	@Variable( "An encapsulated integer" )
	@Description( "whole numbers only" )
	@Category( "Numbers" )
	public int getEncap()
	{
		return encap;
	}

	/**
	 * @param i
	 */
	@Variable( "An encapsulated integer" )
	public void setEncap( int i )
	{
		encap = i;
	}

	/**
	 * @author ryanm
	 */
	@Variable( "Subconf" )
	@Description( "Standard description" )
	public static class SubTest
	{
		/***/
		@Variable
		public int subnum = 5;

		/***/
		@Variable
		public String subString = "boo!";
	}

	/***/
	@Variable( )
	@Description( "A method that will be called on applying the configuration" )
	public void action()
	{
		Log.i( Configuration.LOG_TAG, "Action!" );
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "ConfTest" );
		buff.append( "\n\tenum = " + enumeration );
		buff.append( "\n\tbool = " + aBoolean );
		buff.append( "\n\tfloat = " + aFloat );
		buff.append( "\n\tencapInt = " + encap );
		buff.append( "\n\tString = " + aString );
		buff.append( "\n\tColour = " + Colour.toString( colour ) );
		buff.append( "\n\trange = " + range );
		buff.append( "\n\tvec2 = " + vector2 );
		buff.append( "\n\tvec3 = " + vector3 );
		buff.append( "\n\t\tsub = " + sub.subnum + " " + sub.subString );
		buff.append( "\n\t\tnsub = " + namedSub.subnum + " " + namedSub.subString );
		return buff.toString();
	}
}
