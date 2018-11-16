package maps.Agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class JadeDemo extends Agent {
	
	protected void setup() {
		System.out.println("Hello World, I'm a Test Agent!");
		
		addBehaviour(new TickerBehaviour(this, 10000) {
			protected void onTick() {
				System.out.println("Test Agent ticking ...");
			}
		});
	}
}


