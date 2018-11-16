package maps.Utils;

import java.awt.Point;

public class PositionHandler {
	private int orig_x;
	private int orig_y;
	private int width;
	private int height;
	private boolean debug = false;
	
	public PositionHandler(Object[] args) {
		Point p = (Point) args[0]; // top-left corner of canvas frame
		orig_x = p.x;
		orig_y = p.y;
		width = (int) args[1];
		height = (int) args[2];;
	}
	
	public String updateCoordinates(String[] args) {
		if (debug) System.out.println("Orig: " + args[0] + ":" + args[2]);
		int x = Integer.parseInt(args[0]);
		int vx = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int vy = Integer.parseInt(args[3]);
		
		int new_x = x + vx;
		int new_y = y + vy;
		
		int max_x = orig_x + width;
		int min_y = orig_y - height;
		
		// Handle x-axis boundaries:
		if (new_x < orig_x) {
			new_x = orig_x + (orig_x - new_x);
			vx = -vx;
			vy = -vy;
			if (debug) System.out.println("Hit left wall ...");
		}
		else if (new_x > max_x) {
			new_x = max_x - (new_x - max_x);
			vx = -vx;
			vy = -vy;
			if (debug) System.out.println("Hit right wall ...");
		}
		
		// Handle y-axis boundaries:
		if (new_y > orig_y) {
			new_y = orig_y - (new_y - orig_y);
			vy = -vy;
			vx = -vx;
			if (debug) System.out.println("Hit top wall ...");
		}
		else if (new_y < min_y) {
			new_y = min_y + (min_y - new_y);
			vy = -vy;
			vx = -vx;
			if (debug) System.out.println("Hit bottom wall ...");
		}
		
		// Build updated coordinates string:
		String new_coords = String.valueOf(new_x) + ":";
		new_coords += String.valueOf(vx) + ":";
		new_coords += String.valueOf(new_y) + ":";
		new_coords += String.valueOf(vy);
		
		if (debug) System.out.println("New:  " + String.valueOf(new_x) + ":" + String.valueOf(new_y));
		return new_coords;
	}	
}
