package maps.Utils;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class DirectoryHelper {
	
	/**
	 * Return an array of all registered painter agents.
	 */
	public static DFAgentDescription[] getPainters(Agent a) {
		DFAgentDescription[] result = null;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("PAINTER");
		template.addServices(sd);
		
		try {
			result = DFService.search(a, template);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Register an agent with the yellow pages service.
	 */
	public static void register(String service_type, Agent a) {
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service_type);
		sd.setName(a.getLocalName());
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(a.getAID());
		dfd.addServices(sd);
		
		try {
			DFService.register(a, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}	
	}
}
