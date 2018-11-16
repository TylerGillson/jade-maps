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
	private Color color_preference;
	
	protected void setup() {
		Object[] args = getArguments();
		x = (int) args[0];
		y = (int) args[1];
		vx = (int) args[2];
		vy = (int) args[3];
		bargaining_power = (int) args[4];
		brush_size = (int) args[5];
		color_preference = (Color) args[6];
		
		/**
		 * Paint surrounding area.
		 */
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {			  
				String data = String.valueOf(x) + ':' +
							  String.valueOf(y) + ':' +
							  String.valueOf(brush_size) + ':' +
							  getColourString();
					
				ACLMessage paint_msg = new ACLMessage(ACLMessage.REQUEST);
				paint_msg.addReceiver(new AID("Renderer", AID.ISLOCALNAME));
				paint_msg.setProtocol("PAINT_AREA");
				paint_msg.setContent(data);
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
					String coords = String.valueOf(x) + ':' +
									String.valueOf(vx) + ':' +
									String.valueOf(y) + ':' +
									String.valueOf(vy);
					
					// Send Navigator current coordinates:
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(new AID("Navigator", AID.ISLOCALNAME));
					msg.setProtocol("UPDATE_COORDINATES");
					msg.setContent(coords);
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
		
		System.out.println(getLocalName() + "ready ...");
	}
	
	/**
	 * Get a string representation of a Painter's preferred colour.
	 */
	public String getColourString() {
		String rgb = Integer.toHexString(color_preference.getRGB());
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