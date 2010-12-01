
package com.ryanm.droid.config.annote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ryanm.droid.config.VariableType;

/**
 * Annotation for overriding the type of {@link Variable}s when
 * selecting an appropriate {@link VariableType} to create a widget.
 * For instance, colours might be stored as ints, but it's not useful
 * to edit them as such
 * 
 * @author ryanm
 */
@Documented
@Target( { ElementType.FIELD, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface WidgetHint
{
	/**
	 * The desired variable type.
	 */
	Class value();
}
