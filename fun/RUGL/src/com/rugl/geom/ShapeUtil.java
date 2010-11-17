
package com.rugl.geom;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.rugl.renderer.RenderUtils;
import com.ryanm.util.geom.LineUtils;
import com.ryanm.util.geom.VectorUtils;
import com.ryanm.util.math.Trig;

/**
 * Utility for constructing various {@link Shape}s
 * 
 * @author ryanm
 */
public class ShapeUtil
{
	/**
	 * Vertices will be in nbr, ntr, nbl, ntl, fbr, ftr, fbl, ftl
	 * order, where near is -z, far is +z, bottom is -y, top is +y,
	 * left is -x, right is +x
	 * 
	 * @param minx
	 * @param miny
	 * @param minz
	 * @param maxx
	 * @param maxy
	 * @param maxz
	 * @return An axis-aligned cuboid
	 */
	public static Shape cuboid( float minx, float miny, float minz, float maxx,
			float maxy, float maxz )
	{
		float[] verts = new float[ 8 * 3 ];
		int[] tris = new int[ 6 * 2 * 3 ];

		int vi = 0;
		verts[ vi++ ] = minx;
		verts[ vi++ ] = miny;
		verts[ vi++ ] = minz;

		verts[ vi++ ] = minx;
		verts[ vi++ ] = maxy;
		verts[ vi++ ] = minz;

		verts[ vi++ ] = maxx;
		verts[ vi++ ] = miny;
		verts[ vi++ ] = minz;

		verts[ vi++ ] = maxx;
		verts[ vi++ ] = maxy;
		verts[ vi++ ] = minz;

		verts[ vi++ ] = minx;
		verts[ vi++ ] = miny;
		verts[ vi++ ] = maxz;

		verts[ vi++ ] = minx;
		verts[ vi++ ] = maxy;
		verts[ vi++ ] = maxz;

		verts[ vi++ ] = maxx;
		verts[ vi++ ] = miny;
		verts[ vi++ ] = maxz;

		verts[ vi++ ] = maxx;
		verts[ vi++ ] = maxy;
		verts[ vi++ ] = maxz;

		int ti = 0;
		// near
		tris[ ti++ ] = 0;
		tris[ ti++ ] = 2;
		tris[ ti++ ] = 1;
		tris[ ti++ ] = 2;
		tris[ ti++ ] = 3;
		tris[ ti++ ] = 1;
		// bottom
		tris[ ti++ ] = 0;
		tris[ ti++ ] = 4;
		tris[ ti++ ] = 2;
		tris[ ti++ ] = 4;
		tris[ ti++ ] = 6;
		tris[ ti++ ] = 2;
		// right
		tris[ ti++ ] = 2;
		tris[ ti++ ] = 6;
		tris[ ti++ ] = 3;
		tris[ ti++ ] = 7;
		tris[ ti++ ] = 3;
		tris[ ti++ ] = 6;
		// top
		tris[ ti++ ] = 1;
		tris[ ti++ ] = 3;
		tris[ ti++ ] = 5;
		tris[ ti++ ] = 3;
		tris[ ti++ ] = 7;
		tris[ ti++ ] = 5;
		// left
		tris[ ti++ ] = 4;
		tris[ ti++ ] = 0;
		tris[ ti++ ] = 5;
		tris[ ti++ ] = 0;
		tris[ ti++ ] = 1;
		tris[ ti++ ] = 5;
		// far
		tris[ ti++ ] = 6;
		tris[ ti++ ] = 4;
		tris[ ti++ ] = 7;
		tris[ ti++ ] = 4;
		tris[ ti++ ] = 5;
		tris[ ti++ ] = 7;

		return new Shape( verts, tris );
	}

	/**
	 * @param angle
	 *           the angle range through which to spiral
	 * @param angleInc
	 *           granularity of spiral
	 * @param width
	 *           The width of the line
	 * @return A golden spiral at the origin
	 */
	public static Shape goldenSpiral( float angle, float angleInc, float width )
	{
		return logSpiral( 1, 0.306349f, angle, angleInc, width );
	}

