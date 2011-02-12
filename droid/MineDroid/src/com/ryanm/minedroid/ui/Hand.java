
package com.ryanm.minedroid.ui;

import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.util.Trig;
import com.ryanm.droid.rugl.util.geom.Vector2f;
import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.minedroid.Player;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;

/**
 * Draws the currently-held item
 * 
 * @author ryanm
 */
@Variable( "Hand" )
@Summary( "Hand motion options" )
public class Hand
{
	private final Player player;

	/***/
	@Variable
	public float size = 300;

	/***/
	@Variable
	public Vector2f restPos = new Vector2f( 700, 60 );

	/***/
	@Variable
	public Vector2f strikePos = new Vector2f( 510, 40 );

	/***/
	@Variable
	public float restRotation = 10;

	/***/
	@Variable
	public float strikeRotation = 80;

	/***/
	@Variable
	public float missTime = 0.6f;

	/***/
	@Variable
	public float hitTime = 0.3f;

	private float strikeCycle = 0;

	/***/
	private boolean swing = false;

	private float currentStrikeTime = missTime;

	/**
	 * @param player
	 */
	public Hand( Player player )
	{
		this.player = player;
	}

	/**
	 * Initiates a single strike if we are not already striking
	 * 
	 * @param fast
	 *           <code>true</code> to do a fast strike,
	 *           <code>false</code> for slow
	 */
	public void strike( boolean fast )
	{
		if( strikeCycle == 0 )
		{
			currentStrikeTime = fast ? hitTime : missTime;
			strikeCycle = Float.MIN_VALUE;
		}
	}

	/**
	 * Starts repeated striking
	 * 
	 * @param fast
	 */
	public void repeatedStrike( boolean fast )
	{
		swing = true;
		currentStrikeTime = fast ? hitTime : missTime;
	}

	/**
	 * Stops repeated striking
	 */
	public void stopStriking()
	{
		swing = false;
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		if( swing || strikeCycle != 0 )
		{
			strikeCycle += Trig.TWO_PI * delta / currentStrikeTime;
		}

		if( strikeCycle > Trig.TWO_PI )
		{
			strikeCycle = 0;
		}
	}

	/**
	 * @param r
	 */
	public void draw( StackedRenderer r )
	{
		if( player.inHand != null )
		{
			r.pushMatrix();

			float swing = -Math.abs( Trig.cos( 0.5f * strikeCycle ) ) + 1;
			float rot = Range.toValue( swing, restRotation, strikeRotation );
			float x = Range.toValue( swing, restPos.x, strikePos.x );
			float y = Range.toValue( swing, restPos.y, strikePos.y );

			r.translate( x, y, 0 );
			r.rotate( rot, 0, 0, 1 );
			r.scale( size, size, 1 );

			player.inHand.itemShape.render( r );

			r.popMatrix();
		}
	}

}
