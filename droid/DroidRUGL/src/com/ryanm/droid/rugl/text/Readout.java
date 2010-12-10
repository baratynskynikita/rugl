
package com.ryanm.droid.rugl.text;

import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.util.math.Range;

/**
 * A garbage-efficient way to display "Score = 62387"-type strings
 * where the prefix will not change but the value will
 * 
 * @author ryanm
 */
public class Readout extends TexturedShape
{
	// private final Font font;

	private final int prefixLength;

	private final float minValue;

	private final float maxValue;

	private final int intDigits;

	private final int fracDigits;

	private final int valueOffset;

	private float[][] digitTexCoords = new float[ 10 ][];

	private final int colour;

	private float currentValue = 0;

	/**
	 * @param font
	 * @param colour
	 * @param prefix
	 * @param signed
	 * @param intDigits
	 * @param fracDigits
	 */
	public Readout( Font font, int colour, String prefix, boolean signed, int intDigits,
			int fracDigits )
	{
		super( build( font, colour, prefix, signed, intDigits, fracDigits ) );
		this.colour = colour;
		this.intDigits = intDigits;
		this.fracDigits = fracDigits;
		prefixLength = prefix.length();

		maxValue =
				( float ) ( Math.pow( 10, intDigits ) - 1 + 1 - Math.pow( 10, -fracDigits ) );
		minValue = signed ? -maxValue : 0;

		valueOffset = ( prefix.length() + ( signed ? 1 : 0 ) ) * 4 * 2;

		for( int i = 0; i < 10; i++ )
		{
			digitTexCoords[ i ] = font.getTexCoords( String.valueOf( i ), null, 0 );
		}
	}

	/**
	 * @param value
	 */
	public void updateValue( float value )
	{
		if( currentValue != value )
		{
			currentValue = value;

			value = Range.limit( value, minValue, maxValue );

			// set minus sign colour
			for( int i = prefixLength * 4 - 4; i < prefixLength * 4; i++ )
			{
				colours[ i ] = value < 0 ? colour : 0;
			}

			// shift right
			value /= Math.pow( 10, intDigits - 1 );

			int index = valueOffset;
			for( int i = 0; i < intDigits; i++ )
			{
				// shift up
				int digit = ( int ) value;
				value -= digit;
				value *= 10;
				System.arraycopy( digitTexCoords[ digit ], 0, correctedTexCoords, index, 8 );
				index += 8;
			}

			// skip the .
			index += 8;
			for( int i = 0; i < fracDigits; i++ )
			{
				int digit = ( int ) value;
				value -= digit;
				value *= 10;
				System.arraycopy( digitTexCoords[ index ], 0, texCoords, index, 8 );
				index += 8;
			}

		}
	}

	private static TexturedShape build( Font font, int colour, String prefix,
			boolean signed, int intDigits, int fracDigits )
	{
		StringBuilder buff = new StringBuilder( prefix );
		if( signed )
		{
			buff.append( '-' );
		}
		for( int i = 0; i < intDigits; i++ )
		{
			buff.append( '0' );
		}
		if( fracDigits > 0 )
		{
			buff.append( '.' );
		}
		for( int i = 0; i < fracDigits; i++ )
		{
			buff.append( '0' );
		}

		return font.buildTextShape( buff, colour );
	}
}
