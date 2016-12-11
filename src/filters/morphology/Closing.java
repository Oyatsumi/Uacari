package filters.morphology;

import filters.Filter;
import image.Image;
import morphology.MorphologyConstants;

public class Closing extends Filter implements MorphologyConstants{
	
	private Image resultImage = null;
	private Image structuringElement = STRUCT_PRIMARY;
	private int timesToDilate = 3, timesToErode = 3;
	
	public void setStructuringElement(final Image structuringElement){
		this.structuringElement = structuringElement;
	}
	
	public void setTimesToDilate(final int timesToDilate){
		this.timesToDilate = timesToDilate;
	}
	
	public void setTimesToErode(final int timesToErode){
		this.timesToErode = timesToErode;
	}

	@Override
	public double getFilteredPixel(Image image, int x, int y, int band) {
		if (resultImage == null){
			Dilation dil = new Dilation(structuringElement, timesToDilate);
			
			resultImage = dil.applyFilter(image);
			
			Erosion ero = new Erosion(structuringElement, timesToErode);
			
			resultImage = ero.applyFilter(resultImage);
		}
		
		return resultImage.getPixel(x, y, band);
	}

	
	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		this.resultImage = null;
		return out;
	}
}
