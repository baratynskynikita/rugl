
package com.ryanm.config.imp;

import com.ryanm.config.Configurable;
import com.ryanm.config.Configurator;

/**
 * Utility class for retrieving a Configurator from an object. Handles
 * the case of an annotated Configurable
 * 
 * @author ryanm
 */
public class ConfGet
{
	/**
	 * Gets a Configurator from the supplied Object
	 * 
	 * @param o
	 *           The configurable object
	 * @return The associated Configurator, or null if the objectis
	 *         Configurable, but returns a null Configurator, or is
	 *         neither Configurable nor annotated
	 */
	public static Configurator getConfigurator( Object o )
	{
		Configurator conf = null;

		if( o instanceof Configurable )
		{
			conf = ( ( Configurable ) o ).getConfigurator();
		}

		if( conf == null )
		{
			conf = AnnotatedConfigurator.buildConfigurator( o );
		}

		return conf;
	}

	/**
	 * Finds the last configurator on a path, in a case-insensitive
	 * manner
	 * 
	 * @param path
	 *           The {@link Configurator} path
	 * @param roots
	 *           The roots of the {@link Configurator} forest
	 * @return The {@link Configurator}, or null if not found
	 */
	public static Configurator forPath( String path, Configurator... roots )
	{
		for( Configurator root : roots )
		{
			if( root != null && path.startsWith( root.getPath() ) )
			{
				String pathRemainder = path.substring( root.getPath().length() );

				String[] p = pathRemainder.split( "/" );

				Configurator conf = root;
				Configurator sub = conf;
				int index = 1;

				while( index < p.length && sub != null )
				{
					sub = findSubConfigurator( conf, p[ index ] );

					if( sub != null )
					{
						assert sub.getName().equalsIgnoreCase( p[ index ] ) : sub.getName()
								+ " != " + p[ index ];

						conf = sub;
						index++;
					}
				}

				assert path.startsWith( conf.getPath() ) : "Path = \"" + path
						+ "\"\nConf = \"" + conf.getPath() + "\"";

				return conf;
			}
		}

		return null;
	}

	private static Configurator findSubConfigurator( Configurator conf, String subName )
	{
		if( conf != null )
		{
			for( Object obj : conf.getNames() )
			{
				if( obj instanceof Configurator
						&& ( ( Configurator ) obj ).getName().compareToIgnoreCase( subName ) == 0 )
				{
					return ( Configurator ) obj;
				}
			}
		}

		return null;
	}
}
