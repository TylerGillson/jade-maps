package maps.Utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class Canvas extends JPanel {
	/** Source: https://stackoverflow.com/questions/38108506/animating-a-grid-of-pixels-in-java */
	private BufferedImage img;
	
	/**
	 * Create a new BufferedImage to draw onto and paint it white.
	 * @param w
	 * @param h
	 */
	public Canvas(int w, int h) {
		this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
	}
	
	/**
	 * Check whether all pixels have been coloured.
	 * @return boolean indicator
	 */
	public boolean is_complete() {
		for (int x = 0; x < this.img.getWidth(); x++) {
			for (int y = 0; y < this.img.getHeight(); y++) {
				Color c = new Color(this.img.getRGB(x, y));
				if (c.equals(Color.WHITE))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Draw a square of a particular colour at a particular location.
	 * @param x
	 * @param y
	 * @param brush_size
	 * @param c
	 */
	public void paintArea(int x, int y, int brush_size, Color c) {
		System.out.println("Painting at: " + String.valueOf(x) + ", " + String.valueOf(y));
		Graphics2D g = img.createGraphics();
		g.setColor(c);
		g.fillRect(x, y, brush_size, brush_size);
		g.dispose();
	    repaint();
	}
	
	/**
	 * Override parent method to draw BufferedImage on JFrame.
	 * @param g
	 */
	@Override
	public void paintComponent(Graphics g){
	    super.paintComponent(g);
	    g.drawImage(img, 0, 0, this);
	}
	
	public int getWidth() {return this.img.getWidth();}
	public int getHeight() {return this.img.getHeight();}
}