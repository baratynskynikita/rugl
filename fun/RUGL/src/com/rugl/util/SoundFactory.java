
package com.rugl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.util.WaveData;

import com.rugl.sound.Sound;
import com.rugl.sound.Sound.Format;

/**
 * Utility to convert wav files to {@link Sound}s and back again
 * 
 * @author ryanm
 */
public class SoundFactory
{
	/**
	 * Converts a directory of wav files into ruglsnd files
	 * 
	 * @param args
	 *           first argument is directory containing wavs, second is
	 *           output directory
	 */
	public static void main( String[] args )
	{
		if( args.length == 2 )
		{
			File inputDir = new File( args[ 0 ] );

			if( !inputDir.exists() )
			{
				System.out.println( "Input directory does not exist" );
				return;
			}
			if( !inputDir.isDirectory() )
			{
				System.out.println( "Input directory is not a directory" );
				return;
			}

			File outputDir = new File( args[ 1 ] );
			outputDir.mkdirs();

			File[] wavs = inputDir.listFiles( new FilenameFilter() {
				@Override
				public boolean accept( File dir, String name )
				{
					return dir.getName().toLowerCase().equals( ".wav" );
				}
			} );

			for( File wav : wavs )
			{
				try
				{
					InputStream is = new FileInputStream( wav );
					WaveData wd = WaveData.create( is );
					Sound s = create( wd );

					String fileName =
							outputDir.getAbsolutePath()
									+ wav.getName()
											.substring( 0, wav.getName().lastIndexOf( "." ) )
									+ ".ruglsnd";

					s.write( fileName );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.out.println( "Arguments = <input dir> <output dir>" );
		}
	}

	/**
	 * Loads a {@link Sound} from a wav file
	 * 
	 * @param filename
	 * @return A {@link Sound}, or null
	 * @throws IOException
	 */
	public static Sound load( String filename ) throws IOException
	{
		InputStream is;
		is = new FileInputStream( filename );
		WaveData wd = WaveData.create( is );
		return create( wd );
	}

	/**
	 * Creates a {@link Sound} from a {@link WaveData} object
	 * 
	 * @param wd
	 * @return A {@link Sound}, or null if failure occurred
	 */
	public static Sound create( WaveData wd )
	{
		if( wd != null )
		{
			Format f = null;
			for( Format fm : Format.values() )
			{
				if( fm.alFormat == wd.format )
				{
					f = fm;
					break;
				}
			}

			if( f != null )
			{
				return new Sound( f, wd.samplerate, wd.data );
			}
		}

		return null;
	}
}
