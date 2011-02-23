
package com.ryanm.droid.rugl.gl;

import java.util.Arrays;

import android.opengl.GLES10;
import android.opengl.Matrix;

/**
 * A copy of {@link android.opengl.GLU}, but not requiring a
 * {@link javax.microedition.khronos.opengles.GL} instance to be
 * passed in. Also allows you to access the projection matrix
 * directly, rather than having to read it back from OpenGL
 * 
 * @author ryanm
 */
public class GLU
{
	/**
	 * Return an error string from a GL or GLU error code.
	 * 
	 * @param error
	 *           - a GL or GLU error code.
	 * @return the error string for the input error code, or NULL if
	 *         the input was not a valid GL or GLU error code.
	 */
	public static String gluErrorString( int error )
	{
		switch( error )
		{
			case GLES10.GL_NO_ERROR:
				return "no error";
			case GLES10.GL_INVALID_ENUM:
				return "invalid enum";
			case GLES10.GL_INVALID_VALUE:
				return "invalid value";
			case GLES10.GL_INVALID_OPERATION:
				return "invalid operation";
			case GLES10.GL_STACK_OVERFLOW:
				return "stack overflow";
			case GLES10.GL_STACK_UNDERFLOW:
				return "stack underflow";
			case GLES10.GL_OUT_OF_MEMORY:
				return "out of memory";
			default:
				return null;
		}
	}

	/**
	 * Define a viewing transformation in terms of an eye point, a
	 * center of view, and an up vector.
	 * 
	 * @param eyeX
	 *           eye point X
	 * @param eyeY
	 *           eye point Y
	 * @param eyeZ
	 *           eye point Z
	 * @param centerX
	 *           center of view X
	 * @param centerY
	 *           center of view Y
	 * @param centerZ
	 *           center of view Z
	 * @param upX
	 *           up vector X
	 * @param upY
	 *           up vector Y
	 * @param upZ
	 *           up vector Z
	 * @param matrix
	 *           an array in which to store the resultant matrix, if
	 *           not <code>null</code>
	 */
	public static void gluLookAt( float eyeX, float eyeY, float eyeZ, float centerX,
			float centerY, float centerZ, float upX, float upY, float upZ, float[] matrix )
	{
		float[] scratch = matrix != null ? matrix : sScratch;

		setLookAtM( scratch, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ );

		GLES10.glMultMatrixf( scratch, 0 );
	}

	/**
	 * Set up a 2D orthographic projection matrix
	 * 
	 * @param left
	 * @param right
	 * @param bottom
	 * @param top
	 */
	public static void gluOrtho2D( float left, float right, float bottom, float top )
	{
		GLES10.glOrthof( left, right, bottom, top, -1.0f, 1.0f );
	}

	/**
	 * Set up a perspective projection matrix
	 * 
	 * @param fovy
	 *           specifies the field of view angle, in degrees, in the
	 *           Y direction.
	 * @param aspect
	 *           specifies the aspect ration that determines the field
	 *           of view in the x direction. The aspect ratio is the
	 *           ratio of x (width) to y (height).
	 * @param near
	 *           specifies the distance from the viewer to the near
	 *           clipping plane (always positive).
	 * @param far
	 *           specifies the distance from the viewer to the far
	 *           clipping plane (always positive).
	 * @param matrix
	 *           an array in which to store the resultant matrix, if
	 *           not <code>null</code>
	 */
	public static void gluPerspective( float fovy, float aspect, float near, float far,
			float[] matrix )
	{
		float top = near * ( float ) Math.tan( fovy * Math.PI / 360.0 );
		float bottom = -top;
		float left = bottom * aspect;
		float right = top * aspect;

		if( matrix != null )
		{
			frustum( left, right, top, bottom, near, far, matrix );
			GLES10.glMultMatrixf( matrix, 0 );
		}
		else
		{
			GLES10.glFrustumf( left, right, bottom, top, near, far );
		}
	}

