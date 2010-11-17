
package com.ryanm.util.image;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * @author ryanm
 */
public class ImageClip
{
	/**
	 * Clips the input image to the specified shape
	 * 
	 * @param image
	 *           the input image
	 * @param clipVerts
	 *           list of x, y pairs defining the clip shape, normalised
	 *           to image dimensions (think texture coordinates)
	 * @return The smallest image containing those pixels that fall
	 *         inside the clip shape
	 */
	public static BufferedImage clip( BufferedImage image, float... clipVerts )
	{
		assert clipVerts.length >= 6;
		assert clipVerts.length % 2 == 0;

		int[] xp = new int[ clipVerts.length / 2 ];
		int[] yp = new int[ xp.length ];

		int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;

		for( int j = 0; j < xp.length; j++ )
		{
			xp[ j ] = Math.round( clipVerts[ 2 * j ] * image.getWidth() );
			yp[ j ] = Math.round( clipVerts[ 2 * j + 1 ] * image.getHeight() );

			minX = Math.min( minX, xp[ j ] );
			minY = Math.min( minY, yp[ j ] );
			maxX = Math.max( maxX, xp[ j ] );
			maxY = Math.max( maxY, yp[ j ] );
		}

		for( int i = 0; i < xp.length; i++ )
		{
			xp[ i ] -= minX;
			yp[ i ] -= minY;
		}

		Polygon clip = new Polygon( xp, yp, xp.length );
		BufferedImage out = new BufferedImage( maxX - minX, maxY - minY, image.getType() );
		Graphics g = out.getGraphics();
		g.setClip( clip );

		g.drawImage( image, -minX, -minY, null );
		g.dispose();

		return out;
	}
}
