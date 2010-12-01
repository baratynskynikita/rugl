
package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a descriptive string to an element
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface Summary
{
	/**
	 * The description for the variable or type
	 */
	String value();
}
