package maps.Utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import jade.gui.*;

import maps.Agents.Portal;

public class PortalGUI extends JFrame implements ActionListener {
	private static final int NOT_STARTED = 0;
	private static final int RUNNING = 1;
	private int status = NOT_STARTED;
	
	private JTextField num_painters, max_bargaining_power, canvas_x, canvas_y, brush_size, max_speed;
	private JButton start, quit, reset;
	private Portal myAgent;
    
	public PortalGUI(Portal portal_agent) {
		myAgent = portal_agent;

		// Init main panel:
		setTitle("MAPS Portal");
		JPanel base = new JPanel();
		base.setBorder(new EmptyBorder(15,15,15,15));
		base.setLayout(new BorderLayout(10,10));
		getContentPane().add(base);
  
		// Init header:
		JTextField header = new JTextField("Select MAPS Configuration Options");
		header.setEditable(false);
		header.setBackground(Color.black);
		header.setForeground(Color.white);
		header.setFont(new Font("Arial", Font.BOLD, 14));
		header.setHorizontalAlignment(JTextField.CENTER);
		base.add(header, BorderLayout.PAGE_START);
  
		// Init options grid:
		JPanel options = new JPanel();
		options.setLayout(new GridLayout(0, 2, 2, 2));
		base.add(options, BorderLayout.CENTER);
		options.add(new JLabel("Number of Painters:"));
		options.add(num_painters = new JTextField(7));
		options.add(new JLabel("Max Bargaining Power:"));
		options.add(max_bargaining_power = new JTextField(7));
		options.add(new JLabel("Canvas Width:"));
		options.add(canvas_x = new JTextField(7));
		options.add(new JLabel("Canvas Height:"));
		options.add(canvas_y = new JTextField(7));
		options.add(new JLabel("Brush Size:"));
		options.add(brush_size = new JTextField(7));
		options.add(new JLabel("Painter Max Speed:"));
		options.add(max_speed = new JTextField(7));
	  
		// Init footer:
		JPanel footer = new JPanel();
		footer.add(start = new JButton("START"));
		start.setToolTipText("Start MAPS simulation");
		start.addActionListener(this);
		footer.add(reset = new JButton("RESET"));
		reset.setToolTipText("Reset MAPS simulation");
		reset.addActionListener(this);
		footer.add(quit = new JButton("QUIT"));
		quit.setToolTipText("Stop MAPS simulation and exit");
		quit.addActionListener(this);
		base.add(footer, BorderLayout.PAGE_END);
  
		// Exit listener:
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutDown();
			}
		});
  
		// Configure main panel:
		setSize(470, 350);
		setResizable(false);
		Rectangle r = getGraphicsConfiguration().getBounds();
		setLocation(r.x + (r.width - getWidth())/2,
                  	r.y + (r.height - getHeight())/2);
	}
   
	/**
	 *  Button action listeners.
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == quit) {
			shutDown();
		}
		else if (ae.getSource() == start) {
			if (status == NOT_STARTED && validateFields()) {
				status = RUNNING;
				GuiEvent ge = new GuiEvent(this, Portal.START);
				myAgent.postGuiEvent(ge);
			}
		}
		else if (ae.getSource() == reset) {
			resetSimulation();
		}
	}
	
	/**
	 * Reset the currently running simulation.
	 */
	public void resetSimulation() {
		if (status == RUNNING) {
			status = NOT_STARTED;
			clearFields();
			
			GuiEvent ge = new GuiEvent(this, Portal.RESET);
			myAgent.postGuiEvent(ge);
		}	
	}
   
	/**
	 * Reset configuration options.
	 */
	public void clearFields() {
		num_painters.setText(null);
		max_bargaining_power.setText(null);
		canvas_x.setText(null);
		canvas_y.setText(null);
		brush_size.setText(null);
		max_speed.setText(null);
	}
	
	/**
	 * Validate MAPS configuration options.
	 */
	public boolean validateFields() {
		try {
			getOptions();  // parse error will be thrown if any field is empty
			
			// Ensure minimum canvas size:
			int width = Integer.parseInt(canvas_x.getText());
			int height = Integer.parseInt(canvas_y.getText());
			if (width < 200 || height < 200) {
				JOptionPane.showMessageDialog(this, "Canvas width & height must be >= 200", "Validation Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Please fill out all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
   
	/**
	 * Return array of user-specified configuration options.
	 */
	public int[] getOptions() {
		int[] options = new int[] {
			Integer.parseInt(num_painters.getText()),		  // # painters
			Integer.parseInt(max_bargaining_power.getText()), // bargaining power
			Integer.parseInt(canvas_x.getText()),			  // canvas width
			Integer.parseInt(canvas_y.getText()),			  // canvas height
			Integer.parseInt(brush_size.getText()),			  // brush size
			Integer.parseInt(max_speed.getText())			  // max speed
		};
		return options;
	}
   
	/**
	 * Perform a clean exit.
	 */
	private void shutDown() {
		int rep = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to exit?",
				myAgent.getLocalName(),
				JOptionPane.YES_NO_CANCEL_OPTION);
     
		if (rep == JOptionPane.YES_OPTION) {
			GuiEvent ge = new GuiEvent(this, Portal.QUIT);
			myAgent.postGuiEvent(ge);
		}
	}
}
