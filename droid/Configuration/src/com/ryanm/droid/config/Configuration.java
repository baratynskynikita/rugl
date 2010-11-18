
package com.ryanm.droid.config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ryanm.droid.config.annote.Description;
import com.ryanm.droid.config.annote.EncapVariable;
import com.ryanm.droid.config.annote.NumberRange;
import com.ryanm.droid.config.annote.StringRange;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.config.serial.Codec;

/**
 * @author ryanm
 */
public class Configuration
{
	/**
	 * Logging tag
	 */
	public static final String LOG_TAG = "Configuration";

	/**
	 * The activity request flag used by the {@link ConfigActivity}
	 * (value = 1)
	 */
	public static final int ACTIVITY_REQUEST_FLAG = 1;

	/**
	 * Starts a configuration activity
	 * 
	 * @param returnTo
	 *           The activity to return to when we are done configuring
	 * @param roots
	 *           The objects to configure
	 */
	public static void launchConfig( Activity returnTo, Object... roots )
	{
		// launch config
		Intent i = new Intent( returnTo, ConfigActivity.class );
		i.putExtra( "conf", extract( roots ).toString() );
		returnTo.startActivityForResult( i, ACTIVITY_REQUEST_FLAG );
	}

	/**
	 * Applies a configuration to the objects
	 * 
	 * @param i
	 *           The return intent from the configuration activity
	 * @param roots
	 *           The objects to apply the config to
	 */
	public static void applyConfiguration( Intent i, Object... roots )
	{
		String c = i.getStringExtra( "conf" );

		if( c != null )
		{
			try
			{
				@SuppressWarnings( "unused" )
				JSONObject json = new JSONObject( c );

				// apply
			}
			catch( Exception e )
			{
				Log.e( LOG_TAG, "Problem applying config", e );
			}
		}
	}

	/**
	 * Extracts a {@link JSONObject} describing an object configuration
	 * tree
	 * 
	 * @param roots
	 *           the roots of the tree
	 * @return The configuration of that tree
	 */
	public static JSONObject extract( Object... roots )
	{
		JSONObject json = new JSONObject();

		for( int i = 0; i < roots.length; i++ )
		{
			if( roots[ i ] != null )
			{
				String name = getName( roots[ i ] );
				try
				{
					JSONObject conf = extractConfig( roots[ i ] );
					json.put( name, conf );
				}
				catch( Exception e )
				{
					Log.e( LOG_TAG, "Trouble extracting configuration from " + roots[ i ], e );
				}
			}
		}

		return json;
	}

