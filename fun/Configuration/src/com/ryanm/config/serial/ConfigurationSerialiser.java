
package com.ryanm.config.serial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ryanm.config.Configurator;
import com.ryanm.config.serial.imp.BooleanCodec;
import com.ryanm.config.serial.imp.ColourCodec;
import com.ryanm.config.serial.imp.DimensionCodec;
import com.ryanm.config.serial.imp.EnumCodec;
import com.ryanm.config.serial.imp.FileCodec;
import com.ryanm.config.serial.imp.FloatCodec;
import com.ryanm.config.serial.imp.IntCodec;
import com.ryanm.config.serial.imp.NanoXMLFormatter;
import com.ryanm.config.serial.imp.RangeCodec;
import com.ryanm.config.serial.imp.StringCodec;
import com.ryanm.config.serial.imp.StringListCodec;
import com.ryanm.config.serial.imp.VectorCodec;

/**
 * Reads and writes the state of a group of configurators from and to
 * a file
 * 
 * @author s9902505
 */
public class ConfigurationSerialiser
{
	/**
	 * The formatter to use when writing files
	 */
	private static ConfigurationStringFormatter formatter = null;

	/**
	 * Maps from a type identifier to a codec that reports that it can
	 * handle that type
	 */
	private static Map<Class, ConfiguratorCodec<? extends Object>> codecs =
			new HashMap<Class, ConfiguratorCodec<? extends Object>>();

	static
	{
		setFormatter( new NanoXMLFormatter() );

		registerCodec( new BooleanCodec() );
		registerCodec( new IntCodec() );
		registerCodec( new FloatCodec() );
		registerCodec( new StringCodec() );
		registerCodec( new FileCodec() );
		registerCodec( new StringListCodec() );
		registerCodec( new VectorCodec() );
		registerCodec( new ColourCodec() );
		registerCodec( new DimensionCodec() );
		registerCodec( new EnumCodec() );
		registerCodec( new RangeCodec() );
	}

	/**
	 * Gets the current formatter
	 * 
	 * @return The formatter currently in use
	 */
	public static ConfigurationStringFormatter getFormatter()
	{
		return formatter;
	}

	/**
	 * Sets the formatter object used in serialisation
	 * 
	 * @param formatter
	 */
	public static void setFormatter( ConfigurationStringFormatter formatter )
	{
		ConfigurationSerialiser.formatter = formatter;
	}

	/**
	 * Reads a configuration from the stream and applies it to the
	 * configurators
	 * 
	 * @param stream
	 * @param confs
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void loadConfiguration( InputStream stream, Configurator... confs )
			throws IOException, ParseException
	{
		if( stream != null )
		{
			String s = readStream( stream );

			if( s != null )
			{
				formatter.parse( confs, s );
			}
		}
	}

	/**
	 * Load the supplied file into the supplied {@link Configurator}s
	 * 
	 * @param file
	 *           The file to load
	 * @param confs
	 *           The {@link Configurator}s to attempt to apply the file
	 *           to
	 * @throws IOException
	 *            If there was a problem reading the file
	 * @throws ParseException
	 *            If there was a prolem with parsing the file
	 */
	public static void loadConfiguration( File file, Configurator... confs )
			throws IOException, ParseException
	{
		// read to a string
		String s = readFile( file );

		if( s != null )
		{
			formatter.parse( confs, s );
		}
	}

	/**
	 * Saves the state of the supplied Configurators into the file
	 * 
	 * @param file
	 *           The file to save to
	 * @param confs
	 *           The configurators to save
	 * @throws IOException
	 */
	public static void saveConfiguration( File file, Configurator... confs )
			throws IOException
	{
		// construct the map

		// format to a string
		String s = formatter.format( confs );

		// write to file
		FileWriter fw = new FileWriter( file );
		fw.write( s );
		fw.flush();
		fw.close();
	}

	/**
	 * Reads in a file.
	 * 
	 * @param file
	 *           The file
	 * @return A string containing the contents of the file, or null if
	 *         there is no file or some other error crops up
	 */
	private static String readFile( File file ) throws IOException
	{
		BufferedReader reader = new BufferedReader( new FileReader( file ) );
		StringBuilder buffy = new StringBuilder();
		String line = null;

		do
		{
			line = reader.readLine();

			if( line != null )
			{
				buffy.append( line.trim() );
			}
		}
		while( line != null );

		return buffy.toString();
	}

	private static String readStream( InputStream stream ) throws IOException
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
		StringBuilder buffy = new StringBuilder();
		String line = null;

