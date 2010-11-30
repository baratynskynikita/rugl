
package com.ryanm.droid.configtest;

import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Summary;
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
@Summary( "Cold shriveled testsicles" )
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
	@Summary( "It'd be nice if google would release more widgets" )
	@WidgetHint( Colour.class )
	public int colour = Colour.raspberry;

	/***/
	@Variable( "An enumeration" )
	@Summary( "Mutually exclusive" )
	public TestEnum enumeration = TestEnum.Foo;

	/***/
	@Variable( "A boolean" )
	@Summary( "true or false" )
	public boolean aBoolean = true;

	/***/
	@Variable( "A float" )
	@Summary( "floaty light" )
	@Category( "Numbers" )
	public float aFloat = 1;

	/***/
	@Variable( "A string" )
	@Summary( "The story of one man's downfall, and another's redemption\nDo linebreaks work?" )
	public String aString = "hello";

	/***/
	@Variable( "A range" )
	@Summary( "So these turn out to be really useful" )
	public Range range = new Range( 20, 40 );

	/***/
	@Variable( "A 2D Vector" )
	@Summary( "X and Y" )
	@Category( "Vectors" )
	public Vector2f vector2 = new Vector2f( 1, 2 );

	/***/
	@Variable( "A 3D Vector" )
	@Summary( "X, Y and Z" )
	@Category( "Vectors" )
	public Vector3f vector3 = new Vector3f( 1, 2, 3 );

	private int encap = 4;

	/***/
	@Variable
	public SubTest sub = new SubTest();

	/***/
	@Variable( "Named Subconf" )
	@Summary( "I've overridden the description!" )
	public SubTest namedSub = new SubTest();

	/**
	 * @return a number
	 */
	@Variable( "An encapsulated integer" )
	@Summary( "whole numbers only" )
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
	@Summary( "Standard description" )
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
	@Summary( "A method that will be called on applying the configuration" )
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