	private static JSONObject extractConfig( Object o ) throws Exception
	{
		JSONObject conf = new JSONObject();
		conf.putOpt( "desc", getDescription( o ) );

		// fields

		for( Field f : o.getClass().getFields() )
		{
			String name = getName( f );
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

			String name = getName( m );

			if( name != null )
			{
				// find the counterpart
				Method n = null;
				Iterator<Method> iter = methods.iterator();
				while( iter.hasNext() )
				{
					Method method = iter.next();
					if( name.equals( getName( method ) ) )
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
	private static JSONObject extractConfig( Field f, Object o ) throws Exception
	{
		try
		{
			Object value = f.get( o );

			if( value != null )
			{
				Codec codec = Codec.getCodec( f.getType() );
				if( codec != null )
				{
					JSONObject conf = new JSONObject();

					getOptional( conf, f );

					// type
					conf.put( "type", f.getType().toString() );

					// value
					conf.put( "value", codec.encode( value ) );

					return conf;
				}
				else
				{ // subconfigurable?
					JSONObject conf = extractConfig( value );

					if( conf != null )
					{
						// override the description
						conf.putOpt( "desc", getDescription( f ) );

						return conf;
					}
				}
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
				conf.putOpt( "desc", getDescription( m ) );
			}
			else
			{ // readonly
				Object v = m.invoke( o );
				if( v != null )
				{
					Codec codec = Codec.getCodec( v.getClass() );
					if( codec != null )
					{
						conf = new JSONObject();

						getOptional( conf, m );

						conf.put( "type", v.getClass().toString() );

						conf.put( "value", codec.encode( v ) );

						return conf;
					}
					else
					{ // configurable type
						conf = extractConfig( v );
						if( conf != null )
						{
							// override desc
							conf.putOpt( "desc", getDescription( m ) );
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
			Codec codec = Codec.getCodec( getter.getReturnType() );
			if( codec != null )
			{
				conf = new JSONObject();

				getOptional( conf, getter );
				getOptional( conf, setter );

				conf.put( "type", getter.getReturnType() );

				conf.put( "value", codec.encode( v ) );

				return conf;
			}
			else
			{ // configurable type
				conf = extractConfig( v );
				if( conf != null )
				{
					// override desc
					conf.putOpt( "desc", getDescription( getter ) );
					conf.putOpt( "desc", getDescription( setter ) );
				}
			}
		}

		return conf;
	}

	/**
	 * Adds the description and ranges to the json
	 * 
	 * @param conf
	 * @param f
	 * @throws JSONException
	 */
	private static void getOptional( JSONObject conf, AccessibleObject f )
			throws JSONException
	{
		conf.putOpt( "desc", getDescription( f ) );

		// range
		String[] sr = getStringRange( f );
		if( sr != null )
		{
			JSONArray ja = new JSONArray();
			for( String s : sr )
			{
				ja.put( s );
			}
			conf.put( "stringrange", ja );
		}

		String[] nr = getNumberRange( f );
		if( nr != null )
		{
			JSONArray ja = new JSONArray();
			for( String n : nr )
			{
				ja.put( n );
			}
			conf.put( "numrange", ja );
		}
	}

	/**
	 * The configuration name of a field
	 * 
	 * @param f
	 * @return The name of the field, or <code>null</code> if the field
	 *         is not {@link Variable}
	 */
	public static String getName( Field f )
	{
		String name = null;

		Variable v = f.getAnnotation( Variable.class );

		if( v != null )
		{
			name = v.value();

			if( name.length() == 0 )
			{
				// check if type is configurable
				Variable tc = f.getType().getAnnotation( Variable.class );
				if( tc != null && tc.value().length() != 0 )
				{
					name = tc.value();
				}
				else
				{
					name = f.getName();
				}
			}
		}

		return name;
	}

	/**
	 * The configuration name of an encapsulated variable
	 * 
	 * @param m
	 * @return The name of the variable, or <code>null</code> if the
	 *         method is not {@link EncapVariable}
	 */
	public static String getName( Method m )
	{
		EncapVariable v = m.getAnnotation( EncapVariable.class );

		if( v != null )
		{
			return v.value();
		}

		return null;
	}

	/**
	 * The configuration name of an object
	 * 
	 * @param o
	 * @return The name, or <code>null</code> if o is not
	 *         {@link Variable}
	 */
	public static String getName( Object o )
	{
		Variable c = o.getClass().getAnnotation( Variable.class );
		String name = null;

		if( c != null )
		{
			name = c.value();

			if( name.length() == 0 )
			{
				name = o.getClass().getSimpleName();
			}
		}

		return name;
	}

	/**
	 * @param o
	 * @return {@link Variable} description, or <code>null</code> if
	 *         there is not one present
	 */
	public static String getDescription( Object o )
	{
		Description d = o.getClass().getAnnotation( Description.class );
		if( d != null )
		{
			return d.value();
		}
		return null;
	}

	/**
	 * @param f
	 * @return {@link Variable} description, or <code>null</code> if
	 *         there is not one present
	 */
	public static String getDescription( AccessibleObject f )
	{
		Description d = f.getAnnotation( Description.class );
		if( d != null )
		{
			return d.value();
		}
		return null;
	}

	/**
	 * @param f
	 * @return {@link String} range, or <code>null</code> if there is
	 *         not one present
	 */
	public static String[] getStringRange( AccessibleObject f )
	{
		StringRange r = f.getAnnotation( StringRange.class );
		if( r != null )
		{
			return r.value();
		}
		return null;
	}

	/**
	 * Using strings cos JSON can't represent {@link Float#NaN} as a
	 * number
	 * 
	 * @param f
	 * @return numerical range, or <code>null</code> if there is not
	 *         one present
	 */
	public static String[] getNumberRange( AccessibleObject f )
	{
		NumberRange r = f.getAnnotation( NumberRange.class );
		if( r != null )
		{
			return r.value();
		}
		return null;
	}
}
