package maps.Utils;

import java.awt.Rectangle;

public class PositionHandler {
	private int width;
	private int height;
	private boolean debug = false;
	
	public PositionHandler(Object[] args) {
		width = (int) args[0];
		height = (int) args[1];;
	}
	
	/**
	 * Given a Painter's current position & velocity, calculate its next position.
	 * @param args current coordinate String
	 * @return coordinate String indicating new position
	 */
	public String updateCoordinates(String[] args) {
		int x = Integer.parseInt(args[0]);
		int vx = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int vy = Integer.parseInt(args[3]);
		int bs = Integer.parseInt(args[4]);
		
		// Calculate next position:
		int new_x = x + vx;
		int new_y = y + vy;
		
		// Handle x-axis boundaries:
		if (new_x < 0) {
			new_x = 0;
			vx = -vx;
			if (debug) System.out.println("Hit left wall ...");
		}
		else if (new_x + bs > width) {
			new_x = width - bs;
			vx = -vx;
			if (debug) System.out.println("Hit right wall ...");
		}
		
		// Handle y-axis boundaries:
		if (new_y < 0) {
			new_y = 0;
			vy = -vy;
			if (debug) System.out.println("Hit top wall ...");
		}
		else if (new_y + bs > height) {
			new_y = height - bs;
			vy = -vy;
			if (debug) System.out.println("Hit bottom wall ...");
		}
		
		// Build updated coordinates string:
		String new_coords = String.valueOf(new_x) + ":";
		new_coords += String.valueOf(vx) + ":";
		new_coords += String.valueOf(new_y) + ":";
		new_coords += String.valueOf(vy);
		return new_coords;
	}
	
	/**	
	 * Test if two rectangles overlap.
	 * @param a Rectangle 1
	 * @param b Rectangle 2
	 * @return boolean indicator
	 */
	public static boolean checkCollision(Rectangle a, Rectangle b) {
		return a.intersects(b);
	}
}
