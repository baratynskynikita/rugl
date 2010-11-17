
package com.ryanm.config.imp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote members of a class that are to be included in the
 * Configurator. Can be applied to public variables or get/set
 * methods. Note that variables with this annotation must be public to
 * appear in the configurator. Getter methods must have no parameters
 * and an appropriate return type. Setter methods must have a void
 * return type and exactly one parameter, again with appropriate type.
 * Action methods must have no parameters, and a void return type.
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface Variable
{
	/**
	 * The name of the variable
	 */
	String value() default "";
}
