
package com.rugl.test;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;

import com.rugl.Game;
import com.rugl.GameBox;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.Shape;
import com.rugl.geom.ShapeUtil;
import com.rugl.gl.State;
import com.rugl.gl.enums.RasterMode;
import com.rugl.gl.facets.PolygonMode;
import com.rugl.input.KeyPress;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;
import com.ryanm.config.imp.ConfigurableType;
import com.ryanm.config.imp.Variable;
import com.ryanm.util.math.Trig;

/**
 * @author ryanm
 */
@ConfigurableType( "GeomTest" )
public class GeomTest implements Game
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		GameBox.startGame( new GeomTest(), null );
	}

	private StackedRenderer r = new StackedRenderer();

	private ColouredShape[] shapes;

	private Shape[] sa =
			new Shape[] {
					ShapeUtil.logSpiral( 1f, 0.2f, Trig.toRadians( 720 ),
							Trig.toRadians( 10 ), 1f ),
					ShapeUtil.goldenSpiral( Trig.toRadians( 720 ), Trig.toRadians( 10 ), 1f ),
					ShapeUtil.chipIcon( 8, 0.1f, 0.02f, 0.025f ),
					ShapeUtil.filledQuad( 0, 0, 1, 1, 0 ),
					ShapeUtil.outerQuad( 0, 0, 1, 1, .1f, 0 ), ShapeUtil.cross( 0.3f ),
					ShapeUtil.innerCircle( 0, 0, .5f, .1f, .1f, 0 ),
					ShapeUtil.filledCenteredCircle( 0, 0, .5f, .1f, 0 ),
					ShapeUtil.filledCircle( 0, 0, .5f, .1f, 0 ),
					ShapeUtil.arrow( 0, 0, 1, 1, 0.25f, 0.5f ),
					ShapeUtil.outline( 0.1f, 0.5f, 0, 0, 0, 1, 1, 1, 1, 0 ),
					ShapeUtil.line( 0.1f, 0, 0, 0.5f, 0, 0.5f, 1, 1, 1 ) };

	private KeyPress space = new KeyPress( true, Keyboard.KEY_SPACE );

	private KeyPress print = new KeyPress( true, Keyboard.KEY_P );

	private int mode = 0;

	/***/
	@Variable( "Point separation" )
	public float separation = 10;

	private float basex = 0.13412f;

	private float basey = 0.11623f;

	/***/
	public GeomTest()
	{
		GameBox.dispConf.setResolution( new Dimension( 800, 600 ) );
		GameBox.dispConf.setFullScreen( false );

		Vector2f v = new Vector2f( 10, 10 );
		float maxY = 10;
		float sep = 10;

		sa = new Shape[ 0 ];
		for( int i = 0; i < sa.length; i++ )
		{
			float max =
					Math.max( sa[ i ].getBounds().getHeight(), sa[ i ].getBounds().getWidth() );

			sa[ i ].translate( -sa[ i ].getBounds().x.getMin(),
					-sa[ i ].getBounds().y.getMin(), 0 );

			sa[ i ].scale( 150 / max, 150 / max, 1 );

			// new line?
			if( v.x + sa[ i ].getBounds().getWidth() > 800 )
			{
				v.x = sep;
				v.y = maxY + sep;
			}

			sa[ i ].translate( v.x, v.y, 0 );

			// advance
			v.x = sa[ i ].getBounds().x.getMax() + sep;
			maxY = Math.max( maxY, sa[ i ].getBounds().y.getMax() );
		}

		shapes = new ColouredShape[ sa.length ];
		for( int i = 0; i < sa.length; i++ )
		{
			shapes[ i ] = new ColouredShape( sa[ i ], Colour.white, null );
		}
	}

	@Override
	public String getName()
	{
		return GeomTest.class.getAnnotation( ConfigurableType.class ).value();
	}

	@Override
	public void advance( float delta )
	{
		if( space.isActive() )
		{
			mode++;
			mode %= 4;
		}

		if( print.isActive() )
		{
			for( int i = 0; i < shapes.length; i++ )
			{
				System.out.println( sa[ i ] );
				System.out.println();
			}
		}

		if( Keyboard.isKeyDown( Keyboard.KEY_Q ) )
		{
			separation += 1;
		}
		if( Keyboard.isKeyDown( Keyboard.KEY_A ) )
		{
			separation -= 1;
		}

		separation = Math.max( separation, 5 );

		if( Keyboard.isKeyDown( Keyboard.KEY_UP ) )
		{
			basey += 1;
		}
		if( Keyboard.isKeyDown( Keyboard.KEY_DOWN ) )
		{
			basey -= 1;
		}
		if( Keyboard.isKeyDown( Keyboard.KEY_LEFT ) )
		{
			basex -= 1;
		}
		if( Keyboard.isKeyDown( Keyboard.KEY_RIGHT ) )
		{
			basex += 1;
		}

		basex += separation;
		basex %= separation;
		basey += separation;
		basey %= separation;
	}

	@Override
	public void draw()
	{
		RasterMode front;
		RasterMode back;
		switch( mode )
		{
			case 0:
				front = RasterMode.FILL;
				back = RasterMode.LINE;
				break;
			case 1:
				front = RasterMode.LINE;
				back = RasterMode.FILL;
				break;
			case 2:
				front = RasterMode.POINT;
				back = front;
				break;
			default:
				front = RasterMode.FILL;
				back = RasterMode.FILL;
				break;
		}

		State s = r.intern( GLUtil.typicalState.with( new PolygonMode( front, back ) ) );

		for( int i = 0; i < shapes.length; i++ )
		{
			shapes[ i ].state = s;
		}

		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
		GLUtil.scaledOrtho( 800, 600 );

		if( mode == 3 )
		{
			GL11.glColor3f( 0, 1, 0 );
			GL11.glBegin( GL11.GL_POINTS );

			for( float i = basex; i < 800; i += separation )
			{
				for( float j = basey; j < 800; j += separation )
				{
					if( hits( i, j ) )
					{
						GL11.glVertex2f( i, j );
					}
				}
			}

			GL11.glEnd();
		}
		else
		{
			for( int i = 0; i < shapes.length; i++ )
			{
				shapes[ i ].render( r );
			}
		}

		r.render();
	}

	private boolean hits( float x, float y )
	{
		for( int i = 0; i < sa.length; i++ )
		{
			if( shapes[ i ].contains( x, y ) )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void exit()
	{

	}

	@Override
	public void loadResources()
	{
	}

	@Override
	public void resourcesLoaded()
	{
	}

}