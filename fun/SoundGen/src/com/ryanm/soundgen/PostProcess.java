
package com.ryanm.soundgen;

import java.nio.ShortBuffer;

/**
 * Interface for post-processing effects on sounds
 * 
 * @author ryanm
 */
public interface PostProcess
{
	/**
	 * Process the sound data
	 * 
	 * @param data
	 *           The sound data
	 * @param sampleRate
	 *           The number of samples per second
	 */
	public void process( ShortBuffer data, int sampleRate );
}
