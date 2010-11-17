
package com.rugl.gl.shader;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;

import com.rugl.gl.Facet;
import com.rugl.gl.enums.ShaderType;

/**
 * A GLSL 'Shader Program' consists of a complete rendering pipeline
 * of both vertex and fragment shaders. We need to compile and link
 * vertex and fragment shaders into a single program before we can
 * actually use them for rendering. Note that if only a vertex shader
 * or only a fragment shader is provided then we automagically get the
 * regular fixed-function behaviour for the absent part. Recommended
 * usage: - Create a new class derived from this one for every type of
 * program/effect needed within an app. - Use public-final vars for
 * the used uniform/attribute/sampler bindings (bindings are immutable
 * for this purpose) - In the constructor, load the needed
 * vertex/fragment shaders, link, bind attributes, and finally
 * validate.
 * 
 * @author John Campbell
 */
public abstract class Program extends Facet<Program>
{
	/**
	 * Maps from source file paths to compiled shaders
	 */
	private static Map<String, Shader> vertexShaders = new HashMap<String, Shader>();

	/**
	 * Maps from source file paths to compiled shaders
	 */
	private static Map<String, Shader> fragmentShaders = new HashMap<String, Shader>();

	/**
	 * Maps from concatenated source file paths to compiled programs
	 */
	private static Map<String, Program> programIDs = new HashMap<String, Program>();

	/***/
	public final int programId;

	/***/
	public final Shader vertexShader;

	/***/
	public final Shader fragmentShader;

	/**
	 * @param vsPath
	 *           path to vertex shader source file
	 * @param fsPath
	 *           path to fragment shader source file
	 */
	protected Program( String vsPath, String fsPath )
	{
		vertexShader = loadVertexShader( vsPath );
		fragmentShader = loadFragmentShader( fsPath );

		String concat = vsPath + fsPath;
		if( !programIDs.containsKey( concat ) )
		{
			programId = link( vertexShader, fragmentShader );

			programIDs.put( concat, this );
		}
		else
		{
			programId = programIDs.get( concat ).programId;
		}

		validate();
	}

	private static Shader loadVertexShader( String path )
	{
		if( path != null )
		{
			if( !vertexShaders.containsKey( path ) )
			{
				vertexShaders.put( path, new Shader( ShaderType.Vertex, path ) );
			}

			return vertexShaders.get( path );
		}

		return null;
	}

	private static Shader loadFragmentShader( String path )
	{
		if( path != null )
		{
			if( !fragmentShaders.containsKey( path ) )
			{
				fragmentShaders.put( path, new Shader( ShaderType.Fragment, path ) );
			}

			return fragmentShaders.get( path );
		}

		return null;
	}

	/**
	 * Binds the shader if necessary, also calls {@link #setUniforms()}
	 */
	@Override
	public final void transitionFrom( Program facet )
	{
		if( programId != facet.programId )
		{
			bind();

			setUniforms();
		}
	}

	/**
	 * Implement this to set the shader uniform variables
	 */
	public abstract void setUniforms();

	/**
	 * Compares shader ids, override to compare uniform values also,
	 * but don't forget to call this implementation as well
	 */
	@Override
	public int compareTo( Program o )
	{
		return programId - o.programId;
	}

	@Override
	public String toString()
	{
		return "Program id = " + programId + " VS = " + vertexShader.shaderObjectId
				+ " FS = " + fragmentShader.shaderObjectId;
	}

	/**
	 * Link the specified vertex and fragment shaders together into a
	 * program object. Assumes that the shaders have already been
	 * successfully compiled.
	 */
	private static int link( Shader vertexShader, Shader fragmentShader )
	{
		if( vertexShader == null && fragmentShader == null )
		{ // 'tis the fixed-function pipeline
			return -1;
		}
		else
		{
			// Generate program object
			int programId = GL20.glCreateProgram();

			// Attach vertex and pixel shaders
			if( vertexShader != null )
			{
				GL20.glAttachShader( programId, vertexShader.shaderObjectId );
			}

			if( fragmentShader != null )
			{
				GL20.glAttachShader( programId, fragmentShader.shaderObjectId );
			}

			Program.printProgramLogInfo( programId );

			// Link
			GL20.glLinkProgram( programId );

			Program.printProgramLogInfo( programId );

			return programId;
		}
	}

