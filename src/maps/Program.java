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
		
		// Create the main container to host Portal agent & JADE agents:
		Profile p = new ProfileImpl();
		p.setParameter(Profile.MAIN_HOST, "localhost");
		//p.setParameter(Profile.GUI, "true");
		ContainerController cc = rt.createMainContainer(p);
		
		// Init Portal agent:
		if (cc != null)
			bootAgent(cc, "Portal", "maps.Agents.Portal", null);
	}
	
	/**
	 * Initialize a new agent within a specified container.
	 */
	public static void bootAgent(ContainerController cc, String name, String class_str, Object[] args) {
		if (args != null && args.length == 7)
			System.out.println(name + " booting ... " + args[6].toString());
		
		try {
			AgentController ac = cc.createNewAgent(name, class_str, args);
			ac.start();	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
