
package com.ryanm.droid.config.view;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.view.ConfigActivity.Variable;
import com.ryanm.droid.rugl.util.geom.Vector2f;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.droid.rugl.util.math.Range;

/**
 * Extends this to handle a new variable type
 * 
 * @author ryanm
 */
public abstract class PreferenceFactory
{
	/**
	 * The variable type that this factory can handle
	 */
	public final Class type;

	/**
	 * @param type
	 */
	protected PreferenceFactory( Class type )
	{
		this.type = type;
	}

	/**
	 * Build a widget to control the supplied variable
	 * 
	 * @param context
	 * @param var
	 * @return An appropriate {@link View}
	 */
	public Preference getPreference( final Context context, final Variable var )
	{
		Preference p = buildPreference( context, var );
		p.setTitle( var.name );
		p.setSummary( var.description );
		p.setOrder( var.order );

		p.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange( Preference preference, Object newValue )
			{
				try
				{
					setValue( var, newValue );
					return true;
				}
				catch( JSONException e )
				{
					Log.e( Configuration.LOG_TAG, "shouldn't happen", e );
				}
				catch( Exception e )
				{
					Toast.makeText( context, e.getMessage(), Toast.LENGTH_SHORT ).show();

					Log.e( Configuration.LOG_TAG, "Problem setting value", e );
				}

				return false;
			}
		} );

		return p;
	}

	/**
	 * Build a widget to control the supplied variable
	 * 
	 * @param context
	 * @param var
	 * @return An appropriate {@link View}
	 */
	protected abstract Preference buildPreference( Context context, final Variable var );

	/**
	 * Default set behaviour, just flings the new value into the json.
	 * Override if that doesn't work for you. Throw an exception to
	 * indicate trouble, its message will be {@link Toast}ed at the
	 * user
	 * 
	 * @param var
	 * @param value
	 * @throws JSONException
	 *            I'll handle this for you
	 */
	protected void setValue( Variable var, Object value ) throws JSONException
	{
		var.json.put( "value", value );
	}

	private static Map<Class, PreferenceFactory> factories =
			new HashMap<Class, PreferenceFactory>();

	/**
	 * Registers a new {@link PreferenceFactory}
	 * 
	 * @param factory
	 */
	public static void register( PreferenceFactory factory )
	{
		factories.put( factory.type, factory );
	}

	static
	{
		register( new BooleanPrefFactory( boolean.class ) );
		register( new BooleanPrefFactory( void.class ) );
		register( new StringPrefFactory() );
		register( new EnumPrefFactory() );
		register( new IntPrefFactory() );
		register( new FloatPrefFactory() );
		register( new CSVPrefFactory( Range.class, true, true, "min,max" ) );
		register( new CSVPrefFactory( Vector2f.class, true, true, "x,y" ) );
		register( new CSVPrefFactory( Vector3f.class, true, true, "x,y,z" ) );
		register( new ColourPrefFactory() );
	}

	/**
	 * @param type
	 * @return A factory for the type
	 */
	public static PreferenceFactory getFactory( Class type )
	{
		PreferenceFactory fact = factories.get( type );

		while( fact == null && type != null )
		{
			type = type.getSuperclass();
			fact = factories.get( type );
		}

		return fact;
	}
}
