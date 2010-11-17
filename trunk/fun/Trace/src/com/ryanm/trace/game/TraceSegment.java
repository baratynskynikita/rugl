
package com.ryanm.trace.game;

import com.ryanm.trace.Player;
import com.ryanm.util.Segment;

/**
 * @author ryanm
 */
public class TraceSegment extends Segment
{

	/***/
	public final Player owner;

	/***/
	public final byte type;

	/**
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param owner
	 * @param type
	 */
	public TraceSegment( float ax, float ay, float bx, float by, Player owner, byte type )
	{
		super( ax, ay, bx, by );
		this.owner = owner;
		this.type = type;
	}

}