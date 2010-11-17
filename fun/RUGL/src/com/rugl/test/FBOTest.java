
package com.rugl.test;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;

import com.rugl.Game;
import com.rugl.GameBox;
import com.rugl.console.Console;
import com.rugl.geom.ColouredShape;
import com.rugl.geom.ShapeUtil;
import com.rugl.geom.TexturedShape;
import com.rugl.gl.FrameBufferObject;
import com.rugl.gl.enums.MagFilter;
import com.rugl.gl.enums.MinFilter;
import com.rugl.gl.facets.TextureState.Filters;
import com.rugl.renderer.RenderUtils;
import com.rugl.renderer.StackedRenderer;
import com.rugl.util.Colour;
import com.rugl.util.GLUtil;
import com.ryanm.config.imp.ConfigurableType;

/**
 * 
 */
@ConfigurableType( "FBO test" )
public class FBOTest implements Game
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		System.setProperty( "LWJGL_DISABLE_XRANDR", "true" );
		GameBox.startGame( new FBOTest(), null );
	}

	private FrameBufferObject a, b;

	private StackedRenderer r = new StackedRenderer();

	private FBOTest()
	{
		GameBox.dispConf.setResolution( new Dimension( 800, 600 ) );
		GameBox.dispConf.setFullScreen( false );
	}

	@Override
	public void advance( float delta )
	{

	}

	@Override
	public void draw()
	{
		GLUtil.scaledOrtho( 800, 600 );
		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

		if( a != null )
		{
			TexturedShape aShape =
					new TexturedShape( new ColouredShape( ShapeUtil.filledQuad( 10, 110, 110,
							210, 0 ), Colour.white, null ), RenderUtils.getQuadTexCoords( 1 ),
							a.texture );
			aShape.state =
					aShape.state.with( aShape.state.texture.with( new Filters(
							MinFilter.LINEAR, MagFilter.LINEAR ) ) );
			aShape.render( r );
		}

		if( b != null )
		{
			TexturedShape bShape =
					new TexturedShape( new ColouredShape( ShapeUtil.filledQuad( 120, 110, 220,
							210, 0 ), Colour.white, null ), RenderUtils.getQuadTexCoords( 1 ),
							b.texture );
			bShape.state =
					bShape.state.with( bShape.state.texture.with( new Filters(
							MinFilter.LINEAR, MagFilter.LINEAR ) ) );
			bShape.render( r );
		}

		r.render();

		GLUtil.typicalState.apply();
		drawSquare( 0, 0, 1 );
	}

	private void drawSquare( float r, float g, float b )
	{
		GL11.glColor3f( r, g, b );
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glVertex2i( 10, 10 );
		GL11.glVertex2i( 90, 10 );
		GL11.glVertex2i( 90, 90 );
		GL11.glVertex2i( 10, 90 );
		GL11.glEnd();
	}

	@Override
	public void exit()
	{
	}

	@Override
	public String getName()
	{
		return "FBOTest";
	}

	@Override
	public void loadResources()
	{
	}

	@Override
	public void resourcesLoaded()
	{
		GLUtil.scaledOrtho( 800, 600 );

		a = new FrameBufferObject( 100, 100 );
		a.bind();
		drawSquare( 1, 0, 0 );
		a.unBind();

		Console.log( "FBO created " + a );

		b = new FrameBufferObject( 100, 100 );
		b.bind();
		drawSquare( 0, 1, 0 );
		b.unBind();

		Console.log( "FBO created " + b );
	}

}
