package maps.Agents;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.util.Pair;
import maps.Utils.Canvas;
import maps.Utils.CanvasGUI;
import maps.Utils.PositionHandler;

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
				
				if (messages.isEmpty())
					block();
				else {
					int num_messages = messages.size();
					if (num_messages > 1) {
						
						// Get all pairs:
						ArrayList<Pair<Integer,Integer>> pairs = new ArrayList<Pair<Integer,Integer>>();
						for (int i = 0; i < num_messages - 1; i++) {
							for (int j = i + 1; j < num_messages; j++) {
								pairs.add(new Pair<Integer,Integer>(i,j));
							}
						}
						
						ArrayList<ACLMessage> to_remove = new ArrayList<ACLMessage>();
						
						// Test each pair for paint overlap:
						for (Pair<Integer,Integer> p : pairs) {
							ACLMessage msg1 = messages.get(p.getKey());
							ACLMessage msg2 = messages.get(p.getValue());
							
							int x1, x2, y1, y2, w1, w2, h1, h2;
							String[] data1 = msg1.getContent().split(":");
							String[] data2 = msg2.getContent().split(":");
							
							x1 = Integer.parseInt(data1[0]);
							y1 = Integer.parseInt(data1[1]);
							w1 = Integer.parseInt(data1[2]);
							h1 = w1;
							
							x2 = Integer.parseInt(data2[0]);
							y2 = Integer.parseInt(data2[1]);
							w2 = Integer.parseInt(data2[2]);
							h2 = w2;
							
							Rectangle r1 = new Rectangle(x1, y1, w1, h1);
							Rectangle r2 = new Rectangle(x2, y2, w2, h2);
							
							if (PositionHandler.checkCollision(r1, r2)) {
								System.out.println("Paint collision!");
								to_remove.add(messages.get(p.getKey()));
								to_remove.add(messages.get(p.getValue()));
							}
						}
						
						// Remove paint requests from painters w/ overlap:
						messages.removeAll(to_remove);
					}
					
					Iterator<ACLMessage> it = messages.iterator();
					while (it.hasNext()) {
						msg = it.next();
						String[] data = msg.getContent().split(":");
						int x = Integer.parseInt(data[0]);
						int y = Integer.parseInt(data[1]);
						int bs = Integer.parseInt(data[2]);
						Color c = Color.decode(data[3]);
						paintAreaAsync(x, y, bs, c);	
					}
				}
			}
		});
		
		/**
		 * Check if the canvas has been entirely filled every minute.
		 */
		addBehaviour(new TickerBehaviour(this, 60000) {
			
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
