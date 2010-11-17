
package com.ryanm.trace.game.ai;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;
import com.ryanm.trace.game.ai.behaviours.Behaviour;

/**
 * @author ryanm
 */
public abstract class GeneBot implements Bot
{
	private final String name;

	private final String desc;

	private final Behaviour[] behaviours;

	/**
	 * @param name
	 * @param desc
	 * @param behaviours
	 */
	protected GeneBot( String name, String desc, Behaviour... behaviours )
	{
		this.name = name;
		this.desc = desc;
		this.behaviours = behaviours;
	}

	@Override
	public final String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return desc;
	}

	@Override
	public void reset()
	{
		for( int i = 0; i < behaviours.length; i++ )
		{
			behaviours[ i ].reset();
		}
	}

	@Override
	public Action process( Trace t, Arena game )
	{
		Behaviour dominant = behaviours[ 0 ];
		for( int i = 0; i < behaviours.length; i++ )
		{
			behaviours[ i ].process( t, game );

			if( behaviours[ i ].getWeight() > dominant.getWeight() )
			{
				dominant = behaviours[ i ];
			}
		}

		assert dominant.getWeight() <= 1;

		return dominant.getAction();
	}
}
