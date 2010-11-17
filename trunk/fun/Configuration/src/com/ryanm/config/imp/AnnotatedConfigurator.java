
package com.ryanm.config.imp;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ryanm.config.Configurable;
import com.ryanm.config.Configurator;

/**
 * A configurator that is constructed from an object annotated with
 * the {@link ConfigurableType} annotation.
 * 
 * @author ryanm
 */
public class AnnotatedConfigurator extends AbstractConfigurator
{
	private final Object configurable;

	/**
	 * Maps variable names to getter methods
	 */
	private Map<String, Method> getters = new HashMap<String, Method>();

	/**
	 * Maps variable names to setter methods
	 */
	private Map<String, Method> setters = new HashMap<String, Method>();

	/**
	 * Maps variable names to fields
	 */
	private Map<String, Field> fields = new HashMap<String, Field>();

	/**
	 * Builds a new {@link Configurator} for the supplied object
	 * 
	 * @param configurable
	 *           The object to configure
	 * @return A configurator, or null if the supplied object is not
	 *         configurable
	 */
	public static AnnotatedConfigurator buildConfigurator( Object configurable )
	{
		if( configurable.getClass().isAnnotationPresent( ConfigurableType.class ) )
		{
			return new AnnotatedConfigurator( configurable );
		}
		else
		{
			return null;
		}
	}

	/**
	 * Builds a new {@link Configurator} for the supplied object
	 * 
	 * @param configurable
	 *           The object to configure
	 * @param preferredName
	 *           The preferred name for the resulting
	 *           {@link Configurator}. The name specified in the
	 *           {@link ConfigurableType} annotation will be ignored
	 * @param preferredDescription
	 *           The preferred description for the resulting
	 *           {@link Configurator}, or null to defer to the default
	 *           description supplied by the class
	 * @return A {@link Configurator}, or null if the supplied object
	 *         is not configurable
	 */
	public static AnnotatedConfigurator buildConfigurator( Object configurable,
			String preferredName, String preferredDescription )
	{
		if( configurable.getClass().isAnnotationPresent( ConfigurableType.class ) )
		{
			return new AnnotatedConfigurator( configurable, preferredName,
					preferredDescription );
		}
		else
		{
			return null;
		}
	}

	/**
	 * @param configurable
	 */
	protected AnnotatedConfigurator( Object configurable )
	{
		super(
				configurable.getClass().getAnnotation( ConfigurableType.class ).value(),
				configurable.getClass().getAnnotation( Description.class ) != null ? configurable
						.getClass().getAnnotation( Description.class ).value()
						: null );

		this.configurable = configurable;

		build();
	}

	/**
	 * @param configurable
	 * @param preferredName
	 * @param preferredDescription
	 */
	protected AnnotatedConfigurator( Object configurable, String preferredName,
			String preferredDescription )
	{
		super( preferredName,
				preferredDescription != null ? preferredDescription : configurable.getClass()
						.getAnnotation( Description.class ) != null ? configurable.getClass()
						.getAnnotation( Description.class ).value() : null );
		this.configurable = configurable;

		build();
	}

	@SuppressWarnings( "unchecked" )
	private void build()
	{
		List<ConfiguratorElement> elements = new LinkedList<ConfiguratorElement>();

		// scan the fields for sub-configurators and public members
		for( Field f : configurable.getClass().getFields() )
		{
			if( f.isAnnotationPresent( Variable.class ) )
			{
				Class c = f.getType();
				if( Configurable.class.isAssignableFrom( c )
						|| c.isAnnotationPresent( ConfigurableType.class ) )
				{
					elements.add( new SubConfElement( f ) );
				}
				else
				{
					elements.add( new VariableElement( f ) );
				}
			}
		}

		Map<String, EncapsulatedVariableElement> methods =
				new HashMap<String, EncapsulatedVariableElement>();

		// scan the methods for variables
		for( Method m : configurable.getClass().getMethods() )
		{
			if( m.isAnnotationPresent( Variable.class ) )
			{
				String name = m.getAnnotation( Variable.class ).value();

				if( methods.containsKey( name ) )
				{
					methods.get( name ).update( m );
				}
				else
				{
					methods.put( name, new EncapsulatedVariableElement( m ) );
				}
			}
		}

		elements.addAll( methods.values() );

		Collections.sort( elements, new Comparator<ConfiguratorElement>() {

			@Override
			public int compare( ConfiguratorElement o1, ConfiguratorElement o2 )
			{
				if( o1.priority < o2.priority )
				{
					return -1;
				}
				else if( o1.priority > o2.priority )
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}

		} );

		for( ConfiguratorElement element : elements )
		{
			element.addToConfigurator( this );
		}

		buildPaths();
	}

