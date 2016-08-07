package filters;

import image.Image;

public abstract class Filter {
	
	private Image associatedImage = null;

	public Image process(){
		return apply(associatedImage);
	}
	public abstract Image apply(Image image);
	
	public void setImage(Image image){
		this.associatedImage = image;
	}
	
	public Image getImage(){return this.associatedImage;}
}
