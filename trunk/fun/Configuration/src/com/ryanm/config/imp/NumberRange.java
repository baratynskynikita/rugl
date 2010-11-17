
package com.ryanm.config.imp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a range limit to numerical variables. Can be aplied to
 * Integer and Float variables, in which case the input should be of
 * the form { min, max }, Vector variables, with input form { minX,
 * minY, minZ, maxX, maxY, maxZ }. Note that for Float and Integer
 * variable, specifying Float.NaN will result in no limit for that
 * particluar bound, i.e.: { 0, Float.NaN } will limit the variable to
 * any positive number.
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.METHOD, ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface NumberRange
{
	/**
	 * The boundary values
	 * 
	 * @return a float array of boundary values
	 */
	float[] value();
}