	private abstract class ConfiguratorElement
	{
		protected float priority = 0;

		/**
		 * Adds this element to the supplied
		 * {@link AbstractConfigurator}.
		 * 
		 * @param conf
		 *           The {@link AbstractConfigurator} to add to
		 */
		protected abstract void addToConfigurator( AbstractConfigurator conf );
	}

	private class VariableElement extends ConfiguratorElement
	{
		private Field field;

		private VariableElement( Field f )
		{
			assert f.isAnnotationPresent( Variable.class );

			assert Modifier.isPublic( f.getModifiers() ) : "Variable \"" + f
					+ "\" is not accessible";

			field = f;

			Priority p = field.getAnnotation( Priority.class );
			if( p != null )
			{
				priority = p.value();
			}
		}

		@Override
		protected void addToConfigurator( AbstractConfigurator conf )
		{
			String name = field.getAnnotation( Variable.class ).value().trim();

			if( name.length() == 0 )
			{
				name = field.getName();
			}

			conf.addVariable( name );
			conf.setType( name, field.getType() );

			Description d = field.getAnnotation( Description.class );
			if( d != null )
			{
				conf.setDescription( name, d.value() );
			}

			conf.setRange( name, getRangeObject( field ) );

			assert !fields.containsKey( name ) : "Duplicate variable name \"" + name
					+ "\" found when constructing configurator " + getName();
			fields.put( name, field );
		}
	}

	private class EncapsulatedVariableElement extends ConfiguratorElement
	{
		private final String name;

		private Class type = null;

		private String description = null;

		private Object range = null;

		private boolean prioritySet = false;

		private Method getter = null;

		private Method setter = null;

		private EncapsulatedVariableElement( Method m )
		{
			assert m.isAnnotationPresent( Variable.class );

			Variable cv = m.getAnnotation( Variable.class );

			name = cv.value();

			update( m );
		}

		private void update( Method m )
		{
			Variable cv = m.getAnnotation( Variable.class );

			assert name.equals( cv.value() );

			// determine if a setter or a getter
			if( m.getParameterTypes().length == 0 )
			{ // there are no arguments, it's a getter or an action
				if( m.getReturnType().equals( void.class ) )
				{ // it's void return, it's an action
					setter = m;

					assert type == null : "Second methods marked as same ACTION configurable :\""
							+ m.getName() + "\"";

					type = m.getReturnType();

					assert type.equals( void.class );
				}
				else
				{ // it's a getter, check the return type to see if it
					// matches a legal type
					getter = m;

					// set or confirm the type
					if( type != null )
					{
						assert type.equals( m.getReturnType() ) : "Expecting return type "
								+ type + " on method " + m.getName();
					}
					else
					{
						type = m.getReturnType();
					}
				}
			}
			else if( m.getParameterTypes().length == 1 )
			{ // there's one argument, it's a setter
				setter = m;

				// set or confirm the type
				if( type != null )
				{
					assert type.equals( m.getParameterTypes()[ 0 ] ) : "Expecting parameter type "
							+ type + " on method " + m.getName();
				}
				else
				{
					type = m.getParameterTypes()[ 0 ];
				}
			}
			else
			{
				assert false : m;
			}

			Priority p = m.getAnnotation( Priority.class );
			if( p != null )
			{
				assert !prioritySet : "Variable \"" + name + "\" has it's priority set twice";
				priority = p.value();
				prioritySet = true;
			}

			Description d = m.getAnnotation( Description.class );
			if( d != null )
			{
				assert description == null : "Variable \"" + name
						+ "\" has it's description set twice";
				description = d.value();
			}

			Object r = getRangeObject( m );

			assert !( range != null && r != null ) : "Variable \"" + name
					+ "\" has its range set twice";

			if( r != null )
			{
				range = r;
			}
		}

