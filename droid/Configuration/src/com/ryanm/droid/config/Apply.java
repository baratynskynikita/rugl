
package com.ryanm.droid.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.JSONObject;

import android.util.Log;

import com.ryanm.droid.config.annote.DirtyFlag;

/**
 * Methods for applying JSON to an object tree
 * 
 * @author ryanm
 */
class Apply
{
	private static final int NO_CHANGE = 0;

	private static final int SUB_CHANGE = 1;

	private static final int LOCAL_CHANGE = 2;

	private Apply()
	{
	}

	static void apply( JSONObject json, Object... roots )
	{
		if( roots == null )
		{
			Log.i( Configuration.LOG_TAG,
					"Null applilcation roots. Not sure how this happens, but it apparently does" );
		}
		else if( roots.length == 1 )
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

	private static int apply( JSONObject json, Object o )
	{
		int result = NO_CHANGE;

		for( Field f : o.getClass().getFields() )
		{
			JSONObject fc = json.optJSONObject( Util.getName( f ) );
			if( fc != null )
			{
				int r = apply( o, f, fc );

				result = Math.max( result, r );
			}
		}

		for( Method m : o.getClass().getMethods() )
		{
			JSONObject mc = json.optJSONObject( Util.getName( m ) );
			if( mc != null && m.getReturnType() == void.class )
			{
				int r = apply( o, m, mc );

				result = Math.max( result, r );
			}
		}

		if( result != NO_CHANGE )
		{
			setDirtyFlags( result, o );
		}

		return result;
	}

	private static void setDirtyFlags( int result, Object o )
	{
		for( Field f : o.getClass().getFields() )
		{
			DirtyFlag df = f.getAnnotation( DirtyFlag.class );
			if( df != null )
			{
				if( f.getType() != boolean.class )
				{
					throw new ConfigError(
							"DirtyFlag can only be applied to boolean fields. Remove it from "
									+ o.getClass().getName() + "." + f.getName() );
				}
				else
				{
					if( result == LOCAL_CHANGE || result == SUB_CHANGE && df.watchTree() )
					{
						try
						{
							f.set( o, new Boolean( true ) );
						}
						catch( Exception e )
						{
							throw new RuntimeException( "Problem setting dirty flag "
									+ o.getClass() + "." + f.getName(), e );
						}
					}
				}
			}
		}

		for( Method m : o.getClass().getMethods() )
		{
			DirtyFlag df = m.getAnnotation( DirtyFlag.class );
			if( df != null )
			{
				if( m.getReturnType() != void.class && m.getParameterTypes().length != 0 )
				{
					throw new ConfigError(
							"DirtyFlag can only be applied to void-return/no-arg methods. Remove it from "
									+ o.getClass().getName() + "." + m.getName() );
				}
				else
				{
					if( result == LOCAL_CHANGE || result == SUB_CHANGE && df.watchTree() )
					{
						try
						{
							m.invoke( o );
						}
						catch( Exception e )
						{
							throw new RuntimeException( "Problem calling dirty flag method"
									+ o.getClass() + "." + m.getName(), e );
						}
					}
				}
			}
		}
	}

	private static int apply( Object owner, Field field, JSONObject json )
	{
		VariableType codec = VariableType.get( field.getType() );
		if( codec != null )
		{
			String valueString = json.optString( Util.VALUE );
			try
			{
				Object newValue = codec.decode( valueString, field.getType() );
				Object current = field.get( owner );

				if( !current.equals( newValue ) )
				{
					field.set( owner, newValue );

					return LOCAL_CHANGE;
				}
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
				int result = apply( json, sub );

				if( result != NO_CHANGE )
				{
					return SUB_CHANGE;
				}
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Trouble getting subconf " + owner.getClass()
						+ "." + field.getName(), e );
			}
		}

		return NO_CHANGE;
	}

	/**
	 * @param owner
	 * @param setter
	 * @param json
	 * @return The change type that occurred
	 */
	private static int apply( Object owner, Method setter, JSONObject json )
	{
		String valueString = json.optString( Util.VALUE );

		// is action?
		if( setter.getParameterTypes().length == 0 )
		{
			if( "true".equals( valueString ) )
			{
				try
				{
					setter.invoke( owner );

					return LOCAL_CHANGE;
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
			Method getter = findGetter( owner.getClass(), Util.getName( setter ) );

			try
			{
				Object current = getter.invoke( owner );

				if( codec != null )
				{
					try
					{
						Object newValue =
								codec.decode( valueString, setter.getParameterTypes()[ 0 ] );

						if( !newValue.equals( current ) )
						{
							setter.invoke( owner, newValue );
							return LOCAL_CHANGE;
						}
					}
					catch( Exception e )
					{
						Log.e( Configuration.LOG_TAG,
								"Problem invoking setter method " + owner.getClass() + "."
										+ setter.getName() + " with \"" + valueString + "\" ", e );
					}
				}
				else
				{ // subconfigurable
					// need to get the object, apply the config to
					// that, then set
					if( current != null )
					{
						int change = apply( json, current );
						setter.invoke( owner, current );

						if( change != NO_CHANGE )
						{
							return SUB_CHANGE;
						}
					}
				}
			}
			catch( Exception e )
			{
				Log.e( Configuration.LOG_TAG, "Problem invoking getter " + owner.getClass()
						+ "." + getter + " or setter " + setter, e );
			}
		}

		return NO_CHANGE;
	}

	static final Method findGetter( Class c, String name )
	{
		for( Method m : c.getMethods() )
		{
			if( name.equals( Util.getName( m ) ) && m.getReturnType() != void.class
					&& m.getParameterTypes().length == 0 )
			{
				return m;
			}
		}

		return null;
	}
}
