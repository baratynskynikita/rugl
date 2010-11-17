
package com.ryanm.trace.lobby;

import com.rugl.renderer.StackedRenderer;
import com.ryanm.trace.Buttons;
import com.ryanm.trace.Phase;

/**
 * Options for setting up a network game
 * 
 * @author ryanm
 */
public class NetLobby implements Phase
{
	@Override
	public boolean isFinished()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Phase next()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset()
	{
		Buttons.setText( "back", "Start", "lan", "friends", "global", null );
	}

	@Override
	public void advance( float delta )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void draw( StackedRenderer r )
	{
		// TODO Auto-generated method stub

	}

}
