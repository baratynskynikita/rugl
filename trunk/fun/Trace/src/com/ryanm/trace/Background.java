
package com.ryanm.trace;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.ryanm.soundgen.imp.Constant;
import com.ryanm.soundgen.imp.SineWave;
import com.ryanm.util.math.Range;

/**
 * @author ryanm
 */
public class Background
{
	private static int inner = Colour.packInt( 0, 255, 0, 64 );

	private static int outer = Colour.packInt( 0, 255, 0, 0 );

	private static float minWidth = 1, maxWidth = 5;

	private static float minIntensity = 0.05f, maxIntensity = 0.2f;

	private static float time = 0;

	private static float width = 0;

	private static float intensity = 0;

	/**
	 * Frequency of undulation
	 */
	public static float frequency = 0.2f;

	/**
	 * Amplitude of undulation
	 */
	public static float amplitude = 1;

	private static SineWave sine = new SineWave();

	private static Constant freq = new Constant( frequency );

	private static ColouredShape vertical, horizontal;
	static
	{
		sine.frequency = freq;
		int[] ca = new int[] { outer, outer, inner, inner, inner, inner, outer, outer };

		Shape l = ShapeUtil.line( 800, 400, -2, 400, -0.5f, 400, 0.5f, 400, 2 );
		horizontal = new ColouredShape( l, ca, null );

		l = ShapeUtil.line( 600, -2, 300, -0.5f, 300, 0.5f, 300, 2, 300 );
		vertical = new ColouredShape( l, ca, null );
	}

	/**
	 * @param delta
	 */
	public static void advance( float delta )
	{
		time += delta;
		freq.value = frequency;
		float v = 0.5f * sine.getValue( time ) + 0.5f;

		width = minWidth + v * amplitude * ( maxWidth - minWidth );
		intensity = minIntensity + ( 1 - v ) * amplitude * ( maxIntensity - minIntensity );
	}

	/**
	 * @param sr
	 */
	public static void render( StackedRenderer sr )
	{
		int alpha = ( int ) Range.limit( 255.0f * intensity, 0, 255 );

		for( int i = 2; i < 6; i++ )
		{
			horizontal.colours[ i ] = Colour.withAlphai( horizontal.colours[ i ], alpha );
			vertical.colours[ i ] = Colour.withAlphai( vertical.colours[ i ], alpha );
		}

		float sep = 100;
		int count = ( int ) Math.ceil( 800 / sep );
		for( float i = 0; i <= count; i++ )
		{
			float x = 400.0f + ( i - count / 2.0f ) * sep;
			ColouredShape cs = vertical.clone();
			cs.scale( width, 1, 1 );
			cs.translate( x, 0, -1 );
			cs.render( sr );
		}

		count = ( int ) Math.ceil( 600 / sep );
		for( int i = 0; i <= count; i++ )
		{
			float y = 300 + ( i - count / 2 ) * sep;
			ColouredShape cs = horizontal.clone();
			cs.scale( 1, width, 1 );
			cs.translate( 0, y, -1 );
			cs.render( sr );
		}
	}
}
