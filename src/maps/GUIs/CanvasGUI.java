package maps.GUIs;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jade.gui.GuiEvent;
import maps.Agents.Renderer;
import maps.Utils.Canvas;

public class CanvasGUI extends JFrame {
	private Canvas canvas;
	private Renderer myAgent;
	
	public CanvasGUI(Renderer renderer_agent, Canvas c) {
		myAgent = renderer_agent;
		canvas = c;
		setTitle("Canvas");
		getContentPane().add(c);
		
		// Add exit listener:
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutDown();
			}
		});
				
		// Configure frame, ensuring that content pane dimensions match canvas dimensions:
		getContentPane().setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));
		pack();
		
		// Position frame, prevent re-sizing, prevent close on exit:
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Rectangle r = getGraphicsConfiguration().getBounds();
		setLocation(r.x + (r.width - getWidth())/2,
					r.y + (r.height - getHeight())/2);
	}
	
	/**
	 * Ask the user whether they want to let the simulation continue, or reset.
	 */
	public void done() {
		int input = JOptionPane.showOptionDialog(null,
			"Press OK to Reset, or Cancel to continue.",
			"Painting Complete!",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		// Reset simulation:
		if (input == JOptionPane.OK_OPTION)
			resetSimulation();
	}
	
	/**
	 * Perform a clean exit when user closes canvas frame.
	 */
	private void shutDown() {
		int rep = JOptionPane.showOptionDialog(null,
				"Are you sure you want to exit?",
				"Close Simulation",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		// Reset simulation:
		if (rep == JOptionPane.YES_OPTION)
			resetSimulation();
	}
	
	/**
	 * Ask the Renderer to reset the simulation.
	 */
	private void resetSimulation() {
		GuiEvent ge = new GuiEvent(this, Renderer.RESET);
		myAgent.postGuiEvent(ge);
	}
	
	public Canvas getCanvas() {return canvas;}
}
