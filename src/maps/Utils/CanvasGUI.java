package maps.Utils;

import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jade.gui.GuiEvent;
import maps.Agents.Renderer;

public class CanvasGUI extends JFrame {
	public Canvas canvas;
	private Renderer myAgent;
	
	public CanvasGUI(Renderer renderer_agent, Canvas c) {
		myAgent = renderer_agent;
		canvas = c;
		setTitle("Canvas");
		getContentPane().add(c);
	  
		// Configure frame:
		setSize(c.getWidth(), c.getHeight());
		setResizable(false);
		Rectangle r = getGraphicsConfiguration().getBounds();
		setLocation(r.x + (r.width - getWidth())/2,
					r.y + (r.height - getHeight())/2);
	}
	
	/**
	 * Ask the user whether they want to let the simulation continue, or reset.
	 */
	public void done() {
		int input = JOptionPane.showOptionDialog(null,
			"Press Ok to Reset, or Cancel to continue.",
			"Painting Complete!",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE, null, null, null);
		
		// Reset simulation:
		if (input == JOptionPane.OK_OPTION) {
			GuiEvent ge = new GuiEvent(this, Renderer.RESET);
			myAgent.postGuiEvent(ge);
		}
	}
}