	/**
	 * @param a
	 * @param b
	 * @param tRange
	 * @param tStep
	 * @param width
	 * @return A logarithmic spiral
	 */
	public static Shape logSpiral( float a, float b, float tRange, float tStep, float width )
	{
		assert tRange != 0;

		int points = ( int ) ( tRange / tStep ) + 2;
		tStep = tRange / points;
		float[] verts = new float[ points * 2 ];

		int vi = 0;
		for( int i = 0; i < points; i++ )
		{
			float t = i * tStep;
			float r = ( float ) ( a * Math.pow( Math.E, b * t ) );
			verts[ vi++ ] = r * Trig.cos( t );
			verts[ vi++ ] = r * Trig.sin( t );
		}

		return line( width, verts );
	}

	/**
	 * @param legs
	 *           number of legs on each side
	 * @param legLength
	 *           length of legs
	 * @param legwidth
	 *           width of legs
	 * @param sep
	 *           distance between core and legs
	 * @return A chip icon shape
	 */
	public static Shape chipIcon( int legs, float legLength, float legwidth, float sep )
	{
		float core = 1 - 2 * sep - 2 * legLength;
		Shape coreShape = filledQuad( 0, 0, core, core, 0 );
		coreShape.translate( legLength + sep, legLength + sep, 0 );

		Shape hLeg = filledQuad( 0, -legwidth / 2, legLength, legwidth / 2, 0 );
		Shape vLeg = filledQuad( -legwidth / 2, 0, legwidth / 2, legLength, 0 );

		ShapeBuilder b = new ShapeBuilder();

		b.addShape( coreShape );

		float legSep = core / legs;
		float p = legLength + sep + legSep / 2;

		for( int i = 0; i < legs; i++ )
		{
			b.addShape( hLeg.clone().translate( 0, p, 0 ) );
			b.addShape( hLeg.clone().translate( core + 2 * sep + legLength, p, 0 ) );

			b.addShape( vLeg.clone().translate( p, 0, 0 ) );
			b.addShape( vLeg.clone().translate( p, core + 2 * sep + legLength, 0 ) );

			p += legSep;
		}

		return b.compile();
	}

	/**
	 * Constructs an arrow shape, such as you might find on a return
	 * key
	 * 
	 * @param posx
	 *           The lower-left corner of the bounding box
	 * @param posy
	 * @param width
	 *           The dimensions
	 * @param height
	 * @param arrowlength
	 *           The length of the arrowhead
	 * @param thickness
	 *           The width of the shaft
	 * @return An arrow shape
	 */
	public static Shape retArrow( float posx, float posy, float width, float height,
			float arrowlength, float thickness )
	{
		float pointy = height / 2;
		float low = pointy - thickness / 2;
		float high = low + thickness;

		float[] verts =
				new float[] { 0, pointy, arrowlength, height, arrowlength, high,
						width - thickness, high, width - thickness, height, width, height,
						width, low, arrowlength, low, arrowlength, 0 };
		for( int i = 0; i < verts.length; i += 2 )
		{
			verts[ i ] += posx;
			verts[ i + 1 ] += posy;
		}

		int[] tris =
				new int[] { 0, 2, 1, 0, 7, 2, 0, 8, 7, 7, 6, 2, 2, 6, 3, 6, 4, 3, 6, 5, 4 };

		return new Shape( to3D( verts, 0 ), tris );
	}

	/**
	 * Constructs a filled circle
	 * 
	 * @param cx
	 *           The x coordinate of the center point
	 * @param cy
	 *           The y coordinate of the center point
	 * @param radius
	 *           The radius of the circle
	 * @param maxSegment
	 *           The maximum length of the line segments making up the
	 *           circumference
	 * @param z
	 *           The z coordinate of the circle
	 * @return A filled circle {@link Shape}
	 */
	public static Shape filledCircle( float cx, float cy, float radius, float maxSegment,
			float z )
	{
		assert radius >= 0;

		float c = ( float ) ( 2 * radius * Math.PI );

		int segs = ( int ) Math.ceil( c / maxSegment );

		segs = Math.max( segs, 3 );

		float angleIncrement = ( float ) ( 2 * Math.PI / segs );

		Vector3f[] verts = new Vector3f[ segs ];
		int[] tris = new int[ 3 * ( segs - 2 ) ];

		for( int i = 0; i < verts.length; i++ )
		{
			float a = i * angleIncrement;
			verts[ i ] =
					new Vector3f( ( float ) ( radius * Math.cos( a ) ),
							( float ) ( radius * Math.sin( a ) ), z );
			verts[ i ].x += cx;
			verts[ i ].y += cy;
		}

		int ti = 0;
		for( int i = 0; i < segs - 2; i++ )
		{
			tris[ ti++ ] = 0;
			tris[ ti++ ] = i + 1;
			tris[ ti++ ] = i + 2;
		}

		return new Shape( Shape.extract( verts ), tris );
	}

