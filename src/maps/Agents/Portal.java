package maps.Agents;

import java.awt.Color;
import java.util.Random;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import maps.Utils.Canvas;
import maps.Utils.PortalGUI;

public class Portal extends GuiAgent {
	public static final int WAIT = -1;
	public static final int QUIT = 0;
	public static final int START = 1;
	public static final int RESET = 2;
	
	private int command = WAIT;
	private Random rand = new Random();
	private ContainerController cc;
	
	transient protected PortalGUI myGui;

	protected void setup() {
		startSimulationContainer();	
		myGui = new PortalGUI(this);
		myGui.setVisible(true);
		System.out.println("Portal ready ...");
	}
	
	protected void takeDown() {
		if (myGui != null) {
			myGui.setVisible(false);
			myGui.dispose();
		}
		System.out.println(getLocalName() + " is now shutting down.");
	}
	
	/**
	 * Create a new agent container to hold Navigator + Painters.
	 */
	public void startSimulationContainer() {
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl();
		p.setParameter(Profile.MAIN_HOST, "localhost");
		p.setParameter(Profile.CONTAINER_NAME, "SimulationAgents");
		cc = rt.createAgentContainer(p);
	}
    
	/**
	 *  Respond to various commands issued by PortalGUI.
	 */
	protected void onGuiEvent(GuiEvent ev) {
		command = ev.getType();
		if (command == QUIT) {
			doDelete();
			System.exit(0);
		}
		else if (command == START) {
			// Re-create container if a reset has occurred:
			if (cc == null)
				startSimulationContainer();
			
			int[] options = myGui.getOptions();  // get configuration options from GUI
			
			// Init Renderer agent:
			Object[] args = new Object[] {new Canvas(options[2], options[3])};
			bootAgent("Renderer", "maps.Agents.Renderer", args);
			
			// Init Navigator agent:
			args = new Object[] {options[2], options[3]};
			bootAgent("Navigator", "maps.Agents.Navigator", args);
			
			// Spawn Painter agents:
			spawnPainters(options);
		}
		else if (command == RESET) {
			Thread t = new Thread() {
				public void run() {
					try {
						cc.kill();
						cc = null;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				} 
			}; 
			t.start(); 
		}
	}
	
	/**
	 * Attempt to start a new agent within the current container.
	 */
	public void bootAgent(String name, String class_str, Object[] args) {
		try {
			AgentController ac = cc.createNewAgent(name, class_str, args);
			ac.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize a fixed number of new Painter agents with random metrics.
	 * @param num_painters
	 * @param max_bargaining_power
	 * @param brush_size
	 */
	public void spawnPainters(int[] args) {
		int num_painters = args[0];
		int max_bargaining_power = args[1];
		int canvas_width = args[2];
		int canvas_height = args[3];
		int brush_size = args[4];
		int max_speed = args[5];
		
		String p_name;
		Object[] p_args = new Object[7];
		
		for (int i = 0; i < num_painters; i++) {
			// Generate parameters:
			p_name = "p" + String.valueOf(i);
			p_args[0] = rand.nextInt(canvas_width);   		// painter x coordinate
			p_args[1] = rand.nextInt(canvas_height);  		// painter y coordinate
			p_args[2] = 1 + rand.nextInt(max_speed);		// painter x velocity
			p_args[3] = 1 + rand.nextInt(max_speed);		// painter y velocity
			p_args[4] = rand.nextInt(max_bargaining_power); // bargaining power
			p_args[5] = brush_size;							// brush size
			p_args[6] = new Color(rand.nextInt(255),		// painter colour preference
								  rand.nextInt(255),
								  rand.nextInt(255)); 		 
			bootAgent(p_name, "maps.Agents.Painter", p_args);  // boot up painter agent
		}
	}
}
