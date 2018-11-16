package maps.Agents;

import java.awt.Color;
import javax.swing.SwingUtilities;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import maps.Utils.Canvas;
import maps.Utils.CanvasGUI;

public class Renderer extends GuiAgent {
	transient protected CanvasGUI myGui;

	protected void setup() {
		Object[] args = getArguments();
		Canvas c = (Canvas) args[0];
				
		myGui = new CanvasGUI(this, c);
		myGui.setVisible(true);
		
		/**
		 * Paint canvas according to Painter requests.
		 */
		addBehaviour(new CyclicBehaviour() {
			
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("PAINT_AREA");
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					String[] data = msg.getContent().split(":");
					int x = Integer.parseInt(data[0]);
					int y = Integer.parseInt(data[1]);
					int bs = Integer.parseInt(data[2]);
					Color c = Color.decode(data[3]);
					paintAreaAsync(x, y, bs, c);
					//myGui.canvas.paintArea(x, y, bs, c);
					//myGui.canvas.repaint();
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Check if the canvas has been entirely filled every second.
		 */
		addBehaviour(new TickerBehaviour(this, 1000) {
			
			protected void onTick() {
				if (myGui.canvas.is_complete()) {
					myGui.done();
				}
			}
		});
	}
	
	protected void takeDown() {
		if (myGui != null) {
			myGui.setVisible(false);
			myGui.dispose();
		}
		System.out.println(getLocalName() + " is now shutting down.");
	}
	
	/**
	 * Schedule a task for painting onto the canvas frame.
	 * @param x
	 * @param y
	 * @param bs
	 * @param c
	 */
	public void paintAreaAsync(int x, int y, int bs, Color c) {
		Runnable task = new Runnable() {
			public void run() {
				myGui.canvas.paintArea(x, y, bs, c);
				myGui.repaint();
			}
		};
		SwingUtilities.invokeLater(task);
	}
	
	protected void onGuiEvent(GuiEvent ev) {}
}
