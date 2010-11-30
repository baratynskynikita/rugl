
package com.ryanm.droid.config;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ryanm.droid.config.annote.Category;
import com.ryanm.droid.config.annote.Description;
import com.ryanm.droid.config.annote.Order;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.config.annote.WidgetHint;
import com.ryanm.droid.config.serial.Codec;
import com.ryanm.droid.config.view.ConfigActivity;

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

	private static final Map<String, Class> primitiveClassMap =
			new HashMap<String, Class>();
	static
	{
		Class[] prims =
				new Class[] { boolean.class, byte.class, short.class, char.class, int.class,
						float.class, long.class, double.class, void.class };
		for( Class c : prims )
		{
			primitiveClassMap.put( c.getName(), c );
		}
	}

	/**
	 * Basically {@link Class#forName(String)}, but handles primitive
	 * types like int.class
	 * 
	 * @param name
	 * @return the class for that name, or <code>null</code> if not
	 *         found
	 */
	public static Class getType( String name )
	{
		Class c = primitiveClassMap.get( name );
		if( c == null )
		{
			try
			{
				c = Class.forName( name );
			}
			catch( ClassNotFoundException e )
			{
				Log.e( LOG_TAG, "CNFE for \"" + name + "\"", e );
			}
		}

		return c;
	}

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
	 * Call this from your activity to apply a configuration
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @param roots
	 */
	public static void onActivityResult( int requestCode, int resultCode, Intent data,
			Object... roots )
	{
		if( requestCode == Configuration.ACTIVITY_REQUEST_FLAG
				&& resultCode == Activity.RESULT_OK )
		{
			applyConfiguration( data, roots );
		}
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
				JSONObject json = new JSONObject( c );

				// apply
				for( Object root : roots )
				{
					if( root != null )
					{
						String name = getName( root );
						JSONObject oc = json.optJSONObject( name );
						apply( root, oc );
					}
				}
			}
			catch( Exception e )
			{
				Log.e( LOG_TAG, "Problem applying config", e );
			}
		}
	}

	private static void apply( Object o, JSONObject conf )
	{
		for( Field f : o.getClass().getFields() )
		{
			String name = getName( f );
			if( name != null )
			{
				JSONObject fc = conf.optJSONObject( name );
				if( fc != null )
				{
					Codec codec = Codec.getCodec( f.getType() );
					if( codec != null )
					{
						String valueString = fc.optString( "value" );
						try
						{
							f.set( o, codec.decode( valueString, f.getType() ) );
						}
						catch( Exception e )
						{
							Log.e( LOG_TAG, "Trouble applying value \"" + valueString + "\" to "
									+ o.getClass() + "." + f.getName(), e );
						}
					}
					else
					{
						// subconf
						try
						{
							Object sub = f.get( o );
							apply( sub, fc );
						}
						catch( Exception e )
						{
							Log.e( LOG_TAG,
									"Trouble getting subconf " + o.getClass() + "." + f.getName(),
									e );
						}
					}
				}
			}
		}

		// methods

		for( Method m : o.getClass().getMethods() )
		{
			String name = getName( m );
			if( name != null )
			{
				JSONObject mc = conf.optJSONObject( name );
				if( mc != null )
				{
					String valueString = mc.optString( "value" );

					if( !"".equals( valueString ) && m.getReturnType() == void.class )
					{// is action?
						if( m.getParameterTypes().length == 0 )
						{
							if( "true".equals( valueString ) )
							{
								try
								{
									m.invoke( o );
								}
								catch( Exception e )
								{
									Log.e( LOG_TAG,
											"Problem invoking action method " + o.getClass() + "."
													+ m.getName(), e );
								}
							}
						}
						else
						{ // must be a setter
							Codec codec = Codec.getCodec( m.getParameterTypes()[ 0 ] );
							if( codec != null )
							{
								try
								{
									m.invoke( o,
											codec.decode( valueString, m.getParameterTypes()[ 0 ] ) );
								}
								catch( Exception e )
								{
									Log.e( LOG_TAG,
											"Problem invoking setter method " + o.getClass() + "."
													+ m.getName() + " with \"" + valueString + "\" ",
											e );
								}
							}
						}
					}
				}
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

					// type
					conf.put( "type", f.getType().getName() );

					// value
					conf.put( "value", codec.encode( value ) );

					getOptional( conf, f );

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
				getOptional( conf, m );
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

						conf.put( "type", v.getClass().getName() );

						conf.put( "value", codec.encode( v ) );

						getOptional( conf, m );

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
				conf.put( "type", getter.getReturnType().getName() );
				conf.put( "value", codec.encode( v ) );

				getOptional( conf, getter );
				getOptional( conf, setter );

				return conf;
			}
			else
			{ // configurable type
				conf = extractConfig( v );
				if( conf != null )
				{
					// override desc
					getOptional( conf, getter );
					getOptional( conf, setter );
				}
			}
		}

		return conf;
	}

	/**
	 * Adds the description and ranges to the json
	 * 
	 * @param conf
	 * @param ao
	 * @throws JSONException
	 */
	private static void getOptional( JSONObject conf, AccessibleObject ao )
			throws JSONException
	{
		conf.putOpt( "desc", getDescription( ao ) );
		conf.putOpt( "cat", getCategory( ao ) );

		Order o = ao.getAnnotation( Order.class );
		if( o != null )
		{
			conf.put( "order", o.value() );
		}

		WidgetHint th = ao.getAnnotation( WidgetHint.class );
		if( th != null )
		{
			conf.put( "type", th.value().getName() );
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
	 *         method is not {@link Variable}
	 */
	public static String getName( Method m )
	{
		Variable v = m.getAnnotation( Variable.class );
		String name = null;

		if( v != null )
		{
			name = v.value();

			if( name.length() == 0 )
			{
				name = m.getName();
			}
		}

		return name;
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
	 * @return {@link Category} name, or <code>null</code> if there is
	 *         not one present
	 */
	public static String getCategory( AccessibleObject f )
	{
		Category c = f.getAnnotation( Category.class );
		if( c != null )
		{
			return c.value();
		}
		return null;
	}
}
