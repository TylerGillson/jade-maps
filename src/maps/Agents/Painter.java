package maps.Agents;

import java.awt.Color;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import maps.Utils.DirectoryHelper;

/**
 * Each Painter agent interacts with the Navigator agent to move around the canvas,
 * the Renderer agent to submit paint requests, and other Painter agents whenever 
 * an overlap occurs between their paint areas. When two Painters negotiate, the 
 * winner is determined based on the values of their "bargaining power" attributes.
 * The losing Painter adopts the winning Painter's colour, shrinks its brush size by
 * the difference between their bargaining powers, and increases its own bargaining
 * power by that same difference. In that way, losing Painters are "incentivized" to
 * win future negotiations and preserve their current colour.
 * @author tylergillson
 *
 */
public class Painter extends Agent {
	private int x;
	private int y;	
	private int vx;
	private int vy;
	private int bargaining_power;
	private int brush_size;
	private Color colour_preference;
	private Random r;
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
		r = new Random();
		
		// Register w/ yellow pages service:
		DirectoryHelper.register("PAINTER", this);		
		
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
					
					// Determine difference in bargaining powers:
					int diff = Math.abs(Integer.parseInt(data[1]) - bargaining_power);
					if (diff == 0) {diff = r.nextInt((bargaining_power > 0) ? bargaining_power : 1);}
					
					// Prepare generic message:
					String otherPainterAID = msg.getConversationId();
					msg = new ACLMessage(ACLMessage.UNKNOWN);
					msg.addReceiver(new AID(otherPainterAID, AID.ISLOCALNAME));
					msg.setProtocol("NEGOTIATION");
					msg.setContent(getCollisionNegotiationString());
					msg.setConversationId(String.valueOf(diff));  // light abuse of JADE's communication system ...
					
					// If other Painter has a higher bargaining power, tell it to grow, then shrink & adopt its color:
					if (Integer.parseInt(data[1]) > bargaining_power) {
						msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						myAgent.send(msg);
						brush_size -= diff;
						bargaining_power += diff;
						colour_preference = Color.decode(data[2]);
						if (debug) System.out.println("P2 LOST, ADOPTING P1's PREFERENCES");
					}
					// Otherwise, grow, then tell other Painter to shrink & adopt this Painter's color:
					else {
						brush_size += diff;
						msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
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
		 * Handle reception of negotiation update message from other Painter.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchProtocol("NEGOTIATION"),
						MessageTemplate.or(
								MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
								MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)));
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					String[] data = msg.getContent().split(":");
					int diff = Integer.parseInt(msg.getConversationId());
					
					// Grow if other Painter accepted:
					if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						brush_size += diff;
					}
					// Otherwise, shrink & adopt other Painter's color:
					else {
						brush_size -= diff;
						bargaining_power -= diff;
						colour_preference = Color.decode(data[2]);
						if (debug) System.out.println("P1 LOST, ADOPTING P2's PREFERENCES");
					}
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Tell Renderer to paint surrounding area every quarter second.
		 */
		addBehaviour(new TickerBehaviour(this, 250) {
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
		
		System.out.println(getLocalName() + " ready ... ");
	}
	
	/**
	 * Generate a String containing collision negotiation information for another Painter.
	 */
	public String getCollisionNegotiationString() {
		String s = String.valueOf(brush_size) + ':' +
				   String.valueOf(bargaining_power) + ':' +
				   getColourString(colour_preference);
		return s;
	}
	
	/**
	 * Generate a String containing information for Renderer.
	 */
	public String getRendererString() {
		String s = String.valueOf(x) + ':' +
				   String.valueOf(y) + ':' +
				   String.valueOf(brush_size) + ':' +
				   getColourString(colour_preference);
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
	 * Get a string representation of a Painter's colour.
	 */
	public String getColourString(Color c) {
		String rgb = Integer.toHexString(c.getRGB());
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