	/**
	 * Constructs a filled circle that has a central vertex
	 * 
	 * @param cx
	 *           The x coordinate of the center point
	 * @param cy
	 *           The y coordinate of the center point
	 * @param radius
	 *           The radius of the circle
	 * @param maxSegment
	 *           The maximum length of the line segments making up the
	 *           circumference
	 * @param z
	 *           The z coordinate of the circle
	 * @return A filled circle {@link Shape}. The center vertex is at
	 *         index 0
	 */
	public static Shape filledCenteredCircle( float cx, float cy, float radius,
			float maxSegment, float z )
	{
		assert radius >= 0;

		float c = ( float ) ( 2 * radius * Math.PI );

		int segs = ( int ) Math.ceil( c / maxSegment );

		segs = Math.max( segs, 3 );

		float angleIncrement = ( float ) ( 2 * Math.PI / segs );

		Vector3f[] verts = new Vector3f[ segs + 1 ];
		int[] tris = new int[ 3 * segs ];

		verts[ 0 ] = new Vector3f( cx, cy, z );
		for( int i = 1; i < verts.length; i++ )
		{
			float a = i * angleIncrement;
			verts[ i ] =
					new Vector3f( ( float ) ( radius * Math.cos( a ) ),
							( float ) ( radius * Math.sin( a ) ), z );
			verts[ i ].x += cx;
			verts[ i ].y += cy;
		}

		int ti = 0;
		int vi = 1;
		for( int i = 0; i < segs; i++ )
		{
			tris[ ti++ ] = 0;
			tris[ ti++ ] = vi;
			vi++;
			tris[ ti++ ] = vi;
		}
		tris[ tris.length - 1 ] = 1;

		return new Shape( Shape.extract( verts ), tris );
	}

	/**
	 * Build a circle outline that entirely encompasses the specified
	 * radius
	 * 
	 * @param cx
	 *           The x coordinate of the center point
	 * @param cy
	 *           The y coordinate of the center point
	 * @param radius
	 *           The inner radius of the circle outline
	 * @param width
	 *           The width of the line used to draw the circle
	 * @param maxSegment
	 *           The maximum length of line segment used to draw the
	 *           circle
	 * @param z
	 *           The z coordinate for the circle
	 * @return A {@link Shape} that contains the circle's geometry
	 */
	public static Shape outerCircle( float cx, float cy, float radius, float width,
			float maxSegment, float z )
	{
		return ShapeUtil.innerCircle( cx, cy, radius + width, width, maxSegment, z );
	}

	/**
	 * Builds a circle outline that lies completely within the
	 * specified radius
	 * 
	 * @param cx
	 *           The x coordinate of the center point
	 * @param cy
	 *           The y coordinate of the center point
	 * @param radius
	 *           The outer radius of the circle outline
	 * @param width
	 *           The width of the line used to draw the circle
	 * @param maxSegment
	 *           The maximum length of line segment used to draw the
	 *           circle
	 * @param z
	 *           The z coordinate for the circle
	 * @return A {@link Shape} that contains the circle's geometry
	 */
	public static Shape innerCircle( float cx, float cy, float radius, float width,
			float maxSegment, float z )
	{
		if( width > radius )
		{
			width = radius;
		}

		float inner = radius - width;
		float outer = radius;

		if( inner == 0 )
		{
			return filledCircle( cx, cy, radius, maxSegment, z );
		}

		int segments = ( int ) Math.ceil( Math.PI * 2 * outer / maxSegment );

		segments = Math.max( 3, segments );

		float angleIncrement = ( float ) ( Math.PI * 2 / segments );

		Vector3f[] verts = new Vector3f[ segments * 2 ];
		int[] indices = new int[ segments * 6 ];

		for( int i = 0; i < segments; i++ )
		{
			float cos = ( float ) Math.cos( angleIncrement * i );
			float sin = ( float ) Math.sin( angleIncrement * i );

			verts[ 2 * i ] = new Vector3f( cx + inner * cos, cy + inner * sin, z );
			verts[ 2 * i + 1 ] = new Vector3f( cx + outer * cos, cy + outer * sin, z );

			int ci = 2 * i;
			int co = 2 * i + 1;

			int ni = ( ci + 2 ) % verts.length;
			int no = ( co + 2 ) % verts.length;

			indices[ 6 * i ] = ci;
			indices[ 6 * i + 1 ] = co;
			indices[ 6 * i + 2 ] = ni;

			indices[ 6 * i + 3 ] = co;
			indices[ 6 * i + 4 ] = no;
			indices[ 6 * i + 5 ] = ni;
		}

		return new Shape( Shape.extract( verts ), indices );
	}

