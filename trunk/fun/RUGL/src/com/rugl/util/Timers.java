
package com.rugl.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Convenient way to trigger actions, control variables over time etc
 * 
 * @author ryanm
 */
public class Timers
{
	private float time = 0;

	private PriorityQueue<Event> events = new PriorityQueue<Event>( 10,
			new Comparator<Event>() {
				@Override
				public int compare( Event o1, Event o2 )
				{
					return ( int ) Math.signum( o1.targetTime - o2.targetTime );
				}
			} );

	private List<Action> actions = new LinkedList<Action>();

	private List<Event> toPost = new LinkedList<Event>();

	private List<Action> toAdd = new LinkedList<Action>();

	/**
	 * Gets the current time
	 * 
	 * @return the current time
	 */
	public float getTime()
	{
		return time;
	}

	/**
	 * Execute appropriate events and actions
	 * 
	 * @param delta
	 */
	public void advance( float delta )
	{
		time += delta;

		if( !toPost.isEmpty() )
		{
			events.addAll( toPost );
			toPost.clear();
		}

		// check events
		while( !events.isEmpty() && events.peek().targetTime <= time )
		{
			events.poll().execute();
		}

		if( !toAdd.isEmpty() )
		{
			actions.addAll( toAdd );
			toAdd.clear();
		}

		if( !actions.isEmpty() )
		{
			// update actions
			Iterator<Action> iter = actions.iterator();
			while( iter.hasNext() )
			{
				if( iter.next().execute( delta ) )
				{
					iter.remove();
				}
			}
		}
	}

	/**
	 * Posts an {@link Event}
	 * 
	 * @param e
	 */
	public void post( Event e )
	{
		e.targetTime = time + e.delay;
		toPost.add( e );
	}

	/**
	 * Adds an {@link Action} that determines for itself when it is
	 * finished
	 * 
	 * @param a
	 */
	public void add( Action a )
	{
		toAdd.add( a );
	}

	/**
	 * Extend to perform some action at a specified time in the future
	 * 
	 * @author ryanm
	 */
	public static abstract class Event
	{
		private final float delay;

		/**
		 * The execution time
		 */
		protected float targetTime;

		/**
		 * Constructs an event that will execute some time in the future
		 * 
		 * @param delay
		 *           How far in the future? This far
		 */
		public Event( float delay )
		{
			this.delay = delay;
		}

		/**
		 * Do something
		 */
		public abstract void execute();
	}

	/**
	 * Implement to perform some action on every frame
	 * 
	 * @author ryanm
	 */
	public static interface Action
	{
		/**
		 * Do something
		 * 
		 * @param delta
		 *           The time delta since the last frame
		 * @return <code>true</code> if the {@link Action} is complete
		 *         and should be removed, <code>false</code> if there's
		 *         still work to do
		 */
		public boolean execute( float delta );
	}
}
