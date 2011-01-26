
package com.ryanm.droid.rugl;

import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.ryanm.droid.config.Configuration;
import com.ryanm.droid.config.Persist;
import com.ryanm.droid.config.annote.Summary;
import com.ryanm.droid.config.annote.Variable;
import com.ryanm.droid.rugl.gl.GLUtil;
import com.ryanm.droid.rugl.gl.GLVersion;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.util.CodeTimer;
import com.ryanm.droid.rugl.util.CodeTimer.Output;
import com.ryanm.droid.rugl.util.ExceptionHandler;

/**
 * A convenient {@link Phase}-based game model
 * 
 * @author ryanm
 */
@Variable( "DroidRUGL" )
@Summary( "General settings" )
public class Game implements Renderer
{
	/**
	 * Info logging tag
	 */
	public static final String RUGL_TAG = "RUGL";

	/**
	 * Screen width
	 */
	public static int width;

	/**
	 * Screen height
	 */
	public static int height;

	/**
	 * OpenGLES version
	 */
	public static GLVersion glVersion;

	/**
	 * Roots of the tree that will be configured with
	 * {@link #launchConfiguration()}
	 */
	private static Object[] confRoots = null;

	private static ArrayList<SurfaceListener> surfaceListeners =
			new ArrayList<Game.SurfaceListener>();

	/**
	 * @param sl
	 */
	public static void addSurfaceLIstener( SurfaceListener sl )
	{
		surfaceListeners.add( sl );
	}

	/**
	 * @param sl
	 */
	public static void removeSurfaceListener( SurfaceListener sl )
	{
		surfaceListeners.remove( sl );
	}

	/**
	 * Convenience to set {@link #confRoots}
	 * 
	 * @param roots
	 */
	public static void setConfigurationRoots( Object... roots )
	{
		confRoots = roots;
	}

	/**
	 * Launches a configuration activity for the objects in
	 * {@link #confRoots}
	 * 
	 * @see #setConfigurationRoots(Object...)
	 */
	public void launchConfiguration()
	{
		Configuration.configure( ga, confRoots );
	}

	/**
	 * @return The names of saved configurations
	 */
	public String[] listSavedConfigurations()
	{
		return Persist.listSaves( ga );
	}

	/**
	 * Loads a named configuration
	 * 
	 * @param name
	 */
	public void loadConfiguration( String name )
	{
		Persist.load( ga, name, confRoots );
	}

	private final GameActivity ga;

	/**
	 * The desired logic advance, in seconds, or -1 to disable fixed
	 * interval advances. Be careful when setting a fixed logic advance
	 * - things will not go well if executing the logic code takes
	 * longer than the logic advance value.
	 */
	@Variable( "Logic advance" )
	@Summary( "Fixed logic tick delta in seconds, or <0 for variable advance" )
	public static float logicAdvance = -1;

	/**
	 * Profiles game logic, drawing and GL Rendering
	 */
	@Variable( "Loop timer" )
	public final CodeTimer timer = new CodeTimer( "RUGL loop", Output.Millis,
			Output.Millis );

	private Phase currentPhase;

	private boolean phaseInited = false;

	private long lastLogic = System.currentTimeMillis();

	private final GLVersion requiredVersion;

	/**
	 * @param ga
	 *           used solely to quit when we run out of phases
	 * @param requiredVersion
	 *           The {@link GLVersion} that will be required in the
	 *           game, or <code>null</code> not to bother checking
	 * @param phase
	 *           The initial phase
	 */
	public Game( GameActivity ga, GLVersion requiredVersion, Phase phase )
	{
		this.ga = ga;
		this.requiredVersion = requiredVersion;
		currentPhase = phase;
	}

