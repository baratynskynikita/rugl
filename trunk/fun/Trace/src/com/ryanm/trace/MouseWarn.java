
package com.ryanm.trace;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import com.rugl.GameBox;
import com.rugl.geom.TexturedShape;
import com.rugl.input.MouseListener;
import com.rugl.renderer.StackedRenderer;
import com.rugl.text.TextLayout;
import com.rugl.text.TextLayout.Alignment;
import com.rugl.util.Colour;
import com.ryanm.util.math.FunctionApproximation;

/**
 * Tells the user not to use the mouse
 * 
 * @author ryanm
 */
public class MouseWarn implements MouseListener
{
	private static final String[] convs;
	static
	{
		convs =
				new String[] { "I'm afraid\nI can't\nhelp you",
						"you have to\nuse the\nkeyboard",
						"just use the\narrow keys,\nreturn and escape",
						"honestly,\nmr keyboard\nis your friend",
						"break free of\nyour mousey\nshackles!\nI believe in you!",
						"I'm just getting\nin the way\nnow, aren't i?",
						"I think I'm\ngoing to go", "goodbye!\nhave fun!" };
	}

	private float time = 0;

	private float activeTime = 0;

	private boolean activity = false;

	private float lastActive = -10;

	private float fadeTime = 1;

	private float switchTime = 2;

	private boolean mouseHidden = false;

	private FunctionApproximation size = new FunctionApproximation( 0, 1, 0.5f, 1, 1, 0 );

	/***/
	public MouseWarn()
	{
		GameBox.addMouseListener( this );
	}

	/**
	 * @param delta
	 */
	public void advance( float delta )
	{
		time += delta;

		if( activity )
		{
			lastActive = time;
			activeTime += delta;
			activity = false;
		}

		int convIndex = ( int ) ( activeTime / switchTime );

		if( convIndex >= convs.length && !mouseHidden )
		{
			mouseHidden = true;
			try
			{
				IntBuffer im = BufferUtils.createIntBuffer( 1 );

				Cursor c = new Cursor( 1, 1, 0, 0, 1, im, null );
				Mouse.setNativeCursor( c );
			}
			catch( LWJGLException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param r
	 */
	public void draw( StackedRenderer r )
	{
		float a = size.evaluate( ( time - lastActive ) / fadeTime );

		int convIndex = ( int ) ( activeTime / switchTime );

		if( convIndex < convs.length && a > 0 )
		{
			int color = Colour.packInt( 255, 255, 255, ( int ) ( 255 * a ) );
			TextLayout tl =
					new TextLayout( convs[ convIndex ], TraceGame.font, Alignment.CENTER, 800,
							color );
			TexturedShape ts = tl.textShape;
			Vector3f c = ts.getBounds().getCenter();
			ts.translate( -c.x, -c.y, 0 );
			ts.scale( a / 2, a / 2, 1 );

			ts.translate( Mouse.getX(), Mouse.getY(), 1 );
			Buttons.bounds( ts );

			ts.render( r );
		}
	}

	@Override
	public void mouseButton( int button, boolean down )
	{
		if( down )
		{
			activity = true;
		}
	}

	@Override
	public void mouseMoved( int x, int y, int dx, int dy )
	{
		activity = true;
	}

	@Override
	public void mouseWheel( int dz )
	{
		activity = true;
	}
}
