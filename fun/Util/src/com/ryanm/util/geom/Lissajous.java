
package com.ryanm.util.geom;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;

/**
 * Plots Lissajous curves
 * 
 * @author ryanm
 */
public class Lissajous
{
	/***/
	public final float a;

	/***/
	public final float delta;

	/***/
	public final float b;

	/**
	 * The last result of {@link #evaluate(float)}
	 */
	public final Vector2f lastResult = new Vector2f();

	/**
	 * @param a
	 * @param delta
	 * @param b
	 */
	public Lissajous( float a, float delta, float b )
	{
		this.a = a;
		this.delta = delta;
		this.b = b;
	}

	/**
	 * @param t
	 * @return {@link #lastResult}, with the appropriate values
	 */
	public Vector2f evaluate( float t )
	{
		lastResult.x = ( float ) Math.sin( a * t + delta );
		lastResult.y = ( float ) Math.sin( b * t );

		return lastResult;
	}

	/**
	 * @param a
	 * @param b
	 * @param delta
	 * @param points
	 * @return verts of a lissajous curve
	 */
	public static float[] plot( float a, float b, float delta, int points )
	{
		float[] verts = new float[ 2 * points ];

		int vi = 0;
		for( int i = 0; i < points; i++ )
		{
			float t = ( float ) ( 2 * Math.PI * i / points );
			verts[ vi++ ] = ( float ) Math.sin( a * t + delta );
			verts[ vi++ ] = ( float ) Math.sin( b * t );
		}

		return verts;
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		float[] verts = plot( 1, 2, ( float ) Math.PI / 2, 40 );
		float minx = Float.MAX_VALUE, maxx = -Float.MAX_VALUE;
		float miny = Float.MAX_VALUE, maxy = -Float.MAX_VALUE;
		for( int i = 0; i < verts.length; i += 2 )
		{
			verts[ i ] *= 90;
			verts[ i + 1 ] *= 90;

			minx = Math.min( minx, verts[ i ] );
			maxx = Math.max( maxx, verts[ i ] );
			miny = Math.min( miny, verts[ i + 1 ] );
			maxy = Math.max( maxy, verts[ i + 1 ] );
		}

		for( int i = 0; i < verts.length; i += 2 )
		{
			verts[ i ] -= minx - 10;
			verts[ i + 1 ] -= miny - 10;
		}

		BufferedImage bi = new BufferedImage( 200, 200, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = bi.createGraphics();
		g.setColor( Color.lightGray );
		g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
		g.setColor( Color.black );

		for( int i = 2; i < verts.length; i += 2 )
		{
			g.drawLine( ( int ) verts[ i - 2 ], ( int ) verts[ i - 1 ], ( int ) verts[ i ],
					( int ) verts[ i + 1 ] );
		}

		for( int i = 0; i < verts.length; i += 2 )
		{
			// g.fillRect( ( int ) verts[ i ] - 1, ( int ) verts[ i + 1 ]
			// - 1, 3, 3 );
		}

		try
		{
			ImageIO.write( bi, "PNG", new File( "curve.png" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
