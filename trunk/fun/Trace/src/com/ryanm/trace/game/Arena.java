
package com.ryanm.trace.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.sound.SoundSystem;
import com.rugl.sound.Source;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.util.Colour;
import com.ryanm.trace.Background;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;
import com.ryanm.trace.Player;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.entities.FadeNote;
import com.ryanm.trace.game.entities.Powerup;
import com.ryanm.trace.lobby.Lobby;
import com.ryanm.trace.lobby.Scoreboard;
import com.ryanm.util.Bag;
import com.ryanm.util.BucketGrid;
import com.ryanm.util.Segment;

/**
 * @author ryanm
 */
public class Arena implements Phase
{
	private final Lobby lobby;

	private boolean paused = false;

	/**
	 * Seconds that the game has been running
	 */
	public float time = 0;

	/***/
	public final Trace[] traces;

	/***/
	public Bag<Entity> entities = new Bag<Entity>();

	private boolean done = false;

	private float spawnTime;

	private boolean exiting = false;

	private float endTime = 1;

	private BucketGrid<TraceSegment> collider;

	private List<TraceSegment> gapList;

	private Source[] pausedSounds = null;

	/**
	 * @param lobby
	 * @param players
	 */
	public Arena( Lobby lobby, Trace... players )
	{
		this.lobby = lobby;

		traces = players;

		for( int i = 0; i < traces.length; i++ )
		{
			traces[ i ].arena = this;

			traces[ i ].player.bot.reset();
		}
	}

	/**
	 * Provides an efficient way to perform intersection queries on the
	 * trace segments in the arena
	 * 
	 * @return A {@link BucketGrid} containing the trace segments
	 */
	public BucketGrid<TraceSegment> getCollider()
	{
		if( collider == null )
		{
			float bs = TraceGame.client.bucketSize;

			collider =
					new BucketGrid<TraceSegment>( bs, ( int ) Math.ceil( 800.0f / bs ),
							( int ) Math.ceil( 600.0f / bs ) );

			gapList = new ArrayList<TraceSegment>();

			for( int i = 0; i < traces.length; i++ )
			{
				traces[ i ].populate( collider, gapList );
			}
		}

		return collider;
	}

	/**
	 * Provides a list of all the gaps in the arena
	 * 
	 * @return the gap list
	 */
	public List<TraceSegment> getGapList()
	{
		getCollider();
		return gapList;
	}

	@Override
	public boolean isFinished()
	{
		return done;
	}

	@Override
	public Phase next()
	{
		return lobby;
	}

	@Override
	public void reset()
	{
		Buttons.setText( "Resume", "Quit", null, null, null, null );
		TraceGame.drawLastArena = false;

		spawnTime = TraceGame.game.powerups.spawnRate.toValue( TraceGame.rng.nextFloat() );
	}

	@Override
	public void advance( float delta )
	{
		if( Buttons.esc() )
		{
			paused = !paused;

			if( paused )
			{
				pausedSounds = SoundSystem.getPlayingSources();

				for( Source s : pausedSounds )
				{
					s.pause();
				}
			}
			else
			{
				for( Source s : pausedSounds )
				{
					s.play();
				}
				pausedSounds = null;
			}
		}

		if( Buttons.enter() && paused )
		{
			done = true;

			for( Trace t : traces )
			{
				t.releaseSources();
			}
		}

		Buttons.visible = paused;

		if( !paused )
		{
			time += delta;

			spawnTime -= delta;
			if( spawnTime < 0 )
			{
				// spawn a powerup
				spawnTime =
						TraceGame.game.powerups.spawnRate.toValue( TraceGame.rng.nextFloat() );
				Powerup p = TraceGame.game.powerups.choosePowerup( this );

				addEntity( p );
			}

			advanceEntities( delta );

			boolean allDead = true;
			int aliveCount = 0;
			for( int i = 0; i < traces.length; i++ )
			{
				traces[ i ].advance( delta );

				allDead &= traces[ i ].player.dead;

				aliveCount += traces[ i ].player.dead ? 0 : 1;
			}

			if( allDead )
			{
				exiting = true;
			}

			if( aliveCount == 1 )
			{
				// apply endgame conditions
				for( int i = 0; i < traces.length; i++ )
				{
					if( !traces[ i ].player.dead )
					{
						if( traces[ i ].length != -1 )
						{
							traces[ i ].length += delta * TraceGame.game.endgame.traceGrowth;
						}

						traces[ i ].speed += delta * TraceGame.game.endgame.speedUp;
					}
				}

				if( TraceGame.game.endgame.seizure )
				{
					// background
					Background.frequency += delta * 0.03f;
					Background.amplitude += delta * 0.1f;
				}
			}
		}

		if( exiting )
		{
			endTime -= delta;
			if( endTime < 0 )
			{
				done = true;
			}

			Background.frequency = 0.2f;
			Background.amplitude = 1;
		}

		collider = null;
		gapList = null;
	}

