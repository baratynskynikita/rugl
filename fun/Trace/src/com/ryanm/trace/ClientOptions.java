
package com.ryanm.trace;

import com.rugl.sound.SoundSystem;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Description;
import com.ryanm.config.imp.Variable;

/**
 * @author ryanm
 */
@ConfigurableType( "client" )
@Description( "graphic and audio options" )
public class ClientOptions
{
	/***/
	@Variable( "trace width" )
	public float traceWidth = 2.5f;

	/***/
	@Variable( "trace core" )
	@Description( "Fraction of trace width" )
	public float traceCore = 0.3f;

	/***/
	@Variable( "wireframe" )
	public boolean wireFrame = false;

	/***/
	@Variable( "gap alpha" )
	public int gapAlpha = 96;

	private float volume = 1.0f;

	/***/
	@Variable( "logic rate" )
	@Description( "Frequency at which the game state is updated, in Hz" )
	public float logicRate = 60;

	/***/
	@Variable( "bucket size" )
	@Description( "The resolution of the collision-detection grid. Larger = coarser" )
	public float bucketSize = 10;

	/**
	 * @param f
	 */
	@Variable( "volume" )
	public void setVolume( float f )
	{
		volume = f;
		SoundSystem.setListenerGain( volume );
	}

	/**
	 * @return master volume
	 */
	@Variable( "volume" )
	public float getVolume()
	{
		return volume;
	}

}
