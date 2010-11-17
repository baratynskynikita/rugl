
package com.ryanm.util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author ryanm
 */
public class ImageSorter extends JFrame
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		new ImageSorter( args[ 0 ], args[ 1 ], args[ 2 ] );
	}

	private File[] inputs;

	private int index = -1;

	private File leftOutput;

	private File rightOutput;

	private ImagePanel panel = new ImagePanel();

	/**
	 * @param in
	 * @param leftOut
	 * @param rightOut
	 */
	public ImageSorter( String in, String leftOut, String rightOut )
	{
		super( "ImageSorter" );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		inputs = new File( in ).listFiles( new FilenameFilter() {

			@Override
			public boolean accept( File dir, String name )
			{
				String s = name.toLowerCase();

				return s.endsWith( "jpg" );
			}

		} );

		leftOutput = new File( in, leftOut );
		rightOutput = new File( in, rightOut );

		leftOutput.mkdirs();
		rightOutput.mkdirs();

		assert leftOutput.canWrite();
		assert rightOutput.canWrite();

		panel.setPreferredSize( new Dimension( 640, 480 ) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( panel, BorderLayout.CENTER );

		panel.addKeyListener( new KeyAdapter() {

			@Override
			public void keyReleased( KeyEvent e )
			{
				if( e.getKeyCode() == KeyEvent.VK_RIGHT )
				{
					inputs[ index ].renameTo( new File( rightOutput, inputs[ index ].getName() ) );
				}
				else if( e.getKeyCode() == KeyEvent.VK_LEFT )
				{
					inputs[ index ].renameTo( new File( leftOutput, inputs[ index ].getName() ) );
				}

				nextImage();
			}

		} );

		panel.addMouseListener( new MouseAdapter() {

			@Override
			public void mouseReleased( MouseEvent e )
			{
				panel.requestFocusInWindow();
			}

			@Override
			public void mousePressed( MouseEvent e )
			{
				panel.requestFocusInWindow();
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				panel.requestFocusInWindow();
			}

			@Override
			public void mouseClicked( MouseEvent e )
			{
				panel.requestFocusInWindow();
			}

		} );

		panel.requestFocusInWindow();

		nextImage();

		pack();
		setVisible( true );
	}

	private void nextImage()
	{
		index++;

		if( index >= inputs.length )
		{
			System.exit( 0 );
		}

		try
		{
			panel.image = ImageIO.read( inputs[ index ] );
			panel.repaint();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	private class ImagePanel extends JPanel
	{
		private Image image = null;

		@Override
		public void paint( Graphics g )
		{
			g.setColor( Color.LIGHT_GRAY );
			g.fillRect( 0, 0, getWidth(), getHeight() );
			if( image != null )
			{
				g.drawImage( image, 0, 0, new ImageObserver() {

					@Override
					public boolean imageUpdate( Image img, int infoflags, int x, int y, int width,
							int height )
					{
						repaint();

						return infoflags == ALLBITS;
					}

				} );
			}

			g.setColor( Color.BLACK );
			g.drawString( inputs[ index ].getName(), 10, getHeight() - 10 );
		}
	}

}
