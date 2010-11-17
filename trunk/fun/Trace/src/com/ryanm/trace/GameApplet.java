
package com.ryanm.trace;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.rugl.Game;

/**
 * UNFINISHED
 * 
 * @author ryanm
 */
public abstract class GameApplet extends Applet
{
	private Canvas canvas;

	/** Thread which runs the main game loop */
	private Thread gameThread;

	/** is the game loop running */
	boolean running = false;

	/**
	 * @return the {@link Game} to run
	 */
	public abstract Game buildGame();

	private void startLWJGL()
	{
		gameThread = new Thread() {
			@Override
			public void run()
			{
				running = true;
				try
				{
					Display.setParent( canvas );
					Display.create();
					initGL();
				}
				catch( LWJGLException e )
				{
					e.printStackTrace();
				}
				gameLoop();
			}
		};
		gameThread.start();
	}

	private void initGL()
	{
	}

	private void gameLoop()
	{
		while( running )
		{
			Display.sync( 60 );
			Display.update();
		}

		Display.destroy();
	}

	/**
	 * Tell game loop to stop running, after which the LWJGL Display
	 * will be destoryed. The main thread will wait for the
	 * Display.destroy().
	 */
	private void stopLWJGL()
	{
		running = false;
		try
		{
			gameThread.join();
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void start()
	{

	}

	@Override
	public void stop()
	{

	}

	/**
	 * Applet Destroy method will remove the canvas, before canvas is
	 * destroyed it will notify stopLWJGL() to stop the main game loop
	 * and to destroy the Display
	 */
	@Override
	public void destroy()
	{
		remove( canvas );
		super.destroy();
	}

	@Override
	public void init()
	{
		setLayout( new BorderLayout() );
		try
		{
			canvas = new Canvas() {
				@Override
				public final void addNotify()
				{
					super.addNotify();
					startLWJGL();
				}

				@Override
				public final void removeNotify()
				{
					stopLWJGL();
					super.removeNotify();
				}
			};
			canvas.setSize( getWidth(), getHeight() );
			add( canvas );
			canvas.setFocusable( true );
			canvas.requestFocus();
			canvas.setIgnoreRepaint( true );
			setVisible( true );
		}
		catch( Exception e )
		{
			System.err.println( e );
			throw new RuntimeException( "Unable to create display" );
		}
	}
}
