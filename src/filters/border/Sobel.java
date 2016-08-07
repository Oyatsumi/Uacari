package filters.border;

import filters.Filter;
import image.Image;

/**
 * Classifical Sobel filter
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class Sobel extends Filter{
	private boolean vertical = true, horizontal = true;
	private float weight = 1;
	private Image filterX = new Image(new short[][]{{-1,0,1},{-2,0,2},{-1,0,1}}), 
			filterY = new Image(new short[][]{{1,2,1},{0,0,0},{-1,-2,-1}});
	
	
	public Sobel(Image image, boolean applyHorizontally, boolean applyVertically){
		this.setImage(image);
		this.horizontal = applyHorizontally;
		this.vertical = applyVertically;
	}
	public Sobel(){	}
	public Sobel(boolean applyHorizontally, boolean applyVertically){
		this.vertical = applyVertically;
		this.horizontal = applyHorizontally;
	}
	
	public void setFilterX(Image filter){this.filterX = filter;}
	public void setFilterY(Image filter){this.filterY = filter;}

	@Override
	public Image apply(Image image) {
		for (int i=0; i<image.getHeight(); i++){
			for (int j=0; j<image.getWidth(); j++){
				
			}
		}
		return null;
	}
	
	

}
