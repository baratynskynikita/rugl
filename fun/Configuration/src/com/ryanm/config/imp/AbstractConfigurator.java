/*
 * Created on 26-Jul-2004 by Ryan McNally
 */

package com.ryanm.config.imp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ryanm.config.Configurator;
import com.ryanm.config.ConfiguratorListener;
import com.ryanm.config.ValueListener;

/**
 * Eases implementation of a configurator. Handles all requirements of
 * a configurator, subclasses need only concern themselves with
 * defining variables, and getting/setting values.
 * 
 * @author ryanm
 */
public abstract class AbstractConfigurator implements Configurator
{
	private final String confName;

	private final String description;

	/**
	 * The parent configurator
	 */
	private Configurator parent = null;

	/**
	 * Indicates if the paths have been built for this configurator or
	 * not
	 */
	private boolean pathsDirty = true;

	/**
	 * The list that controls the content and structure of the
	 * configurator
	 */
	private List<Object> names = new LinkedList<Object>();

	private Map<String, Class> types = new HashMap<String, Class>();

	private Map<String, String> descriptions = new HashMap<String, String>();

	private Map<String, Object> range = new HashMap<String, Object>();

	private Map<String, Set<Object>> disabled = new HashMap<String, Set<Object>>();

	private Set<ValueListener> valueListeners = new LinkedHashSet<ValueListener>();

	private Set<ConfiguratorListener> confListeners =
			new LinkedHashSet<ConfiguratorListener>();

	private Map<String, ApplicationTarget> applicationTargets =
			new HashMap<String, ApplicationTarget>();

	/**
	 * Constructs a new AbstractConfigurator
	 * 
	 * @param name
	 *           The name for this configurator
	 */
	public AbstractConfigurator( String name )
	{
		confName = name;
		description = null;
	}

	/**
	 * Constructs a new AbstractConfigurator
	 * 
	 * @param name
	 *           The name for this configurator
	 * @param description
	 *           A decsriptive string for this configurator
	 */
	protected AbstractConfigurator( String name, String description )
	{
		confName = name;
		this.description = description;
	}

	@Override
	public final String getName()
	{
		return confName;
	}

	@Override
	public Object[] getNames()
	{
		return names.toArray();
	}

