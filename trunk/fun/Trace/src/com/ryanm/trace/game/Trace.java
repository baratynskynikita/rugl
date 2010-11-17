
package com.ryanm.trace.game;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.renderer.StackedRenderer;
import com.rugl.sound.Sound;
import com.rugl.sound.SoundSystem;
import com.rugl.sound.Source;
import com.rugl.util.Colour;
import com.ryanm.soundgen.SoundSpec;
import com.ryanm.soundgen.Waveform;
import com.ryanm.soundgen.imp.Addition;
import com.ryanm.soundgen.imp.Constant;
import com.ryanm.soundgen.imp.Multiplication;
import com.ryanm.soundgen.imp.NoiseWave;
import com.ryanm.soundgen.imp.SineWave;
import com.ryanm.trace.Player;
import com.ryanm.trace.Sounds;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.ai.Bot.Action;
import com.ryanm.trace.game.entities.SeekSparks;
import com.ryanm.trace.game.entities.Sparks;
import com.ryanm.trace.game.entities.Virus;
import com.ryanm.util.BucketGrid;
import com.ryanm.util.geom.Pointf;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Range;
import com.ryanm.util.math.Trig;

/***/
public class Trace
{
	/**
	 * Shield shape
	 */
	public static Shape shieldShape = new Shape( ShapeUtil.to3D( new float[] { -10, 7, 2,
			0, -10, -7, 12, 0 }, 0 ), new int[] { 0, 1, 3, 1, 2, 3 } );

	private ColouredShape shield;

	/**
	 * Slip-speed sound
	 */
	private static final Sound hiss;
	static
	{
		SoundSpec ss = new SoundSpec();
		ss.length = .5f;
		ss.volumeEnvelope = new Constant( 1 );
		ss.waveform = new SineWave();
		Waveform vib = new NoiseWave();
		vib.frequency = new Constant( 30 );
		ss.waveform.frequency =
				new Addition( new Constant( 2000 ), new Multiplication( vib, new Constant(
						900 ) ) );

		hiss = new Sound( ss, 44100 );
	}

	/***/
	public final Player player;

	/***/
	public Arena arena;

	/***/
	public static byte PATH = 1;

	/***/
	public static byte GAP = 2;

	/***/
	public SegmentPath path = new SegmentPath();

	/***/
	public float speed = 100;

	private float slipSpeed = 0;

	/** turn rate - degrees per second */
	public float rotate = 100;

	/***/
	public float pathLength = 50;

	/***/
	public float gapLength = 15;

	/***/
	public float length = 200;

	private boolean onPath = true;

	private float l;

	/**
	 * The position of the trace's head
	 */
	public final Vector2f position;

	/**
	 * The angle of travel
	 */
	public float angleRads = 0;

	private Source slipSource;

	/**
	 * How many times we can barge through a trace and survive
	 */
	public int shields = TraceGame.game.trace.startShields;

	/**
	 * How long we are cloaked from virii for
	 */
	public float cloakTime = 0;

	private float cloakRot = TraceGame.rng.nextFloat() * Trig.PI * 2;

	/**
	 * @param player
	 *           the player
	 * @param x
	 *           start position x
	 * @param y
	 *           start position y
	 * @param angle
	 *           start angle in radians
	 */
	public Trace( Player player, float x, float y, float angle )
	{
		this.player = player;

		position = new Vector2f( x, y );
		angleRads = angle;

		path.startSeg( position.x, position.y, onPath ? PATH : GAP );
		path.startSeg( position.x + Trig.cos( angle ), position.y + Trig.sin( angle ),
				onPath ? PATH : GAP );

		speed = TraceGame.game.trace.speed;
		rotate = TraceGame.game.trace.rotate;
		pathLength = TraceGame.game.trace.pathLength;
		gapLength = TraceGame.game.trace.gapLength;
		length = TraceGame.game.trace.traceLength;

		l = ( 0.5f + TraceGame.rng.nextFloat() * 0.5f ) * pathLength;

		shield = new ColouredShape( shieldShape, player.colour, null );
	}

