
package com.ryanm.minedroid.chunk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ryanm.droid.rugl.Game;
import com.ryanm.droid.rugl.geom.CompiledShape;
import com.ryanm.droid.rugl.geom.ShapeBuilder;
import com.ryanm.droid.rugl.geom.TexturedShape;
import com.ryanm.droid.rugl.gl.GLVersion;
import com.ryanm.droid.rugl.gl.VBOShape;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.minedroid.BlockFactory;
import com.ryanm.minedroid.BlockFactory.Block;
import com.ryanm.minedroid.BlockFactory.Face;

/**
 * @author ryanm
 */
public class GeometryGenerator
{
	private static ShapeBuilder opaqueVBOBuilder = new ShapeBuilder();

	private static ShapeBuilder transVBOBuilder = new ShapeBuilder();

	/**
	 * The service where we generate geometry. Note that there's only
	 * one worker thread: if you want more, you're going to have to use
	 * separate {@link ShapeBuilder}s for each rather than the static
	 * ones above in {@link #opaqueVBOBuilder} and
	 * {@link #transVBOBuilder}
	 */
	private static ExecutorService geomGenService = Executors.newSingleThreadExecutor();

	private static int queueSize = 0;

	/**
	 * @return The number of chunklets awaiting geometry generation
	 */
	public static int getChunkletQueueSize()
	{
		return queueSize;
	}

	/**
	 * Generates geometry for a {@link Chunklet}
	 * 
	 * @param c
	 * @param synchronous
	 */
	public static void generate( final Chunklet c, boolean synchronous )
	{
		Runnable r = new Runnable() {
			@Override
			public void run()
			{
				// not sure why this is needed, but it is
				opaqueVBOBuilder.clear();
				transVBOBuilder.clear();

				for( int xi = 0; xi < 16; xi++ )
				{
					for( int yi = 0; yi < 16; yi++ )
					{
						for( int zi = 0; zi < 16; zi++ )
						{
							Block b = BlockFactory.getBlock( c.blockType( xi, yi, zi ) );

							float light = c.light( xi, yi, zi );

							if( b == Block.HalfBlock )
							{ // wow, so now I know why Markus doesn't like
								// the half-blocks
								light = c.light( xi, yi + 1, zi );
							}

							int colour = Colour.packFloat( light, light, light, 1 );

							if( b == null || !b.opaque )
							{
								addFace( c, b, xi - 1, yi, zi, Face.South, colour,
										opaqueVBOBuilder, transVBOBuilder );
								addFace( c, b, xi + 1, yi, zi, Face.North, colour,
										opaqueVBOBuilder, transVBOBuilder );
								addFace( c, b, xi, yi, zi - 1, Face.West, colour,
										opaqueVBOBuilder, transVBOBuilder );
								addFace( c, b, xi, yi, zi + 1, Face.East, colour,
										opaqueVBOBuilder, transVBOBuilder );
								addFace( c, b, xi, yi + 1, zi, Face.Bottom, colour,
										opaqueVBOBuilder, transVBOBuilder );
								addFace( c, b, xi, yi - 1, zi, Face.Top, colour,
										opaqueVBOBuilder, transVBOBuilder );
							}
						}
					}
				}

				TexturedShape s = opaqueVBOBuilder.compile();
				if( s != null )
				{
					s.state = BlockFactory.state;
					s.translate( c.x, c.y, c.z );
				}
				TexturedShape t = transVBOBuilder.compile();
				if( t != null )
				{
					t.state = BlockFactory.state;
					t.translate( c.x, c.y, c.z );
				}

				if( Game.glVersion == GLVersion.OnePointOne )
				{
					VBOShape solid = null;
					if( s != null )
					{
						solid = new VBOShape( s );
					}

					VBOShape transparent = null;
					if( t != null )
					{
						transparent = new VBOShape( t );
					}

					c.geometryComplete( solid, transparent );
				}
				else
				{
					CompiledShape solid = null;
					if( s != null )
					{
						solid = new CompiledShape( s );
					}

					CompiledShape transparent = null;
					if( t != null )
					{
						transparent = new CompiledShape( t );
					}

					c.geometryComplete( solid, transparent );
				}

				queueSize--;
			}
		};

		queueSize++;
		if( synchronous )
		{
			r.run();
		}
		else
		{
			geomGenService.submit( r );
		}
	}

	private static void addFace( Chunklet c, Block facing, int x, int y, int z, Face f,
			int colour, ShapeBuilder opaque, ShapeBuilder transparent )
	{
		Block b = BlockFactory.getBlock( c.blockType( x, y, z ) );

		if( b != null && b != facing )
		{
			b.face( f, x, y, z, colour, b.opaque ? opaque : transparent );
		}
	}
}
