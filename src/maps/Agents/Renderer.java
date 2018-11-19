package maps.Agents;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import maps.GUIs.CanvasGUI;
import maps.Utils.Canvas;
import maps.Utils.PositionHandler;

/**
 * The Renderer agent is responsible for handling paint requests from each Painter
 * agent. It also initializes the canvas GUI and responds to its requests.
 * @author tylergillson
 *
 */
public class Renderer extends GuiAgent {
	public static final int RESET = 0;
	
	transient protected CanvasGUI myGui;
	
	/**
	 * Internal class for simplifying data serialization.
	 * @author tylergillson
	 */
	public class PainterData {
		public int x;
		public int y;
		public int bs;
		public Color c;
		
		public PainterData(ACLMessage msg) {
			String[] data = msg.getContent().split(":");
			x = Integer.parseInt(data[0]);
			y = Integer.parseInt(data[1]);
			bs = Integer.parseInt(data[2]);
			c = Color.decode(data[3]);
		}
	}
	
	protected void setup() {
		Object[] args = getArguments();
		Canvas c = (Canvas) args[0];
		
		// Initialize painting window:
		myGui = new CanvasGUI(this, c);
		myGui.setVisible(true);
		
		/**
		 * Paint canvas according to Painter requests.
		 */
		addBehaviour(new CyclicBehaviour() {
			
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("PAINT_AREA");
				ArrayList<ACLMessage> messages = new ArrayList<ACLMessage>();
				ACLMessage msg;
				
				// Receive all painting requests:
				while (true) {
					msg = myAgent.receive(mt);
					if (msg != null)
						messages.add(msg);
					else
						break;
				}
				// If there are none, return to idle:
				if (messages.isEmpty())
					block();
				// Otherwise, detect Painter collisions & process requests:
				else {
					int len = messages.size();
					if (len > 1) {
						// For each pair of painters whose paint areas overlap,
						// send the AID of the pair's second Painter to the pair's
						// first Painter so that they may begin negotiating:
						ArrayList<Integer[]> collisions = getPainterCollisions(messages, len);
						for (Integer[] p : collisions) {
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(messages.get(p[0]).getSender());
							msg.setProtocol("COLLISION_DETECTED");
							msg.setContent(messages.get(p[1]).getSender().getLocalName());
							myAgent.send(msg);	
						}
					}
					
					// Process each paint request:
					Iterator<ACLMessage> it = messages.iterator();
					while (it.hasNext()) {
						msg = it.next();
						PainterData d = new PainterData(msg);
						paintAreaAsync(d.x, d.y, d.bs, d.c);	
					}
				}
			}
		});
		
		/**
		 * Check if the canvas has been entirely filled every 10 seconds.
		 */
		addBehaviour(new TickerBehaviour(this, 10000) {
			
			protected void onTick() {
				if (myGui.getCanvas().is_complete()) {
					myGui.done();
				}
			}
		});
		
		System.out.println("Renderer ready ...");
	}
	
	/**
	 * Given an array of paint request messages, return each pair of indices whose
	 * requested paint areas are overlapping.
	 * 
	 * @param msgs	a list of Agent messages containing paint requests
	 * @param len	length of msgs
	 * @return An ArrayList of indices from msgs.
	 */
	public ArrayList<Integer[]> getPainterCollisions(ArrayList<ACLMessage> msgs, int len) {
		ArrayList<Integer[]> result = new ArrayList<Integer[]>();
		// Get all possible message pairings:
		ArrayList<Integer[]> pairs = new ArrayList<Integer[]>();
		for (int i = 0; i < len - 1; i++) {
			for (int j = i + 1; j < len; j++) {
				pairs.add(new Integer[] {i,j});
			}
		}
		// Test each pair for paint overlap:
		for (Integer[] p : pairs) {
			PainterData d1 = new PainterData(msgs.get(p[0]));
			PainterData d2 = new PainterData(msgs.get(p[1]));
			Rectangle r1 = new Rectangle(d1.x, d1.y, d1.bs, d1.bs);
			Rectangle r2 = new Rectangle(d2.x, d2.y, d2.bs, d2.bs);
			if (PositionHandler.checkCollision(r1, r2))
				result.add(p);
		}
		return result;
	}
	
	/**
	 * Schedule a task for painting onto the canvas frame.
	 * @param x  	x coordinate of paint area
	 * @param y  	y coordinate of paint area
	 * @param bs	width & height of paint area
	 * @param c  	colour to paint
	 */
	public void paintAreaAsync(int x, int y, int bs, Color c) {
		Runnable task = new Runnable() {
			public void run() {
				myGui.getCanvas().paintArea(x, y, bs, c);
				myGui.repaint();
			}
		};
		SwingUtilities.invokeLater(task);
	}
	
	/**
	 * Respond to commands issued by CanvasGUI.
	 */
	protected void onGuiEvent(GuiEvent ev) {
		int command = ev.getType();
		
		// Ask the Portal agent to quit the simulation:
		if (command == RESET) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID("Portal", AID.ISLOCALNAME));
			msg.setProtocol("RESET_SIMULATION");
			this.send(msg);
		}	
	}
	
	protected void takeDown() {
		if (myGui != null) {
			myGui.setVisible(false);
			myGui.dispose();
		}
		System.out.println(getLocalName() + " is now shutting down.");
	}
}