	private void die( Player cause )
	{
		player.dead = true;
		if( slipSource != null )
		{
			slipSource.release( this );
			slipSource.stop();
			slipSource = null;
		}
		Sounds.death();

		if( cause == null )
		{ // hit arena
		}
		else
		{
			if( cause == player )
			{
				arena.score( player, TraceGame.game.score.death.suicide );
			}
			else
			{
				arena.score( cause, cause.dead ? TraceGame.game.score.death.deadKill
						: TraceGame.game.score.death.kill );
			}
		}
	}

	/**
	 * Releases held {@link Source}s
	 */
	public void releaseSources()
	{
		if( slipSource != null )
		{
			slipSource.release( this );
			slipSource.stop();
			slipSource = null;
		}
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		cloakRot += delta * Trig.toRadians( 90 );
		cloakTime -= delta;
		if( cloakTime < 0 )
		{
			cloakTime = 0;
		}

		if( !player.dead )
		{
			float oldangle = angleRads;

			Action a = player.bot.process( this, arena );

			switch( a )
			{
				case LEFT:
					angleRads += Trig.toRadians( rotate * delta );
					break;
				case RIGHT:
					angleRads -= Trig.toRadians( rotate * delta );
					break;
				default:
					break;
			}

			boolean turn = oldangle != angleRads;

			float dirx = Trig.cos( angleRads );
			float diry = Trig.sin( angleRads );
			Vector2f dir = new Vector2f( dirx, diry );
			VectorUtils.rotate90( dir );
			dir.scale( TraceGame.game.trace.slipDistance );

			if( arena.collision( position.x + dirx - dir.x, position.y + diry - dir.y,
					position.x + dirx + dir.x, position.y + diry + dir.y ).length > 0 )
			{
				slipSpeed +=
						TraceGame.game.trace.slipBoost * delta / TraceGame.game.trace.slipAccel;
			}
			else
			{
				slipSpeed -=
						TraceGame.game.trace.slipBoost * delta
								/ TraceGame.game.trace.slipDeccel;
			}
			slipSpeed = Range.limit( slipSpeed, 1, TraceGame.game.trace.slipBoost );

			if( slipSpeed > 1 )
			{
				if( slipSource == null )
				{
					slipSource = SoundSystem.getSource();
					slipSource.lock( this );
					slipSource.setLoop( true );
					slipSource.bindSound( hiss );
					slipSource.play();
				}

				slipSource.setGain( 0.1f * ( slipSpeed - 1 )
						/ ( TraceGame.game.trace.slipBoost - 1 ) );
			}
			else if( slipSource != null )
			{
				slipSource.stop();
				slipSource.release( this );
				slipSource = null;
			}

			float d = delta * speed * slipSpeed;

			Vector2f old = new Vector2f( position );

			position.x += d * Trig.cos( angleRads );
			position.y += d * Trig.sin( angleRads );

			boolean bounce = arenaCollision();
			bounce |= traceCollision( old, position );

			if( bounce )
			{
				Sounds.bounce();
			}

			boolean segSwitch = false;

			l -= d;
			if( l <= 0 && gapLength > 0 )
			{
				onPath = !onPath;
				l = onPath ? pathLength : gapLength;
				segSwitch = true;
			}

			if( bounce || turn || segSwitch )
			{ // starts a new segment
				path.startSeg( position.x, position.y, onPath ? PATH : GAP );

				if( segSwitch || bounce )
				{
					// need to have a degenerate segment
					path.startSeg( position.x, position.y, onPath ? PATH : GAP );
				}
			}
			else
			{
				// just extend the current segment
				path.length +=
						Pointf.distance( path.xCoords[ path.segCount() - 1 ],
								path.yCoords[ path.segCount() - 1 ], position.x, position.y );
				path.xCoords[ path.segCount() - 1 ] = position.x;
				path.yCoords[ path.segCount() - 1 ] = position.y;
			}

			if( length > -1 && path.length > length )
			{
				// trace will be removed three times faster than it is
				// produced
				path.removeFromStart( Math.min( path.length - length, 3 * d ) );
			}
		}
		else
		{
			if( slipSource != null )
			{
				slipSource.stop();
				slipSource.release( this );
				slipSource = null;
			}
		}
	}

