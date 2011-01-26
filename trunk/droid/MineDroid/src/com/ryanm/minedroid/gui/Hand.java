
package com.ryanm.minedroid.gui;

import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.gl.StackedRenderer;
import com.ryanm.droid.rugl.util.Trig;
import com.ryanm.droid.rugl.util.geom.Vector2f;
import com.ryanm.droid.rugl.util.math.Range;
import com.ryanm.minedroid.Player;

/**
 * Draws the currently-held item
 * 
 * @author ryanm
 */
@Variable
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
	public boolean swing = false;

	/**
	 * @param player
	 */
	public Hand( Player player )
	{
		this.player = player;
	}

	/**
	 * Initiates a single strike if we are not already striking
	 */
	public void strike()
	{
		if( strikeCycle == 0 )
		{
			strikeCycle = Float.MIN_VALUE;
		}
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		if( swing || strikeCycle != 0 )
		{
			strikeCycle += Trig.TWO_PI * delta / missTime;
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
