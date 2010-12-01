
package com.ryanm.droid.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.JSONObject;

import android.util.Log;

/**
 * Methods for applying JSON to an object tree
 * 
 * @author ryanm
 */
class Apply
{
	private Apply()
	{
	}

	static void apply( JSONObject json, Object... roots )
	{
		if( roots.length == 1 )
		{
			apply( json, roots[ 0 ] );
		}
		else
		{
			for( Object root : roots )
			{
				if( root != null )
				{
					String name = Util.getName( root );
					JSONObject oc = json.optJSONObject( name );
					apply( oc, root );
				}
			}
		}
	}

	private static void apply( JSONObject json, Object o )
	{
		for( Field f : o.getClass().getFields() )
		{
			JSONObject fc = json.optJSONObject( Util.getName( f ) );
			if( fc != null )
			{
				apply( o, f, fc );
			}
		}

		for( Method m : o.getClass().getMethods() )
		{
			JSONObject mc = json.optJSONObject( Util.getName( m ) );
			if( mc != null && m.getReturnType() == void.class )
			{
				apply( o, m, mc );
			}
		}
	}

	private static void apply( Object owner, Field field, JSONObject json )
	{
		VariableType codec = VariableType.get( field.getType() );
		if( codec != null )
		{
			String valueString = json.optString( "value" );
			try
			{
				field.set( owner, codec.decode( valueString, field.getType() ) );
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Trouble applying value \"" + valueString
						+ "\" to " + owner.getClass() + "." + field.getName(), e );
			}
		}
		else
		{
			// subconf
			try
			{
				Object sub = field.get( owner );
				apply( json, sub );
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Trouble getting subconf " + owner.getClass()
						+ "." + field.getName(), e );
			}
		}
	}

	private static void apply( Object owner, Method setter, JSONObject json )
	{
		String valueString = json.optString( "value" );

		// is action?
		if( setter.getParameterTypes().length == 0 )
		{
			if( "true".equals( valueString ) )
			{
				try
				{
					setter.invoke( owner );
				}
				catch( Exception e )
				{
					Log.e(
							Configuration.LOG_TAG,
							"Problem invoking action method " + owner.getClass() + "."
									+ setter.getName(), e );
				}
			}
		}
		else
		{ // must be a setter
			VariableType codec = VariableType.get( setter.getParameterTypes()[ 0 ] );
			if( codec != null )
			{
				try
				{
					setter.invoke( owner,
							codec.decode( valueString, setter.getParameterTypes()[ 0 ] ) );
				}
				catch( Exception e )
				{
					Log.e(
							Configuration.LOG_TAG,
							"Problem invoking setter method " + owner.getClass() + "."
									+ setter.getName() + " with \"" + valueString + "\" ", e );
				}
			}
			else
			{ // subconfigurable
				// need to get the object, apply the config to
				// that, then set
				Method getter = findGetter( owner.getClass(), Util.getName( setter ) );
				if( getter != null )
				{
					try
					{
						Object gotten = getter.invoke( owner );

						if( gotten != null )
						{
							apply( json, gotten );
							setter.invoke( owner, gotten );
						}
					}
					catch( Exception e )
					{
						Log.e( Configuration.LOG_TAG,
								"Problem invoking getter method " + owner.getClass() + "."
										+ getter.getName() + " or setter " + setter.getName(), e );
					}
				}
			}
		}
	}

	static final Method findGetter( Class c, String name )
	{
		for( Method m : c.getMethods() )
		{
			if( name.equals( Util.getName( m ) ) )
			{
				return m;
			}
		}
		return null;
	}
}
