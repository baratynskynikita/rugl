
package com.ryanm.trace;

import java.lang.reflect.Constructor;

import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.Variable;
import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.entities.GapInf;
import com.ryanm.trace.game.entities.GapMinus;
import com.ryanm.trace.game.entities.GapPlus;
import com.ryanm.trace.game.entities.Powerup;
import com.ryanm.trace.game.entities.ShieldPowerup;
import com.ryanm.trace.game.entities.SizeInf;
import com.ryanm.trace.game.entities.SizeMinus;
import com.ryanm.trace.game.entities.SizePlus;
import com.ryanm.trace.game.entities.SpeedMinus;
import com.ryanm.trace.game.entities.SpeedPlus;
import com.ryanm.trace.game.entities.Virus;
import com.ryanm.util.math.Range;

/**
 * @author ryanm
 */
@ConfigurableType( "powerups" )
public class PowerupOptions
{
	/**
	 * Powerup classes
	 */
	@SuppressWarnings( "unchecked" )
	public static Class<? extends Powerup>[] powerupClasses = new Class[] { SizePlus.class,
			SizeMinus.class, SizeInf.class, GapPlus.class, GapMinus.class, GapInf.class,
			SpeedPlus.class, SpeedMinus.class, ShieldPowerup.class, Virus.class };

	/***/
	@Variable( "spawn rate" )
	@Description( "Seconds between spawns" )
	public Range spawnRate = new Range( 5, 6 );

	/***/
	@Variable( "radius" )
	@Description( "Size of powerups" )
	public float radius = 10;

	/***/
	@Variable( "life" )
	@Description( "How long a powerup will last in the arena, in seconds" )
	public float life = 10;

	/***/
	@Variable( "speed" )
	@Description( "speed of powerups, in units per second" )
	public float speed = 20;

	/***/
	@Variable
	public TraceLength traceLength = new TraceLength();

	/***/
	@Variable
	public TraceSpeed traceSpeed = new TraceSpeed();

	/***/
	@Variable
	public GapSize gapSize = new GapSize();

	/***/
	@Variable( "shield chance" )
	@Description( "relative chance of a shield powerup" )
	public float shield = 1;

	/***/
	@Variable( "virus chance" )
	@Description( "relative chance of a virus appearing" )
	public float virus = 1;

	/***/
	@Variable( "virus cloak" )
	@Description( "duration range of virus cloak" )
	public Range virusCloak = new Range( 3, 10 );

	/**
	 * @param arena
	 * @return the {@link Powerup} to add
	 */
	public Powerup choosePowerup( Arena arena )
	{
		float[] chances =
				new float[] { traceLength.plus, traceLength.minus, traceLength.inf, gapSize.plus,
						gapSize.minus, gapSize.inf, traceSpeed.plus, traceSpeed.minus, shield, virus };

		assert chances.length == powerupClasses.length;

		float sum = 0;
		for( int i = 0; i < chances.length; i++ )
		{
			sum += chances[ i ];
		}
		float choice = TraceGame.rng.nextFloat() * sum;
		int i = 0;
		while( choice > chances[ i ] )
		{
			choice -= chances[ i ];
			i++;
		}

		try
		{
			Constructor<? extends Powerup> c = powerupClasses[ i ].getConstructor( Arena.class );

			return c.newInstance( arena );
		}
		catch( Exception e )
		{ // catch a whole heap of crap that can come flying out of
			// reflection
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "trace length" )
	public static class TraceLength
	{
		/***/
		@Variable( "plus" )
		@Description( "relative chance of a length-plus powerup" )
		public float plus = 1;

		/***/
		@Variable( "minus" )
		@Description( "relative chance of a length-minus powerup" )
		public float minus = 1;

		/***/
		@Variable( "inf" )
		@Description( "relative chance of a length-infinite powerup" )
		public float inf = 1;

		/***/
		@Variable( "amount" )
		@Description( "fraction of current length that is gained or lost" )
		public float amount = 0.2f;
	}

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "trace speed" )
	public static class TraceSpeed
	{
		/***/
		@Variable( "plus" )
		@Description( "relative chance of a speed-plus powerup" )
		public float plus = 1;

		/***/
		@Variable( "minus" )
		@Description( "relative chance of a speed-minus powerup" )
		public float minus = 1;

		/***/
		@Variable( "amount" )
		@Description( "fraction of current speed that is gained or lost" )
		public float amount = 0.2f;
	}

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "gapsize" )
	public static class GapSize
	{
		/***/
		@Variable( "plus" )
		@Description( "relative chance of a gap-plus powerup" )
		public float plus = 1;

		/***/
		@Variable( "minus" )
		@Description( "relative chance of a gap-minus powerup" )
		public float minus = 1;

		/***/
		@Variable( "inf" )
		@Description( "relative chance of a gap-infinite powerup" )
		public float inf = 1;

		/***/
		@Variable( "amount" )
		@Description( "fraction of current gap size that is gained or lost" )
		public float amount = 0.2f;
	}
}