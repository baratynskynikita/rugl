package com.ryanm.droid.config;

import com.ryanm.droid.config.annote.Description;
import com.ryanm.droid.config.annote.EncapVariable;
import com.ryanm.droid.config.annote.NumberRange;
import com.ryanm.droid.config.annote.StringRange;
import com.ryanm.droid.config.annote.Variable;

/**
 * @author ryanm
 */
@Variable("Testy")
@Description("testicles")
public class ConfTest {
	/***/
	@Variable
	@Description("a description")
	public float number = 1;

	/***/
	@Variable
	@NumberRange({ "0", "10" })
	public float unnamedNumber = 2;

	/***/
	@Variable("A string")
	@StringRange({ "foo", "bar" })
	public String s = "hello";

	private int encap = 4;

	/***/
	@Variable
	public SubTest sub = new SubTest();

	/***/
	@Variable("named sub")
	@Description("overridden")
	public SubTest namedSub = new SubTest();

	/**
	 * @return a number
	 */
	@EncapVariable("an encapsulated integer")
	@Description("whole numbers only")
	public int getEncap() {
		return encap;
	}

	/**
	 * @param i
	 */
	@EncapVariable("an encapsulated integer")
	@NumberRange({ "10", "100" })
	public void setEncap(int i) {
		encap = i;
	}

	/**
	 * @author ryanm
	 */
	@Variable("Subconf")
	@Description("Standard")
	public static class SubTest {
		/***/
		@Variable
		public int subnum = 5;
	}
}
