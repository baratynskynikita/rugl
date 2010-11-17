
package com.rugl.gl;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.rugl.texture.Image;
import com.rugl.texture.Image.Format;
import com.rugl.texture.Texture;
import com.rugl.texture.TextureFactory;
import com.rugl.util.GLUtil;

/**
 * For render-to-texture
 * 
 * @author ryanm
 */
public class FrameBufferObject
{
	/**
	 * The ID of this FBO
	 */
	public final int fbo;

	/**
	 * The texture that is rendered to
	 */
	public final Texture texture;

	/**
	 * @param width
	 * @param height
	 */
	public FrameBufferObject( int width, int height )
	{
		assert available();

		fbo = EXTFramebufferObject.glGenFramebuffersEXT();
		assert fbo > 0;

		ByteBuffer data = BufferUtils.createByteBuffer( width * height * 4 );
		Image texImage = new Image( width, height, Format.RGBA, data );

		texture = TextureFactory.buildTexture( texImage, true, false );

		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				fbo );
		EXTFramebufferObject.glFramebufferTexture2DEXT(
				EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D,
				texture.getTextureID(), 0 );

		completenessCheck();

		// unbind
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				0 );

		GLUtil.checkGLError();
	}

	private void completenessCheck()
	{
		int framebuffer =
				EXTFramebufferObject
						.glCheckFramebufferStatusEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT );
		switch( framebuffer )
		{
			case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
				break;
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
				throw new RuntimeException( "FrameBuffer: " + fbo
						+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception" );
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
				throw new RuntimeException(
						"FrameBuffer: "
								+ fbo
								+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception" );
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
				throw new RuntimeException( "FrameBuffer: " + fbo
						+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception" );
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
				throw new RuntimeException( "FrameBuffer: " + fbo
						+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception" );
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
				throw new RuntimeException( "FrameBuffer: " + fbo
						+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception" );
			case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
				throw new RuntimeException( "FrameBuffer: " + fbo
						+ ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception" );
			default:
				throw new RuntimeException(
						"Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer );
		}
	}

	/**
	 * Checks to see if FBOs are available
	 * 
	 * @return <code>true</code> if available, <code>false</code>
	 *         otherwise
	 */
	public static boolean available()
	{
		return GLContext.getCapabilities().GL_EXT_framebuffer_object;
	}

	/**
	 * Binds the fbo for rendering
	 */
	public void bind()
	{
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				fbo );
		GL11.glPushAttrib( GL11.GL_VIEWPORT_BIT );
		GL11.glViewport( texture.getXPosition(), texture.getYPosition(),
				texture.getWidth(), texture.getHeight() );
	}

	/**
	 * Binds the screen for rendering
	 */
	public void unBind()
	{
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
				0 );
		GL11.glPopAttrib();
	}

	/**
	 * Deletes the framebuffer
	 */
	public void destroy()
	{
		EXTFramebufferObject.glDeleteFramebuffersEXT( fbo );
		TextureFactory.deleteTexture( texture );
	}

	@Override
	public String toString()
	{
		return "FBO " + fbo + " " + texture.toString();
	}

}