	private static void frustum( float left, float right, float top, float bottom,
			float near, float far, float[] matrix )
	{
		float a = ( right + left ) / ( right - left );
		float b = ( top + bottom ) / ( top - bottom );
		float c = -( far + near ) / ( far - near );
		float d = -( 2 * far * near ) / ( far - near );

		// [ 0 4 8 12 ]
		// [ 1 5 9 13 ]
		// [ 2 6 10 14 ]
		// [ 3 7 11 15 ]

		Arrays.fill( matrix, 0 );
		matrix[ 0 ] = 2 * near / ( right - left );
		matrix[ 8 ] = a;
		matrix[ 5 ] = 2 * near / ( top - bottom );
		matrix[ 9 ] = b;
		matrix[ 10 ] = c;
		matrix[ 14 ] = d;
		matrix[ 11 ] = -1;
	}

	/**
	 * Map object coordinates into window coordinates. gluProject
	 * transforms the specified object coordinates into window
	 * coordinates using model, proj, and view. The result is stored in
	 * win.
	 * <p>
	 * Note that you can use the OES_matrix_get extension, if present,
	 * to get the current modelView and projection matrices.
	 * 
	 * @param objX
	 *           object coordinates X
	 * @param objY
	 *           object coordinates Y
	 * @param objZ
	 *           object coordinates Z
	 * @param model
	 *           the current modelview matrix
	 * @param modelOffset
	 *           the offset into the model array where the modelview
	 *           maxtrix data starts.
	 * @param project
	 *           the current projection matrix
	 * @param projectOffset
	 *           the offset into the project array where the project
	 *           matrix data starts.
	 * @param view
	 *           the current view, {x, y, width, height}
	 * @param viewOffset
	 *           the offset into the view array where the view vector
	 *           data starts.
	 * @param win
	 *           the output vector {winX, winY, winZ}, that returns the
	 *           computed window coordinates.
	 * @param winOffset
	 *           the offset into the win array where the win vector
	 *           data starts.
	 * @return A return value of <code>true</code> indicates success, a
	 *         return value of <code>false</code> indicates failure.
	 */
	public static boolean gluProject( float objX, float objY, float objZ, float[] model,
			int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset,
			float[] win, int winOffset )
	{
		float[] scratch = sScratch;

		final int M_OFFSET = 0; // 0..15
		final int V_OFFSET = 16; // 16..19
		final int V2_OFFSET = 20; // 20..23
		Matrix.multiplyMM( scratch, M_OFFSET, project, projectOffset, model, modelOffset );

		scratch[ V_OFFSET + 0 ] = objX;
		scratch[ V_OFFSET + 1 ] = objY;
		scratch[ V_OFFSET + 2 ] = objZ;
		scratch[ V_OFFSET + 3 ] = 1.0f;

		Matrix.multiplyMV( scratch, V2_OFFSET, scratch, M_OFFSET, scratch, V_OFFSET );

		float w = scratch[ V2_OFFSET + 3 ];
		if( w == 0.0f )
		{
			return false;
		}

		float rw = 1.0f / w;

		win[ winOffset ] =
				view[ viewOffset ] + view[ viewOffset + 2 ]
						* ( scratch[ V2_OFFSET + 0 ] * rw + 1.0f ) * 0.5f;
		win[ winOffset + 1 ] =
				view[ viewOffset + 1 ] + view[ viewOffset + 3 ]
						* ( scratch[ V2_OFFSET + 1 ] * rw + 1.0f ) * 0.5f;
		win[ winOffset + 2 ] = ( scratch[ V2_OFFSET + 2 ] * rw + 1.0f ) * 0.5f;

		return true;
	}

