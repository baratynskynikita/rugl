
package com.ryanm.trace;

import java.io.File;
import java.io.IOException;

import com.ryanm.config.Configurator;
import com.ryanm.config.serial.ConfigurationSerialiser;

/**
 * @author ryanm
 */
public class SaveGame extends LoadGame
{
	/***/
	public SaveGame()
	{
		super( "savegametype " );
	}

	@Override
	protected void operate( File f, Configurator conf )
	{
		try
		{
			ConfigurationSerialiser.saveConfiguration( f, conf );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
