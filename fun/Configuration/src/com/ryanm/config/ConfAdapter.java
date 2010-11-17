
package com.ryanm.config;

/**
 * Empty implementation to make inner classes more convenient
 * 
 * @author ryanm
 */
public class ConfAdapter implements ConfiguratorListener, ValueListener
{

	@Override
	public void configuratorDescribed( String description )
	{
	}

	@Override
	public void variableAdded( Object variable )
	{
	}

	@Override
	public void variableDescribed( String variable )
	{
	}

	@Override
	public void variableRanged( String variable )
	{
	}

	@Override
	public void variableRemoved( Object variable )
	{
	}

	@Override
	public void variableStatusChanged( String name, boolean enabled )
	{
	}

	@Override
	public void variableTyped( String variable )
	{
	}

	@Override
	public void valueChanged( String name )
	{
	}

}