		do
		{
			line = reader.readLine();

			if( line != null )
			{
				buffy.append( line.trim() );
			}
		}
		while( line != null );

		return buffy.toString();
	}

	/**
	 * Registers a codec for use
	 * 
	 * @param classname
	 *           The classname of the codec class
	 * @return true if the codec is registered succesfully, false
	 *         otherwise
	 */
	public static boolean registerCodec( String classname )
	{

		ClassLoader loader = ClassLoader.getSystemClassLoader();

		try
		{
			// load the class
			Class type = loader.loadClass( classname );

			// check to see if it implements the Speck interface
			if( ConfiguratorCodec.class.isAssignableFrom( type ) )
			{
				ConfiguratorCodec instance = ( ConfiguratorCodec ) type.newInstance();

				return registerCodec( instance );
			}
		}
		catch( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		catch( IllegalAccessException iae )
		{
			iae.printStackTrace();
		}
		catch( InstantiationException ie )
		{
			ie.printStackTrace();
		}

		return false;
	}

	/**
	 * Registers a codec for use
	 * 
	 * @param codec
	 *           The codec to register
	 * @return true if the codec is registered successfully, false
	 *         otherwise
	 */
	public static boolean registerCodec( ConfiguratorCodec<?> codec )
	{
		if( !codecs.containsKey( codec.getType() ) )
		{
			codecs.put( codec.getType(), codec );
			return true;
		}
		else
		{
			System.err.println( "Attempted to register duplicate codec for type \""
					+ codec.getType() + "\"" );
			return false;
		}
	}

	/**
	 * Encodes the value of the named variable into a string
	 * 
	 * @param conf
	 *           The {@link Configurator} that the variable belongs to
	 * @param variable
	 *           The name of the variable
	 * @return An encoded string containing the value of the variable,
	 *         or null if there is no codec suitable for the type of
	 *         the variable
	 */
	@SuppressWarnings( "unchecked" )
	public static String encode( Configurator conf, String variable )
	{
		Class type = conf.getType( variable );

		ConfiguratorCodec codec = getCodec( type );

		if( codec != null )
		{
			return codec.encode( conf.getValue( variable ) );
		}

		System.err.println( "No suitable codec found for variable \"" + conf.getPath()
				+ "/" + variable + "\" with type \"" + type + "\"" );

		return null;
	}

	/**
	 * Determines whether or not there is a codec present that can
	 * handle the supplied type identifier
	 * 
	 * @param type
	 *           The type identifier to check for
	 * @return true if there is an appropriate codec present, false
	 *         otherwise
	 */
	public static boolean codecPresent( Class type )
	{
		return codecs.containsKey( type );
	}

	/**
	 * Decodes the value of the named variable from the supplied string
	 * 
	 * @param conf
	 *           The {@link Configurator} that the variable belongs to
	 * @param variable
	 *           The name of the variable
	 * @param encoded
	 *           The encoded {@link String} form of the value
	 * @return The decoded value
	 * @throws ParseException
	 */
	public static Object decode( Configurator conf, String variable, String encoded )
			throws ParseException
	{
		Class type = conf.getType( variable );

		if( type != null )
		{
			ConfiguratorCodec codec = getCodec( type );

			if( codec != null )
			{
				return codec.decode( encoded, type );
			}

			System.err.println( "No suitable codec found for variable \"" + conf.getPath()
					+ "/" + variable + "\" with type \"" + type + "\"" );
		}
		return null;
	}

	/**
	 * Gets a format description for a variable
	 * 
	 * @param conf
	 *           The {@link Configurator} that holds the variable
	 * @param variable
	 *           The variable name
	 * @return a format description, or null if no suitable codec could
	 *         be found or if the variable was not found in the
	 *         {@link Configurator}
	 */
	public static String getFormatDescription( Configurator conf, String variable )
	{
		Class type = conf.getType( variable );

		if( type != null )
		{
			ConfiguratorCodec codec = getCodec( type );

			if( codec != null )
			{
				return codec.getDescription();
			}

			System.err.println( "No suitable codec found for variable \"" + conf.getPath()
					+ "/" + variable + "\" with type \"" + type + "\"" );
		}
		return null;
	}

	/**
	 * Finds the most suitable codec for a type. Searches through the
	 * type's superclass hierarchy for a matching codec
	 * 
	 * @param type
	 * @return The most specific codec possible, or null if none is
	 *         found
	 */
	private static ConfiguratorCodec getCodec( Class type )
	{
		ConfiguratorCodec codec = codecs.get( type );

		while( codec == null && type != null )
		{
			type = type.getSuperclass();

			codec = codecs.get( type );
		}

		return codec;
	}
}
