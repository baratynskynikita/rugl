package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines numerical range limits. Uses strings for now due to <a
 * href="http://code.google.com/p/android/issues/detail?id=5964">Issue 5964</a>.
 * 
 * @author ryanm
 */
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberRange {
	/**
	 * The boundary values
	 * 
	 * @return an array of boundary values
	 */
	String[] value();
}
