
package com.ryanm.config.imp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ryanm.config.Configurator;

/**
 * Used to indicate an elements' position in the configurator.
 * Configurable elements have a default priority of 0;
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface Priority
{
	/**
	 * The priority of the element. Lower numbers appear earlier in the
	 * {@link Configurator}
	 */
	int value() default 0;
}
