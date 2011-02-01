
package com.ryanm.droid.rugl.gl.facets.mutable;

import com.ryanm.droid.rugl.gl.enums.MagFilter;
import com.ryanm.droid.rugl.gl.enums.MinFilter;
import com.ryanm.droid.rugl.gl.enums.TextureWrap;
import com.ryanm.droid.rugl.gl.facets.TextureState;
import com.ryanm.droid.rugl.gl.facets.TextureState.Filters;
import com.ryanm.droid.rugl.gl.facets.TextureState.WrapParameters;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;

/**
 * Controls the bound texture and parameters
 * 
 * @author ryanm
 */
@Variable( "Texture" )
public class MutTextureState extends MutableFacet<TextureState>
{
	/***/
	@Variable
	@Summary( "The openGL texture name. Set to 0 to disable texturing" )
	public int id;

	/***/
	@Summary( "Texture filter modes" )
	@Variable
	public final MutFilters filter;

	/***/
	@Variable
	@Summary( "Texture wrapping modes" )
	public final MutWrapParameters wrap;

	/**
	 * @param ts
	 */
	public MutTextureState( TextureState ts )
	{
		id = ts.id;
		filter = new MutFilters( ts.filter );
		wrap = new MutWrapParameters( ts.wrap );
	}

	@Override
	public TextureState compile()
	{
		return new TextureState( id, filter.compile(), wrap.compile() );
	}

	/**
	 * @author ryanm
	 */
	@Variable( "Filters" )
	public static class MutFilters extends MutableFacet<Filters>
	{
		/**
		 * The texture minification filter, see glTexParameters
		 */
		@Variable
		public MinFilter min;

		/**
		 * The texture magnification filter, see glTexParameters
		 */
		@Variable
		public MagFilter mag;

		/**
		 * @param f
		 */
		public MutFilters( Filters f )
		{
			min = f.min;
			mag = f.mag;
		}

		@Override
		public Filters compile()
		{
			return new Filters( min, mag );
		}
	}

	/**
	 * @author ryanm
	 */
	@Variable( "Wrap" )
	public static class MutWrapParameters extends MutableFacet<WrapParameters>
	{
		/**
		 * Wrapping in the s direction
		 */
		@Variable
		public TextureWrap s;

		/**
		 * Wrapping in the t direction
		 */
		@Variable
		public TextureWrap t;

		/**
		 * @param wp
		 */
		public MutWrapParameters( WrapParameters wp )
		{
			s = wp.s;
			t = wp.t;
		}

		@Override
		public WrapParameters compile()
		{
			return new WrapParameters( s, t );
		}
	}
}
