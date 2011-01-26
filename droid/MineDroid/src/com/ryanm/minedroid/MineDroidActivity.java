
package com.ryanm.minedroid;

import java.io.File;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.GameActivity;
import com.ryanm.droid.rugl.gl.GLVersion;
import com.ryanm.droid.rugl.res.ResourceLoader;
import com.ryanm.droid.rugl.util.geom.Vector3f;
import com.ryanm.minedroid.nbt.Tag;
import com.ryanm.minedroid.nbt.TagLoader;

/**
 * Entry point for application. Not much happens here, look to
 * {@link BlockView} for actual behaviour
 * 
 * @author ryanm
 */
public class MineDroidActivity extends GameActivity
{
	private ProgressDialog loadDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		File dir = new File( Environment.getExternalStorageDirectory(), ".minecraft/saves" );

		boolean created = dir.mkdirs();

		if( created )
		{
			Toast t =
					Toast.makeText( this,
							"\".minecraft/saves\" directory structure was missing,"
									+ " but has been created. Copy your \"World1\" "
									+ "directory there", Toast.LENGTH_LONG );
			t.show();
			finish();
		}
		else
		{
			final ProgressDialog pd =
					ProgressDialog.show( this, "", "Loading level.dat", true, true,
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel( DialogInterface dialog )
								{
									MineDroidActivity.this.finish();
								}
							} );
			loadDialog = pd;

			final File world1Dir =
					new File( Environment.getExternalStorageDirectory(),
							".minecraft/saves/World1" );

			// It's verboten to do IO on the main event thread, so let's
			// load level.dat using the resourceloader
			TagLoader tl = new TagLoader( new File( world1Dir, "level.dat" ) ) {
				@Override
				public void complete()
				{
					// we're currently in the resourceLoader's processing
					// thread, get back onto the gui thread
					MineDroidActivity.this.runOnUiThread( new Runnable() {
						@Override
						public void run()
						{
							if( resource == null )
							{
								showToast( "Could not load world level.dat, check logcat", true );

								Log.e( Game.RUGL_TAG, "Problem loading level.dat" );

								finish();
							}
							else
							{
								try
								{
									Tag player = resource.findTagByName( "Player" );
									Tag pos = player.findTagByName( "Pos" );

									Tag[] tl = ( com.ryanm.minedroid.nbt.Tag[] ) pos.getValue();
									Vector3f p = new Vector3f();
									p.x = ( ( Double ) tl[ 0 ].getValue() ).floatValue();
									p.y = ( ( Double ) tl[ 1 ].getValue() ).floatValue();
									p.z = ( ( Double ) tl[ 2 ].getValue() ).floatValue();

									World w = new World( world1Dir, p );

									Game game =
											new Game( MineDroidActivity.this,
													GLVersion.OnePointZero, new BlockView( w ) );

									pd.dismiss();

									start( game, "therealryan+minedroid@gmail.com" );
								}
								catch( Exception e )
								{
									showToast(
											"Problem parsing level.dat - Maybe a corrupt file?",
											true );
									Log.e( Game.RUGL_TAG, "Level.dat corrupted?", e );

									finish();
								}
							}
						}
					} );
				}
			};

			tl.selfCompleting = true;

			ResourceLoader.load( tl );
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if( loadDialog != null )
		{
			loadDialog.dismiss();
		}
	}
}