
package com.ryanm.droid.configtest;

import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Order;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;

/**
 * @author ryanm
 */
@Variable( "Testy" )
@Summary( "Icy shriveled testsicles" )
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
	};

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
	@Summary( "it's floaty light" )
	@Category( "Numbers" )
	public float aFloat = 1;

	/***/
	@Variable( "A string" )
	@Summary( "The story of one man's downfall, and another's redemption\nDo linebreaks work?" )
	public String aString = "hello";

	private int encap = 4;

	/***/
	private SubTest sub = new SubTest();

	/**
	 * @return the sub
	 */
	@Variable( "Encapsulated sub-configurable" )
	public SubTest getSub()
	{
		return sub;
	}

	/**
	 * @param sub
	 */
	@Variable( "Encapsulated sub-configurable" )
	public void setSub( SubTest sub )
	{
		this.sub = sub;
	}

	/***/
	@Variable( "Named sub-configurable" )
	@Summary( "I've overridden the name and description!" )
	public SubTest namedSub = new SubTest();

	/**
	 * @return a number
	 */
	@Variable( "An encapsulated integer" )
	@Summary( "whole numbers only" )
	@Category( "Numbers" )
	@Order( 0 )
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

		@Override
		public String toString()
		{
			return subnum + " " + subString;
		}
	}

	/***/
	@Variable( "An action" )
	@Summary( "Tick the box and the method will be called on application" )
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
		buff.append( "\n\t\tsub = " + sub );
		buff.append( "\n\t\tnsub = " + namedSub );
		return buff.toString();
	}
}
