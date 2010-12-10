
package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking a boolean field or a void-return/no-arg
 * method. No widget will be generated for this field or method. The
 * boolean will be set to true, or the method called, if the value of
 * any other variable in the type is altered in a configuration
 * application. This provides a convenient way to react to
 * configuration changes without having to encapsulate every field
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface DirtyFlag
{
	/**
	 * If <code>true</code>, the dirty flag will also be set if any
	 * variable deeper in the tree is changed
	 */
	boolean watchTree() default false;
}
