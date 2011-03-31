package com.ryanm.util.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The class makes it easy to select a numerical value, possibly from a given
 * range
 * 
 * @author ryanm
 */
public class FloatChooser extends JPanel implements ChangeListener
{
	/**
	 * The size of steps in the spinner to aim for. What you get will depend on
	 * the range.
	 */
	private float stepSize = 0.1f;

	/**
	 * The number of steps in the slider to aim for. What you get will depend on
	 * the range.
	 */
	private static final int SCALE = 10000;

	private static final float MAX_VELOCITY = 5;

	private List<Listener> listeners = new LinkedList<Listener>();

	private JSlider slider = new JSlider();

	private SpinnerNumberModel snm;

	private JSpinner spinner = new JSpinner();

	private boolean absolute = false;

	private Timer timer = new Timer( 50, new ActionListener() {
		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( e.getSource() == timer )
			{
				// set the value
				if( velocity != 0 )
				{
					setValue( getValue() + velocity );
				}
			}
		}
	} );

	private float velocity = 0;

	private float oldValue;

	private boolean integer = false;

	private RangeEditor rangeEditor = new RangeEditor();

	private float[] range = new float[] { Float.NaN, Float.NaN };

	/**
	 * Controls the unbounded slider operation
	 */
	private MouseListener sliderListener = new MouseAdapter() {
		@Override
		public void mousePressed( MouseEvent e )
		{
			// start the Timertask
			timer.start();
		}

		@Override
		public void mouseReleased( MouseEvent e )
		{
			// end the timertask
			timer.stop();

			slider.getModel().setValue( SCALE / 2 );
		}
	};

	private MouseListener rangeAdjustListener = new MouseAdapter() {
		@Override
		public void mouseReleased( MouseEvent e )
		{
			if( e.isPopupTrigger() && e.isShiftDown() )
			{
				showRangeAdjuster();
			}
		}

		@Override
		public void mouseClicked( MouseEvent e )
		{
			if( e.isPopupTrigger() && e.isShiftDown() )
			{
				showRangeAdjuster();
			}
		}

		@Override
		public void mousePressed( MouseEvent e )
		{
			if( e.isPopupTrigger() && e.isShiftDown() )
			{
				showRangeAdjuster();
			}
		}

		private void showRangeAdjuster()
		{
			rangeEditor.lower.setText( snm.getMinimum() == null ? "None" : snm
					.getMinimum().toString() );
			rangeEditor.upper.setText( snm.getMaximum() == null ? "None" : snm
					.getMaximum().toString() );

			rangeEditor.setLocationRelativeTo( FloatChooser.this );
			rangeEditor.setVisible( true );
		}
	};

	/**
	 * Constructs a new FloatChooser
	 * 
	 * @param min
	 *           The minimum value, or null for no minimum
	 * @param max
	 *           The maximum value, or null for no maximum
	 * @param value
	 *           The current value
	 */
	public FloatChooser( Float min, Float max, float value )
	{
		this( min, max, value, false );
	}

	/**
	 * Constructs a new FloatChooser
	 * 
	 * @param min
	 *           The minimum vlaue, or null for no minimum
	 * @param max
	 *           The maximum value, or null for no maximum
	 * @param value
	 *           The current value
	 * @param integer
	 *           If <code>true</code>, values will be rounded to the nearest
	 *           integer, and the velocity slider will be scaled linearly
	 */
	public FloatChooser( Float min, Float max, float value, boolean integer )
	{
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		oldValue = value;

		add( spinner );
		spinner.addChangeListener( this );
		add( slider );
		slider.addChangeListener( this );

		this.integer = integer;

		setRange( min, max );

		JSpinner.NumberEditor ne = new JSpinner.NumberEditor( spinner, "0.#####" );
		spinner.setEditor( ne );

		slider.setPreferredSize( new Dimension( 120,
				slider.getPreferredSize().height ) );
	}

	/**
	 * Gets the listener that will trigger the range adjust dialog. Apply it to
	 * what you want sensitised
	 * 
	 * @return The range adjust dialog trigger
	 */
	public MouseListener getRangeAdjustListener()
	{
		return rangeAdjustListener;
	}

	/**
	 * Sets the range of possible values
	 * 
	 * @param min
	 *           The minimum possible value, or {@link Float}.NaN for no minimum
	 *           limit
	 * @param max
	 *           The maximum possible value, or {@link Float}.NaN for no maximum
	 *           limit
	 */
	public void setRange( float min, float max )
	{
		setRange( Float.isNaN( min ) ? null : new Float( min ),
				Float.isNaN( max ) ? null : new Float( max ) );
	}

	/**
	 * Sets the range of possible values
	 * 
	 * @param min
	 *           The minimum possible value, or null for no minimum limit
	 * @param max
	 *           The maximum possible value, or null for no maximum limit
	 */
	public void setRange( Float min, Float max )
	{
		range[ 0 ] = min != null ? min.floatValue() : Float.NaN;
		range[ 1 ] = max != null ? max.floatValue() : Float.NaN;

		slider.removeChangeListener( this );

		// get the limits

		snm = new SpinnerNumberModel();
		snm.setMinimum( min );
		snm.setMaximum( max );

		snm.setValue( new Float( oldValue ) );
		spinner.setModel( snm );

		BoundedRangeModel brm = new DefaultBoundedRangeModel();

		if( min != null && max != null )
		{ // put the slider into absolute mode
			absolute = true;

			snm.setStepSize( new Float( stepSize ) );

			// build the slider
			brm.setMinimum( ( int ) ( min.floatValue() * SCALE ) );
			brm.setMaximum( ( int ) ( max.floatValue() * SCALE ) );
			brm.setValue( ( int ) ( oldValue * SCALE ) );

			slider.removeMouseListener( sliderListener );
		}
		else
		{ // put the slider into velocity mode
			absolute = false;

			brm.setMinimum( 0 );
			brm.setMaximum( SCALE );
			brm.setValue( SCALE / 2 );

			slider.addMouseListener( sliderListener );
		}

		slider.setPaintTrack( absolute );
		slider.setModel( brm );
		slider.addChangeListener( this );
		validate();

		synchronized( listeners )
		{
			for( Listener l : listeners )
			{
				l.rangeChanged( min == null ? Float.NaN : min.floatValue(),
						max == null ? Float.NaN : max.floatValue() );
			}
		}
	}

	/**
	 * Gets the currently set range
	 * 
	 * @return an {min,max} array, where {@link Float#NaN} signifies no limit
	 */
	public float[] getRange()
	{
		return range;
	}

	/**
	 * Gets the minimum value possible in this FloatChooser
	 * 
	 * @return the minimum value possible, or null if there is no lower limit
	 */
	public Float getMinValue()
	{
		SpinnerNumberModel snm = ( SpinnerNumberModel ) spinner.getModel();

		return ( Float ) snm.getMinimum();
	}

	/**
	 * Gets the maximum value possible in this FloatChooser
	 * 
	 * @return the maximum value possible, or null if there is no upper limit
	 */
	public Float getMaxValue()
	{
		SpinnerNumberModel snm = ( SpinnerNumberModel ) spinner.getModel();

		return ( Float ) snm.getMaximum();
	}

	/**
	 * Gets the current value
	 * 
	 * @return The current value
	 */
	public float getValue()
	{
		return ( ( Number ) spinner.getValue() ).floatValue();
	}

	/**
	 * Sets the current value. If the supplied value is outside of the current
	 * range, the closest legal value will be set. Listeners will be notified
	 * 
	 * @param value
	 *           The value to set.
	 */
	public void setValue( float value )
	{
		if( integer )
		{
			value = Math.round( value );
		}

		if( getMinValue() != null )
		{
			value = Math.max( getMinValue().floatValue(), value );
		}
		if( getMaxValue() != null )
		{
			value = Math.min( getMaxValue().floatValue(), value );
		}

		if( value != oldValue )
		{
			oldValue = value;
			spinner.setValue( new Float( value ) );

			if( absolute )
			{
				slider.setValue( ( int ) ( SCALE * value ) );
			}

			synchronized( listeners )
			{
				for( Listener listener : listeners )
				{
					listener.valueChanged( value );
				}
			}
		}
	}

	/**
	 * Sets the step size for the spinner's up and down buttons
	 * 
	 * @param stepSize
	 *           The new step size
	 */
	public void setSpinnerStepSize( float stepSize )
	{
		this.stepSize = stepSize;

		( ( SpinnerNumberModel ) spinner.getModel() ).setStepSize( new Float(
				stepSize ) );
	}

	/**
	 * Gets the step size for the spinner's up and down buttons
	 * 
	 * @return The current step size
	 */
	public float getSpinnerStepSize()
	{
		return stepSize;
	}

	@Override
	public void setEnabled( boolean enabled )
	{
		spinner.setEnabled( enabled );
		if( slider != null )
		{
			slider.setEnabled( enabled );
		}
	}

	@Override
	public boolean isEnabled()
	{
		return spinner.isEnabled();
	}

	/**
	 * Adds a {@link Listener} to this FloatChooser. The {@link Listener} will be
	 * appraised of any changes to the selected value
	 * 
	 * @param listener
	 *           The {@link Listener} to add
	 */
	public void addListener( Listener listener )
	{
		synchronized( listeners )
		{
			listeners.add( listener );
		}
	}

	/**
	 * Removes a {@link Listener} from this FloatChooser. The {@link Listener}
	 * will no longer be appraised of changes to the selected value
	 * 
	 * @param listener
	 *           The {@link Listener} to remove
	 */
	public void removeListener( Listener listener )
	{
		synchronized( listeners )
		{
			listeners.remove( listener );
		}
	}

	@Override
	public void stateChanged( ChangeEvent e )
	{
		Float value = null;
		if( e.getSource() == spinner )
		{
			value = ( Float ) spinner.getValue();
		}
		else if( e.getSource() == slider )
		{
			if( absolute )
			{
				value = new Float( ( float ) slider.getValue() / SCALE );
			}
			else
			{
				// change the velocity
				float fraction = ( ( float ) slider.getValue() - SCALE / 2 )
						/ ( SCALE / 2 );

				if( !integer )
				{
					fraction = ( float ) Math.pow( fraction, 3 );
				}

				velocity = fraction * MAX_VELOCITY;

				if( velocity == 0 )
				{
					timer.stop();
				}
				else
				{
					timer.start();
				}
			}
		}

		if( value != null )
		{
			setValue( value.floatValue() );
		}
	}

	/**
	 * Interface for keeping track of the state of this widget
	 * 
	 * @author ryanm
	 */
	public interface Listener
	{
		/**
		 * Called when the value of the widget is changed, either by the user or
		 * via code
		 * 
		 * @param value
		 *           the new value
		 */
		public void valueChanged( float value );

		/**
		 * Called when the valid range is changed, either by the user or via code
		 * 
		 * @param low
		 *           the new lower bound, or {@link Float#NaN} for no bound
		 * @param high
		 *           the new upper bound, or {@link Float#NaN} for no bound
		 */
		public void rangeChanged( float low, float high );
	}

	private class RangeEditor extends JDialog
	{
		private JTextField lower = new JTextField( 16 );

		private JTextField upper = new JTextField( 16 );

		private JButton yes = new JButton( "OK" );

		private JButton no = new JButton( "Cancel" );

		private RangeEditor()
		{
			setTitle( "Valid Range" );
			setModal( true );

			setResizable( false );

			lower.setBorder( new TitledBorder( "Lower bound" ) );
			lower.setHorizontalAlignment( SwingConstants.CENTER );
			upper.setBorder( new TitledBorder( "Upper bound" ) );
			upper.setHorizontalAlignment( SwingConstants.CENTER );

			no.setMnemonic( 'C' );
			getRootPane().setDefaultButton( yes );

			JPanel bp = new JPanel();
			bp.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
			bp.add( no );
			bp.add( yes );

			no.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e )
				{
					setVisible( false );
				}
			} );

			yes.addActionListener( new ActionListener() {

				@Override
				public void actionPerformed( ActionEvent e )
				{
					Float low = null;
					try
					{
						low = new Float( lower.getText() );
					}
					catch( NumberFormatException nfe )
					{
					}
					Float high = null;
					try
					{
						high = new Float( upper.getText() );
					}
					catch( NumberFormatException nfe )
					{
					}

					if( integer )
					{
						if( low != null )
						{
							low = new Float( low.intValue() );
						}
						if( high != null )
						{
							high = new Float( high.intValue() );
						}
					}

					setRange( low, high );

					setVisible( false );
				}
			} );

			getContentPane().setLayout(
					new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

			add( lower );
			add( upper );
			add( bp );

			pack();
		}
	}

}
