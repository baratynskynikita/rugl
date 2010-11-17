
package com.rugl.test;

import com.rugl.sound.Sound;
import com.rugl.sound.SoundSystem;
import com.rugl.sound.Source;
import com.ryanm.soundgen.SoundSpec;
import com.ryanm.soundgen.imp.Constant;
import com.ryanm.soundgen.imp.SineWave;

/**
 * @author ryanm
 */
public class SoundTest
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		System.out.println( "init" );
		SoundSystem.init();

		SoundSpec ss = new SoundSpec();
		ss.length = 1;
		ss.volumeEnvelope = new Constant( 1 );
		ss.waveform = new SineWave();
		ss.waveform.frequency = new Constant( 500 );

		System.out.println( "generation" );
		Sound s = new Sound( ss, 44100 );

		System.out.println( "playing" );

		Source source = SoundSystem.getSource();
		source.bindSound( s ).play();

		try
		{
			Thread.sleep( 1100 );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		System.out.println( "destroying" );
		SoundSystem.destroy();
		System.out.println( "done" );
	}
}
