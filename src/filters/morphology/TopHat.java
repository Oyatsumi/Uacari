package filters.morphology;

import image.Image;

public class TopHat extends Opening{
	
	private boolean firstTime = true;

	@Override
	public double getFilteredPixel(Image image, int x, int y, int band) {
		super.getFilteredPixel(image, x, y, band);
		
		if (firstTime){
			image.subtract(super.resultImage);
			firstTime = false;
		}
		
		return image.getPixelBoundaryMode(x, y, band);
	}
	

	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		this.firstTime = true;
		return out;
	}

}
