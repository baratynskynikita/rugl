
package com.ryanm.minedroid;

import com.ryanm.droid.rugl.geom.ColouredShape;
import com.ryanm.droid.rugl.geom.Shape;
import com.ryanm.droid.rugl.geom.WireUtil;
import com.ryanm.droid.rugl.gl.Renderer;
import com.ryanm.droid.rugl.gl.VBOShape;
import com.ryanm.droid.rugl.util.Colour;
import com.ryanm.droid.rugl.util.geom.Frustum;
import com.ryanm.droid.rugl.util.geom.Frustum.Result;

/**
 * A 16 * 16 * 16 cube of a {@link Chunk}
 * 
 * @author ryanm
 */
public class Chunklet
{
	/**
	 * Parent chunk
	 */
	public final Chunk parent;

	/**
	 * World coordinate
	 */
	public final int x;

	/**
	 * World coordinate
	 */
	public final int y;

	/**
	 * World coordinate
	 */
	public final int z;

	private ColouredShape outline = null;

	private boolean geomDirty = true;

	/**
	 * Solid geometry
	 */
	private VBOShape solidVBO;

	/**
	 * This is where we hold a new solid geometry vbo, fresh from the
	 * generation thread
	 */
	private VBOShape pendingSolid;

	/**
	 * Transparent geometry
	 */
	private VBOShape transparentVBO;

	/**
	 * This is where we hold a new transparent geometry vbo, fresh from
	 * the generation thread
	 */
	private VBOShape pendingTransparent;

	/**
	 * <code>true</code> if we're waiting on being processed by the
	 * geometry-generating thread
	 */
	boolean geomPending = false;

	/**
	 * <code>true</code> if the north side of this chunklet is
	 * completely opaque
	 */
	public boolean northSheet = true;

	/**
	 * <code>true</code> if the bottom side of this chunklet is
	 * completely opaque
	 */
	public boolean bottomSheet = true;

	/**
	 * <code>true</code> if the east side of this chunklet is
	 * completely opaque
	 */
	public boolean eastSheet = true;

	/**
	 * <code>true</code> if the south side of this chunklet is
	 * completely opaque
	 */
	public boolean southSheet = true;

	/**
	 * <code>true</code> if the top side of this chunklet is completely
	 * opaque
	 */
	public boolean topSheet = true;

	/**
	 * <code>true</code> if the west side of this chunklet is
	 * completely opaque
	 */
	public boolean westSheet = true;

	private boolean empty = true;

	private boolean boundariesEmptyChecked = false;

	/**
	 * Stops us revisiting this chunklet when we flood-fill the view
	 * frustum to find which chunklets to render
	 */
	public int drawFlag = 0;

	/**
	 * @param parent
	 * @param y
	 *           in chunk index coordinates
	 */
	public Chunklet( Chunk parent, int y )
	{
		this.parent = parent;
		x = parent.chunkX * 16;
		this.y = y * 16;
		z = parent.chunkZ * 16;

		// find sheets
		for( int i = 0; i < 16; i++ )
		{
			for( int j = 0; j < 16; j++ )
			{
				byte bt = blockType( 0, i, j );
				northSheet &= BlockFactory.opaque( bt );

				bt = blockType( 15, i, j );
				southSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, j, 0 );
				eastSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, j, 15 );
				westSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, 15, j );
				topSheet &= BlockFactory.opaque( bt );