	/**
	 * Creates a cross occupying the unit square
	 * 
	 * @param width
	 *           The width of the arms
	 * @return A cross shape
	 */
	public static Shape cross( float width )
	{
		float n = 0.5f - width / 2;
		float f = n + width;

		float[] verts =
				new float[] { n, 0, n, n, 0, n, 0, f, n, f, n, 1, f, 1, f, f, 1, f, 1, n, f,
						n, f, 0 };
		int[] tris =
				new int[] { 1, 3, 2, 1, 4, 3, 4, 6, 5, 4, 7, 6, 7, 9, 8, 7, 10, 9, 10, 0, 11,
						10, 1, 0, 1, 7, 4, 1, 10, 7 };

		return new Shape( to3D( verts, 0 ), tris );
	}

	/**
	 * Takes a 2D vertex array and adds the z-coordinates
	 * 
	 * @param verts
	 * @param z
	 * @return A 3D vertex array
	 */
	public static float[] to3D( float[] verts, float z )
	{
		float[] tdv = new float[ verts.length / 2 * 3 ];

		int vi = 0;

		for( int i = 0; i < verts.length; i += 2 )
		{
			tdv[ vi++ ] = verts[ i ];
			tdv[ vi++ ] = verts[ i + 1 ];
			tdv[ vi++ ] = z;
		}

		return tdv;
	}

	/**
	 * @param verts
	 * @return the 2D projection of the 3D coordinates
	 */
	public static float[] to2D( float[] verts )
	{
		float[] tdv = new float[ verts.length / 3 * 2 ];

		int vi = 0;

		for( int i = 0; i < verts.length; i += 3 )
		{
			tdv[ vi++ ] = verts[ i ];
			tdv[ vi++ ] = verts[ i + 1 ];
		}

		return tdv;
	}

	/**
	 * Creates an upward-pointing arrow shape
	 * 
	 * @param bx
	 *           The bottom corner
	 * @param by
	 * @param tx
	 *           The top corner
	 * @param ty
	 * @param cx
	 *           The inner corner nearest the bottom corner, in terms
	 *           of the bounding box
	 * @param cy
	 * @return An arrow shape
	 */
	public static Shape arrow( float bx, float by, float tx, float ty, float cx, float cy )
	{
		float dx = tx - bx;
		float dy = ty - by;

		float ix = dx * cx;
		float iy = dy * cy;

		float near = bx + ix;
		float far = tx - ix;

		float peakx = ( bx + tx ) / 2;

		float[] verts =
				new float[] { near, by, near, iy, bx, iy, peakx, ty, tx, iy, far, iy, far, bx };
		int[] tris = new int[] { 1, 3, 2, 1, 5, 3, 5, 4, 3, 0, 5, 1, 0, 6, 5 };

		return new Shape( to3D( verts, 0 ), tris );
	}

