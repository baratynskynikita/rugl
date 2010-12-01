
package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for ordering {@link Variable}s. Variables without order
 * values are sorted alphabetically after those with. The order of a
 * {@link Category} is given by the minimum order of its member
 * {@link Variable}s
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface Order
{
	/**
	 * The order of the variable. Lower numbers appear at the top of
	 * the list
	 */
	int value();
}
