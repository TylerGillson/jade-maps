package maps.Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import maps.Utils.DirectoryHelper;
import maps.Utils.PositionHandler;

/**
 * The Navigator agent is responsible for handling the movement of each Painter agent.
 * @author tylergillson
 *
 */
public class Navigator extends Agent {
	private PositionHandler ph;
	
	protected void setup() {
		
		// Initialize movement & collision handler:
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
		 * Ask Painters to send their coordinates every quarter second.
		 */
		addBehaviour(new TickerBehaviour(this, 250) { 
			protected void onTick() {
				DFAgentDescription[] painters = DirectoryHelper.getPainters(myAgent);
				
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
	
	protected void takeDown() {
		System.out.println(getLocalName() + " is now shutting down.");
	}
}