	/**
	 * Check for collisions with other traces
	 * 
	 * @param oldPos
	 * @param newpos
	 * @return <code>true</code> if we bounce, <code>false</code>
	 *         otherwise
	 */
	private boolean traceCollision( final Vector2f oldPos, Vector2f newpos )
	{
		// look for collisions
		Collision[] collision = arena.collision( oldPos.x, oldPos.y, newpos.x, newpos.y );

		arena.getCollider().add(
				new TraceSegment( oldPos.x, oldPos.y, newpos.x, newpos.y, player,
						onPath ? PATH : GAP ) );

		if( collision.length > 0 )
		{
			Collision.sort( oldPos, collision );

			for( int i = 0; i < collision.length; i++ )
			{
				if( collision[ i ].seg.type == GAP )
				{
					Sounds.score();
					boolean own = collision[ i ].seg.owner == player;
					arena.score( player, own ? TraceGame.game.score.gap.own
							: TraceGame.game.score.gap.other );

					if( !own )
					{
						arena.score( collision[ i ].seg.owner,
								TraceGame.game.score.gap.scoredOn );
					}

					float bv = speed * 0.5f;
					SeekSparks scoreBurst =
							new SeekSparks( 30, 2, 5, 1, collision[ i ].seg.owner.colour );
					scoreBurst.position( collision[ i ].collisionPoint().getX(), collision[ i ]
							.collisionPoint().getY(), 0 );
					scoreBurst.velocity( bv * Trig.cos( angleRads ),
							bv * Trig.sin( angleRads ), 200, 2 );
					scoreBurst.target( position, 5 );

					arena.addEntity( scoreBurst );
				}
				else
				{ // hit a seg
					Vector2f seg =
							new Vector2f( collision[ i ].seg.bx - collision[ i ].seg.ax,
									collision[ i ].seg.by - collision[ i ].seg.ay );
					seg.normalise();

					Vector2f trace =
							new Vector2f( collision[ i ].testSeg.bx - collision[ i ].testSeg.ax,
									collision[ i ].testSeg.by - collision[ i ].testSeg.ay );
					trace.normalise();

					Vector2f parallel = new Vector2f( seg );
					parallel.scale( VectorUtils.projection( trace, seg ) );
					Vector2f perp = new Vector2f( trace );
					Vector2f.sub( perp, parallel, perp );

					float allowance =
							Trig.sin( Trig.toRadians( TraceGame.game.trace.traceBounce ) );

					if( perp.length() < allowance )
					{
						position.set( collision[ i ].collisionPoint() );
						perp.scale( -1 );

						// move away from the seg a bit
						Vector2f.add( perp, position, position );

						// reflect
						perp.scale( 2 );
						Vector2f.add( trace, perp, trace );
						angleRads = Trig.atan2( trace.y, trace.x );

						return true;
					}
					else
					{
						if( shields > 0 )
						{
							shields--;

							Sparks shieldBurst = new Sparks( 15, 5, 5, 1, player.colour );
							shieldBurst.position( position.x, position.y, 0 );
							shieldBurst.velocity( 0.3f * speed * Trig.cos( angleRads ), 0.3f
									* speed * Trig.sin( angleRads ), 50, 1 );
							arena.addEntity( shieldBurst );

							Sounds.shieldStrike();

							if( collision[ i ].seg.owner == player )
							{
								// selfstrike
								arena.score( player, TraceGame.game.score.strike.self );
							}
							else
							{
								// strike
								arena.score( player, TraceGame.game.score.strike.other );
								arena.score( collision[ i ].seg.owner,
										TraceGame.game.score.strike.struck );
							}
						}
						else
						{
							position.set( collision[ i ].collisionPoint() );
							die( collision[ i ].seg.owner );
						}

						return false;
					}
				}
			}
		}

		return false;
	}

