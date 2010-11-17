
package com.ryanm.soundgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ryanm.soundgen.imp.Terrain;
import com.ryanm.soundgen.imp.TerrainSound;

/**
 * @author ryanm
 */
public class SoundGenApp extends JFrame
{
	private TerrainSound sound = new TerrainSound();

	private Clip clip;

	private JButton play = new JButton( "Play" );

	private JButton save = new JButton( "Save" );

	private JButton load = new JButton( "Load" );

	private JButton export = new JButton( "Export" );

	private String[] buttonNames = new String[] { "Mutate", "Randomise", "Coin", "Laser",
			"Explosion", "Powerup", "Hit", "Jump" };

	private JButton[] buttons = new JButton[ buttonNames.length ];

	private JSpinner length = new JSpinner( new SpinnerNumberModel( 1, 0.0001, 20, 0.1 ) );

	private JSpinner sSamples = new JSpinner( new SpinnerNumberModel( 8, 1, 16, 1 ) );

	private TerrainPlot volumePlot = new TerrainPlot( sound.volume, 0, 1 );

	private JComboBox baseWaveChoice;

	private TerrainPlot freqPlot = new TerrainPlot( sound.baseFrequency, 0, 2000 );

	private JComboBox vibWaveChoice;

	private TerrainPlot vibFreqPlot = new TerrainPlot( sound.vibratoFrequency, 0, 100 );

	private TerrainPlot vibAmpPlot = new TerrainPlot( sound.vibratoAmplitude, 0, 500 );

	private TerrainPlot flangeDelayPlot = new TerrainPlot( sound.flangeDelay, 0, 0.02f );

	private TerrainPlot flangeAlphaPlot = new TerrainPlot( sound.flangeAlpha, 0, 1 );

	private VariablePlot frequency = new VariablePlot( sound.getFrequency(), 0, 2500 );

	private VariablePlot[] plots = new VariablePlot[] { volumePlot, freqPlot, vibFreqPlot,
			vibAmpPlot, flangeDelayPlot, flangeAlphaPlot, frequency };

	private JFileChooser chooser = new JFileChooser();

	private ChangeListener spinnerListener = new ChangeListener() {
		@Override
		public void stateChanged( ChangeEvent e )
		{
			if( e.getSource() == length )
			{
				sound.length = ( ( Number ) length.getValue() ).floatValue();

				for( VariablePlot vp : plots )
				{
					vp.refresh();
				}
			}
			else if( e.getSource() == sSamples )
			{
				sound.superSamples = ( ( Number ) sSamples.getValue() ).intValue();
			}
		}
	};

	private ActionListener buttonListener = new ActionListener() {

		@Override
		public void actionPerformed( ActionEvent e )
		{
			String name = ( ( JButton ) e.getSource() ).getText();
			int index = -1;
			for( int i = 0; i < buttonNames.length; i++ )
			{
				if( buttonNames[ i ].equals( name ) )
				{
					index = i;
					break;
				}
			}

			switch( index )
			{
				case 0:
					sound.mutate();
					break;
				case 1:
					sound.randomise();
					break;
				case 2:
					sound.coin();
					break;
				case 3:
					sound.laser();
					break;
				case 4:
					sound.explosion();
					break;
				case 5:
					sound.powerup();
					break;
				case 6:
					sound.hit();
					break;
				case 7:
					sound.jump();
					break;
				default:
					assert false : index;
			}

			length.removeChangeListener( spinnerListener );
			length.setValue( new Float( sound.length ) );
			length.addChangeListener( spinnerListener );
			baseWaveChoice.setSelectedIndex( sound.wave.ordinal() );
			vibWaveChoice.setSelectedIndex( sound.vibrato.ordinal() );
			for( VariablePlot p : plots )
			{
				p.refresh();
			}

			play();
		}

	};

