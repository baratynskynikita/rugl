
package com.ryanm.droid.rugl.gl.facets;

import android.opengl.GLES10;

import com.ryanm.droid.rugl.gl.Facet;
import com.ryanm.droid.rugl.gl.enums.MagFilter;
import com.ryanm.droid.rugl.gl.enums.MinFilter;
import com.ryanm.droid.rugl.gl.enums.TextureWrap;

/**
 * Controls the bound texture and parameters
 * 
 * @author ryanm
 */
public class TextureState extends Facet<TextureState>
{
	/**
	 * The openGL texture name. Set to -1 to disable texturing
	 */
	public final int id;

	/**
	 * Texture filter modes
	 */
	public final Filters filter;

	/**
	 * Texture wrapping parameters
	 */
	public final WrapParameters wrap;

	/**
	 * Use this to disable texturing
	 */
	public static final TextureState disabled = new TextureState();

	private TextureState()
	{
		id = -1;
		filter = new Filters();
		wrap = new WrapParameters();
	}

	/**
	 * @param id
	 * @param filter
	 * @param wrap
	 */
	public TextureState( int id, Filters filter, WrapParameters wrap )
	{
		this.id = id;
		this.filter = filter;
		this.wrap = wrap;
	}

	/**
	 * @param id
	 * @return an altered clone
	 */
	public TextureState with( int id )
	{
		return new TextureState( id, filter, wrap );
	}

	/**
	 * @param filter
	 * @return an altered clone
	 */
	public TextureState with( Filters filter )
	{
		return new TextureState( id, filter, wrap );
	}

	/**
	 * @param wrap
	 * @return an altered clone
	 */
	public TextureState with( WrapParameters wrap )
	{
		return new TextureState( id, filter, wrap );
	}

	@Override
	public void transitionFrom( TextureState t )
	{
		if( id != t.id )
		{
			if( t.id == -1 )
			{
				GLES10.glEnable( GLES10.GL_TEXTURE_2D );
			}

			if( id == -1 )
			{
				GLES10.glDisable( GLES10.GL_TEXTURE_2D );
			}
			else
			{
				GLES10.glBindTexture( GLES10.GL_TEXTURE_2D, id );
			}
		}

		if( id != -1 )
		{
			if( id != t.id )
			{
				// we've just bound a different texture, who knows what
				// params it last had set?
				filter.force();
				wrap.force();
			}
			else
			{ // we can do the minimal change
				filter.transitionFrom( t.filter );
				wrap.transitionFrom( t.wrap );
			}
		}
	}

	@Override
	public int compareTo( TextureState t )
	{
		if( id == -1 && t.id == -1 )
		{
			return 0;
		}

		int d = id - t.id;

		if( d == 0 )
		{
			d = filter.compareTo( filter );

			if( d == 0 )
			{
				d = wrap.compareTo( wrap );
			}
		}

		return d;
	}

	@Override
	public String toString()
	{
		StringBuilder buff = new StringBuilder();

		buff.append( "Texture id = " + id );
		buff.append( " " ).append( filter );
		buff.append( " " ).append( wrap );

		return buff.toString();
	}

	/**
	 * @author ryanm
	 */
	public static class Filters extends Facet<Filters>
	{
		/**
		 * The texture minification filter, see glTexParameters
		 */
		public final MinFilter min;

		/**
		 * The texture magnification filter, see glTexParameters
		 */
		public final MagFilter mag;

		/***/
		public Filters()
		{
			min = MinFilter.NEAREST_MIPMAP_LINEAR;
			mag = MagFilter.LINEAR;
		}

		/**
		 * @param min
		 * @param mag
		 */
		public Filters( MinFilter min, MagFilter mag )
		{
			this.min = min;
			this.mag = mag;
		}

		/**
		 * @param min
		 * @return altered clone
		 */
		public Filters with( MinFilter min )
		{
			return new Filters( min, mag );
		}

		/**
		 * @param mag
		 * @return altered clone
		 */
		public Filters with( MagFilter mag )
		{
			return new Filters( min, mag );
		}

		@Override
		public int compareTo( Filters t )
		{
			int d = min.ordinal() - t.min.ordinal();

			if( d == 0 )
			{
				d = mag.ordinal() - t.mag.ordinal();
			}

			return d;
		}

		@Override
		public void transitionFrom( Filters t )
		{
			if( min != t.min )
			{
				GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER,
						min.value );
			}

			if( mag != t.mag )
			{
				GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER,
						mag.value );
			}
		}

		private void force()
		{
			GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER,
					min.value );
			GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER,
					mag.value );
		}

		@Override
		public String toString()
		{
			return "filters min = " + min + " mag = " + mag;
		}
	}

	/**
	 * @author ryanm
	 */
	public static class WrapParameters extends Facet<WrapParameters>
	{
		/**
		 * Wrapping in the s direction
		 */
		public final TextureWrap s;

		/**
		 * Wrapping in the t direction
		 */
		public final TextureWrap t;

		/***/
		public WrapParameters()
		{
			s = TextureWrap.REPEAT;
			t = TextureWrap.REPEAT;
		}

		/**
		 * @param s
		 * @param t
		 */
		public WrapParameters( TextureWrap s, TextureWrap t )
		{
			this.s = s;
			this.t = t;
		}

		@Override
		public void transitionFrom( WrapParameters w )
		{
			if( s != w.s )
			{
				GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S,
						s.value );
			}

			if( t != w.t )
			{
				GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T,
						t.value );
			}
		}

		private void force()
		{
			GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, s.value );
			GLES10.glTexParameterx( GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, t.value );
		}

		@Override
		public int compareTo( WrapParameters w )
		{
			int d = s.ordinal() - w.s.ordinal();

			if( d == 0 )
			{
				d = t.ordinal() - w.t.ordinal();
			}

			return d;
		}

		@Override
		public String toString()
		{
			return "Wrap s = " + s + " t = " + t;
		}
	}
}
