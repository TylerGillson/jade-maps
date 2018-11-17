package maps.Agents;

import java.awt.Color;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Painter extends Agent {
	// Position & velocity:
	public int x;
	public int y;	
	public int vx;
	public int vy;
	
	private int bargaining_power;
	private int brush_size;
	private Color colour_preference;
	private boolean debug = false;
	
	protected void setup() {
		Object[] args = getArguments();
		x = (int) args[0];
		y = (int) args[1];
		vx = (int) args[2];
		vy = (int) args[3];
		bargaining_power = (int) args[4];
		brush_size = (int) args[5];
		colour_preference = (Color) args[6];
		
		/**
		 * Handle reception of collision detected message from Renderer.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("COLLISION_DETECTED");
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					// Send other Painter current velocity, bargaining_power, and colour:
					String otherPainterAID = msg.getContent();
					msg = new ACLMessage(ACLMessage.PROPOSE);
					msg.addReceiver(new AID(otherPainterAID, AID.ISLOCALNAME));
					msg.setProtocol("NEGOTIATION");
					msg.setConversationId(myAgent.getLocalName());
					msg.setContent(getCollisionNegotiationString());
					myAgent.send(msg);
					if (debug) System.out.println("NEGOTIATION BEGIN ...");
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Handle reception of negotiation message from other Painter.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchProtocol("NEGOTIATION"),
						MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					String[] data = msg.getContent().split(":");
					int diff = Math.abs(Integer.parseInt(data[1]) - bargaining_power);
					
					// If other Painter has a higher bargaining power, shrink & adopt its color:
					if (Integer.parseInt(data[1]) > bargaining_power) {
						brush_size -= diff;
						colour_preference = Color.decode(data[2]);
						if (debug) System.out.println("P2 LOST, ADOPTING P1's PREFERENCES");
					}
					// Otherwise, grow, then tell other Painter to shrink & adopt this Painter's color:
					else {
						brush_size += diff;
						String otherPainterAID = msg.getConversationId();
						msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						msg.addReceiver(new AID(otherPainterAID, AID.ISLOCALNAME));
						msg.setProtocol("NEGOTIATION");
						msg.setContent(getCollisionNegotiationString());
						myAgent.send(msg);
						if (debug) System.out.println("NEGOTIATION REJECTED ...");
					}
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Handle reception of failed negotiation message from other Painter.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchProtocol("NEGOTIATION"),
						MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					// Shrink & adopt other Painter's color:
					String[] data = msg.getContent().split(":");
					brush_size -= (Integer.parseInt(data[0]) - bargaining_power);
					colour_preference = Color.decode(data[2]);
					if (debug) System.out.println("P1 LOST, ADOPTING P2's PREFERENCES");
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Tell Renderer to paint surrounding area.
		 */
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {			  
				// Send Renderer current position, brush size, & colour:
				ACLMessage paint_msg = new ACLMessage(ACLMessage.REQUEST);
				paint_msg.addReceiver(new AID("Renderer", AID.ISLOCALNAME));
				paint_msg.setProtocol("PAINT_AREA");
				paint_msg.setContent(getRendererString());
				myAgent.send(paint_msg);
			}
		});
		
		/**
		 * Send Navigator current coordinates when requested.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("GET_COORDINATES");
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					// Send Navigator current position & velocity:
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(new AID("Navigator", AID.ISLOCALNAME));
					msg.setProtocol("UPDATE_COORDINATES");
					msg.setContent(getNavigatorString());
					myAgent.send(msg);
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Handle reception of updated coordinates from Navigator.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("UPDATE_COORDINATES");
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					String[] args = msg.getContent().split(":");
					x = Integer.parseInt(args[0]);
					vx = Integer.parseInt(args[1]);
					y = Integer.parseInt(args[2]);
					vy = Integer.parseInt(args[3]);
				}
				else {
					block();
				}
			}
		});
		
		// Register w/ yellow pages service:
		ServiceDescription sd = new ServiceDescription();
		sd.setType("PAINTER");
		sd.setName(getLocalName());
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Print Painter created message:
		System.out.println(getLocalName() + " ready ... " + colour_preference.toString() );
	}
	
	/**
	 * Generate a String containing collision negotiation information for another Painter.
	 */
	public String getCollisionNegotiationString() {
		String s = String.valueOf(brush_size) + ':' +
				   String.valueOf(bargaining_power) + ':' +
				   getColourString();
		return s;
	}
	
	/**
	 * Generate a String containing information for Renderer.
	 */
	public String getRendererString() {
		String s = String.valueOf(x) + ':' +
				   String.valueOf(y) + ':' +
				   String.valueOf(brush_size) + ':' +
				   getColourString();
		return s;
	}
	
	/**
	 * Generate a String containing information for Navigator.
	 */
	public String getNavigatorString() {
		String s = String.valueOf(x) + ':' +
				   String.valueOf(vx) + ':' +
				   String.valueOf(y) + ':' +
				   String.valueOf(vy) + ':' +
				   String.valueOf(brush_size);
		return s;
	}
	
	/**
	 * Get a string representation of a Painter's preferred colour.
	 */
	public String getColourString() {
		String rgb = Integer.toHexString(colour_preference.getRGB());
		rgb = "#" + rgb.substring(2, rgb.length());
		return rgb;
	}
	
	public void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println(getLocalName() + " is now shutting down.");
	}
}