	/**
	 * 
	 */
	public SoundGenApp()
	{
		super( "SoundGen" );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		try
		{
			clip = AudioSystem.getClip();
		}
		catch( LineUnavailableException e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}

		Box bp = new Box( BoxLayout.Y_AXIS );
		bp.add( play );

		String[] wc = new String[ TerrainSound.WaveType.values().length ];
		for( int i = 0; i < wc.length; i++ )
		{
			wc[ i ] = TerrainSound.WaveType.values()[ i ].toString();
		}

		baseWaveChoice = new JComboBox( wc );
		baseWaveChoice.setSelectedIndex( sound.wave.ordinal() );
		vibWaveChoice = new JComboBox( wc );
		vibWaveChoice.setSelectedIndex( sound.vibrato.ordinal() );

		baseWaveChoice.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				sound.wave =
						TerrainSound.WaveType.values()[ baseWaveChoice.getSelectedIndex() ];
			}
		} );

		vibWaveChoice.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				sound.vibrato =
						TerrainSound.WaveType.values()[ vibWaveChoice.getSelectedIndex() ];

				frequency.v = sound.getFrequency();
				frequency.refresh();
			}
		} );

		play.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				play();
			}
		} );

		save.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				chooser.setSelectedFile( new File( "sound.ruglsg" ) );
				int r = chooser.showSaveDialog( SoundGenApp.this );

				if( r == JFileChooser.APPROVE_OPTION )
				{
					try
					{
						sound.write( chooser.getSelectedFile().getAbsolutePath() );
					}
					catch( IOException e1 )
					{
						e1.printStackTrace();
					}
				}
			}
		} );

		load.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				chooser.setSelectedFile( new File( "sound.ruglsg" ) );
				int r = chooser.showOpenDialog( SoundGenApp.this );

				if( r == JFileChooser.APPROVE_OPTION )
				{
					try
					{
						sound.read( new FileInputStream( chooser.getSelectedFile() ) );

						for( VariablePlot vp : plots )
						{
							vp.refresh();
						}

						length.removeChangeListener( spinnerListener );
						length.setValue( new Float( sound.length ) );
						length.addChangeListener( spinnerListener );

						baseWaveChoice.setSelectedIndex( sound.wave.ordinal() );
						vibWaveChoice.setSelectedIndex( sound.vibrato.ordinal() );
					}
					catch( IOException e1 )
					{
						e1.printStackTrace();
					}
				}
			}
		} );

		export.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				chooser.setSelectedFile( new File( "sound.wav" ) );
				int r = chooser.showSaveDialog( SoundGenApp.this );

				if( r == JFileChooser.APPROVE_OPTION )
				{
					try
					{
						WavUtil.saveAsWav( 44100, 2, sound.generate( 44100 ), chooser
								.getSelectedFile().getAbsolutePath() );
					}
					catch( IOException e1 )
					{
						e1.printStackTrace();
					}
				}
			}
		} );

		length.addChangeListener( spinnerListener );
		sSamples.addChangeListener( spinnerListener );

		Box pBox = new Box( BoxLayout.Y_AXIS );
		pBox.add( wrap( volumePlot, "Volume" ) );
		pBox.add( wrap( freqPlot, "Base frequency" ) );
		pBox.add( wrap( vibFreqPlot, "Vibrato frequency" ) );
		pBox.add( wrap( vibAmpPlot, "Vibrato depth" ) );
		pBox.add( wrap( frequency, "Output frequency" ) );
		pBox.add( wrap( flangeDelayPlot, "Flange delay" ) );
		pBox.add( wrap( flangeAlphaPlot, "Flange alpha" ) );

		Box nePanel = new Box( BoxLayout.Y_AXIS );
		nePanel.add( wrap( baseWaveChoice, "Signal" ) );
		nePanel.add( wrap( vibWaveChoice, "Vibrato" ) );
		nePanel.add( wrap( length, "Length" ) );
		// nePanel.add( wrap( sSamples, "Samples" ) );

		Box ecPanel = new Box( BoxLayout.Y_AXIS );
		ecPanel.add( wrap( play, null ) );

		Box sePanel = new Box( BoxLayout.Y_AXIS );

		for( int i = 0; i < buttonNames.length; i++ )
		{
			buttons[ i ] = new JButton( buttonNames[ i ] );
			buttons[ i ].addActionListener( buttonListener );
			sePanel.add( wrap( buttons[ i ], null ) );
		}

		sePanel.add( wrap( save, null ) );
		sePanel.add( wrap( load, null ) );
		sePanel.add( wrap( export, null ) );

		JPanel east = new JPanel( new BorderLayout() );
		east.add( nePanel, BorderLayout.NORTH );
		east.add( ecPanel, BorderLayout.CENTER );
		east.add( sePanel, BorderLayout.SOUTH );

		getContentPane().add( pBox, BorderLayout.CENTER );
		getContentPane().add( east, BorderLayout.EAST );

		pack();
	}

	private Component wrap( Component c, String title )
	{
		JPanel p = new JPanel( new BorderLayout() );
		p.setBorder( new TitledBorder( title ) );
		p.add( c, BorderLayout.CENTER );
		return p;
	}

	private void play()
	{
		if( clip.isOpen() )
		{
			clip.close();
		}

		ByteBuffer pcm = sound.generate( 44100 );

		try
		{
			clip.open( WavUtil.getAudioStream( 44100, 2, pcm ) );

			clip.start();
		}
		catch( LineUnavailableException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	private class VariablePlot extends JPanel
	{
		private Variable v;

		/**
		 * min value
		 */
		protected float min;

		/**
		 * max value
		 */
		protected float max;

		/**
		 * set to true to refresh
		 */
		protected boolean pointsDirty = true;

		private int[] xpoints = new int[ 0 ], ypoints = new int[ 0 ];

		/**
		 * 
		 */
		protected int x = 0;

		/**
		 * 
		 */
		protected int y = 0;

		/**
		 * 
		 */
		protected float[] temp = new float[ 2 ];

		/**
		 * 
		 */
		protected boolean mouseOver = false;

		private VariablePlot( Variable v, float min, float max )
		{
			this.v = v;
			this.min = min;
			this.max = max;

			addComponentListener( new ComponentAdapter() {
				@Override
				public void componentResized( ComponentEvent e )
				{
					refresh();
				}
			} );

			addMouseMotionListener( new MouseMotionListener() {
				@Override
				public void mouseMoved( MouseEvent e )
				{
					x = e.getX();
					y = e.getY();

					repaint();
				}

				@Override
				public void mouseDragged( MouseEvent e )
				{
					x = e.getX();
					y = e.getY();

					repaint();
				}
			} );

			addMouseWheelListener( new MouseWheelListener() {

				@Override
				public void mouseWheelMoved( MouseWheelEvent e )
				{
					float m = e.getWheelRotation() > 0 ? 0.8f : 1.2f;

					m = ( float ) Math.pow( m, Math.abs( e.getWheelRotation() ) );

					VariablePlot.this.max *= m;

					if( m != 1 )
					{
						refresh();
					}

					repaint();
				}
			} );

			addMouseListener( new MouseAdapter() {
				@Override
				public void mouseExited( MouseEvent e )
				{
					mouseOver = false;
					repaint();
				}

				@Override
				public void mouseEntered( MouseEvent e )
				{
					mouseOver = true;
					repaint();
				}

				@Override
				public void mouseClicked( MouseEvent e )
				{
					if( e.isShiftDown() )
					{
						// set the min and max
						String s =
								( String ) JOptionPane.showInputDialog( SoundGenApp.this,
										"Specify bounds e.eg. \"0 100\"", "Edit bounds",
										JOptionPane.QUESTION_MESSAGE, null, null,
										VariablePlot.this.min + " " + VariablePlot.this.max );

						if( s != null )
						{
							String[] mm = s.split( "\\s" );

							try
							{
								VariablePlot.this.min = new Float( mm[ 0 ] ).floatValue();
								VariablePlot.this.max = new Float( mm[ 1 ] ).floatValue();

								refresh();
							}
							catch( NumberFormatException nfe )
							{
							}
						}
					}
				}
			} );
		}

		private void recreatePoints()
		{
			if( pointsDirty )
			{
				xpoints = new int[ getWidth() ];
				ypoints = new int[ xpoints.length ];

				float ts = sound.length / getWidth();

				float[] f = new float[ 2 ];

				for( int i = 0; i < xpoints.length; i++ )
				{
					float time = ts * i;
					float value = v.getValue( time );

					f[ 0 ] = time;
					f[ 1 ] = value;

					toPixels( f );

					xpoints[ i ] = ( int ) f[ 0 ];
					ypoints[ i ] = ( int ) f[ 1 ];
				}

				pointsDirty = false;
			}
		}

		/**
		 * 
		 */
		protected void refresh()
		{
			pointsDirty = true;
			repaint();
		}

		/**
		 * @param coords
		 */
		protected void toPixels( float[] coords )
		{
			float pixelsPerSecond = getWidth() / sound.length;
			float pixelsperValue = getHeight() / ( max - min );

			coords[ 0 ] = coords[ 0 ] * pixelsPerSecond;
			coords[ 1 ] = Math.round( getHeight() - ( coords[ 1 ] - min ) * pixelsperValue );
		}

		/**
		 * @param coords
		 */
		protected void toValues( float[] coords )
		{
			coords[ 0 ] = Math.max( 0, coords[ 0 ] );
			coords[ 0 ] = Math.min( getWidth(), coords[ 0 ] );
			coords[ 1 ] = Math.max( 0, coords[ 1 ] );
			coords[ 1 ] = Math.min( getHeight(), coords[ 1 ] );

			float secondsPerPixel = sound.length / getWidth();
			float valuesPerPixel = ( max - min ) / getHeight();

			coords[ 0 ] = coords[ 0 ] * secondsPerPixel;
			coords[ 1 ] = min + ( getHeight() - coords[ 1 ] ) * valuesPerPixel;
		}

		@Override
		public void paintComponent( Graphics gpoo )
		{
			recreatePoints();

			Graphics2D g = ( Graphics2D ) gpoo;

			g.setColor( Color.BLACK );
			g.fillRect( 0, 0, getWidth(), getHeight() );

			// draw plot
			if( xpoints.length >= 2 )
			{
				g.setColor( Color.WHITE );
				for( int i = 1; i < xpoints.length; i++ )
				{
					g.drawLine( xpoints[ i - 1 ], ypoints[ i - 1 ], xpoints[ i ], ypoints[ i ] );
				}
			}

			// draw labels
			g.setColor( Color.GRAY );
			Font f = g.getFont();
			f = f.deriveFont( 10.0f );
			g.setFont( f );

			g.drawString( "" + max, 1, f.getSize() + 1 );
			g.drawString( "" + min, 1, getHeight() - 2 );

			if( mouseOver )
			{
				temp[ 0 ] = x;
				temp[ 1 ] = y;
				toValues( temp );

				g.drawString( temp[ 0 ] + ", " + temp[ 1 ], x, y );
			}
		}
	}

	private class TerrainPlot extends VariablePlot
	{
		private Terrain t;

		private float[] dragging = null;

		private MouseAdapter ma = new MouseAdapter() {

			@Override
			public void mousePressed( MouseEvent e )
			{
				x = e.getX();
				y = e.getY();
				if( e.getButton() == MouseEvent.BUTTON1 )
				{
					dragging = findClosest( x, y, 25 );
					mouseDragged( e );
				}
			}

			@Override
			public void mouseReleased( MouseEvent e )
			{
				x = e.getX();
				y = e.getY();
				if( dragging != null )
				{
					if( e.getButton() == MouseEvent.BUTTON1 )
					{
						dragging = null;
					}
				}
				else
				{
					if( e.getButton() == MouseEvent.BUTTON3 )
					{
						float[] closest = findClosest( x, y, -1 );

						if( closest != null )
						{
							t.removePoint( closest[ 0 ], closest[ 1 ] );
						}
						else
						{
							assert t.points.isEmpty();
						}

						repaint();
					}
					else if( e.getButton() == MouseEvent.BUTTON1 )
					{
						temp[ 0 ] = x;
						temp[ 1 ] = y;

						toValues( temp );

						t.addPoint( temp[ 0 ], temp[ 1 ] );
					}
				}

				TerrainPlot.this.refresh();

				if( TerrainPlot.this == freqPlot || TerrainPlot.this == vibFreqPlot
						|| TerrainPlot.this == vibAmpPlot )
				{
					frequency.refresh();
				}
			}

			@Override
			public void mouseDragged( MouseEvent arg0 )
			{
				x = arg0.getX();
				y = arg0.getY();

				if( dragging != null )
				{
					temp[ 0 ] = x;
					temp[ 1 ] = y;

					toValues( temp );

					dragging[ 0 ] = temp[ 0 ];
					dragging[ 1 ] = temp[ 1 ];

					t.orderDirty = true;

					TerrainPlot.this.refresh();

					if( TerrainPlot.this == freqPlot || TerrainPlot.this == vibFreqPlot
							|| TerrainPlot.this == vibAmpPlot )
					{
						frequency.refresh();
					}
				}
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				x = e.getX();
				y = e.getY();
				repaint();
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
				x = -100;
				y = -100;
				repaint();
			}
		};

		private TerrainPlot( Terrain t, float min, float max )
		{
			super( t, min, max );
			this.t = t;

			addMouseListener( ma );
			addMouseMotionListener( ma );
		}

		@Override
		public void paintComponent( Graphics gpoo )
		{
			super.paintComponent( gpoo );

			Graphics2D g = ( Graphics2D ) gpoo;

			for( float[] v : t.points )
			{
				g.setColor( Color.WHITE );

				temp[ 0 ] = v[ 0 ];
				temp[ 1 ] = v[ 1 ];

				toPixels( temp );

				g.drawRect( ( int ) ( temp[ 0 ] - 4 ), ( int ) ( temp[ 1 ] - 4 ), 8, 8 );
			}

			if( mouseOver )
			{
				float[] c = findClosest( x, y, 25 );

				if( c != null )
				{
					temp[ 0 ] = c[ 0 ];
					temp[ 1 ] = c[ 1 ];

					toPixels( temp );

					g.fillRect( ( int ) ( temp[ 0 ] - 4 ), ( int ) ( temp[ 1 ] - 4 ), 8, 8 );
					g.drawLine( x, y, ( int ) temp[ 0 ], ( int ) temp[ 1 ] );
				}
			}
		}

		private float[] findClosest( int px, int py, float selectDistance )
		{
			float minDelta;

			if( selectDistance == -1 )
			{
				minDelta = Float.MAX_VALUE;
			}
			else
			{
				minDelta = selectDistance * selectDistance;
			}

			float[] closest = null;

			for( float[] p : t.points )
			{
				temp[ 0 ] = p[ 0 ];
				temp[ 1 ] = p[ 1 ];
				toPixels( temp );

				float dx = px - temp[ 0 ];
				float dy = py - temp[ 1 ];

				float d = dx * dx + dy * dy;

				if( d < minDelta )
				{
					minDelta = d;
					closest = p;
				}
			}

			return closest;
		}
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		SoundGenApp sgt = new SoundGenApp();

		sgt.setSize( 800, 600 );
		sgt.setVisible( true );
	}
}
