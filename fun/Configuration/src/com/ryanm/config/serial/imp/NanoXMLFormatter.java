
package com.ryanm.config.serial.imp;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import com.ryanm.config.Configurator;
import com.ryanm.config.serial.ConfigurationSerialiser;
import com.ryanm.config.serial.ConfigurationStringFormatter;
import com.ryanm.config.serial.ParseException;

/**
 * Serialises to XML
 * 
 * @author ryanm
 */
public class NanoXMLFormatter implements ConfigurationStringFormatter
{

	@Override
	public String format( Configurator[] confs )
	{
		XMLElement root = new XMLElement();
		root.setName( "root" );

		for( int i = 0; i < confs.length; i++ )
		{
			XMLElement c = buildXMLElement( confs[ i ] );
			root.addChild( c );
		}

		return root.indentPrint();
	}

	private XMLElement buildXMLElement( Configurator c )
	{
		XMLElement e = new XMLElement();
		e.setName( "conf" );

		e.setAttribute( "name", c.getName() );

		for( Object o : c.getNames() )
		{
			if( o instanceof String )
			{
				String var = ( String ) o;

				if( c.getType( var ) != void.class )
				{ // ignore action variables
					XMLElement v = new XMLElement();
					v.setName( "var" );
					v.setAttribute( "name", var );
					v.setContent( ConfigurationSerialiser.encode( c, var ) );
					e.addChild( v );
				}
			}
			else if( o instanceof Configurator )
			{
				Configurator conf = ( Configurator ) o;
				e.addChild( buildXMLElement( conf ) );
			}
		}

		return e;
	}

	@Override
	public void parse( Configurator[] confs, String string ) throws ParseException
	{
		StringReader sr = new StringReader( string );
		XMLElement root = new XMLElement();

		try
		{
			root.parseFromReader( sr );
		}
		catch( XMLParseException e )
		{
			throw new ParseException( e );
		}
		catch( IOException e )
		{
			// shouldn't ever happen
			e.printStackTrace();
		}

		for( int i = 0; i < confs.length; i++ )
		{
			String n = confs[ i ].getName();

			Iterator<XMLElement> iter = root.iterateChildren();
			while( iter.hasNext() )
			{
				XMLElement e = iter.next();
				if( e.getAttribute( "name" ).equals( n ) )
				{
					parseXMLElement( confs[ i ], e );
					break;
				}
			}

		}
	}

	private void parseXMLElement( Configurator conf, XMLElement xml )
	{
		assert xml.getContent() == "";

		for( int i = 0; i < conf.getNames().length; i++ )
		{
			if( conf.getNames()[ i ] instanceof String )
			{
				String var = ( String ) conf.getNames()[ i ];

				Iterator<XMLElement> iter = xml.iterateChildren();
				while( iter.hasNext() )
				{
					XMLElement e = iter.next();
					if( e.getName().equals( "var" ) && e.getAttribute( "name" ).equals( var ) )
					{
						try
						{
							Object decoded =
									ConfigurationSerialiser.decode( conf, var, e.getContent() );
							conf.setValue( var, decoded );
						}
						catch( ParseException e1 )
						{
							e1.printStackTrace();
						}

						break;
					}
				}
			}
			else if( conf.getNames()[ i ] instanceof Configurator )
			{
				Configurator sc = ( Configurator ) conf.getNames()[ i ];

				Iterator<XMLElement> iter = xml.iterateChildren();
				while( iter.hasNext() )
				{
					XMLElement e = iter.next();
					if( e.getName().equals( "conf" )
							&& e.getAttribute( "name" ).equals( sc.getName() ) )
					{
						parseXMLElement( sc, e );
						break;
					}
				}
			}
		}
	}
}
