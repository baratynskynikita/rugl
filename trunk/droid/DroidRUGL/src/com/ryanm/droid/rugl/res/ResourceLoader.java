
package com.ryanm.droid.rugl.res;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.Resources;
import android.util.Log;

/**
 * Provides a resource-loading thread
 * 
 * @author ryanm
 */
public class ResourceLoader
{
	/***/
	public static Resources resources;

	private static LoaderThread thread;

	private static List<Loader> loaders = Collections
			.synchronizedList( new LinkedList<Loader>() );

	private static List<Loader> loaded = Collections
			.synchronizedList( new LinkedList<Loader>() );

	private static Object waitLock = new Object();

	private static ExecutorService postLoader = Executors.newSingleThreadExecutor();

	/***/
	public static final String LOG_TAG = "ResourceLoader";

	/**
	 * Starts the loader thread
	 * 
	 * @param resources
	 */
	public static void start( Resources resources )
	{
		ResourceLoader.resources = resources;

		if( thread == null )
		{
			thread = new LoaderThread();
			thread.start();
		}
	}

	/**
	 * Asynchronously load a resource
	 * 
	 * @param l
	 */
	public static void load( Loader l )
	{
		loaders.add( l );

		synchronized( waitLock )
		{
			waitLock.notify();
		}
	}

	/**
	 * Call this in the main thread, it'll cause completed loaders to
	 * call {@link Loader#complete()}
	 */
	public static void checkCompletion()
	{
		while( !loaded.isEmpty() )
		{
			Loader l = loaded.remove( 0 );

			Log.i( LOG_TAG, "Loaded resource " + l );

			l.complete();
		}
	}

	/**
	 * Override this class to load resources
	 * 
	 * @author ryanm
	 * @param <T>
	 */
	public static abstract class Loader<T>
	{
		/**
		 * The loaded resource
		 */
		protected T resource;

		/**
		 * Overload this to do the loading and set {@link #resource}.
		 * This is called on a common loading thread
		 */
		public abstract void load();

		/**
		 * This method is called on it's own thread. Use it to do any
		 * processing
		 */
		public void loaded()
		{
		};

		/**
		 * This is called on the main thread when loading is complete
		 */
		public abstract void complete();
	}

	private static class LoaderThread extends Thread
	{
		private LoaderThread()
		{
			super( "Resource Loader" );
			setDaemon( true );
		}

		@Override
		public void run()
		{
			while( true )
			{
				while( !loaders.isEmpty() )
				{
					final Loader l = loaders.remove( 0 );

					Log.i( LOG_TAG, "Loading resource " + l );

					l.load();

					postLoader.submit( new Runnable() {
						@Override
						public void run()
						{
							l.loaded();

							loaded.add( l );
						}
					} );
				}

				synchronized( waitLock )
				{
					try
					{
						waitLock.wait();
					}
					catch( InterruptedException e )
					{
					}
				}
			}
		}
	}
}