	/**
	 * Builds a simple filled quad. The vertices are in bl tl br tr
	 * order
	 * 
	 * @param px
	 *           corner x
	 * @param py
	 *           corner y
	 * @param qx
	 *           opposite corner x
	 * @param qy
	 *           opposite corner y
	 * @param z
	 *           The z coordinate of the quad
	 * @return A filled quad
	 */
	public static Shape filledQuad( float px, float py, float qx, float qy, float z )
	{
		if( px > qx )
		{
			float t = qx;
			qx = px;
			px = t;
		}

		if( py > qy )
		{
			float t = qy;
			qy = py;
			py = t;
		}

		Vector3f[] verts = new Vector3f[ 4 ];

		verts[ 0 ] = new Vector3f( px, py, z );
		verts[ 1 ] = new Vector3f( px, qy, z );
		verts[ 2 ] = new Vector3f( qx, py, z );
		verts[ 3 ] = new Vector3f( qx, qy, z );

		return new Shape( Shape.extract( verts ), RenderUtils.makeQuads( 4, 0, null, 0 ) );
	}

	/**
	 * Builds a quad outline that lies completely within the specified
	 * bounds
	 * 
	 * @param px
	 *           corner x
	 * @param py
	 *           corner y
	 * @param qx
	 *           opposite corner x
	 * @param qy
	 *           opposite corner y
	 * @param width
	 *           the width of the outline
	 * @param z
	 *           The z coordinate of the quad outline
	 * @return A quad outline
	 */
	public static Shape innerQuad( float px, float py, float qx, float qy, float width,
			float z )
	{
		if( px > qx )
		{
			float t = qx;
			qx = px;
			px = t;
		}

		if( py > qy )
		{
			float t = qy;
			qy = py;
			py = t;
		}

		Vector3f[] verts = new Vector3f[ 8 ];
		int[] tris = new int[ 8 * 3 ];

		float xwidth = width;
		float ywidth = width;

		if( Math.abs( qx - px ) < 2 * width )
		{
			xwidth = Math.abs( qx - px ) / 2.0f;
		}
		if( Math.abs( qy - py ) < 2 * width )
		{
			ywidth = Math.abs( qy - py ) / 2.0f;
		}

		int index = 0;
		verts[ index++ ] = new Vector3f( px, py, z );
		verts[ index++ ] = new Vector3f( px + xwidth, py + ywidth, z );
		verts[ index++ ] = new Vector3f( px, qy, z );
		verts[ index++ ] = new Vector3f( px + xwidth, qy - ywidth, z );
		verts[ index++ ] = new Vector3f( qx, qy, z );
		verts[ index++ ] = new Vector3f( qx - xwidth, qy - ywidth, z );
		verts[ index++ ] = new Vector3f( qx, py, z );
		verts[ index++ ] = new Vector3f( qx - xwidth, py + ywidth, z );

		index = 0;
		for( int i = 0; i < 4; i++ )
		{
			int a = i * 2;
			int b = a + 1;
			int c = ( b + 1 ) % verts.length;
			int d = ( c + 1 ) % verts.length;

			addTriangle( a, b, c, tris, index );
			index += 3;
			addTriangle( b, d, c, tris, index );
			index += 3;
		}

		return new Shape( Shape.extract( verts ), tris );

	}

	/**
	 * Builds a quad outline that encompasses the supplied bounds
	 * 
	 * @param px
	 *           corner x
	 * @param py
	 *           corner y
	 * @param qx
	 *           opposite corner x
	 * @param qy
	 *           opposite corner y
	 * @param width
	 *           The outline width
	 * @param z
	 *           The outline's z-component
	 * @return A quad outline
	 */
	public static Shape outerQuad( float px, float py, float qx, float qy, float width,
			float z )
	{
		float minx, miny, maxx, maxy;

		if( px < qx )
		{
			minx = px;
			maxx = qx;
		}
		else
		{
			minx = qx;
			maxx = px;
		}

		if( py < qy )
		{
			miny = py;
			maxy = qy;
		}
		else
		{
			miny = qy;
			maxy = py;
		}

		return innerQuad( minx - width, miny - width, maxx + width, maxy + width, width, z );
	}

	static void addTriangle( int a, int b, int c, int[] array, int index )
	{
		array[ index ] = a;
		array[ index + 1 ] = b;
		array[ index + 2 ] = c;
	}

	/**
	 * Creates a 2D triangle shape
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param cx
	 * @param cy
	 * @return The triangle abc
	 */
	public static Shape triangle( int ax, int ay, int bx, int by, int cx, int cy )
	{
		float[] v = new float[] { ax, ay, 0, bx, by, 0, cx, cy, 0 };
		int[] t = new int[] { 0, 1, 2 };

		return new Shape( v, t );
	}