	private void advanceEntities( float delta )
	{
		for( int i = 0; i < entities.size(); )
		{
			Entity e = entities.get( i );
			if( e.advance( delta ) )
			{
				entities.take( i );
			}
			else
			{
				i++;
			}
		}
	}

	/**
	 * Adds an entity to the arena
	 * 
	 * @param e
	 */
	public void addEntity( Entity e )
	{
		entities.put( e );
	}

	/**
	 * Adds a score to a player, and adds the score text to the arena
	 * 
	 * @param recipient
	 * @param score
	 */
	public void score( Player recipient, int score )
	{
		if( score != 0 )
		{
			recipient.score += score;

			for( int i = 0; i < traces.length; i++ )
			{
				if( traces[ i ].player == recipient )
				{
					addEntity( new FadeNote( String.valueOf( score ), recipient.colour,
							traces[ i ].position.x, traces[ i ].position.y + 20, 30, 1 ) );
				}
			}
		}
	}

	private static TexturedShape pausedSign;

	private static ColouredShape border;

	@Override
	public void draw( StackedRenderer r )
	{
		if( border == null )
		{
			Shape s = ShapeUtil.outerQuad( 0, 0, 800, 600, 1, 0 );
			border = new ColouredShape( s, Colour.white, null );
		}

		border.render( r );

		for( int i = 0; i < entities.size(); i++ )
		{
			entities.get( i ).draw( r );
		}

		drawTraces( r );

		renderEndgameScore( r );

		if( paused )
		{
			if( pausedSign == null )
			{
				pausedSign = TraceGame.font.buildTextShape( "paused", Colour.white );
				pausedSign.translate( 500, 260, 1 );
			}

			pausedSign.render( r );

			List<Player> players = new ArrayList<Player>( traces.length );
			for( int i = 0; i < traces.length; i++ )
			{
				players.add( traces[ i ].player );
			}

			Scoreboard.render( r, players, 130, 20, 410, true );
		}
	}

	/**
	 * Draws the traces
	 * 
	 * @param r
	 */
	public void drawTraces( StackedRenderer r )
	{
		for( int i = 0; i < traces.length; i++ )
		{
			traces[ i ].render( r );
		}
	}

	/**
	 * Finds segments that intersect the supplied segment
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return An array of intersecting segments
	 */
	public Collision[] collision( float ax, float ay, float bx, float by )
	{
		Set<TraceSegment> segs = new HashSet<TraceSegment>();
		Segment testSeg = new Segment( ax, ay, bx, by );
		getCollider().test( testSeg, segs );

		TraceSegment[] c = segs.toArray( new TraceSegment[ segs.size() ] );
		Collision[] collisions = new Collision[ c.length ];

		for( int i = 0; i < c.length; i++ )
		{
			collisions[ i ] = new Collision( c[ i ], testSeg );
		}

		return collisions;
	}

	private void renderEndgameScore( StackedRenderer sr )
	{
		if( traces.length > 1 )
		{
			// find alive
			Trace alive = null;
			int aliveCount = 0;
			for( int i = 0; i < traces.length; i++ )
			{
				if( !traces[ i ].player.dead )
				{
					aliveCount++;
					alive = traces[ i ];
				}
			}

			if( aliveCount == 1 )
			{
				assert alive != null;

				// find highest scorer
				Trace leader = null;
				for( int i = 0; i < traces.length; i++ )
				{
					if( leader == null || leader.player.score < traces[ i ].player.score )
					{
						leader = traces[ i ];
					}
				}

				assert leader != null;

				String text;

				if( alive == leader )
				{
					// find second place
					Trace second = null;
					for( int i = 0; i < traces.length; i++ )
					{
						if( ( second == null || second.player.score < traces[ i ].player.score )
								&& traces[ i ] != leader )
						{
							second = traces[ i ];
						}
					}
					assert second != null;

					text = leader.player.score - second.player.score + "\npoint lead";
				}
				else
				{
					text = leader.player.score - alive.player.score + 1 + "\nto win!";
				}

				TextLayout tl =
						new TextLayout( text, TraceGame.font, Alignment.CENTER, 800,
								alive.player.colour );
				TexturedShape ts = tl.textShape;
				ts.scale( 0.5f, 0.5f, 1 );
				Vector3f c = ts.getBounds().getCenter();
				ts.translate( -c.x, -c.y, 1 );

				Vector2f delta =
						new Vector2f( 400 - alive.position.x, 300 - alive.position.y );
				if( delta.x == 0 && delta.y == 0 )
				{
					delta.x = 1;
				}
				delta.normalise();
				delta.scale( 500 );
				ts.translate( 400 + delta.x, 300 + delta.y, 0 );
				Buttons.bounds( ts );
				ts.render( sr );
			}
		}
	}
}