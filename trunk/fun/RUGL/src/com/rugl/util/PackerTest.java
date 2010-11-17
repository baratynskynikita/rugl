/*
 * Copyright (c) 2007, Ryan McNally All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the <ORGANIZATION> nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package com.rugl.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A gui test class for the {@link RectanglePacker}
 * 
 * @author ryanm
 */
public class PackerTest
{
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		int w = 640, h = 480;

		if( args.length >= 2 )
		{
			w = Integer.valueOf( args[ 0 ] ).intValue();
			h = Integer.valueOf( args[ 1 ] ).intValue();
		}

		new PackerTest( w, h );
	}

	private Dimension packerDim;

	private RectanglePacker<Dimension> packer;

	private int failureCount = 0;

	private int filledCount = 0;

	private Map<Rectangle, Dimension> packed = new HashMap<Rectangle, Dimension>();

	private Random rng = new Random();

	private RangeWidget itemWidth = new RangeWidget( "Width", 10, 100 );

	private RangeWidget itemHeight = new RangeWidget( "Height", 10, 100 );

	private JSpinner bulkSize = new JSpinner( new SpinnerNumberModel( 10, 0, 100, 1 ) );

	private JComboBox bulkOrder = new JComboBox( new String[] { "Random", "Ascending",
			"Descending" } );

	private JComboBox bulkKey = new JComboBox( new String[] { "Area", "Max Dimension",
			"Min Dimension" } );

	private JButton add = new JButton( "Add item" );

	private JButton bulkAdd = new JButton( "Bulk add" );

	private JButton clear = new JButton( "Clear" );

	private JTextField efficiency = new JTextField( 8 );

	private JTextField failures = new JTextField( 8 );

	private PackerView view = new PackerView();

	/**
	 * @param w
	 *           width of packer
	 * @param h
	 *           height of packer
	 */
	public PackerTest( int w, int h )
	{
		packerDim = new Dimension( w, h );

		packer = new RectanglePacker<Dimension>( w, h, 0 );

		view.setPreferredSize( new Dimension( w, h ) );

		Box srp = new Box( BoxLayout.X_AXIS );
		srp.setBorder( new TitledBorder( "Item size range" ) );
		srp.add( itemWidth );
		srp.add( itemHeight );

		Box bp = new Box( BoxLayout.Y_AXIS );
		bp.setBorder( new TitledBorder( "" ) );
		bp.add( add );
		bp.add( bulkAdd );
		bp.add( clear );

		add.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				addItem( generateItem() );
			}
		} );

		bulkAdd.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				bulkAdd();
				view.repaint();
			}
		} );

		clear.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				packer.clear();
				packed.clear();

				failureCount = 0;
				filledCount = 0;

				updateStats();

				view.repaint();
			}
		} );

		Box bmp = new Box( BoxLayout.Y_AXIS );
		bmp.setBorder( new TitledBorder( "Bulk mode" ) );
		bmp.add( bulkSize );
		bmp.add( bulkOrder );
		bmp.add( bulkKey );

		Box sp = new Box( BoxLayout.Y_AXIS );
		sp.setBorder( new TitledBorder( "Stats" ) );
		efficiency.setEditable( false );
		efficiency.setBorder( new TitledBorder( "Efficiency" ) );
		sp.add( efficiency );
		failures.setEditable( false );
		failures.setBorder( new TitledBorder( "Failures" ) );
		sp.add( failures );

		JPanel south = new JPanel();
		south.add( srp );
		south.add( bp );
		south.add( bmp );
		south.add( sp );

		JFrame frame = new JFrame( "Packer Test" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		frame.setLayout( new BorderLayout() );

		frame.add( view, BorderLayout.CENTER );
		frame.add( south, BorderLayout.SOUTH );

		frame.pack();
		frame.setVisible( true );
	}

	private Dimension generateItem()
	{
		int range = itemWidth.getMaxValue() - itemWidth.getMinValue();
		int w = ( int ) ( itemWidth.getMinValue() + range * rng.nextDouble() );

		range = itemHeight.getMaxValue() - itemHeight.getMinValue();
		int h = ( int ) ( itemHeight.getMinValue() + range * rng.nextDouble() );

		return new Dimension( w, h );
	}

	private void addItem( Dimension d )
	{
		com.rugl.util.RectanglePacker.Rectangle r = packer.insert( d.width, d.height, d );

		if( r != null )
		{
			Rectangle pr = new Rectangle( r.x, r.y, r.width, r.height );
			packed.put( pr, d );

			filledCount += r.width * r.height;

			view.repaint();
		}
		else
		{
			failureCount++;
		}

		updateStats();
	}

	private void updateStats()
	{
		failures.setText( "" + failureCount );
		efficiency.setText( "" + ( float ) filledCount
				/ ( packerDim.width * packerDim.height ) );
	}

	private void bulkAdd()
	{
		List<Dimension> items =
				new ArrayList<Dimension>( ( ( Integer ) bulkSize.getValue() ).intValue() );

		for( int i = 0; i < ( ( Integer ) bulkSize.getValue() ).intValue(); i++ )
		{
			items.add( generateItem() );
		}

		sort( items, bulkOrder.getSelectedIndex(), bulkKey.getSelectedIndex() );

		for( Dimension d : items )
		{
			addItem( d );
		}
	}

	private void sort( List<Dimension> items, final int order, final int key )
	{
		if( order != 0 )
		{
			Comparator<Dimension> comp = new Comparator<Dimension>() {

				@Override
				public int compare( Dimension o1, Dimension o2 )
				{
					int left = 0, right = 0;

					switch( key )
					{
						case 0:
							left = o1.width * o1.height;
							right = o2.width * o2.height;
							break;
						case 1:
							left = Math.max( o1.width, o1.height );
							right = Math.max( o2.width, o2.height );
							break;
						case 2:
							left = Math.min( o1.width, o1.height );
							right = Math.min( o2.width, o2.height );
							break;
						default:
							assert false;
					}

					int c = 0;

					if( left > right )
					{
						c = 1;
					}
					else if( left < right )
					{
						c = -1;
					}

					if( order == 2 )
					{
						c = -c;
					}

					return c;
				}

			};

			Collections.sort( items, comp );
		}
	}

	private class PackerView extends JPanel
	{
		private PackerView()
		{
			addMouseListener( new MouseAdapter() {

				@Override
				public void mouseReleased( MouseEvent e )
				{
					Rectangle removed = null;
					for( Rectangle rect : packed.keySet() )
					{
						if( rect.contains( e.getX(), e.getY() ) )
						{
							packer.remove( packed.get( rect ) );
							removed = rect;
							break;
						}
					}

					if( removed != null )
					{
						packed.remove( removed );

						filledCount -= removed.width * removed.height;

						updateStats();

						repaint();
					}
				}

			} );
		}

		@Override
		protected void paintComponent( Graphics gpoo )
		{
			Graphics2D g = ( Graphics2D ) gpoo;

			g.setColor( Color.WHITE );
			g.fillRect( 0, 0, getWidth(), getHeight() );

			List<com.rugl.util.RectanglePacker.Rectangle> rects =
					new LinkedList<com.rugl.util.RectanglePacker.Rectangle>();
			packer.inspectRectangles( rects );

			int filledArea = 0;

			g.setFont( g.getFont().deriveFont( 8 ) );
			for( Rectangle r : packed.keySet() )
			{
				filledArea += r.width * r.height;

				g.setColor( Color.LIGHT_GRAY );
				g.fillRect( r.x, r.y, r.width, r.height );

				g.setColor( Color.BLACK );
				g.drawString( r.width + "x" + r.height, r.x + 1, r.y + 11 );
			}

			g.setColor( Color.DARK_GRAY );
			for( com.rugl.util.RectanglePacker.Rectangle r : rects )
			{
				g.drawRect( r.x, r.y, r.width, r.height );
			}
		}
	}

	private class RangeWidget extends Box implements ChangeListener
	{
		private JSpinner minSpinner, maxSpinner;

		private RangeWidget( String name, int min, int max )
		{
			super( BoxLayout.Y_AXIS );

			setBorder( new TitledBorder( name ) );
			minSpinner = new JSpinner( new SpinnerNumberModel( min, min, max, 1 ) );
			maxSpinner = new JSpinner( new SpinnerNumberModel( max, min, max, 1 ) );

			minSpinner.addChangeListener( this );
			maxSpinner.addChangeListener( this );

			Box minp = new Box( BoxLayout.X_AXIS );
			minp.add( new JLabel( "Min" ) );
			minp.add( minSpinner );

			Box maxp = new Box( BoxLayout.X_AXIS );
			maxp.add( new JLabel( "Max" ) );
			maxp.add( maxSpinner );

			add( minp );
			add( maxp );
		}

		private int getMinValue()
		{
			return ( ( Integer ) minSpinner.getValue() ).intValue();
		}

		private int getMaxValue()
		{
			return ( ( Integer ) maxSpinner.getValue() ).intValue();
		}

		@Override
		public void stateChanged( ChangeEvent e )
		{
			minSpinner.removeChangeListener( this );
			maxSpinner.removeChangeListener( this );

			int minv = ( ( Integer ) minSpinner.getValue() ).intValue();
			int maxv = ( ( Integer ) maxSpinner.getValue() ).intValue();

			if( e.getSource() == minSpinner )
			{
				maxv = Math.max( minv, maxv );
			}
			else
			{
				assert e.getSource() == maxSpinner;
				minv = Math.min( minv, maxv );
			}

			minSpinner.setValue( new Integer( minv ) );
			maxSpinner.setValue( new Integer( maxv ) );

			minSpinner.addChangeListener( this );
			maxSpinner.addChangeListener( this );
		}
	}
}
