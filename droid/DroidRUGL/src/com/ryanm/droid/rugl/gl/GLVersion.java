
package com.ryanm.droid.rugl.gl;


/**
 * For determining the gl version
 * 
 * @author ryanm
 */
public enum GLVersion
{
	/***/
	OnePointZero( "1.0" ),
	/***/
	OnePointOne( "1.1" ),
	/***/
	TwoPointZero( "2.0" );

	private final String versionString;

	private GLVersion( String versionString )
	{
		this.versionString = versionString;
	}

	/**
	 * @param glVersionString
	 * @return The {@link GLVersion} that matches the string, or
	 *         <code>null</code> if none did
	 */
	public static GLVersion findVersion( String glVersionString )
	{
		for( GLVersion glv : GLVersion.values() )
		{
			if( glVersionString.contains( glv.versionString ) )
			{
				return glv;
			}
		}
		return null;
	}
}