	/**
	 * Returns the outline around some polygon
	 * 
	 * @param width
	 *           The width of the outline
	 * @param pos
	 *           The position of the input polygon faces relative to
	 *           the output outline edges 0 = on the right-hand edge,
	 *           0.5 in the middle, 1 = on the left-hand edge
	 * @param vin
	 *           The input polygon vertices, in x,y,x,y,x,y format.
	 *           Also assumed to be specified in clockwise order
	 * @return The outline shape
	 */
	public static Shape outline( float width, float pos, float... vin )
	{
		assert vin.length >= 6;

		Vector2f prevDir = new Vector2f(), nextDir = new Vector2f(), point = new Vector2f();
		Vector2f ta = new Vector2f(), tb = new Vector2f(), tc = new Vector2f(), td =
				new Vector2f();

		float[] vout = new float[ vin.length * 2 ];
		int vi = 0;
		for( int i = 1; i <= vin.length / 2; i++ )
		{
			int c = 2 * i % vin.length;
			int p = 2 * ( i - 1 ) % vin.length;
			int n = 2 * ( i + 1 ) % vin.length;

			prevDir.set( vin[ c ] - vin[ p ], vin[ c + 1 ] - vin[ p + 1 ] );
			prevDir.normalise();

			nextDir.set( vin[ n ] - vin[ c ], vin[ n + 1 ] - vin[ c + 1 ] );
			nextDir.normalise();

			prevDir.scale( width );
			nextDir.scale( width );
			VectorUtils.rotate90( prevDir );
			VectorUtils.rotate90( nextDir );

			// inner
			ta.set( vin[ p ] - pos * prevDir.x, vin[ p + 1 ] - pos * prevDir.y );
			tb.set( vin[ c ] - pos * prevDir.x, vin[ c + 1 ] - pos * prevDir.y );
			tc.set( vin[ c ] - pos * nextDir.x, vin[ c + 1 ] - pos * nextDir.y );
			td.set( vin[ n ] - pos * nextDir.x, vin[ n + 1 ] - pos * nextDir.y );
			LineUtils.lineIntersection( ta, tb, tc, td, point );
			vout[ vi++ ] = point.x;
			vout[ vi++ ] = point.y;

			// outer
			ta.set( vin[ p ] + ( 1 - pos ) * prevDir.x, vin[ p + 1 ] + ( 1 - pos )
					* prevDir.y );
			tb.set( vin[ c ] + ( 1 - pos ) * prevDir.x, vin[ c + 1 ] + ( 1 - pos )
					* prevDir.y );
			tc.set( vin[ c ] + ( 1 - pos ) * nextDir.x, vin[ c + 1 ] + ( 1 - pos )
					* nextDir.y );
			td.set( vin[ n ] + ( 1 - pos ) * nextDir.x, vin[ n + 1 ] + ( 1 - pos )
					* nextDir.y );
			LineUtils.lineIntersection( ta, tb, tc, td, point );
			vout[ vi++ ] = point.x;
			vout[ vi++ ] = point.y;
		}

		assert vi == vout.length : vi + " " + vout.length;

		int[] tris = new int[ 3 * vin.length ];
		int ti = 0;
		for( int i = 0; i < vin.length / 2; i++ )
		{
			tris[ ti++ ] = 2 * i;
			tris[ ti++ ] = 2 * i + 2;
			tris[ ti++ ] = 2 * i + 1;

			tris[ ti++ ] = 2 * i + 2;
			tris[ ti++ ] = 2 * i + 3;
			tris[ ti++ ] = 2 * i + 1;
		}

		for( int i = 0; i < tris.length; i++ )
		{
			tris[ i ] %= vout.length / 2;
		}

		return new Shape( to3D( vout, 0 ), tris );
	}

