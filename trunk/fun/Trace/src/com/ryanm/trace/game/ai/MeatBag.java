
package com.ryanm.trace.game.ai;

import org.lwjgl.input.Keyboard;

import com.ryanm.trace.game.Arena;
import com.ryanm.trace.game.Trace;

/**
 * For human input
 * 
 * @author ryanm
 */
public class MeatBag implements Bot
{
	/**
	 * Name for human input bot
	 */
	public static final String NAME = "meatbag";

	/***/
	public MeatBag()
	{
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String getDescription()
	{
		return "slow, error-prone, moist.";
	}

	@Override
	public void reset()
	{
	}

	@Override
	public Action process( Trace t, Arena game )
	{
		if( Keyboard.isKeyDown( t.player.leftKey ) )
		{
			return Action.LEFT;
		}
		else if( Keyboard.isKeyDown( t.player.rightKey ) )
		{
			return Action.RIGHT;
		}

		return Action.STRAIGHT;
	}

}