	private boolean arenaCollision()
	{
		Vector2f v = new Vector2f( Trig.cos( angleRads ), Trig.sin( angleRads ) );

		float allowance = Trig.sin( Trig.toRadians( TraceGame.game.trace.arenaBounce ) );

		boolean bounce = false;

		if( position.x < 0 )
		{
			position.x = 0;

			if( Math.abs( v.x ) <= allowance )
			{ // boing
				bounce = true;
				v.x = -v.x;
			}
			else
			{ // splat
				die( null );
			}
		}
		else if( position.x > 800 )
		{
			position.x = 800;

			if( Math.abs( v.x ) <= allowance )
			{
				bounce = true;
				v.x = -v.x;
			}
			else
			{
				die( null );
			}
		}

		if( position.y < 0 )
		{
			position.y = 0;

			if( Math.abs( v.y ) < allowance )
			{
				bounce = true;
				v.y = -v.y;
			}
			else
			{
				die( null );
			}
		}
		else if( position.y > 600 )
		{
			position.y = 600;

			if( Math.abs( v.y ) < allowance )
			{
				bounce = true;
				v.y = -v.y;
			}
			else
			{
				die( null );
			}
		}

		if( bounce )
		{
			angleRads = Trig.atan2( v.y, v.x );
		}

		return bounce;
	}

