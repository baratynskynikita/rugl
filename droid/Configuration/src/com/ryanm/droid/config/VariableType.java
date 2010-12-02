
package com.ryanm.droid.config;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ryanm.droid.config.ConfigActivity.Variable;
import com.ryanm.droid.config.imp.BooleanType;
import com.ryanm.droid.config.imp.EnumType;
import com.ryanm.droid.config.imp.FloatType;
import com.ryanm.droid.config.imp.IntType;
import com.ryanm.droid.config.imp.StringType;
import com.ryanm.droid.config.imp.VoidType;

/**
 * Extend this to define new variable types. You'll need code to
 * encode an decode a value to and from strings, construct an
 * appropriate {@link Preference}, and optionally to validate/format
 * user input. Remember to {@link #register(VariableType)} your new
 * variable type or it won't be used
 * 
 * @author ryanm
 * @param <T>
 *           Type of variable to handle
 */
public abstract class VariableType<T>
{
	/**
	 * Variable type
	 */
	public final Class<? extends T> type;

	/**
	 * @param type
	 */
	protected VariableType( Class<? extends T> type )
	{
		this.type = type;
	}

	/**
	 * Encodes the value of a given object
	 * 
	 * @param value
	 *           The value to encode
	 * @return The String encoding for the value, or null if encoding
	 *         was not possible
	 */
	public abstract String encode( T value );

	/**
	 * Decodes the encoded string into a value object
	 * 
	 * @param encoded
	 *           The encoded string
	 * @param runtimeType
	 *           The desired type of the object
	 * @return The value of the encoded string
	 * @throws ParseException
	 */
	public abstract T decode( String encoded, Class runtimeType ) throws ParseException;

	/**
	 * Gets a widget to control the supplied variable
	 * 
	 * @param context
	 * @param var
	 * @return An appropriate {@link View}
	 */
	final Preference getPreference( final Context context, final Variable var )
	{
		Preference p = buildPreference( context, var.type, var.json.optString( "value" ) );
		p.setTitle( var.name );
		p.setSummary( var.description );
		p.setOrder( var.order );

		if( p instanceof DialogPreference )
		{
			DialogPreference dp = ( DialogPreference ) p;
			dp.setDialogTitle( var.name );
		}

		p.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange( Preference preference, Object newValue )
			{
				try
				{
					var.json.put( "value", formatInput( newValue ) );
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
	 * Build a widget to control the supplied variable. Variable title,
	 * summary, order and a default input formatting action are handled
	 * for you.
	 * 
	 * @see VariableType#formatInput(Object)
	 * @param context
	 *           Well who'd have thought it! A context!
	 * @param type
	 *           runtime type of the variable. Needed to get enum
	 *           values, not much use otherwise
	 * @param value
	 *           The current value of the variable
	 * @return An appropriate {@link View}
	 */
	protected abstract Preference buildPreference( Context context, Class type,
			final String value );

	/**
	 * Default format behaviour, just does toString(). Override if that
	 * doesn't work for you. Throw an exception to indicate trouble,
	 * the exception message will be {@link Toast}ed at the user
	 * 
	 * @param input
	 *           The user input, fresh from the {@link Preference}
	 *           created in
	 *           {@link #buildPreference(Context, Class, String)}
	 * @return A properly-formatted value that won't cause trouble on
	 *         the other end
	 */
	protected String formatInput( Object input )
	{
		return input.toString();
	}

	private static final Map<Class, VariableType> types =
			new HashMap<Class, VariableType>();

	static
	{
		register( new BooleanType() );
		register( new EnumType() );
		register( new FloatType() );
		register( new IntType() );
		register( new StringType() );
		register( new VoidType() );
	}

	/**
	 * Call this to register your {@link VariableType}s
	 * 
	 * @param varType
	 */
	public static void register( VariableType varType )
	{
		types.put( varType.type, varType );
	}

	/**
	 * @param type
	 * @return A factory for the type
	 */
	public static VariableType get( Class type )
	{
		VariableType t = types.get( type );

		while( t == null && type != null )
		{
			type = type.getSuperclass();
			t = types.get( type );
		}

		return t;
	}
}
