
package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking types, fields and methods that are
 * configurable.
 * <p>
 * Types:
 * <ul>
 * <li>Must be <code>public</code>
 * <li>Will take the supplied name value, or the type name if the
 * annotation value is left blank
 * </ul>
 * <p>
 * <p>
 * Primitive, <code>enum</code> and {@link String} fields:
 * <ul>
 * <li>Must be <code>public</code>
 * <li>Will take the supplied name value, or the field name if the
 * annotation value is left blank
 * </ul>
 * <p>
 * {@link Variable} type fields:
 * <ul>
 * <li>Must be <code>public</code>
 * <li>Will take the supplied name value, or the value of the type's
 * {@link Variable} annotation, or the type name if both are left
 * blank
 * </ul>
 * <p>
 * <p>
 * Encapsulated fields: Use a method to get and set the values -
 * allows validation of input
 * <ul>
 * <li>Getter and setter methods must be <code>public</code>
 * <li>Return type of the getter must be the same as the single
 * argument type of the setter
 * <li>Getter and setter methods must have the same {@link Variable}
 * annotation name value
 * <li>Will take the supplied name value, or the value of the type's
 * {@link Variable} annotation. If both of those are blank, the
 * methods names will be taken, they won't match up, and it won't work
 * <li>Read-only variables are possible by only supplying a getter
 * method
 * </ul>
 * <p>
 * <p>
 * Action Methods: Invoked when the configuration is applied
 * <ul>
 * <li>Must be <code>public</code>
 * <li>Will take the supplied name value, or the method name if left
 * blank
 * <li>Must have <code>void</code> return type and no arguments
 * <li>Must have a unique {@link Variable} name in the class
 * </ul>
 * <p>
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface Variable
{
	/**
	 * The name of the resulting type or variable. Leave blank or
	 * specify "" to just use the type, field or method name
	 */
	String value() default "";
}