	/**
	 * A simple line segment path
	 * 
	 * @param width
	 * @param vin
	 * @return A line path of the specified width
	 */
	public static Shape line( float width, float... vin )
	{
		assert vin.length >= 4;

		Vector2f prevDir = new Vector2f(), nextDir = new Vector2f(), point = new Vector2f();
		Vector2f ta = new Vector2f(), tb = new Vector2f(), tc = new Vector2f(), td =
				new Vector2f();

		float pos = 0.5f;

		float[] vout = new float[ vin.length * 2 ];
		int vi = 0;

		prevDir.set( vin[ 2 ] - vin[ 0 ], vin[ 3 ] - vin[ 1 ] );
		prevDir.normalise();
		prevDir.scale( width );
		VectorUtils.rotate90( prevDir );

		vout[ vi++ ] = vin[ 0 ] - pos * prevDir.x;
		vout[ vi++ ] = vin[ 1 ] - pos * prevDir.y;
		vout[ vi++ ] = vin[ 0 ] + ( 1 - pos ) * prevDir.x;
		vout[ vi++ ] = vin[ 1 ] + ( 1 - pos ) * prevDir.y;

		nextDir.set( prevDir );

		for( int i = 1; i < vin.length / 2 - 1; i++ )
		{
			int p = 2 * ( i - 1 );
			int c = 2 * i;
			int n = 2 * ( i + 1 );

			prevDir.set( vin[ c ] - vin[ p ], vin[ c + 1 ] - vin[ p + 1 ] );
			prevDir.normalise();

			nextDir.set( vin[ n ] - vin[ c ], vin[ n + 1 ] - vin[ c + 1 ] );
			nextDir.normalise();

			prevDir.scale( width );
			nextDir.scale( width );
			VectorUtils.rotate90( prevDir );
			VectorUtils.rotate90( nextDir );

			// inner
			ta.set( vin[ p ] - pos * prevDir.x, vin[ p + 1 ] - pos * prevDir.y );
			tb.set( vin[ c ] - pos * prevDir.x, vin[ c + 1 ] - pos * prevDir.y );
			tc.set( vin[ c ] - pos * nextDir.x, vin[ c + 1 ] - pos * nextDir.y );
			td.set( vin[ n ] - pos * nextDir.x, vin[ n + 1 ] - pos * nextDir.y );

			if( LineUtils.lineIntersection( ta, tb, tc, td, point ) == null )
			{
				// parallel segments
				vout[ vi++ ] = tb.x;
				vout[ vi++ ] = tb.y;
			}
			else
			{
				vout[ vi++ ] = point.x;
				vout[ vi++ ] = point.y;
			}

			// outer
			ta.set( vin[ p ] + ( 1 - pos ) * prevDir.x, vin[ p + 1 ] + ( 1 - pos )
					* prevDir.y );
			tb.set( vin[ c ] + ( 1 - pos ) * prevDir.x, vin[ c + 1 ] + ( 1 - pos )
					* prevDir.y );
			tc.set( vin[ c ] + ( 1 - pos ) * nextDir.x, vin[ c + 1 ] + ( 1 - pos )
					* nextDir.y );
			td.set( vin[ n ] + ( 1 - pos ) * nextDir.x, vin[ n + 1 ] + ( 1 - pos )
					* nextDir.y );

			if( LineUtils.lineIntersection( ta, tb, tc, td, point ) == null )
			{
				// parallel segments
				vout[ vi++ ] = tb.x;
				vout[ vi++ ] = tb.y;
			}
			else
			{
				vout[ vi++ ] = point.x;
				vout[ vi++ ] = point.y;
			}
		}

		vout[ vi++ ] = vin[ vin.length - 2 ] - pos * nextDir.x;
		vout[ vi++ ] = vin[ vin.length - 1 ] - pos * nextDir.y;
		vout[ vi++ ] = vin[ vin.length - 2 ] + ( 1 - pos ) * nextDir.x;
		vout[ vi++ ] = vin[ vin.length - 1 ] + ( 1 - pos ) * nextDir.y;

		assert vi == vout.length : vi + " " + vout.length;

		int[] tris = new int[ 3 * ( vin.length - 2 ) ];
		int ti = 0;
		for( int i = 0; i < vin.length / 2 - 1; i++ )
		{
			tris[ ti++ ] = 2 * i;
			tris[ ti++ ] = 2 * i + 2;
			tris[ ti++ ] = 2 * i + 1;

			tris[ ti++ ] = 2 * i + 2;
			tris[ ti++ ] = 2 * i + 3;
			tris[ ti++ ] = 2 * i + 1;
		}

		return new Shape( to3D( vout, 0 ), tris );
	}

}
