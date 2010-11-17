
package com.rugl.gl.shader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Util;

import com.rugl.gl.enums.ShaderType;

/**
 * A GLSL fragment shader replaces the traditional fixed-function
 * fragment pipeline. Fragment shaders are loaded from a source file
 * and compiled by the graphics card driver when created. In order to
 * use the result then need to be added and linked into a valid
 * ShaderProgram.
 * 
 * @author John Campbell
 */
public class Shader
{
	/***/
	public final int shaderObjectId;

	/**
	 * Create a shader object from a source file. Shader source is
	 * automatically loaded in and compiled, displaying any errors to
	 * the console.
	 * 
	 * @param type
	 * @param sourcePath
	 */
	public Shader( ShaderType type, String sourcePath )
	{
		// Generate shader object
		shaderObjectId = GL20.glCreateShader( type.glFlag );

		// Load in shader source
		GL20.glShaderSource( shaderObjectId, getProgramCode( sourcePath ) );

		// Compile
		GL20.glCompileShader( shaderObjectId );

		printShaderLogInfo( shaderObjectId );
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
	private static void printShaderLogInfo( int obj )
	{
		int ill = GL20.glGetShader( obj, GL20.GL_INFO_LOG_LENGTH );

		if( ill > 0 )
		{
			System.out.println( "Info log:\n" + GL20.glGetShaderInfoLog( obj, ill ) );
		}

		Util.checkGLError();
	}

	/**
	 * Loads shader code from a text file specified. The source is
	 * shoved into a byte buffer that is suitable for passing to GLSL
	 * for compilation. Based on some code in JCD's LWJGL framework.
	 * 
	 * @param filename
	 * @return A buffer containing the code, or null if the program was
	 *         not found
	 */
	private static String getProgramCode( String filename )
	{
		try
		{
			ClassLoader fileLoader = Thread.currentThread().getContextClassLoader();
			InputStream is = fileLoader.getResourceAsStream( filename );
			BufferedReader br = null;

			if( is != null )
			{
				br = new BufferedReader( new InputStreamReader( is ) );
			}
			else
			{
				br = new BufferedReader( new FileReader( filename ) );
			}

			StringBuilder buff = new StringBuilder();
			String line;
			while( ( line = br.readLine() ) != null )
			{
				buff.append( line ).append( "\n" );
			}

			return buff.toString();
		}
		catch( Exception e )
		{
			System.out.println( e.getMessage() );
		}

		return null;
	}
}
