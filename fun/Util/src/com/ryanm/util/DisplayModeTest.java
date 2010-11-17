
package com.ryanm.util;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ryanm.util.XRandR.Screen;

/**
 * @author ryanm
 */
public class DisplayModeTest
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		JFrame frame = new JFrame( "DisplayMode test" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		JTextArea text = new JTextArea();
		text.setTabSize( 3 );
		frame.getContentPane().add( new JScrollPane( text ) );

		text.append( "AWT display mode handling\n" );
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for( GraphicsDevice dev : ge.getScreenDevices() )
		{
			text.append( "\tDevice: " + dev.getIDstring() + "\n" );
			text.append( "\tFullscreen support: " + dev.isFullScreenSupported() + "\n" );
			text.append( "\tDisplay modes:\n" );
			for( DisplayMode mode : dev.getDisplayModes() )
			{
				text.append( "\t\t" + mode.getWidth() + "x" + mode.getHeight() + "\n" );
			}
		}

		text.append( "xrandr display mode handling\n" );
		for( String name : XRandR.getScreenNames() )
		{
			text.append( "\tScreen: " + name + "\n" );

			for( Screen s : XRandR.getResolutions( name ) )
			{
				text.append( "\t\t" + s.width + "x" + s.height + "\n" );
			}
		}

		text.append( "xrandr raw output\n" );
		text.append( XRandR.getXrandrOutput() );

		frame.pack();
		frame.setVisible( true );
	}
}
