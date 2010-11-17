
package com.rugl.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.Shape;
import com.rugl.geom.ShapeBuilder;
import com.ryanm.util.geom.GeomUtils;
import com.ryanm.util.io.DataStream;
import com.ryanm.util.march.Direction;
import com.ryanm.util.march.MarchingSquares;
import com.ryanm.util.march.Path;

/**
 * Extracts simple {@link Shape}s from images
 * 
 * @author ryanm
 */
public class ShapeFactory
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		float minF = Float.parseFloat( args[ 0 ] );
		for( int i = 1; i < args.length; i++ )
		{
			try
			{
				BufferedImage bi = ImageIO.read( new File( args[ i ] ) );
				System.out.println( "Extracting from " + args[ i ] );

				Shape s = extract( bi, minF );

				System.out.println( "Final shape " + s.vertexCount() + " vertices" );

				String name = args[ i ].substring( 0, args[ i ].lastIndexOf( "." ) );
				System.out.println( "Writing to " + name + ".ruglshp" );

				DataStream out = new DataStream( new FileOutputStream( name + ".ruglshp" ) );
				s.write( out );

				draw( s, name + "-shape" );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Extracts the shapes from an image
	 * 
	 * @param bi
	 * @param featureLimit
	 * @return A compound shape from the image
	 */
	private static Shape extract( BufferedImage bi, float featureLimit )
	{
		MarchingSquares ms = new MarchingSquares( bi, Color.black );
		Path[] paths = ms.identifyPerimeters();
		System.out.println( "\tfound " + paths.length + " paths" );

		ShapeBuilder sb = new ShapeBuilder();

		for( Path path : paths )
		{
			Shape s = buildShape( path, featureLimit );
			draw( s, "shape" + n );

			System.out.println( "built" );

			sb.addShape( s );

			n++;
		}

		return sb.compile();
	}

	private static int n = 0;

	private static Shape buildShape( Path p, float featureLimit )
	{
		float[] verts = new float[ 2 + p.getDirections().size() * 2 ];
		verts[ 0 ] = p.getOriginX();
		verts[ 1 ] = p.getOriginY();
		int vi = 2;

		for( Direction d : p.getDirections() )
		{
			verts[ vi++ ] = verts[ vi - 3 ] + d.planeX;
			verts[ vi++ ] = verts[ vi - 3 ] + d.planeY;
		}

		System.out.println( "\t\tin  = " + verts.length / 2 + " verts" );

		for( int i = 0; i < 3; i++ )
		{
			verts = GeomUtils.decimate( verts, featureLimit );
		}

		System.out.println( "\t\tout = " + verts.length / 2 + " verts" );

		draw( verts, "verts" + n );

		return Tesselator.tesselate( verts, 0 );
	}

	/**
	 * Draws a shape to a file
	 * 
	 * @param s
	 * @param name
	 */
	public static void draw( Shape s, String name )
	{
		s = s.clone();
		s.translate( -s.getBounds().x.getMin() + 10, -s.getBounds().y.getMin() + 10, 0 );

		BufferedImage bi =
				new BufferedImage( ( int ) ( s.getBounds().getWidth() + 20 ), ( int ) ( s
						.getBounds().getHeight() + 20 ), BufferedImage.TYPE_INT_ARGB );

		Graphics2D g = bi.createGraphics();
		g.setColor( Color.white );
		g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
		g.setColor( new Color( 0, 0, 0, 64 ) );

		for( int i = 0; i < s.triangles.length; i += 3 )
		{
			int a = s.triangles[ i ];
			int b = s.triangles[ i + 1 ];
			int c = s.triangles[ i + 2 ];

			Vector2f av = new Vector2f( s.vertices[ 3 * a ], s.vertices[ 3 * a + 1 ] );
			Vector2f bv = new Vector2f( s.vertices[ 3 * b ], s.vertices[ 3 * b + 1 ] );
			Vector2f cv = new Vector2f( s.vertices[ 3 * c ], s.vertices[ 3 * c + 1 ] );

			g.drawLine( ( int ) av.x, ( int ) av.y, ( int ) bv.x, ( int ) bv.y );
			g.drawLine( ( int ) av.x, ( int ) av.y, ( int ) cv.x, ( int ) cv.y );
			g.drawLine( ( int ) cv.x, ( int ) cv.y, ( int ) bv.x, ( int ) bv.y );
		}

		g.setColor( Color.red );
		for( int i = 0; i < s.vertices.length; i += 3 )
		{
			int x = ( int ) s.vertices[ i ];
			int y = ( int ) s.vertices[ i + 1 ];

			g.drawRect( x - 1, y - 1, 2, 2 );
		}

		try
		{
			ImageIO.write( bi, "PNG", new File( name + ".png" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	/**
	 * Draws a vertex array
	 * 
	 * @param verts
	 * @param name
	 */
	public static void draw( float[] verts, String name )
	{
		verts = verts.clone();

		Vector2f min = new Vector2f( Float.MAX_VALUE, Float.MAX_VALUE );
		Vector2f max = new Vector2f( -Float.MAX_VALUE, -Float.MAX_VALUE );

		for( int i = 0; i < verts.length; i += 2 )
		{
			min.x = Math.min( min.x, verts[ i ] );
			max.x = Math.max( max.x, verts[ i ] );
			min.y = Math.min( min.y, verts[ i + 1 ] );
			max.y = Math.max( max.y, verts[ i + 1 ] );
		}
		float w = max.x - min.x;
		float h = max.y - min.y;

		for( int i = 0; i < verts.length; i += 2 )
		{
			verts[ i ] -= min.x;
			verts[ i + 1 ] -= min.y;
			verts[ i ] += 10;
			verts[ i + 1 ] += 10;
		}

		BufferedImage bi =
				new BufferedImage( ( int ) w + 20, ( int ) h + 20,
						BufferedImage.TYPE_INT_ARGB );

		Graphics2D g = bi.createGraphics();
		g.setColor( Color.white );
		g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
		g.setColor( new Color( 0, 0, 0, 64 ) );

		for( int i = 0; i < verts.length - 2; i += 2 )
		{
			g.drawLine( ( int ) verts[ i ], ( int ) verts[ i + 1 ], ( int ) verts[ i + 2 ],
					( int ) verts[ i + 3 ] );
		}
		g.setColor( Color.red );
		for( int i = 0; i < verts.length; i += 2 )
		{
			g.drawRect( ( int ) verts[ i ] - 1, ( int ) verts[ i + 1 ] - 1, 2, 2 );
		}

		try
		{
			ImageIO.write( bi, "PNG", new File( name + ".png" ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
