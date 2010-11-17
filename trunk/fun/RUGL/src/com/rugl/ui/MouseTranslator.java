
package com.rugl.ui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.rugl.DisplayConfigurable;
import com.rugl.GameBox;
import com.ryanm.util.geom.Pointf;

/**
 * Hides the difference between screen and game resolution
 * 
 * @author ryanm
 */
public class MouseTranslator
{
	private static float gameWidth = 800;

	private static float gameheight = 600;

	private static float wm;

	private static float hm;

	private static Pointf m = new Pointf();

	static
	{
		GameBox.dispConf.addListener( new DisplayConfigurable.Listener() {
			@Override
			public void displayChanged( boolean res, boolean fs, boolean vsync, boolean fr,
					boolean fsaa )
			{
				if( res )
				{
					refresh();
				}
			}
		} );
	}

	/**
	 * Sets the desired game resolution
	 * 
	 * @param width
	 * @param height
	 */
	public static void setGameDimenion( float width, float height )
	{
		gameWidth = width;
		gameheight = height;
		refresh();
	}

	private static void refresh()
	{
		wm = gameWidth / Display.getDisplayMode().getWidth();
		hm = gameheight / Display.getDisplayMode().getHeight();
	}

	/**
	 * Gets the current position of the mouse
	 * 
	 * @return the mouse position, in terms of the game resolution
	 */
	public static Pointf getMouse()
	{
		m.setLocation( getMouseX(), getMouseY() );
		return m;
	}

	/**
	 * @return The mouse x-coordinate, in game space
	 */
	public static float getMouseX()
	{
		return wm * Mouse.getX();
	}

	/**
	 * @return The mouse y-coordinate, in game space
	 */
	public static float getMouseY()
	{
		return hm * Mouse.getY();
	}
}
