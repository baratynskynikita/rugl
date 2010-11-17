
package com.ryanm.trace.game.entities;

import com.rugl.geom.TexturedShape;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.trace.TraceGame;
import com.ryanm.trace.game.Entity;

/**
 * @author ryanm
 */
public class FadeNote implements Entity
{
	/***/
	public final TexturedShape ts;

	/***/
	private float lifeTotal, life;

	/**
	 * @param text
	 * @param c
	 * @param x
	 * @param y
	 * @param height
	 * @param life
	 */
	public FadeNote( String text, int c, float x, float y, float height, float life )
	{
		ts = TraceGame.font.buildTextShape( text, c );
		float scale = height / ts.getBounds().getHeight();
		ts.scale( scale, scale, 1 );

		ts.translate( -ts.getBounds().getWidth() / 2, -ts.getBounds().getHeight() / 2, 0 );
		ts.translate( x, y, 0 );

		lifeTotal = life;
		this.life = life;
	}

	@Override
	public boolean advance( float delta )
	{
		life -= delta;

		return life < 0;
	}

	@Override
	public void draw( StackedRenderer r )
	{
		Colour.withAlphai( ts.colours, ( int ) ( 255 * life / lifeTotal ) );
		ts.render( r );
	}
}
