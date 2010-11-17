
package com.rugl.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A filter to create a distance field from a source image. Snaffled
 * into RUGL wholesale from Slick and given a mild tinkering. Pixels
 * are considered "in" the shape if they are white, everything else is
 * "out"
 * 
 * @author Orangy
 */
public class DistanceFieldFilter implements Runnable
{
	private boolean[][] image;

	private final int scaleFactor;

	private final int scanSize;

	private final Listener listener;

	private BufferedImage result;

	/**
	 * @param bi
	 *           The input image
	 * @param scale
	 *           The input image is this many times larger than the
	 *           desired output
	 * @param scanDist
	 *           The furthest an "in" output pixel can be from an "in"
	 *           input pixel. Larger numbers will allow larger
	 *           outline/shadow regions at the expense of precision and
	 *           longer processing times.
	 * @param listener
	 *           will be appraised of progress
	 */
	public DistanceFieldFilter( BufferedImage bi, int scale, int scanDist,
			Listener listener )
	{
		scaleFactor = scale;
		scanSize = scanDist > 0 ? scanDist : Math.max( bi.getWidth(), bi.getHeight() );
		this.listener = listener;

		image = new boolean[ bi.getWidth() ][ bi.getHeight() ];

		int white = Color.WHITE.getRGB();
		for( int x = 0; x < image.length; x++ )
		{
			for( int y = 0; y < image[ x ].length; y++ )
			{
				image[ x ][ y ] = bi.getRGB( x, y ) == white;
			}
		}

	}

	@Override
	public void run()
	{
		int outWidth = image.length / scaleFactor;
		int outHeight = image[ 0 ].length / scaleFactor;

		float[][] distances = new float[ outWidth ][ outHeight ];

		// find distances
		for( int x = 0; x < distances.length; x++ )
		{
			if( listener != null )
			{
				listener.progress( ( float ) x / outWidth );
			}

			for( int y = 0; y < distances[ x ].length; y++ )
			{
				distances[ x ][ y ] =
						findSignedDistance( x * scaleFactor + scaleFactor / 2, y * scaleFactor
								+ scaleFactor / 2 );
			}
		}

		// find max and min values
		float max = 0;
		float min = 0;

		for( int x = 0; x < distances.length; x++ )
		{
			for( int y = 0; y < distances[ x ].length; y++ )
			{
				float d = distances[ x ][ y ];

				if( d > max )
				{
					max = d;
				}
				if( d < min )
				{
					min = d;
				}
			}
		}

		// normalise distances to 0-1 range, while keeping distance 0 at
		// 0.5
		final float scale = Math.max( Math.abs( min ), Math.abs( max ) );

		for( int x = 0; x < distances.length; x++ )
		{
			for( int y = 0; y < distances[ x ].length; y++ )
			{
				float d = distances[ x ][ y ];

				// to -1 : 1
				d /= scale;

				// to -.5 : .5
				d /= 2;

				// to 0 ; 1
				d += 0.5f;

				assert d >= 0;
				assert d <= 1;

				distances[ x ][ y ] = d;
			}
		}

		// generate image
		BufferedImage outImage =
				new BufferedImage( outWidth, outHeight, BufferedImage.TYPE_4BYTE_ABGR );
		for( int x = 0; x < distances.length; x++ )
		{
			for( int y = 0; y < distances[ 0 ].length; y++ )
			{
				float d = distances[ x ][ y ];
				if( d == Float.NaN )
				{
					d = 0;
				}

				// As alpha
				outImage.setRGB( x, y, new Color( 1.0f, 1.0f, 1.0f, d ).getRGB() );
			}
		}

		result = outImage;

		if( listener != null )
		{
			listener.finished( result );
		}
	}

	private float separationSq( final float x1, final float y1, final float x2,
			final float y2 )
	{
		final float dx = x1 - x2;
		final float dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	private float findSignedDistance( final int pointX, final int pointY )
	{
		boolean baseIn = image[ pointX ][ pointY ];

		// start with the max value
		float[] closestDistance = new float[] { scanSize * scanSize };

		int maxScan = scanSize;

		for( int i = 0; i <= maxScan; i++ )
		{
			boolean found = scan( pointX, pointY, i, closestDistance );

			if( found )
			{
				// we can limit the search
				maxScan = ( int ) Math.ceil( Math.sqrt( closestDistance[ 0 ] ) );
			}
		}

		float d = ( float ) Math.sqrt( closestDistance[ 0 ] );

		return baseIn ? d : -d;
	}

	/**
	 * @param pointX
	 * @param pointY
	 * @param x
	 * @param y
	 * @param closest
	 * @return true if closest was updated
	 */
	private boolean checkPoint( int pointX, int pointY, int x, int y, float[] closest )
	{
		boolean baseIn = image[ pointX ][ pointY ];
		boolean pointIn;

		if( x < 0 || x >= image.length || y < 0 || y >= image[ 0 ].length )
		{
			pointIn = false;
		}
		else
		{
			pointIn = image[ x ][ y ];
		}

		if( baseIn != pointIn )
		{
			final float dist = separationSq( pointX, pointY, x, y );
			if( dist < closest[ 0 ] )
			{
				closest[ 0 ] = dist;
				return true;
			}
		}

		return false;
	}

	/**
	 * Scans the outline of the box around x,y
	 * 
	 * @param x
	 * @param y
	 * @param size
	 * @param closest
	 * @return true if closest was updated
	 */
	private boolean scan( int x, int y, int size, float[] closest )
	{
		boolean updated = false;

		// top edge
		for( int i = x - size; i < x + size; i++ )
		{
			updated |= checkPoint( x, y, i, y + size, closest );
		}

		// bottom edge
		for( int i = x - size; i < x + size; i++ )
		{
			updated |= checkPoint( x, y, i, y - size, closest );
		}

		// left
		for( int i = y - size; i < y + size; i++ )
		{
			updated |= checkPoint( x, y, x - size, i, closest );
		}

		// right
		for( int i = y - size; i < y + size; i++ )
		{
			updated |= checkPoint( x, y, x + size, i, closest );
		}

		return updated;
	}

	/**
	 * Gets the resulting distance map image
	 * 
	 * @return The result, or null if processing has not finished yet
	 */
	public BufferedImage getResult()
	{
		return result;
	}

	/**
	 * Interface for monitoring the progress of the processing
	 * 
	 * @author ryanm
	 */
	public interface Listener
	{
		/**
		 * Called to update the listener on processing progression
		 * 
		 * @param progress
		 *           0 at the start, 1 at the end
		 */
		public void progress( float progress );

		/**
		 * Called when processing is complete
		 * 
		 * @param result
		 *           the output image
		 */
		public void finished( BufferedImage result );
	}
}