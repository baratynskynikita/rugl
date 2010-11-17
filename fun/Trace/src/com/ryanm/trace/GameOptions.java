
package com.ryanm.trace;

import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.Variable;

/**
 * Game setup options
 * 
 * @author ryanm
 */
@ConfigurableType( "game" )
public class GameOptions implements Cloneable
{
	/***/
	@Variable( "name" )
	public String name = "default";

	/***/
	@Variable( "description" )
	public String description = "mmmmm, vanilla!";

	/***/
	@Variable
	public final TraceOptions trace = new TraceOptions();

	/***/
	@Variable
	public final ScoreOptions score = new ScoreOptions();

	/***/
	@Variable
	public final PowerupOptions powerups = new PowerupOptions();

	/***/
	@Variable
	public final EndGameOptions endgame = new EndGameOptions();

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "trace" )
	public static class TraceOptions
	{
		/***/
		@Variable( "max shields" )
		@Description( "The maximum number of shields that can be carried" )
		public int maxShields = 3;

		/***/
		@Variable( "start shields" )
		@Description( "The number of shields held at the start of a game" )
		public int startShields = 3;

		/***/
		@Variable( "speed" )
		@Description( "Base speed, in units per second" )
		public float speed = 100;

		/***/
		@Variable( "rotation" )
		@Description( "Rotation speed, in degrees per second" )
		public float rotate = 150;

		/***/
		@Variable( "path length" )
		@Description( "The distance between gaps" )
		public float pathLength = 80;

		/***/
		@Variable( "gap Length" )
		@Description( "The length of gaps, -1 for no gaps" )
		public float gapLength = 15;

		/***/
		@Variable( "trace length" )
		@Description( "The length of a trace, -1 for unlimited" )
		public float traceLength = 500;

		/***/
		@Variable( "arena bounce" )
		@Description( "The maximum angle you can make with a wall and survive" )
		public float arenaBounce = 30;

		/***/
		@Variable( "trace bounce" )
		@Description( "The maximum angle you can make with a trace and survive" )
		public float traceBounce = 30;

		/***/
		@Variable( "slip boost" )
		@Description( "The maximum speed multiplier gained from being within the slip distance of another trace" )
		public float slipBoost = 2f;

		/***/
		@Variable( "slip accel" )
		@Description( "How long it takes to reach maximum slip speed from normal" )
		public float slipAccel = 1f;

		/***/
		@Variable( "slip deccel" )
		@Description( "How long it takes to reach normal speed from maximum slip speed" )
		public float slipDeccel = 2f;

		/***/
		@Variable( "slip distance" )
		@Description( "The distance from a trace affected by slip boost" )
		public float slipDistance = 20;

		/**
		 * @param maxShields
		 * @param startShields
		 * @param speed
		 * @param rotate
		 * @param pathLength
		 * @param gapLength
		 * @param traceLength
		 * @param arenaBounce
		 * @param traceBounce
		 * @param slipBoost
		 * @param slipAccel
		 * @param slipDeccel
		 * @param slipDistance
		 */
		public void set( int maxShields, int startShields, float speed, float rotate,
				float pathLength, float gapLength, float traceLength, float arenaBounce,
				float traceBounce, float slipBoost, float slipAccel, float slipDeccel,
				float slipDistance )
		{
			this.maxShields = maxShields;
			this.startShields = startShields;
			this.speed = speed;
			this.rotate = rotate;
			this.pathLength = pathLength;
			this.gapLength = gapLength;
			this.traceLength = traceLength;
			this.arenaBounce = arenaBounce;
			this.traceBounce = traceBounce;
			this.slipBoost = slipBoost;
			this.slipAccel = slipAccel;
			this.slipDeccel = slipDeccel;
			this.slipDistance = slipDistance;
		}

	}

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "score" )
	public static class ScoreOptions
	{
		/**
		 * @author ryanm
		 */
		@ConfigurableType( "strike" )
		public static class Strike
		{
			/***/
			@Variable
			@Description( "Someone else hit your trace" )
			public int struck = 5;

			/***/
			@Variable
			@Description( "You hit your own trace" )
			public int self = -5;

			/***/
			@Variable
			@Description( "You hit someone else's trace" )
			public int other = 0;

			/**
			 * @param struck
			 * @param self
			 * @param other
			 */
			public void set( int struck, int self, int other )
			{
				this.struck = struck;
				this.self = self;
				this.other = other;
			}
		}

		/**
		 * @author ryanm
		 */
		@ConfigurableType( "death" )
		public static class Death
		{
			/***/
			@Variable
			@Description( "You killed someone" )
			public int kill = 10;

			/***/
			@Variable
			@Description( "You died on someone else's trace" )
			public int killed = 0;

			/***/
			@Variable
			@Description( "You died on your own trace" )
			public int suicide = -10;

			/***/
			@Variable
			@Description( "You killed someone after you died" )
			public int deadKill = 20;

			/**
			 * @param kill
			 * @param killed
			 * @param suicide
			 * @param deadKill
			 */
			public void set( int kill, int killed, int suicide, int deadKill )
			{
				this.kill = kill;
				this.killed = killed;
				this.suicide = suicide;
				this.deadKill = deadKill;
			}
		}

		/**
		 * @author ryanm
		 */
		@ConfigurableType( "gap" )
		public static class Gap
		{
			/***/
			@Variable
			@Description( "You hit someone else's gap" )
			public int other = 10;

			/***/
			@Variable
			@Description( "You hit your own gap" )
			public int own = 5;

			/***/
			@Variable
			@Description( "Someone else hit your gap" )
			public int scoredOn = 0;

			/**
			 * @param other
			 *           You hit someone else's gap
			 * @param own
			 *           You hit your own gap
			 * @param scoredOn
			 *           Someone else hit your gap
			 */
			public void set( int other, int own, int scoredOn )
			{
				this.other = other;
				this.own = own;
				this.scoredOn = scoredOn;
			}
		}

		/***/
		@Variable
		public final Strike strike = new Strike();

		/***/
		@Variable
		public final Death death = new Death();

		/***/
		@Variable
		public final Gap gap = new Gap();
	}

	/**
	 * @author ryanm
	 */
	@ConfigurableType( "endgame" )
	public static class EndGameOptions
	{
		/***/
		@Variable( "acceleration" )
		@Description( "Rate of acceleration of surviving trace, in units per second per second" )
		public float speedUp = 10;

		/***/
		@Variable( "trace growth" )
		@Description( "Rate of increase of surviving trace length, in units per second" )
		public float traceGrowth = 30;

		/***/
		@Variable( "frantic mode" )
		@Description( "Last player alive risks epilepsy" )
		public boolean seizure = true;

		/**
		 * @param speedUp
		 * @param traceGrowth
		 * @param seizure
		 */
		public void set( float speedUp, float traceGrowth, boolean seizure )
		{
			this.speedUp = speedUp;
			this.traceGrowth = traceGrowth;
			this.seizure = seizure;
		}
	}
}