	/**
	 * Adds a variable to this configurator
	 * 
	 * @param variable
	 *           The name of the variable, or the Configurator object
	 *           itself, to add
	 */
	public void addVariable( String variable )
	{
		names.add( variable );

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableAdded( variable );
		}
	}

	/**
	 * Adds a sub-configurator
	 * 
	 * @param conf
	 *           The configurator to add
	 */
	public void addVariable( Configurator conf )
	{
		assert conf != null : "Attempted to add a null configurator to " + getPath();

		names.add( conf );
		pathsDirty = true;
		buildPaths();

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableAdded( conf );
		}
	}

	/**
	 * Removes a variable or subconfigurator from this configurator
	 * 
	 * @param variable
	 *           The name of the variable, or the configurator object,
	 *           to remove
	 */
	public void removeVariable( Object variable )
	{
		names.remove( variable );

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableRemoved( variable );
		}
	}

	@Override
	public Class getType( String name )
	{
		return types.get( name );
	}

	/**
	 * Sets the type of a variable
	 * 
	 * @param name
	 *           The name of the variable to set
	 * @param type
	 *           The type identifier of the variable
	 */
	public void setType( String name, Class type )
	{
		types.put( name, type );

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableTyped( name );
		}
	}

	@Override
	public String getDescription( String name )
	{
		return descriptions.get( name );
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description for a variable
	 * 
	 * @param name
	 *           The name of the variable to describe
	 * @param desc
	 *           The description for the variable
	 */
	public void setDescription( String name, String desc )
	{
		descriptions.put( name, desc );

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableDescribed( name );
		}
	}

	@Override
	public Object getRange( String name )
	{
		return range.get( name );
	}

	/**
	 * Sets the bounds for a variable. Permissable types to pass a s a
	 * range object are null, String[] and float[]. How these bounds
	 * are interpreted is dependent on the variable type. See the type
	 * flags in {@link Configurator} for examples of the default types
	 * 
	 * @param name
	 *           The variable to constrain
	 * @param bounds
	 *           The bounds object
	 */
	public void setRange( String name, Object bounds )
	{
		assert bounds == null || bounds instanceof Comparable[]
				|| bounds instanceof float[] : "Illegal bounds type detected for variable "
				+ getPath() + "/" + name;

		range.put( name, bounds );

		for( ConfiguratorListener listener : confListeners )
		{
			listener.variableRanged( name );
		}
	}

	/**
	 * Adds an application target for the specified variable. The
	 * indicated field will be updated with changes to the named
	 * variable, without the intervention of the subclass. This
	 * automates the most common case of using a configurator to
	 * control a simple variable.
	 * 
	 * @param name
	 *           The name of the variable
	 * @param object
	 *           The object that owns the relevant field
	 * @param fieldName
	 *           The name of the field
	 */
	public void setApplicationTarget( String name, Object object, String fieldName )
	{
		try
		{
			ApplicationTarget at =
					new ApplicationTarget( object, object.getClass().getDeclaredField(
							fieldName ) );

			applicationTargets.put( name, at );
		}
		catch( NoSuchFieldException nsfe )
		{
			nsfe.printStackTrace();
		}
	}

	/**
	 * Removes the application target for the specified variable
	 * 
	 * @param name
	 *           The name of the variable
	 */
	public void removeApplicationTarget( String name )
	{
		applicationTargets.remove( name );
	}

	@Override
	public final void setValue( String name, Object value )
	{
		// try for an application target first
		ApplicationTarget at = applicationTargets.get( name );

		if( at != null )
		{
			at.applyValue( value );
		}

		applyValue( name, value );

		// notify all valueListeners
		for( ValueListener listener : valueListeners )
		{
			listener.valueChanged( name );
		}
	}

	/**
	 * This should be overridden to actually apply the new value
	 * 
	 * @param name
	 *           The name of the variable to change
	 * @param value
	 *           The new value of that variable
	 */
	protected abstract void applyValue( String name, Object value );

	@Override
	public Object getValue( String name )
	{
		// try for an application target first
		ApplicationTarget at = applicationTargets.get( name );

		if( at != null )
		{
			return at.getValue();
		}

		return retrieveValue( name );
	}

	/**
	 * Get the current value of the named variable
	 * 
	 * @param name
	 *           The name of the variable
	 * @return The value of the variable
	 */
	public abstract Object retrieveValue( String name );

	@Override
	public String getPath()
	{
		if( parent == null )
		{
			return "/" + getName();
		}

		return parent.getPath() + "/" + getName();
	}

	@Override
	public void addValueListener( ValueListener listener )
	{
		valueListeners.add( listener );
	}

	@Override
	public void removeValueListener( ValueListener listener )
	{
		valueListeners.remove( listener );
	}

	@Override
	public void addConfiguratorListener( ConfiguratorListener listener )
	{
		confListeners.add( listener );
	}

	@Override
	public void removeConfiguratorListener( ConfiguratorListener listener )
	{
		confListeners.remove( listener );
	}

	@Override
	public void setGUIEnabled( String variable, boolean b, Object key )
	{
		assert key != null;

		if( b )
		{
			if( disabled.containsKey( variable ) && disabled.get( variable ).contains( key ) )
			{
				disabled.get( variable ).remove( key );

				if( disabled.get( variable ).size() == 0 )
				{
					// there are no more locks on this variable, we can
					// enable it
					disabled.remove( variable );

					// signal valueListeners
					for( ConfiguratorListener listener : confListeners )
					{
						listener.variableStatusChanged( variable, b );
					}
				}
			}
		}
		else
		{
			if( !disabled.containsKey( variable ) )
			{
				disabled.put( variable, new HashSet<Object>() );
				disabled.get( variable ).add( key );

				// signal valueListeners
				for( ConfiguratorListener listener : confListeners )
				{
					listener.variableStatusChanged( variable, b );
				}
			}
			else
			{
				disabled.get( variable ).add( key );
			}
		}
	}

	@Override
	public boolean isGUIEnabled( String variable )
	{
		return !disabled.containsKey( variable );
	}

	@Override
	public Configurator getParent()
	{
		return parent;
	}

	/**
	 * This should be called on the root configurator to build
	 * configurator path information
	 */
	public void buildPaths()
	{
		if( pathsDirty )
		{
			for( Object obj : names )
			{
				if( obj instanceof AbstractConfigurator )
				{
					( ( AbstractConfigurator ) obj ).parent = this;
					( ( AbstractConfigurator ) obj ).buildPaths();
				}
			}
		}

		pathsDirty = false;
	}

	/**
	 * Determines whether or not the paths have been built for this
	 * configurator
	 * 
	 * @return true if the paths have been built, false otherwise
	 */
	public boolean pathsBuilt()
	{
		return !pathsDirty;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder( "Configurator : " );
		buff.append( confName );
		return buff.toString();
	}

	/**
	 * Sets the values of the sink {@link Configurator}'s variables to
	 * be the same as the source {@link Configurator}. Variables with
	 * the same name will have their values copied accross. This
	 * process will recurse into sub-configurators
	 * 
	 * @param source
	 *           The source of the configuration
	 * @param sink
	 *           The configurator to change
	 */
	public static void copyValues( Configurator source, Configurator sink )
	{
		for( int i = 0; i < source.getNames().length; i++ )
		{
			if( source.getNames()[ i ] instanceof String )
			{
				String variable = ( String ) source.getNames()[ i ];
				sink.setValue( variable, source.getValue( variable ) );
			}
			else if( source.getNames()[ i ] instanceof Configurator )
			{
				if( sink.getNames()[ i ] instanceof Configurator )
				{
					Configurator subconf = ( Configurator ) source.getNames()[ i ];
					copyValues( subconf, ( Configurator ) sink.getNames()[ i ] );
				}
			}
		}
	}

	/**
	 * Encapsulates the information needed to automatically apply a
	 * value to a field
	 * 
	 * @author ryanm
	 */
	private class ApplicationTarget
	{
		/**
		 * The field to apply to
		 */
		private Field field;

		/**
		 * The object that owns the field
		 */
		private Object object;

		private ApplicationTarget( Object object, Field field )
		{
			this.object = object;
			this.field = field;

			assert Modifier.isPublic( field.getModifiers() );
		}

		private void applyValue( Object value )
		{
			try
			{
				field.set( object, value );
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

		private Object getValue()
		{
			try
			{
				return field.get( object );
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
			}

			return null;
		}

	}
}