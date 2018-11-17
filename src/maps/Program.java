package maps;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Program {
	
	public static void main(String[] args) {

		// Retrieve the singleton instance of the JADE Runtime:
		Runtime rt = Runtime.instance();
		
		// Create a container to host the agents:
		Profile p = new ProfileImpl();
		p.setParameter(Profile.MAIN_HOST, "localhost");
		//p.setParameter(Profile.GUI, "true");
		ContainerController cc = rt.createMainContainer(p);
		
		// Create & start Portal agent:
		if (cc != null)
			try {
				AgentController ac = cc.createNewAgent("Portal", "maps.Agents.Portal", null);
				ac.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	}
}
