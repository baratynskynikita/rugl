
package com.ryanm.trace;

import java.io.IOException;

import com.rugl.sound.Sound;
import com.rugl.sound.SoundSystem;
import com.ryanm.soundgen.imp.TerrainSound;

/**
 * Repository of sounds
 * 
 * @author ryanm
 */
public class Sounds
{
	private static Sound click;

	private static Sound yes;

	private static Sound no;

	private static Sound score;

	private static Sound shieldStrike;

	private static Sound death;

	private static Sound bounce;

	private static Sound powerup;

	private static Sound cloak;

	/***/
	public static void init()
	{
		click = new Sound( load( "click" ), 44100 );
		yes = new Sound( load( "yes" ), 44100 );
		no = new Sound( load( "no" ), 44100 );
		score = new Sound( load( "score" ), 44100 );
		shieldStrike = new Sound( load( "shieldstrike" ), 44100 );
		bounce = new Sound( load( "bounce" ), 44100 );
		death = new Sound( load( "death" ), 44100 );
		powerup = new Sound( load( "powerup" ), 44100 );
		cloak = new Sound( load( "cloak" ), 44100 );
	}

	private static TerrainSound load( String name )
	{
		try
		{
			return new TerrainSound( Thread.currentThread().getContextClassLoader()
					.getResourceAsStream( name + ".ruglsg" ) );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	/***/
	public static void click()
	{
		SoundSystem.getSource().bindSound( click ).play();
	}

	/***/
	public static void yes()
	{
		SoundSystem.getSource().bindSound( yes ).play();
	}

	/***/
	public static void no()
	{
		SoundSystem.getSource().bindSound( no ).play();
	}

	/***/
	public static void score()
	{
		SoundSystem.getSource().bindSound( score ).play();
	}

	/***/
	public static void shieldStrike()
	{
		SoundSystem.getSource().bindSound( shieldStrike ).play();
	}

	/***/
	public static void death()
	{
		SoundSystem.getSource().bindSound( death ).play();
	}

	/***/
	public static void bounce()
	{
		SoundSystem.getSource().bindSound( bounce ).play();
	}

	/***/
	public static void powerup()
	{
		SoundSystem.getSource().bindSound( powerup ).play();
	}

	/***/
	public static void cloak()
	{
		SoundSystem.getSource().bindSound( cloak ).play();
	}
}