	@Override
	public void onSurfaceCreated( GL10 gl, EGLConfig config )
	{
		Log.i( RUGL_TAG, "Surface created at " + new Date() );

		String glVersionString = GLES10.glGetString( GLES10.GL_VERSION );
		String extensionsString = GLES10.glGetString( GLES10.GL_EXTENSIONS );

		StringBuilder buff = new StringBuilder();
		buff.append( "\tVendor = " ).append( GLES10.glGetString( GLES10.GL_VENDOR ) );
		buff.append( "\n\tRenderer = " ).append( GLES10.glGetString( GLES10.GL_RENDERER ) );
		buff.append( "\n\tVersion = " ).append( glVersionString );
		buff.append( "\n\tExtensions" );
		if( extensionsString != null )
		{
			for( String ex : extensionsString.split( " " ) )
			{
				buff.append( "\n\t\t" + ex );
			}
		}
		else
		{
			buff.append( "null" );
		}

		ExceptionHandler.addLogInfo( "GLInfo", buff.toString() );

		Log.i( RUGL_TAG, buff.toString() );

		glVersion = GLVersion.findVersion( glVersionString );

		Log.i( RUGL_TAG, "Detected " + glVersion );

		if( requiredVersion != null && requiredVersion.ordinal() > glVersion.ordinal() )
		{
			// requirements fail!
			ga.showToast( "Required OpenGLES version " + requiredVersion
					+ " but found version " + glVersion, true );
			ga.finish();
		}

		GLUtil.enableVertexArrays();

		currentPhase.openGLinit();

		lastLogic = System.currentTimeMillis();

		GLUtil.checkGLError();

		for( int i = 0; i < surfaceListeners.size(); i++ )
		{
			surfaceListeners.get( i ).onSurfaceCreated();
		}
	}

	/**
	 * Default implementation is to set up a 1:1 orthographic
	 * projection
	 */
	@Override
	public void onSurfaceChanged( GL10 gl, int width, int height )
	{
		Game.width = width;
		Game.height = height;

		GLUtil.scaledOrtho( width, height, width, height, -1, 1 );

		Log.i( RUGL_TAG, "Surface changed " + width + " x " + height );

		GLUtil.checkGLError();

		for( int i = 0; i < surfaceListeners.size(); i++ )
		{
			surfaceListeners.get( i ).onSurfaceChanged( width, height );
		}
	}

	@Override
	public void onDrawFrame( GL10 gl )
	{
		if( currentPhase == null )
		{ // time to quit
			Log.i( RUGL_TAG, "Exiting" );
			ga.finish();
			return;
		}

		if( !phaseInited )
		{
			Log.i( RUGL_TAG, "Phase " + currentPhase + " initing" );
			currentPhase.openGLinit();
			currentPhase.init( this );
			phaseInited = true;
		}

		if( confRoots != null )
		{
			Configuration.applyDeferredConfigurations( confRoots );
		}

		ResourceLoader.checkCompletion();

		timer.tick( "logic" );

		long now = System.currentTimeMillis();
		long dur = now - lastLogic;

		if( logicAdvance > 0 )
		{
			while( lastLogic < now )
			{
				currentPhase.advance( logicAdvance );
				lastLogic += ( long ) ( 1000 * logicAdvance );
			}
		}
		else
		{
			currentPhase.advance( dur / 1000.0f );
		}

		lastLogic = now;

		timer.tick( "draw " );

		currentPhase.draw();

		timer.lastTick();

		// phase transition
		if( currentPhase.complete )
		{
			Log.i( RUGL_TAG, "Phase " + currentPhase + " complete" );
			currentPhase = currentPhase.next();
			phaseInited = false;
		}

		GLUtil.checkGLError();
	}

	/**
	 * @return the current phase
	 */
	public Phase currentPhase()
	{
		return currentPhase;
	}

	/**
	 * @author ryanm
	 */
	public abstract static class SurfaceListener
	{
		/**
		 * Called when the surface is created
		 */
		public void onSurfaceCreated()
		{
		}

		/**
		 * Called when the surface is changed
		 * 
		 * @param width
		 * @param height
		 */
		public void onSurfaceChanged( int width, int height )
		{
		}
	}
}