				bt = blockType( i, 0, j );
				bottomSheet &= BlockFactory.opaque( bt );
			}
		}

		// empty check
		empty =
				!( northSheet || southSheet || eastSheet || westSheet || topSheet || bottomSheet );
		for( int x = 0; x < 16 && empty; x++ )
		{
			for( int z = 0; z < 16 && empty; z++ )
			{
				for( int k = 0; k < 16 && empty; k++ )
				{
					empty &= blockType( x, k, z ) == 0;
				}
			}
		}
	}

	/**
	 * @return <code>true</code> if there is no solid geometry in this
	 *         chunklet
	 */
	public boolean isEmpty()
	{
		return empty;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The distance from the center of this chunklet to the
	 *         point
	 */
	public float distanceSq( float x, float y, float z )
	{
		float dx = this.x + 8f - x;
		float dy = this.y + 8f - y;
		float dz = this.z + 8f - z;

		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Call this to refresh the chunklet's geometry the next time it is
	 * rendered
	 */
	public void geomDirty()
	{
		geomDirty = true;
		boundariesEmptyChecked = false;
	}

	/**
	 * Draws the solid geometry
	 */
	public void drawSolid()
	{
		generateGeometry();

		if( pendingSolid != null )
		{
			if( solidVBO != null )
			{
				solidVBO.delete();
			}
			solidVBO = pendingSolid;
			pendingSolid = null;
		}

		if( solidVBO != null )
		{
			solidVBO.state = BlockFactory.state;
			solidVBO.draw();
		}
	}

	/**
	 * Draws the transparent geometry
	 */
	public void drawTransparent()
	{
		generateGeometry();

		if( pendingTransparent != null )
		{
			if( transparentVBO != null )
			{
				transparentVBO.delete();
			}
			transparentVBO = pendingTransparent;
			pendingTransparent = null;
		}

		if( transparentVBO != null )
		{
			transparentVBO.state = BlockFactory.state;
			transparentVBO.draw();
		}
	}

	/**
	 * 
	 */
	public void generateGeometry()
	{
		if( empty && !boundariesEmptyChecked )
		{
			// need to check the sides of neighbouring blocks too
			for( int i = 0; i < 16 && empty; i++ )
			{
				for( int j = 0; j < 16 && empty; j++ )
				{
					empty &= blockType( -1, i, j ) == 0;
					empty &= blockType( 16, i, j ) == 0;

					empty &= blockType( i, -1, j ) == 0;
					empty &= blockType( i, 16, j ) == 0;

					empty &= blockType( i, j, -1 ) == 0;
					empty &= blockType( i, j, 16 ) == 0;
				}
			}
			boundariesEmptyChecked = true;
		}

		if( !empty && geomDirty && !geomPending )
		{
			geomPending = true;
			GeometryGenerator.generate( this );
		}
	}

	/**
	 * @param solid
	 * @param transparent
	 */
	public void geometryComplete( VBOShape solid, VBOShape transparent )
	{
		geomPending = false;
		geomDirty = false;
		pendingSolid = solid;
		pendingTransparent = transparent;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return the so-indexed block
	 */
	public byte blockType( int x, int y, int z )
	{
		return parent.blockType( x, this.y + y, z );
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return The light value of the so-indexed block
	 */
	public float light( int x, int y, int z )
	{
		int sl = parent.skyLight( x, this.y + y, z );
		int bl = parent.blockLight( x, this.y + y, z );
		int l = Math.max( sl, bl );
		return ( float ) Math.pow( 0.8, 15 - l );
	}

	/**
	 * @param frustum
	 * @return The intersection status of the chunk and frustum
	 */
	public Result intersection( Frustum frustum )
	{
		return frustum.cuboidIntersects( x, y, z, x + 16, y + 16, z + 16 );
	}

	@Override
	public String toString()
	{
		return "Chunklet @ " + x + ", " + y + ", " + z + "\nsheets n " + northSheet + " s "
				+ southSheet + "\n e " + eastSheet + " w " + westSheet + "\n t " + topSheet
				+ " b " + bottomSheet;
	}

	/**
	 * Draws wireframe outline
	 * 
	 * @param r
	 */
	public void drawOutline( Renderer r )
	{
		if( solidVBO != null || transparentVBO != null || geomPending )
		{
			if( outline == null )
			{
				Shape s = WireUtil.unitCube();
				s.scale( 15.5f, 15.5f, 15.5f );
				s.translate( 0.25f, 0.25f, 0.25f );
				s.translate( x, y, z );

				outline = new ColouredShape( s, Colour.black, WireUtil.state );
			}

			outline.render( r );
		}
	}

	/**
	 * Deletes VBOs
	 */
	public void unload()
	{
		if( solidVBO != null )
		{
			solidVBO.delete();
		}

		if( transparentVBO != null )
		{
			transparentVBO.delete();
		}
	}
}
