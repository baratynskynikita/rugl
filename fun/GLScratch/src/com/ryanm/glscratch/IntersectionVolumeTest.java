
package com.ryanm.glscratch;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.ryanm.util.geom.volume.Box;
import com.ryanm.util.geom.volume.Dipyramid;
import com.ryanm.util.geom.volume.GridSurface;
import com.ryanm.util.geom.volume.IntersectionVolume;
import com.ryanm.util.geom.volume.Tetrahedron;

/**
 * A mouse-rotatable viewer, based on Gears
 * 
 * @author ryanm
 */
public class IntersectionVolumeTest
{
	private float view_rotx = 20.0f;

	private float view_roty = 30.0f;

	private float tilt_roty = 0;

	private float tilt_rotx = 0;

	private Dipyramid dp = new Dipyramid( 25 );

	private float[][] hm = new float[ 30 ][ 30 ];

	private GridSurface surf = new GridSurface( hm.length, hm[ 0 ].length );

	private IntersectionVolume[] volumes =
			new IntersectionVolume[] { new Box(), new Tetrahedron(), new Dipyramid( 5 ), dp, surf };

	private float scale = 4;

	private int shape = 1;

	private int slices = 7;

	private float offset = 0;

	private int speed = 3;

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		System.setProperty( "LWJGL_DISABLE_XRANDR", "true" );
		new IntersectionVolumeTest().execute();
		System.exit( 0 );
	}

	/**
	 * 
	 */
	private void execute()
	{
		try
		{
			init();
		}
		catch( LWJGLException le )
		{
			le.printStackTrace();
			System.out.println( "Failed to initialize" );
			return;
		}

		loop();

		destroy();
	}

	/**
	 * 
	 */
	private void destroy()
	{
		Display.destroy();
	}

	/**
	 * 
	 */
	private void loop()
	{
		while( !Display.isCloseRequested() )
		{
			while( Keyboard.next() )
			{
				switch( Keyboard.getEventKey() )
				{
					case Keyboard.KEY_Q:
						slices += Keyboard.getEventKeyState() ? 1 : 0;
						break;
					case Keyboard.KEY_W:
						slices += Keyboard.getEventKeyState() ? -1 : 0;
						break;
					case Keyboard.KEY_S:
						speed = 0;
						break;
					default:
						break;
				}

				try
				{
					shape = Integer.parseInt( String.valueOf( Keyboard.getEventCharacter() ) );
				}
				catch( NumberFormatException nfe )
				{
				}
			}

			if( Keyboard.isKeyDown( Keyboard.KEY_A ) )
			{
				speed++;
			}

			if( Keyboard.isKeyDown( Keyboard.KEY_D ) )
			{
				speed--;
			}

			shape = shape < 1 ? 1 : shape;
			shape = shape > volumes.length ? volumes.length : shape;
			slices = slices < 2 ? 2 : slices;

			if( Mouse.isButtonDown( 0 ) )
			{
				view_roty += Mouse.getDX();
				view_rotx -= Mouse.getDY();
			}
			else if( Mouse.isButtonDown( 1 ) )
			{
				tilt_roty += Math.toRadians( Mouse.getDX() );
				tilt_rotx -= Math.toRadians( Mouse.getDY() );
			}

			offset += speed * 0.001f;
			offset %= 4.0f / slices;

			scale += Mouse.getDWheel() / 1000.0f;

			GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

			GL11.glPushMatrix();
			GL11.glRotatef( view_rotx, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( view_roty, 0.0f, 1.0f, 0.0f );

			GL11.glScaled( scale, scale, scale );

			tiltMesh( volumes[ shape - 1 ], 2, slices );

			GL11.glPopMatrix();

			Display.update();
		}
	}

	/**
	 * 
	 */
	private void init() throws LWJGLException
	{
		// create Window of size 300x300
		Display.setLocation( ( Display.getDisplayMode().getWidth() - 300 ) / 2, ( Display
				.getDisplayMode().getHeight() - 300 ) / 2 );
		Display.setDisplayMode( new DisplayMode( 500, 500 ) );
		Display.setTitle( "Intersection" );
		Display.setVSyncEnabled( true );
		Display.create();

		// setup ogl
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();

		float h = ( float ) 300 / ( float ) 300;
		GL11.glFrustum( -1.0f, 1.0f, -h, h, 5.0f, 60.0f );
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		GL11.glLoadIdentity();
		GL11.glTranslatef( 0.0f, 0.0f, -40.0f );

		dp.verts[ 0 ].set( 0, 2, 0 );
		dp.verts[ 1 ].set( 0, 1, 0 );

		for( int i = 0; i < dp.verts.length; i++ )
		{
			dp.verts[ i ].y -= .75f;
		}

		// start random
		for( int i = 0; i < hm.length; i++ )
		{
			for( int j = 0; j < hm.length; j++ )
			{
				hm[ i ][ j ] = ( float ) ( 3 * Math.random() - 1.5 );
			}
		}

		// smooth
		for( int p = 0; p < 10; p++ )
		{
			for( int i = 1; i <= hm.length; i++ )
			{
				for( int j = 1; j <= hm.length; j++ )
				{
					float sum = hm[ i % hm.length ][ j % hm[ 0 ].length ];
					sum += hm[ ( i - 1 ) % hm.length ][ j % hm[ 0 ].length ];
					sum += hm[ ( i + 1 ) % hm.length ][ j % hm[ 0 ].length ];
					sum += hm[ i % hm.length ][ ( j - 1 ) % hm[ 0 ].length ];
					sum += hm[ i % hm.length ][ ( j + 1 ) % hm[ 0 ].length ];

					hm[ i % hm.length ][ j % hm[ 0 ].length ] = sum / 5;
				}
			}
		}

		surf.setTerrain( new Vector2f( -1, -1 ), new Vector2f( 1, 1 ), hm );
	}

	private void tiltMesh( IntersectionVolume iv, float r, int step )
	{
		GL11.glColor3f( 0, 1, 0 );

		Vector4f v = new Vector4f();
		Matrix4f m = new Matrix4f();
		m.rotate( tilt_rotx, new Vector3f( 1, 0, 0 ) );
		m.rotate( tilt_roty, new Vector3f( 0, 1, 0 ) );

		Vector3f norm = new Vector3f();
		Vector3f p = new Vector3f();

		norm.set( 0, 0, 1 );

		v.set( norm.x, norm.y, norm.z );
		Matrix4f.transform( m, v, v );
		norm.set( v );

		norm.normalise();
		for( float i = -r; i < r; i += 2 * r / step )
		{
			p.set( norm );
			p.scale( i + offset );

			draw( iv.findIntersection( p, norm ) );
		}

		GL11.glColor4f( 1, 1, 1, 0.25f );

		for( IntersectionVolume.Face f : iv.faces )
		{
			GL11.glBegin( GL11.GL_LINES );

			for( IntersectionVolume.Edge e : f.adj )
			{
				GL11.glVertex3f( e.p.x, e.p.y, e.p.z );
				GL11.glVertex3f( e.q.x, e.q.y, e.q.z );
			}

			GL11.glEnd();
		}
	}

	private void draw( Vector3f[][] pi )
	{
		for( int i = 0; i < pi.length; i++ )
		{
			GL11.glBegin( GL11.GL_LINE_LOOP );
			for( int j = 0; j < pi[ i ].length; j++ )
			{
				GL11.glVertex3f( pi[ i ][ j ].x, pi[ i ][ j ].y, pi[ i ][ j ].z );
			}
			GL11.glEnd();
		}
	}
}
