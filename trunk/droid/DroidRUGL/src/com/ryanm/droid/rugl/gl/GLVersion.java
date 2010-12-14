
package com.ryanm.droid.rugl.gl;

import android.opengl.GLES10;

/**
 * For determining the gl version
 * 
 * @author ryanm
 */
public enum GLVersion
{
	/***/
	OnePointZero
	{
		@Override
		protected boolean match( String glVersionString )
		{
			return glVersionString.endsWith( "1.0" );
		}

		@Override
		public String toString()
		{
			return "1.0";
		}
	},
	/***/
	OnePointOne
	{
		@Override
		protected boolean match( String glVersionString )
		{
			return glVersionString.endsWith( "1.1" );
		}

		@Override
		public String toString()
		{
			return "1.1";
		}
	},
	/***/
	TwoPointZero
	{
		@Override
		protected boolean match( String glVersionString )
		{
			return glVersionString.length() > 10
					&& glVersionString.substring( 10 ).startsWith( "2.0" );
		}

		@Override
		public String toString()
		{
			return "2.0";
		}
	};

	/**
	 * @param glVersionString
	 *           As returned from {@link GLES10#glGetString(int)} with
	 *           {@link GLES10#GL_VERSION}
	 * @return <code>true</code> if the version matches that reported
	 *         in the string
	 */
	protected abstract boolean match( String glVersionString );

	/**
	 * @param glVersionString
	 * @return The {@link GLVersion} that matches the string, or
	 *         <code>null</code> if none did
	 */
	public static GLVersion findVersion( String glVersionString )
	{
		for( GLVersion glv : GLVersion.values() )
		{
			if( glv.match( glVersionString ) )
			{
				return glv;
			}
		}
		return null;
	}
}
