
package com.ryanm.minedroid;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glClear;
import android.opengl.GLES10;
import android.view.KeyEvent;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.Phase;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.FPSCamera;
import com.ryanm.minedroid.ItemFactory.Item;
import com.ryanm.minedroid.ui.GUI;
import com.ryanm.preflect.annote.Summary;
import com.ryanm.preflect.annote.Variable;
import com.ryanm.preflect.annote.WidgetHint;

/**
 * @author ryanm
 */
@Variable
@Summary( "Explore minecraft worlds" )
public class BlockView extends Phase
{
	/***/
	@Variable
	public final Player player;

	/***/
	@Variable
	public GUI gui;

	/***/
	@Variable
	public FPSCamera cam = new FPSCamera();

	/***/
	@Variable( "Sky colour" )
	@WidgetHint( Colour.class )
	public int skyColour = Colour.packFloat( 0.7f, 0.7f, 0.9f, 1 );

	/***/
	@Variable
	public final World world;

	private Game game;

	private boolean defaultConfigLoaded = false;

	/**
	 * @param world
	 */
	public BlockView( World world )
	{
		this.world = world;
		player = new Player( world );
	}

	@Override
	public void init( Game game )
	{
		Game.setConfigurationRoots( game, this );
		Game.logicAdvance = 1.0f / 60;

		this.game = game;

		cam.far = 80;

		if( gui == null )
		{
			gui = new GUI( player, world, cam );
		}

		BlockFactory.loadTexture();
		ItemFactory.loadTexture();

		int i = 0;
		player.hotbar[ i++ ] = Item.DiamondPick;
		player.hotbar[ i++ ] = Item.DiamondShovel;
		player.hotbar[ i++ ] = Item.DiamondSword;
		player.hotbar[ i++ ] = Item.DiamondAxe;
		player.hotbar[ i++ ] = Item.Dirt;
		player.hotbar[ i++ ] = Item.Cobble;
		player.hotbar[ i++ ] = Item.Log;
		player.hotbar[ i++ ] = Item.Wood;
		player.hotbar[ i++ ] = Item.Glass;
	}

	@Override
	public void openGLinit()
	{
		GLES10.glClearColor( Colour.redf( skyColour ), Colour.greenf( skyColour ),
				Colour.bluef( skyColour ), Colour.alphaf( skyColour ) );
	}

	@Override
	public void advance( float delta )
	{

		gui.advance( delta );

		cam.advance( delta, gui.right.x, gui.right.y );

		player.advance( delta, cam, gui );

		world.advance( player.position.x, player.position.z );
	}

	@Override
	public void draw()
	{
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		cam.setPosition( player.position.x, player.position.y, player.position.z );

		world.draw( player.position, cam.getFrustum() );

		gui.draw();

		if( !defaultConfigLoaded )
		{ // have to wait to do this now to make sure all objects are
			// there
			game.loadConfiguration( "default" );
			defaultConfigLoaded = true;
		}
	}

	@Override
	public Phase next()
	{
		return null;
	}

	@Override
	public void onKeyDown( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			complete = true;
		}
		else if( keyCode == KeyEvent.KEYCODE_MENU )
		{
			game.launchConfiguration();
		}
	}
}
