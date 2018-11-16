package maps.Agents;

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

import maps.Utils.PositionHandler;

public class Navigator extends Agent {
	private PositionHandler ph;
	
	protected void setup() {
		ph = new PositionHandler(getArguments());
		
		/**
		 * Update Painter coordinates whenever they are received,
		 * then return updated coordinates to Painter.
		 */
		addBehaviour(new CyclicBehaviour() {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("UPDATE_COORDINATES");
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					AID painter = msg.getSender();
					String[] args = msg.getContent().split(":");
					String new_coords = ph.updateCoordinates(args);
					
					// Send Painter new coordinates:
					msg = new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(painter);
					msg.setProtocol("UPDATE_COORDINATES");
					msg.setContent(new_coords);
					myAgent.send(msg);
				}
				else {
					block();
				}
			}
		});
		
		/**
		 * Ask Painters to send their coordinates every second.
		 */
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {
				DFAgentDescription[] painters = getPainters();
				
				for (DFAgentDescription p : painters) {
					ACLMessage painter_coords_msg = new ACLMessage(ACLMessage.REQUEST);
					painter_coords_msg.addReceiver(p.getName());
					painter_coords_msg.setProtocol("GET_COORDINATES");
					myAgent.send(painter_coords_msg);
				}
			}
		});
		
		System.out.println("Navigator ready ...");
	}
	
	/**
	 * Return an array of all registered painter agents.
	 */
	public DFAgentDescription[] getPainters() {
		DFAgentDescription[] result = null;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("PAINTER");
		template.addServices(sd);
		
		try {
			result = DFService.search(this, template);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
	}
	
	protected void takeDown() {
		System.out.println(getLocalName() + " is now shutting down.");
	}
}