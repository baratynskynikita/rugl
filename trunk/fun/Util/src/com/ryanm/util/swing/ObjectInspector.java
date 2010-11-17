
package com.ryanm.util.swing;

import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 * Allows the user to reflectively inspect an object hierarchy
 * 
 * @author ryanm
 */
public class ObjectInspector extends JTree
{
	private boolean showInaccessibleFields = true;

	private boolean showStaticFields = true;

	private ObjectNode treeRoot = new ObjectNode( null, true );

	private DefaultTreeModel treeModel = new DefaultTreeModel( treeRoot );

	private TreeWillExpandListener expansionListener = new TreeWillExpandListener() {

		@Override
		public void treeWillCollapse( TreeExpansionEvent event ) throws ExpandVetoException
		{
			Object obj = event.getPath().getLastPathComponent();

			if( obj instanceof ObjectNode )
			{
				ObjectNode on = ( ObjectNode ) obj;
				assert !on.root;

				on.expanded = false;

				on.refreshValue( on.inspectedObject );
			}
		}

		@Override
		public void treeWillExpand( TreeExpansionEvent event ) throws ExpandVetoException
		{
			Object obj = event.getPath().getLastPathComponent();

			if( obj instanceof ObjectNode )
			{
				ObjectNode on = ( ObjectNode ) obj;
				on.expanded = true;

				on.buildChildren();

				on.refreshTree( on.inspectedObject );

				treeModel.reload( on );
			}
		}

	};

	/**
	 * Builds a new {@link ObjectInspector}
	 * 
	 * @param o
	 *           The object to inspect
	 * @param showInaccessible
	 *           <code>true</code> to display inaccessible fields in
	 *           the tree, <code>false</code> to hide them
	 * @param showStatic
	 *           <code>true</code> to show static fields,
	 *           <code>false</code> to hide them
	 */
	public ObjectInspector( Object o, boolean showInaccessible, boolean showStatic )
	{
		setModel( treeModel );

		showInaccessibleFields = showInaccessible;
		showStaticFields = showStatic;

		setEditable( false );
		addTreeWillExpandListener( expansionListener );

		treeRoot.refreshTree( o );

		ToolTipManager.sharedInstance().registerComponent( this );
	}

	/**
	 * Inspects an object
	 * 
	 * @param o
	 *           The object to inspect
	 */
	public void inspect( Object o )
	{
		treeRoot.refreshTree( o );
	}

	@Override
	public String getToolTipText( MouseEvent me )
	{
		TreePath pathForLocation = getPathForLocation( me.getX(), me.getY() );

		if( pathForLocation != null )
		{
			Object lastPathComponent = pathForLocation.getLastPathComponent();
			if( lastPathComponent instanceof ObjectNode )
			{
				ObjectNode on = ( ObjectNode ) lastPathComponent;

				return on.tooltip;
			}
		}

		return null;
	}

	private class ObjectNode extends DefaultMutableTreeNode
	{
		private Object inspectedObject = null;

		private Field inspectedField = null;

		private final boolean root;

		private final boolean accessible;

		private final boolean primitive;

		private boolean array = false;

		private boolean childrenBuilt = false;

		private TreePath path;

		private final DefaultMutableTreeNode dummyNode = new DefaultMutableTreeNode( "Inspecting..." );

		private String tooltip;

		private boolean expanded = false;

		private ObjectNode( Object inspectedObject, boolean root )
		{
			this.root = root;

			this.inspectedObject = inspectedObject;

			accessible = true;
			primitive = false;

			if( root )
			{
				buildChildren();
				expanded = true;
			}
		}

		private ObjectNode( Field inspectedField )
		{
			root = false;

			setUserObject( inspectedField.getType().getSimpleName() + " : " + inspectedField.getName() );

			this.inspectedField = inspectedField;

			primitive = inspectedField.getType().isPrimitive();

			boolean a = false;
			try
			{
				inspectedField.setAccessible( true );
				a = true;
			}
			catch( SecurityException se )
			{
				a = false;
			}

			accessible = a;

			if( !primitive && accessible )
			{
				insert( dummyNode, 0 );
			}

			if( !accessible )
			{
				setUserObject( inspectedField.getName() + " : Inaccessible" );
			}

			tooltip = inspectedField.getType().toString();
		}

		private void refreshTree( Object o )
		{
			if( objectTypeChanged( o ) )
			{
				/*
				 * the object class has changed, we need to change the
				 * tree
				 */
				removeAllChildren();
				childrenBuilt = false;

				inspectedObject = o;

				if( inspectedObject != null )
				{

					array = o.getClass().isArray();

					if( !primitive && accessible )
					{
						insert( dummyNode, getChildCount() );
					}

					if( expanded )
					{
						buildChildren();
					}
				}
				else
				{
					childrenBuilt = true;
				}

				treeModel.nodeStructureChanged( this );
			}
			else if( array )
			{ // need to check if the array length has changed
				int oldCount = getChildCount();
				int desiredCount = Array.getLength( o );

				// may need to add or remove children
				while( getChildCount() < desiredCount )
				{
					ObjectNode on = new ObjectNode( null, false );

					insert( on, getChildCount() );
				}

				while( getChildCount() > desiredCount )
				{
					remove( getChildCount() - 1 );
				}

				if( oldCount != desiredCount )
				{
					treeModel.nodeStructureChanged( this );
				}

				assert getChildCount() == desiredCount;
			}

			inspectedObject = o;

			if( !root && getChildCount() == 0 )
			{
				expanded = false;
			}

			if( expanded && getChildCount() > 0 )
			{
				int index = 0;

				for( Object child : children )
				{
					assert child != dummyNode;

					ObjectNode on = ( ObjectNode ) child;

					if( array )
					{
						on.refreshTree( Array.get( inspectedObject, index ) );
					}
					else if( on.accessible )
					{
						try
						{
							on.refreshTree( on.inspectedField.get( inspectedObject ) );
						}
						catch( IllegalArgumentException e )
						{
							e.printStackTrace();
						}
						catch( IllegalAccessException e )
						{
							e.printStackTrace();
						}
					}

					index++;
				}
			}

			refreshValue( o );
		}