	/**
	 * Map window coordinates to object coordinates. gluUnProject maps
	 * the specified window coordinates into object coordinates using
	 * model, proj, and view. The result is stored in obj.
	 * <p>
	 * Note that you can use the OES_matrix_get extension, if present,
	 * to get the current modelView and projection matrices.
	 * 
	 * @param winX
	 *           window coordinates X
	 * @param winY
	 *           window coordinates Y
	 * @param winZ
	 *           window coordinates Z
	 * @param model
	 *           the current modelview matrix
	 * @param modelOffset
	 *           the offset into the model array where the modelview
	 *           maxtrix data starts.
	 * @param project
	 *           the current projection matrix
	 * @param projectOffset
	 *           the offset into the project array where the project
	 *           matrix data starts.
	 * @param view
	 *           the current view, {x, y, width, height}
	 * @param viewOffset
	 *           the offset into the view array where the view vector
	 *           data starts.
	 * @param obj
	 *           the output vector {objX, objY, objZ}, that returns the
	 *           computed object coordinates.
	 * @param objOffset
	 *           the offset into the obj array where the obj vector
	 *           data starts.
	 * @return A return value of <code>true</code> indicates success, a
	 *         return value of <code>false</code> indicates failure.
	 */
	public static boolean gluUnProject( float winX, float winY, float winZ, float[] model,
			int modelOffset, float[] project, int projectOffset, int[] view, int viewOffset,
			float[] obj, int objOffset )
	{
		float[] scratch = sScratch;

		final int PM_OFFSET = 0; // 0..15
		final int INVPM_OFFSET = 16; // 16..31
		final int V_OFFSET = 0; // 0..3 Reuses PM_OFFSET space
		Matrix.multiplyMM( scratch, PM_OFFSET, project, projectOffset, model, modelOffset );

		if( !Matrix.invertM( scratch, INVPM_OFFSET, scratch, PM_OFFSET ) )
		{
			return false;
		}

		scratch[ V_OFFSET + 0 ] =
				2.0f * ( winX - view[ viewOffset + 0 ] ) / view[ viewOffset + 2 ] - 1.0f;
		scratch[ V_OFFSET + 1 ] =
				2.0f * ( winY - view[ viewOffset + 1 ] ) / view[ viewOffset + 3 ] - 1.0f;
		scratch[ V_OFFSET + 2 ] = 2.0f * winZ - 1.0f;
		scratch[ V_OFFSET + 3 ] = 1.0f;

		Matrix.multiplyMV( obj, objOffset, scratch, INVPM_OFFSET, scratch, V_OFFSET );

		return true;
	}

	/**
	 * Define a viewing transformation in terms of an eye point, a
	 * center of view, and an up vector.
	 * 
	 * @param rm
	 *           returns the result
	 * @param rmOffset
	 *           index into rm where the result matrix starts
	 * @param eyeX
	 *           eye point X
	 * @param eyeY
	 *           eye point Y
	 * @param eyeZ
	 *           eye point Z
	 * @param centerX
	 *           center of view X
	 * @param centerY
	 *           center of view Y
	 * @param centerZ
	 *           center of view Z
	 * @param upX
	 *           up vector X
	 * @param upY
	 *           up vector Y
	 * @param upZ
	 *           up vector Z
	 */
	private static void setLookAtM( float[] rm, int rmOffset, float eyeX, float eyeY,
			float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY,
			float upZ )
	{

		// See the OpenGL GLUT documentation for gluLookAt for a
		// description
		// of the algorithm. We implement it in a straightforward way:

		float fx = centerX - eyeX;
		float fy = centerY - eyeY;
		float fz = centerZ - eyeZ;

		// Normalize f
		float rlf = 1.0f / Matrix.length( fx, fy, fz );
		fx *= rlf;
		fy *= rlf;
		fz *= rlf;

		// compute s = f x up (x means "cross product")
		float sx = fy * upZ - fz * upY;
		float sy = fz * upX - fx * upZ;
		float sz = fx * upY - fy * upX;

		// and normalize s
		float rls = 1.0f / Matrix.length( sx, sy, sz );
		sx *= rls;
		sy *= rls;
		sz *= rls;

		// compute u = s x f
		float ux = sy * fz - sz * fy;
		float uy = sz * fx - sx * fz;
		float uz = sx * fy - sy * fx;

		rm[ rmOffset + 0 ] = sx;
		rm[ rmOffset + 1 ] = ux;
		rm[ rmOffset + 2 ] = -fx;
		rm[ rmOffset + 3 ] = 0.0f;

		rm[ rmOffset + 4 ] = sy;
		rm[ rmOffset + 5 ] = uy;
		rm[ rmOffset + 6 ] = -fy;
		rm[ rmOffset + 7 ] = 0.0f;

		rm[ rmOffset + 8 ] = sz;
		rm[ rmOffset + 9 ] = uz;
		rm[ rmOffset + 10 ] = -fz;
		rm[ rmOffset + 11 ] = 0.0f;

		rm[ rmOffset + 12 ] = 0.0f;
		rm[ rmOffset + 13 ] = 0.0f;
		rm[ rmOffset + 14 ] = 0.0f;
		rm[ rmOffset + 15 ] = 1.0f;

		Matrix.translateM( rm, rmOffset, -eyeX, -eyeY, -eyeZ );
	}

	private static final float[] sScratch = new float[ 32 ];
}
