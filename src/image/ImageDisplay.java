package image;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A simple display for images using JFrame.
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class ImageDisplay extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Panel p = new Panel(this);
	
	ImageDisplay(){
		this.setLayout(new BorderLayout());
	    this.add(p, BorderLayout.CENTER);
	    this.setSize(500, 400);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void setImage(BufferedImage img){p.setImage(img); this.setVisible(true);}
	public void setImage(Image img) throws Exception{
		p.setImage(img); 
		if (img.getHeight() < 600 && img.getWidth() < 600)
			this.setSize(img.getWidth(), img.getHeight());
		else{
			int x = img.getWidth()/2; int y = img.getHeight()/2;
			while (x > 600 || y > 600){
				x /= 2;
				y /= 2;
			}
			this.setSize(x, y);
		}
		this.setVisible(true);
	}
	
	public class Panel extends JPanel{
		private static final long serialVersionUID = 1L;
		private BufferedImage image = null;
		private JFrame frame = null;
		
		Panel(JFrame frame){this.frame = frame;}
	
		/*
	    public ImageDisplay(BufferedImage img) {
	       try {                
	          image = ImageIO.read(new File("image name and path"));
	       } catch (IOException ex) {
	            // handle exception...
	       }
	    }*/
		public void setImage(Image img) throws Exception{
			this.image = img.getBufferedImage(); 
			this.frame.setTitle("(" + img.getWidth() + " x " + img.getHeight() + ") - N. of Bands: " + img.getNumBands() + " - Min Value (band 0): " + img.getMinimalIntesity(0) + " - Max Value (band 0): " + img.getMaximalIntensity(0));
			if (this.getGraphics() != null) this.paintComponent(this.getGraphics());
		}
		public void setImage(BufferedImage img){
			this.image = img; 
			this.frame.setTitle("(" + img.getWidth() + " x " + img.getHeight() + ")");
			if (this.getGraphics() != null) this.paintComponent(this.getGraphics());
		}
	
	    @Override
	    protected void paintComponent(Graphics g) {
	    	Graphics2D g2 = (Graphics2D) g;
	    	float rX = (frame.getWidth() - 18)/(float)image.getWidth(), rY = (frame.getHeight() - 40)/(float)image.getHeight();
	    	g2.scale(rX, rY);
	        super.paintComponent(g2);
	        g2.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
	    }
	}

}