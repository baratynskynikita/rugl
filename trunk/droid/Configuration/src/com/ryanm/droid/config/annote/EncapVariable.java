package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that encapsulate variables
 * 
 * @author ryanm
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncapVariable {
	/**
	 * The name of the resulting Configurator
	 */
	String value();
}
