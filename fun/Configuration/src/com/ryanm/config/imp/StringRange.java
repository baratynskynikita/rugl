
package com.ryanm.config.imp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to provide a range on the legal values of String, String list
 * and File variables. For String and String list variables, the value
 * array limits the String values that can be entered. For File
 * variables, the value array limits legal file suffixes
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface StringRange
{
	/**
	 * The range of legal values
	 */
	String[] value();
}
