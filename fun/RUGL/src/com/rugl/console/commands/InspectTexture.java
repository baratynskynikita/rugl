
package com.rugl.console.commands;

import java.util.List;

import com.rugl.console.Command;
import com.rugl.console.Console;
import com.rugl.texture.Texture;
import com.rugl.texture.TextureFactory;
import com.rugl.texture.TextureFactory.GLTexture;

/**
 * Allows textures to be displayed on the console
 * 
 * @author ryanm
 */
public class InspectTexture extends Command
{
	/***/
	public InspectTexture()
	{
		super( "inspecttexture" );
	}

	@Override
	public void execute( String command )
	{
		if( command.startsWith( "inspecttexture " ) )
		{
			String[] tex = command.substring( 15 ).trim().split( "\\s" );

			if( "hide".equals( tex[ 0 ] ) )
			{
				Console.inspectedTexture = null;
			}
			else
			{
				try
				{
					int tID = Integer.parseInt( tex[ 0 ] );

					// find the texture
					Texture texture = null;
					for( GLTexture glt : TextureFactory.getTextures() )
					{
						if( glt.id() == tID )
						{
							texture = glt.getTexture();
							break;
						}
					}

					if( texture != null )
					{
						Console.inspectedTexture = texture;
						Console.log( texture.parent.toString() );
					}
					else
					{
						Console.inspectedTexture = null;
						Console.error( getUsage() );
						Console.log( "Texture ID \"" + tID + "\" does not exist." );
					}
				}
				catch( NumberFormatException nfe )
				{
					Console.inspectedTexture = null;
					Console.error( getUsage() );
					Console.error( "\"" + tex
							+ "\" is not a valid texture handle. Check the autocomplete." );
				}
			}
		}
		else
		{
			Console.error( getUsage() );
		}
	}

	@Override
	public String getUsage()
	{
		return "inspecttexture <texID>\n\tDisplays the contents of an OpenGL texture object";
	}

	@Override
	public void suggest( String current, List<Suggestion> suggestions )
	{
		if( current.length() < 14 && "inspecttexture".startsWith( current ) )
		{
			suggestions.add( new Suggestion( "inspecttexture ", 0 ) );
		}
		else if( current.startsWith( "inspecttexture " ) )
		{
			String path = current.substring( 15 );
			path = path.trim();

			if( "hide".startsWith( path ) )
			{
				suggestions.add( new Suggestion( "hide", 15 ) );
			}

			for( GLTexture glt : TextureFactory.getTextures() )
			{
				String tn = String.valueOf( glt.id() );

				if( tn.startsWith( path ) )
				{
					suggestions.add( new Suggestion( tn, 15 ) );
				}
			}

		}
	}

}
