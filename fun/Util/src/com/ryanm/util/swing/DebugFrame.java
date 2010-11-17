
package com.ryanm.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * A simple frame that allows quick and easy visualisation of
 * something
 * 
 * @author ryanm
 */
public class DebugFrame extends JFrame
{
	private DebugPanel panel;

	/**
	 * Creates a new Debug frame
	 * 
	 * @param name
	 *           The name for the frame
	 * @param x
	 *           The width
	 * @param y
	 *           The height
	 */
	public DebugFrame( String name, int x, int y )
	{
		setTitle( "Debug Frame : " + name );

		panel = new DebugPanel( x, y );

		getContentPane().add( panel );

		clear();

		pack();
		setVisible( true );
	}

	/**
	 * Creates a new Debug frame
	 * 
	 * @param x
	 *           The width
	 * @param y
	 *           The height
	 */
	public DebugFrame( int x, int y )
	{
		setTitle( "Debug Frame" );

		panel = new DebugPanel( x, y );

		getContentPane().add( panel );

		clear();

		pack();
		setVisible( true );
	}

	/**
	 * Gets the graphics for the backing image. Stuff drawn on this
	 * context can only be cleared with a call to clear()
	 * 
	 * @return A {@link Graphics2D} object
	 */
	public Graphics2D getDebugGraphics()
	{
		Graphics2D g = panel.buff.createGraphics();
		return g;
	}

	/**
	 * Gets the graphics for the frame itself. Stuff drawn to this
	 * context will be cleared when refresh() is called
	 * 
	 * @return A {@link Graphics2D} object
	 */
	public Graphics2D getTempDebugGraphics()
	{
		Graphics2D g = ( Graphics2D ) panel.getGraphics();
		return g;
	}

	/**
	 * Refreshes the frame by drawing the contents of the back buffer
	 */
	public void refresh()
	{
		panel.paint( panel.getGraphics() );
	}

	/**
	 * Clears the back buffer
	 */
	public void clear()
	{
		Graphics g = panel.buff.getGraphics();

		g.setColor( Color.white );
		g.fillRect( 0, 0, panel.getWidth(), panel.getHeight() );
	}

	private class DebugPanel extends JComponent
	{
		private int x, y;

		private BufferedImage buff;

		private DebugPanel( int x, int y )
		{
			this.x = x;
			this.y = y;
			buff = new BufferedImage( x, y, BufferedImage.TYPE_INT_ARGB );
		}

		@Override
		public void paint( Graphics g )
		{
			g.drawImage( buff, 0, 0, this );
		}

		@Override
		public int getWidth()
		{
			return x;
		}

		@Override
		public int getHeight()
		{
			return y;
		}

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension( x, y );
		}
	}
}