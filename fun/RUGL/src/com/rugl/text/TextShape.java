
package com.rugl.text;

import com.rugl.geom.TexturedShape;

/**
 * Represensta string of text
 * 
 * @author ryanm
 */
public class TextShape extends TexturedShape
{
	/**
	 * The text that this shape represents
	 */
	public final String string;

	/**
	 * The font that produced this shape
	 */
	public final Font font;

	/**
	 * @param shape
	 * @param font
	 * @param string
	 */
	public TextShape( TexturedShape shape, Font font, String string )
	{
		super( shape );
		this.font = font;
		this.string = string;
	}
}
