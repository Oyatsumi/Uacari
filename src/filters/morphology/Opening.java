package filters.morphology;


import filters.Filter;
import image.Image;
import morphology.MorphologyConstants;

public class Opening extends Filter implements MorphologyConstants{

	protected Image resultImage = null;
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
			
			Erosion ero = new Erosion(structuringElement, timesToErode);
			
			resultImage = ero.applyFilter(image);
			
			Dilation dil = new Dilation(structuringElement, timesToDilate);
			
			resultImage = dil.applyFilter(resultImage);
			
		}
		
		return resultImage.getPixel(x, y, band);
	}
	
	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		this.resultImage = null;
		return out;
	}
}