	/**
	 * @param r
	 */
	public void render( StackedRenderer r )
	{
		float[] verts = new float[ 4 * 3 * path.segCount() + 6 ];
		int[] colours = new int[ 4 * path.segCount() + 2 ];

		Vector2f dir = new Vector2f();

		int vi = 0;
		int ci = 0;

		float edge = TraceGame.client.traceWidth;
		float core = edge * TraceGame.client.traceCore;
		int outerPath = Colour.withAlphai( player.colour, 0 );
		int innergap = Colour.withAlphai( player.colour, TraceGame.client.gapAlpha );

		int outergap = Colour.withAlphai( innergap, 0 );

		Vector2f firstDir = null;

		for( int i = 0; i < path.segCount(); i++ )
		{
			if( i < path.segCount() - 1 )
			{
				float dx = path.xCoords[ i + 1 ] - path.xCoords[ i ];
				float dy = path.yCoords[ i + 1 ] - path.yCoords[ i ];

				if( i > 0 )
				{
					dx += path.xCoords[ i ] - path.xCoords[ i - 1 ];
					dx /= 2;
					dy += path.yCoords[ i ] - path.yCoords[ i - 1 ];
					dy /= 2;
				}

				if( dx != 0 || dy != 0 )
				{
					dir.x = dx;
					dir.y = dy;
					VectorUtils.rotate90( dir );
					dir.normalise();

					if( firstDir == null )
					{
						firstDir = new Vector2f( dir );
					}
				}
			}

			boolean onpath = path.segType[ i ] == PATH;

			verts[ vi++ ] = path.xCoords[ i ] - dir.x * edge;
			verts[ vi++ ] = path.yCoords[ i ] - dir.y * edge;
			verts[ vi++ ] = onpath ? 0 : -0.1f;
			colours[ ci++ ] = onpath ? outerPath : outergap;

			verts[ vi++ ] = path.xCoords[ i ] - dir.x * core;
			verts[ vi++ ] = path.yCoords[ i ] - dir.y * core;
			verts[ vi++ ] = onpath ? 0 : -0.1f;
			colours[ ci++ ] = onpath ? player.colour : innergap;

			verts[ vi++ ] = path.xCoords[ i ] + dir.x * core;
			verts[ vi++ ] = path.yCoords[ i ] + dir.y * core;
			verts[ vi++ ] = onpath ? 0 : -0.1f;
			colours[ ci++ ] = onpath ? player.colour : innergap;

			verts[ vi++ ] = path.xCoords[ i ] + dir.x * edge;
			verts[ vi++ ] = path.yCoords[ i ] + dir.y * edge;
			verts[ vi++ ] = onpath ? 0 : -0.1f;
			colours[ ci++ ] = onpath ? outerPath : outergap;
		}

		// end points
		VectorUtils.rotateMinus90( dir );
		verts[ vi++ ] =
				path.xCoords[ path.segCount() - 1 ] + TraceGame.client.traceWidth
						* Trig.cos( angleRads );
		verts[ vi++ ] =
				path.yCoords[ path.segCount() - 1 ] + TraceGame.client.traceWidth
						* Trig.sin( angleRads );
		verts[ vi++ ] = 0;
		colours[ ci++ ] = player.colour;

		assert firstDir != null;
		verts[ vi++ ] = path.xCoords[ 0 ] + TraceGame.client.traceWidth * firstDir.x;
		verts[ vi++ ] = path.yCoords[ 0 ] + TraceGame.client.traceWidth * firstDir.y;
		verts[ vi++ ] = 0;
		colours[ ci++ ] = player.colour;

		int[] tris = new int[ 6 * 3 * ( path.segCount() - 1 ) + 3 * 8 ];

		int ti = 0;
		for( int i = 0; i < path.segCount() - 1; i++ )
		{
			int b = i * 4;

			for( int j = 0; j < 3; j++ )
			{
				tris[ ti++ ] = j + b;
				tris[ ti++ ] = j + b + 4;
				tris[ ti++ ] = j + b + 5;

				tris[ ti++ ] = j + b;
				tris[ ti++ ] = j + b + 5;
				tris[ ti++ ] = j + b + 1;
			}
		}

		// endpoints
		int vc = verts.length / 3;
		tris[ ti++ ] = vc - 1;
		tris[ ti++ ] = 0;
		tris[ ti++ ] = 1;
		tris[ ti++ ] = vc - 1;
		tris[ ti++ ] = 1;
		tris[ ti++ ] = 2;
		tris[ ti++ ] = vc - 1;
		tris[ ti++ ] = 2;
		tris[ ti++ ] = 3;

		tris[ ti++ ] = vc - 2;
		tris[ ti++ ] = vc - 3;
		tris[ ti++ ] = vc - 4;
		tris[ ti++ ] = vc - 2;
		tris[ ti++ ] = vc - 4;
		tris[ ti++ ] = vc - 5;
		tris[ ti++ ] = vc - 2;
		tris[ ti++ ] = vc - 5;
		tris[ ti++ ] = vc - 6;

		r.addTriangles( verts, null, colours, tris, null );

		if( !player.dead && shields > 0 )
		{
			int alpha = ( int ) ( 255 * ( float ) shields / TraceGame.game.trace.maxShields );
			r.pushMatrix();
			r.translate( position.x, position.y, 0 );
			r.rotate( angleRads, 0, 0, 1 );

			Colour.withAlphai( shield.colours, alpha );
			shield.render( r );
			Colour.withAlphai( shield.colours, 255 );

			r.popMatrix();
		}

		// cloak
		if( !player.dead && cloakTime > 0 )
		{
			float a = Math.min( 1, cloakTime );
			Virus.drawCloak( position.x, position.y, cloakRot, 10, a, r );
		}
	}

	void populate( BucketGrid<TraceSegment> segs, List<TraceSegment> gaps )
	{
		boolean onGap = path.segType[ 0 ] == GAP;
		float x = path.xCoords[ 0 ];
		float y = path.yCoords[ 0 ];

		for( int i = 0; i < path.segCount() - 1; i++ )
		{
			TraceSegment ts =
					new TraceSegment( path.xCoords[ i ], path.yCoords[ i ],
							path.xCoords[ i + 1 ], path.yCoords[ i + 1 ], player,
							path.segType[ i ] );
			segs.add( ts );

			if( onGap && path.segType[ i ] == PATH )
			{ // come to the end of a gap
				TraceSegment gap =
						new TraceSegment( x, y, path.xCoords[ i ], path.yCoords[ i ], player,
								GAP );
				gaps.add( gap );
				onGap = false;
			}
			else if( !onGap && path.segType[ i ] == GAP )
			{
				x = path.xCoords[ i ];
				y = path.yCoords[ i ];
				onGap = true;
			}
		}
	}
}
