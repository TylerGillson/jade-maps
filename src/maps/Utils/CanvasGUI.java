package maps.Utils;

import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	
	public void done() {
		JOptionPane.showMessageDialog(this, "Simulation complete!", "Complete", JOptionPane.OK_OPTION);
	}
}