	/**
	 * Replace the current pipeline (either fixed function or another
	 * program) with this one. Remember that switching shader programs
	 * is one of the most expensive state changes you can do!
	 */
	public void bind()
	{
		GL20.glUseProgram( programId );
	}

	/***/
	public void validate()
	{
		if( programId != -1 )
		{
			GL20.glValidateProgram( programId );

			int vs = GL20.glGetProgram( programId, GL20.GL_VALIDATE_STATUS );

			if( vs <= 0 )
			{
				System.out.println( "Program validation error" );

				Program.printProgramLogInfo( programId );
			}
		}
	}

	/**
	 * @param attributeName
	 * @return a {@link VertexFloatAttribute} for this shader
	 */
	protected VertexFloatAttribute createVertexAttribute( String attributeName )
	{
		int location = GL20.glGetAttribLocation( programId, attributeName );

		return new VertexFloatAttribute( attributeName, location );
	}

	/**
	 * @param uniformName
	 * @return a {@link UniformFloatVariable} for this shader
	 */
	protected UniformFloatVariable createUniformFloatVariable( String uniformName )
	{
		int location = GL20.glGetUniformLocation( programId, uniformName );

		return new UniformFloatVariable( this, uniformName, location );
	}

	/**
	 * @param uniformName
	 * @return a {@link UniformVec2Variable} for this shader
	 */
	protected UniformVec2Variable createUniformVec2Variable( String uniformName )
	{
		int location = GL20.glGetUniformLocation( programId, uniformName );

		return new UniformVec2Variable( this, uniformName, location );
	}

	/**
	 * @param uniformName
	 * @return a {@link UniformVec3Variable} for this shader
	 */
	protected UniformVec3Variable createUniformVec3Variable( String uniformName )
	{
		int location = GL20.glGetUniformLocation( programId, uniformName );

		return new UniformVec3Variable( this, uniformName, location );
	}

	/**
	 * @param uniformName
	 * @return a {@link UniformVec3Variable} for this shader
	 */
	protected UniformVec4Variable createUniformVec4Variable( String uniformName )
	{
		int location = GL20.glGetUniformLocation( programId, uniformName );

		return new UniformVec4Variable( this, uniformName, location );
	}

	/**
	 * @param uniformName
	 * @return a {@link UniformFloatArray} for this shader
	 */
	protected UniformFloatArray createUniformFloatArray( String uniformName )
	{
		int location = GL20.glGetUniformLocation( programId, uniformName );

		int arrSize = GL20.glGetActiveUniformSize( programId, location );
		int arrType = GL20.glGetActiveUniformType( programId, location );

		if( arrType != GL11.GL_FLOAT )
		{
			throw new OpenGLException(
					"createUniformFloat array with a name for a non-float array" );
		}

		return new UniformFloatArray( this, uniformName, location, arrSize );
	}

	/**
	 * @param samplerName
	 * @return a {@link UniformSampler2D} for this shader
	 */
	protected UniformSampler2D createUniformSampler2D( String samplerName )
	{
		int location = GL20.glGetUniformLocation( programId, samplerName );

		return new UniformSampler2D( this, samplerName, location );
	}

	/**
	 * Prints log information for the specified shader object. Log
	 * information is highly vendor and driver dependent, so don't go
	 * relying on any given format from output. Log info is mainly
	 * useful for development and debugging because it shows output and
	 * errors from shader compilation and linking.
	 * 
	 * @param obj
	 */
	private static void printProgramLogInfo( int obj )
	{
		int ill = GL20.glGetProgram( obj, GL20.GL_INFO_LOG_LENGTH );

		if( ill > 0 )
		{
			System.out.println( "Info log:\n" + GL20.glGetProgramInfoLog( obj, ill ) );
		}

		Util.checkGLError();
	}
}
