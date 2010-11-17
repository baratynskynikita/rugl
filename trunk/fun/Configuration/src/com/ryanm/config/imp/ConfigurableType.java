
package com.ryanm.config.imp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking types that can be used to construct a
 * AnnotatedConfigurator
 * 
 * @author ryanm
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
public @interface ConfigurableType
{
	/**
	 * A human-friendly name for this object. This will be overridden
	 * by a {@link Variable} name value
	 */
	String value();
}
