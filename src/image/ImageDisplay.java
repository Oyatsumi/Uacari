package image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A simple display for images using JFrame.
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class ImageDisplay extends JFrame{

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Panel panel = new Panel(this);
	private String title = "";
	
	ImageDisplay(){
		this.setLayout(new BorderLayout());
	    this.add(panel, BorderLayout.CENTER);
	    this.setSize(500, 400);
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void setTitle(final String title){this.title = title; super.setTitle(title);}
	public void appendTitle(final String title){super.setTitle(this.title + title);}
	public void setImage(BufferedImage img){panel.setImage(img); this.setVisible(true);}
	public void setImage(Image img) throws Exception{
		
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		
		panel.setImage(img); 
		if (img.getHeight() < height/2 && img.getWidth() < width/2)
			if (img.getWidth() < 200 && img.getHeight() < 100) {
				this.setSize(200 + 200, 100);
			}
			else {
				this.setSize(img.getWidth(), img.getHeight());
			}
		else{
			int x = img.getWidth()/2; int y = img.getHeight()/2;
			while (x > width/2 || y > height/2){
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
		private ImageDisplay frame = null;
		
		Panel(ImageDisplay frame){this.frame = frame;}
	
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
			this.frame.appendTitle(" (" + img.getWidth() + " x " + img.getHeight() + ") - N. of Bands: " + img.getNumBands() + " - Min Value (band 0): " + img.getMinimalIntesity(0) + " - Max Value (band 0): " + img.getMaximalIntensity(0));
			if (this.getGraphics() != null) this.paintComponent(this.getGraphics());
		}
		public void setImage(BufferedImage img){
			this.image = img; 
			this.frame.appendTitle(" (" + img.getWidth() + " x " + img.getHeight() + ")");
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