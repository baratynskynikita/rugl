
package com.ryanm.droid.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONObject;

import android.util.Log;

/**
 * Methods for extracting JSON from an object tree
 * 
 * @author ryanm
 */
class Extract
{
	private Extract()
	{
	}

	/**
	 * Extracts a {@link JSONObject} describing an object configuration
	 * tree
	 * 
	 * @param roots
	 *           the roots of the tree
	 * @return The configuration of that tree
	 */
	static JSONObject extract( Object... roots )
	{
		if( roots.length == 1 )
		{
			try
			{
				return extract( roots[ 0 ] );
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Trouble extracting configuration from "
						+ roots[ 0 ], e );
			}
		}
		JSONObject json = new JSONObject();
		for( int i = 0; i < roots.length; i++ )
		{
			if( roots[ i ] != null )
			{
				String name = Util.getName( roots[ i ] );
				try
				{
					JSONObject conf = extract( roots[ i ] );
					json.put( name, conf );
				}
				catch( Exception e )
				{
					Log.e( Configuration.LOG_TAG, "Trouble extracting configuration from "
							+ roots[ i ], e );
				}
			}
		}
		return json;
	}

	private static JSONObject extract( Object o ) throws Exception
	{
		JSONObject conf = new JSONObject();
		conf.putOpt( "desc", Util.getDescription( o ) );

		// fields

		for( Field f : o.getClass().getFields() )
		{
			String name = Util.getName( f );
			if( name != null )
			{
				conf.put( name, extractConfig( f, o ) );
			}
		}

		// methods

		LinkedList<Method> methods = new LinkedList<Method>();
		for( Method m : o.getClass().getMethods() )
		{
			methods.add( m );
		}

		while( !methods.isEmpty() )
		{
			Method m = methods.removeFirst();

			String name = Util.getName( m );

			if( name != null )
			{
				// find the counterpart
				Method n = null;
				Iterator<Method> iter = methods.iterator();
				while( iter.hasNext() )
				{
					Method method = iter.next();
					if( name.equals( Util.getName( method ) ) )
					{
						if( n == null )
						{
							n = method;
							iter.remove();
						}
						else
						{
							throw new ConfigError(
									"More than two methods annotated with the same name \"" + name
											+ "\" in " + o.getClass() );
						}
					}
				}

				conf.put( name, extractConfig( o, m, n ) );
			}
		}

		return conf;
	}

	@SuppressWarnings( "unchecked" )
	static JSONObject extractConfig( Field f, Object o ) throws Exception
	{
		try
		{
			Object value = f.get( o );

			if( value != null )
			{
				JSONObject conf = null;
				VariableType codec = VariableType.get( f.getType() );
				if( codec != null )
				{
					conf = new JSONObject();
					conf.put( "type", f.getType().getName() );
					conf.put( "value", codec.encode( value ) );
				}
				else
				{ // subconfigurable?
					conf = extract( value );
				}

				// get description, order, widget hint etc
				Util.getOptional( conf, f );
				return conf;
			}
		}
		catch( Exception e )
		{
			throw new Exception( "Problem with " + o.getClass() + "." + f.getName(), e );
		}

		return null;
	}

	@SuppressWarnings( "unchecked" )
	private static JSONObject extractConfig( Object o, Method m, Method n )
			throws Exception
	{
		JSONObject conf = null;

		if( n == null )
		{ // void or readonly
			if( m.getParameterTypes().length > 0 )
			{
				throw new ConfigError( "Solely named method " + o.getClass() + "."
						+ m.getName() + " has arguments, it is not an action or a getter" );
			}

			if( m.getReturnType() == void.class )
			{ // action
				conf = new JSONObject();
				conf.put( "type", "void" );
				Util.getOptional( conf, m );
			}
			else
			{ // readonly
				Object v = m.invoke( o );
				if( v != null )
				{
					VariableType codec = VariableType.get( v.getClass() );
					if( codec != null )
					{
						conf = new JSONObject();

						conf.put( "type", v.getClass().getName() );

						conf.put( "value", codec.encode( v ) );

						Util.getOptional( conf, m );

						return conf;
					}
					else
					{ // configurable type
						conf = extract( v );
						if( conf != null )
						{
							// override desc
							conf.putOpt( "desc", Util.getDescription( m ) );
						}
					}
				}

			}
		}
		else
		{
			Method setter = m;
			Method getter = n;
			if( n.getReturnType() == void.class )
			{
				setter = n;
				getter = m;
			}

			if( setter.getReturnType() != void.class
					|| setter.getParameterTypes().length != 1 )
			{
				throw new ConfigError( "Setter method " + setter.getDeclaringClass() + "."
						+ setter.getName() + " must have only one argument and a void return" );
			}

			if( getter.getReturnType() == void.class
					|| getter.getParameterTypes().length != 0 )
			{
				throw new ConfigError( "Getter method " + getter.getDeclaringClass() + "."
						+ getter.getName() + " must have zero arguments and a non-void return" );
			}

			if( getter.getReturnType() != setter.getParameterTypes()[ 0 ] )
			{
				throw new ConfigError( "Return type of " + getter.getName()
						+ " must match argument type of " + setter.getName() + " in "
						+ setter.getDeclaringClass() );
			}

			Object v = getter.invoke( o );
			VariableType codec = VariableType.get( getter.getReturnType() );
			if( codec != null )
			{
				conf = new JSONObject();
				conf.put( "type", getter.getReturnType().getName() );
				conf.put( "value", codec.encode( v ) );

				Util.getOptional( conf, getter );
				Util.getOptional( conf, setter );

				return conf;
			}
			else
			{ // configurable type
				conf = extract( v );
				if( conf != null )
				{
					// override desc
					Util.getOptional( conf, getter );
					Util.getOptional( conf, setter );
				}
			}
		}

		return conf;
	}
}
