
uniform sampler2D textMap;

// Scaling factor for AA width
uniform float aaWidth;

// Alpha value at the outside of the border
uniform float minBorder;

// Alpha value at the inside of the border
uniform float maxBorder;

// Colour of the border
uniform vec4 borderColour;

// Colour of the glow outside the border
uniform vec4 glowColour;

// alpha value at the outer edge of the glow
uniform float minGlow;

void main()
{
	vec4 baseColour = gl_Color;
	
	 float distanceFactor = texture2D(textMap, gl_TexCoord[0].xy).a;
	
	 float width = fwidth(gl_TexCoord[0].x) * aaWidth; 
	
	baseColour.a *= smoothstep( 0.5 - width, 0.5 + width, distanceFactor );
	
	// Outline constants
	 vec4 outlineColour = borderColour;
	 float OUTLINE_MIN_0 = minBorder;
	 float OUTLINE_MIN_1 = OUTLINE_MIN_0 + width * 2.0;
	
	 float OUTLINE_MAX_1 = maxBorder;
	 float OUTLINE_MAX_0 = OUTLINE_MAX_1 - width * 2.0;

	// Outline calculation
	if( distanceFactor >= OUTLINE_MIN_0 && distanceFactor <= OUTLINE_MAX_1 )
	{
		float outlineAlpha = 1.0;
		if (distanceFactor <= OUTLINE_MIN_1)
		{
			outlineAlpha = smoothstep( OUTLINE_MIN_0, OUTLINE_MIN_1, distanceFactor );
		}
		else
		{
			outlineAlpha = smoothstep( OUTLINE_MAX_1, OUTLINE_MAX_0, distanceFactor );
		}
		
		baseColour = mix( baseColour, outlineColour, outlineAlpha );
	}
	
	 float exterior = min( minBorder, maxBorder );
	
	if( distanceFactor < exterior && minGlow < exterior )
	{
		// glow calculation
		float glowFactor = smoothstep( minGlow, exterior, distanceFactor);
	
		baseColour = mix( baseColour, glowColour, glowFactor );
	}
	
	gl_FragColor = baseColour;
}