		/**
		 * Updates the value of this node
		 * 
		 * @param o
		 */
		private void refreshValue( Object o )
		{
			StringBuilder buff = new StringBuilder();

			if( inspectedField != null )
			{
				buff.append( inspectedField.getName() );
				buff.append( " : " );
				buff.append( inspectedField.getType().getSimpleName() );
			}
			else
			{
				assert inspectedField == null;

				if( o != null )
				{
					buff.append( o.getClass().getSimpleName() );
				}
				else
				{
					buff.append( "null" );
				}
			}

			if( primitive )
			{
				buff.append( " : " );
				buff.append( o );
			}
			else if( !expanded )
			{
				buff.append( " : " );
				buff.append( buildString( o ) );
			}

			setUserObject( buff.toString() );

			if( path != null )
			{
				path = new TreePath( getPath() );
			}

			if( path == null )
			{
				path = new TreePath( getPath() );
			}

			treeModel.valueForPathChanged( path, getUserObject() );

			if( o != null )
			{
				if( !primitive )
				{
					tooltip = o.getClass().getName();
				}
				else
				{
					tooltip = inspectedField.getType().getName();
				}
			}
			else
			{
				tooltip = "null";
			}
		}

		/**
		 * Determines if the object type has changed
		 * 
		 * @param o
		 *           the new object
		 * @return <code>true</code> if the tree needs to be changed,
		 *         false otherwise
		 */
		private boolean objectTypeChanged( Object o )
		{
			if( inspectedObject == null && o == null )
			{
				return false;
			}
			else if( inspectedObject == null != ( o == null ) )
			{
				return true;
			}
			else if( inspectedObject != null && o != null
					&& !inspectedObject.getClass().equals( o.getClass() ) )
			{
				return true;
			}

			return false;
		}

		private void buildChildren()
		{
			if( !childrenBuilt )
			{
				if( children != null && children.contains( dummyNode ) )
				{
					remove( dummyNode );
				}

				if( inspectedObject != null )
				{
					if( array )
					{
						for( int i = 0; i < Array.getLength( inspectedObject ); i++ )
						{
							ObjectNode on = new ObjectNode( inspectedObject, false );

							insert( on, getChildCount() );
						}
					}
					else
					{
						Collection<Field> fields = new LinkedList<Field>();

						getFields( fields, inspectedObject.getClass() );

						for( Field f : fields )
						{
							ObjectNode on = new ObjectNode( f );

							if( ( showInaccessibleFields || on.accessible )
									&& ( showStaticFields || !Modifier.isStatic( f.getModifiers() ) ) )
							{
								insert( on, getChildCount() );
							}
						}
					}

					treeModel.nodeStructureChanged( this );
				}
				else
				{
					setUserObject( "null" );
				}

				childrenBuilt = true;
			}
		}

	}

	/**
	 * Recurses up the inheritance chain and collects all the fields
	 * 
	 * @param fields
	 *           The collection of fields found so far
	 * @param c
	 *           The class to get fields from
	 */
	private static void getFields( Collection<Field> fields, Class c )
	{
		for( Field f : c.getDeclaredFields() )
		{
			fields.add( f );
		}

		if( c.getSuperclass() != null )
		{
			getFields( fields, c.getSuperclass() );
		}
	}

	/**
	 * Attempts to build a nicer looking string than the basic
	 * {@link Object}.toString()
	 * 
	 * @param o
	 *           The object to build from
	 * @return A descriptive string
	 */
	private static String buildString( Object o )
	{
		if( o == null )
		{
			return "null";
		}

		// first see if there is a version of toString more specific
		// than that supplied by Object...
		try
		{
			Method m = o.getClass().getMethod( "toString" );

			if( !m.getDeclaringClass().equals( Object.class ) )
			{
				return o.toString();
			}
		}
		catch( SecurityException e )
		{
		}
		catch( NoSuchMethodException e )
		{
		}

		// then see if it is an array...
		if( o.getClass().isArray() )
		{
			StringBuilder buff = new StringBuilder( " [ " );

			for( int i = 0; i < Array.getLength( o ); i++ )
			{
				/*
				 * this could recurse infinitely, but only if the user is
				 * trying to be malicious, like so - Object[] array = new
				 * Object[ 1 ]; array[ 0 ] = array; - which, I'm sure
				 * we'll agree, is and odd thing to do. I say let the
				 * StackOverflowException catch it.
				 */

				buff.append( buildString( Array.get( o, i ) ) );
				buff.append( ", " );
			}

			if( Array.getLength( o ) > 0 )
			{
				buff.delete( buff.length() - 2, buff.length() );
			}

			buff.append( " ]" );

			return buff.toString();
		}

		return getObjectPosition( o );
	}

	/**
	 * Returns a String of an object's position in memory
	 * 
	 * @param o
	 * @return The object's memory position
	 */
	private static String getObjectPosition( Object o )
	{
		String s = o.toString();
		s = s.substring( s.lastIndexOf( "@" ) );
		return s;
	}
}
