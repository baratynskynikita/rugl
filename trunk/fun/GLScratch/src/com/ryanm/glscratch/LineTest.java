
package com.ryanm.glscratch;

import java.util.LinkedList;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.line.BevelJoin;
import com.rugl.geom.line.Line;
import com.rugl.geom.line.MiterDecoration;
import com.rugl.geom.line.RoundDecoration;
import com.rugl.geom.line.SquareCap;
import com.rugl.gl.State;
import com.rugl.gl.enums.RasterMode;
import com.rugl.gl.facets.PolygonMode;
import com.rugl.input.KeyPress;
import com.rugl.renderer.Renderer;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;

/**
 * @author ryanm
 */
public class LineTest
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		new LineTest().execute();
	}

	private int sizeLevel = 1;

	private boolean wireframe = true;

	private Line line = new Line();

	private RoundDecoration round = new RoundDecoration();

	private MiterDecoration pointy = new MiterDecoration();

	private BevelJoin bevel = new BevelJoin();

	private SquareCap square = new SquareCap();

	private LinkedList<Vector2f> points = new LinkedList<Vector2f>();

	private Vector2f toAdd = new Vector2f();

	private Renderer r = new Renderer();

	private State fillState = GLUtil.typicalState.with( new PolygonMode( RasterMode.FILL,
			RasterMode.FILL ) );

	private State lineState = GLUtil.typicalState.with( new PolygonMode( RasterMode.LINE,
			RasterMode.FILL ) );

	private boolean loop = false;

	private KeyPress print = new KeyPress( true, Keyboard.KEY_P );

	private void execute()
	{
		try
		{
			init();

			while( !Display.isCloseRequested() )
			{
				loop();
			}
		}
		catch( LWJGLException e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	private void init() throws LWJGLException
	{
		Display.setLocation( ( Display.getDisplayMode().getWidth() - 300 ) / 2, ( Display
				.getDisplayMode().getHeight() - 300 ) / 2 );
		Display.setDisplayMode( new DisplayMode( 500, 500 ) );
		Display.setTitle( "Line Test" );
		Display.setVSyncEnabled( true );
		Display.create();

		// setup ogl
		GLUtil.typicalState.apply();

		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		GLU.gluOrtho2D( 0, Display.getDisplayMode().getWidth(), 0, Display.getDisplayMode()
				.getHeight() );
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		GL11.glLoadIdentity();
		GL11.glViewport( 0, 0, Display.getDisplayMode().getWidth(), Display
				.getDisplayMode().getHeight() );
		GL11.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );

		line.cap = round;
		line.join = round;

		AL.create();
		AL.destroy();
	}

	private void loop()
	{
		// read keys
		while( Keyboard.next() )
		{
			switch( Keyboard.getEventKey() )
			{
				case Keyboard.KEY_Q:
					sizeLevel += Keyboard.getEventKeyState() ? 1 : 0;
					break;
				case Keyboard.KEY_A:
					sizeLevel += Keyboard.getEventKeyState() ? -1 : 0;
					break;
				case Keyboard.KEY_W:
					wireframe = Keyboard.getEventKeyState() ? !wireframe : wireframe;
					break;
				case Keyboard.KEY_L:
					loop = Keyboard.getEventKeyState() ? !loop : loop;
					break;
				case Keyboard.KEY_1:
					line.cap = round;
					line.join = round;
					break;
				case Keyboard.KEY_2:
					line.cap = pointy;
					line.join = pointy;
					break;
				case Keyboard.KEY_3:
					line.cap = square;
					line.join = bevel;
					break;
				case Keyboard.KEY_4:
					line.cap = null;
					line.join = null;
					break;
				default:
					break;
			}
		}

		while( Mouse.next() )
		{
			if( Mouse.getEventButton() == 0 && Mouse.getEventButtonState() )
			{
				points.add( toAdd );
				toAdd = new Vector2f();
			}
			else if( Mouse.getEventButton() == 1 && Mouse.getEventButtonState() )
			{
				if( !points.isEmpty() )
				{
					points.removeLast();
				}
			}
		}

		// get mouse coords
		toAdd.set( Mouse.getX(), Mouse.getY() );

		// GL11.glClearColor( 0.5f, 0.5f, 0.5f, 1 );
		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

		// construct line
		line.width = ( float ) Math.pow( 2, sizeLevel );

		State s = wireframe ? lineState : fillState;

		for( Vector2f p : points )
		{
			line.addPoint( p );
		}

		if( points.size() > 0
				&& ( toAdd.x != points.getLast().x || toAdd.y != points.getLast().y ) )
		{
			line.addPoint( toAdd );
		}

		if( line.pointCount() >= 2 )
		{
			// draw
			Shape shape = loop ? line.buildLoop( 0 ) : line.buildLine( 0 );

			if( print.isActive() )
			{
				System.out.println( shape );
			}

			ColouredShape cs =
					new ColouredShape( shape, Colour.packInt( 255, 255, 255, 128 ), s );
			cs.render( r );
			r.render();
		}
		else
		{
			line.clear();
		}

		GL11.glColor3f( 0, 1, 0 );
		GL11.glBegin( loop ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP );

		for( Vector2f p : points )
		{
			GL11.glVertex3f( p.x, p.y, 1 );
		}

		GL11.glVertex3f( toAdd.x, toAdd.y, 1 );

		GL11.glEnd();

		Display.update();

		GLUtil.checkGLError();
	}
}