		@Override
		protected void addToConfigurator( AbstractConfigurator conf )
		{
			assert name != null;
			assert type != null;

			conf.addVariable( name );
			conf.setType( name, type );

			if( description != null )
			{
				conf.setDescription( name, description );
			}

			if( range != null )
			{
				conf.setRange( name, range );
			}

			assert type.equals( void.class ) || getter != null : "Variable \"" + name
					+ "\" of type \"" + type
					+ "\" lacks a getter method. Is the method present and public?";

			assert setter != null : "Variable \"" + name
					+ "\" lacks a setter method. Is the method present and public?";

			assert !getters.containsKey( name ) && !setters.containsKey( name ) : "Duplicate variable name \""
					+ name + "\" found when constructing configurator " + getName();

			getters.put( name, getter );
			setters.put( name, setter );
		}
	}

	private class SubConfElement extends ConfiguratorElement
	{

		private Configurator subconf;

		private SubConfElement( Field f )
		{
			try
			{
				// deal with null fields
				if( f.get( configurable ) != null )
				{
					// look for name, description overrides. We can only do
					// this with annotated configuration
					Variable cv = f.getAnnotation( Variable.class );
					if( cv.value().length() >= 1 )
					{
						String desc =
								f.getAnnotation( Description.class ) == null ? null : f
										.getAnnotation( Description.class ).value();

						subconf =
								AnnotatedConfigurator.buildConfigurator( f.get( configurable ),
										cv.value(), desc );

						assert subconf != null : "Field \"" + f.getDeclaringClass() + "."
								+ f.getName()
								+ " must be annotated to apply name/description override";
					}
					else
					{
						subconf = ConfGet.getConfigurator( f.get( configurable ) );
					}

					assert subconf != null : "Field \""
							+ f.getDeclaringClass()
							+ "."
							+ f.getName()
							+ "\" is not Configurable, returns a null configurator or is not annotated";
				}

				Priority p = f.getAnnotation( Priority.class );
				if( p != null )
				{
					priority = p.value();
				}
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
				assert false;
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
				assert false;
			}
		}

		@Override
		protected void addToConfigurator( AbstractConfigurator conf )
		{
			if( subconf != null )
			{
				conf.addVariable( subconf );
			}
		}

	}

	@Override
	protected void applyValue( String name, Object value )
	{
		if( setters.containsKey( name ) )
		{
			Method m = setters.get( name );

			try
			{
				if( m != null )
				{
					if( getType( name ) == void.class )
					{
						m.invoke( configurable );
					}
					else
					{
						m.invoke( configurable, value );
					}
				}
			}
			catch( IllegalArgumentException e )
			{
				System.err.println( name + " = " + value );
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				System.err.println( name + " = " + value );
				e.printStackTrace();
			}
			catch( InvocationTargetException e )
			{
				System.err.println( name + " = " + value );
				e.printStackTrace();
			}
		}
		else if( fields.containsKey( name ) )
		{
			Field f = fields.get( name );

			try
			{
				if( f.getType().equals( int.class ) )
				{
					f.set( configurable, new Integer( ( ( Number ) value ).intValue() ) );
				}
				else if( f.getType().equals( float.class ) )
				{
					f.set( configurable, new Float( ( ( Number ) value ).floatValue() ) );
				}
				else
				{
					f.set( configurable, value );
				}
			}
			catch( IllegalArgumentException e )
			{
				System.err.println( name + " = " + value );
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				System.err.println( name + " = " + value );
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object retrieveValue( String name )
	{
		if( getters.containsKey( name ) )
		{
			Method m = getters.get( name );

			try
			{
				if( m != null )
				{
					return m.invoke( configurable );
				}
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
			}
			catch( InvocationTargetException e )
			{
				e.printStackTrace();
			}
		}
		else if( fields.containsKey( name ) )
		{
			try
			{
				return fields.get( name ).get( configurable );
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	private static Object getRangeObject( AccessibleObject m )
	{
		Object range = null;

		NumberRange nr = m.getAnnotation( NumberRange.class );
		if( nr != null )
		{
			range = nr.value();
		}

		StringRange sr = m.getAnnotation( StringRange.class );
		if( sr != null )
		{
			assert range == null : "Variable \"" + m.toString()
					+ "\" has it's range set as numerical AND String. Make up your mind!";

			range = sr.value();
		}

		return range;
	}
}
