
package com.ryanm.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A widget to manipulate an RGBA colour.
 * 
 * @author ryanm
 */
public class RGBAColourChooser extends Box implements ChangeListener
{
	private JLabel redLabel = new JLabel( "R" );

	private JSlider redSlider = new JSlider( 0, 255 );

	private JLabel greenLabel = new JLabel( "G" );

	private JSlider greenSlider = new JSlider( 0, 255 );

	private JLabel blueLabel = new JLabel( "B" );

	private JSlider blueSlider = new JSlider( 0, 255 );

	private JLabel alphaLabel = new JLabel( "A" );

	private JSlider alphaSlider = new JSlider( 0, 255 );

	private Box sliderBox;

	private Color oldValue = Color.WHITE;

	private List<ChangeListener> listeners = new LinkedList<ChangeListener>();

	private JPanel previewer = new JPanel() {
		@Override
		public void paint( Graphics g )
		{
			int width = getWidth();
			int height = getHeight();

			g.setColor( Color.white );
			g.fillRect( 0, 0, width, height );

			g.setColor( Color.black );
			g.fillRect( 0, height / 2, width / 2, height / 2 );
			g.fillRect( width / 2, 0, width / 2, height / 2 );

			g.setColor( oldValue );
			g.fillRect( width / 4, height / 4, width / 2, height / 2 );
		}

		@Override
		public Dimension getPreferredSize()
		{
			int size = sliderBox.getHeight();

			size = size == 0 ? 64 : size;

			return new Dimension( size, size );
		}

		@Override
		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}
	};

	/**
	 * Constructs a new RGBAColourChooser
	 */
	public RGBAColourChooser()
	{
		super( BoxLayout.X_AXIS );

		Box rb = new Box( BoxLayout.X_AXIS );
		rb.add( redLabel );
		rb.add( redSlider );
		Box gb = new Box( BoxLayout.X_AXIS );
		gb.add( greenLabel );
		gb.add( greenSlider );
		Box bb = new Box( BoxLayout.X_AXIS );
		bb.add( blueLabel );
		bb.add( blueSlider );
		Box ab = new Box( BoxLayout.X_AXIS );
		ab.add( alphaLabel );
		ab.add( alphaSlider );

		redSlider.setValue( oldValue.getRed() );
		greenSlider.setValue( oldValue.getGreen() );
		blueSlider.setValue( oldValue.getBlue() );
		alphaSlider.setValue( oldValue.getAlpha() );

		redSlider.addChangeListener( this );
		greenSlider.addChangeListener( this );
		blueSlider.addChangeListener( this );
		alphaSlider.addChangeListener( this );

		redSlider.setToolTipText( String.valueOf( redSlider.getValue() ) );
		greenSlider.setToolTipText( String.valueOf( greenSlider.getValue() ) );
		blueSlider.setToolTipText( String.valueOf( blueSlider.getValue() ) );
		alphaSlider.setToolTipText( String.valueOf( alphaSlider.getValue() ) );

		sliderBox = new Box( BoxLayout.Y_AXIS );
		sliderBox.add( rb );
		sliderBox.add( gb );
		sliderBox.add( bb );
		sliderBox.add( ab );

		add( previewer );
		add( Box.createHorizontalStrut( 5 ) );
		add( sliderBox );

	}

	/**
	 * Sets the widget's selected colour
	 * 
	 * @param color
	 *           The new colour
	 */
	public void setColour( Color color )
	{
		if( !oldValue.equals( color ) )
		{
			// copy the values
			oldValue = color;

			redSlider.removeChangeListener( this );
			greenSlider.removeChangeListener( this );
			blueSlider.removeChangeListener( this );
			alphaSlider.removeChangeListener( this );

			redSlider.setValue( oldValue.getRed() );
			greenSlider.setValue( oldValue.getGreen() );
			blueSlider.setValue( oldValue.getBlue() );
			alphaSlider.setValue( oldValue.getAlpha() );

			redSlider.setToolTipText( String.valueOf( redSlider.getValue() ) );
			greenSlider.setToolTipText( String.valueOf( greenSlider.getValue() ) );
			blueSlider.setToolTipText( String.valueOf( blueSlider.getValue() ) );
			alphaSlider.setToolTipText( String.valueOf( alphaSlider.getValue() ) );

			redSlider.addChangeListener( this );
			greenSlider.addChangeListener( this );
			blueSlider.addChangeListener( this );
			alphaSlider.addChangeListener( this );

			previewer.repaint();

			ChangeEvent ce = new ChangeEvent( this );
			for( ChangeListener listener : listeners )
			{
				listener.stateChanged( ce );
			}
		}
	}

	/**
	 * Gets the widget's currently selected colour
	 * 
	 * @return The current colour
	 */
	public Color getColour()
	{
		return oldValue;
	}

	@Override
	public void stateChanged( ChangeEvent e )
	{
		int red = redSlider.getValue();
		int green = greenSlider.getValue();
		int blue = blueSlider.getValue();
		int alpha = alphaSlider.getValue();

		setColour( new Color( red, green, blue, alpha ) );
	}

	@Override
	public void setEnabled( boolean b )
	{
		alphaLabel.setEnabled( b );
		alphaSlider.setEnabled( b );
		blueLabel.setEnabled( b );
		blueSlider.setEnabled( b );
		greenLabel.setEnabled( b );
		greenSlider.setEnabled( b );
		redLabel.setEnabled( b );
		redSlider.setEnabled( b );
	}

	@Override
	public boolean isEnabled()
	{
		return alphaSlider.isEnabled();
	}

	/**
	 * Adds a change listener to this widget. The ChangeListener will
	 * be appraised of any changes to the selected colour
	 * 
	 * @param listener
	 *           The listener to add
	 */
	public void addChangeListener( ChangeListener listener )
	{
		listeners.add( listener );
	}

	/**
	 * Removes a ChangeListener from this widget. The ChangeListener
	 * will no longer be appraised of changes to the selected colour
	 * 
	 * @param listener
	 *           The listener to remove
	 */
	public void removeChangeListener( ChangeListener listener )
	{
		listeners.remove( listener );
	}
}
