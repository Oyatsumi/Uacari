package filters.morphology;

import static morphology.Morphology.FILLED_RING_STRUCT;

import filters.Filter;
import image.Image;
import log.Logger;
import morphology.Morphology;

public class Erosion extends Filter{
	private int timesToErode = 2;
	
	private Morphology morphology = null;
	private Image structuringElement = FILLED_RING_STRUCT;
	private Image resultImage = null;
	
	
	public Erosion(){
		
	}
	public Erosion(final Image image, final Image structuringElement, final int timesToErode){
		this.setImage(image);
		this.setStructuringElement(structuringElement);
		this.setTimesToErode(timesToErode);
	}
	
	public Erosion(final Image structuringElement, final int timesToErode){
		this.setStructuringElement(structuringElement);
		this.setTimesToErode(timesToErode);
	}
	
	
	
	public void setStructuringElement(final Image structuringElement){
		this.structuringElement = structuringElement;
	}
	public void setTimesToErode(final int timesToDilate){
		this.timesToErode = timesToDilate;
	}

	@Override
	public synchronized double getFilteredPixel(Image image, int x, int y, int band) {
		if (morphology == null){
			morphology = new Morphology();
			
			try {
				resultImage = morphology.erode(image, structuringElement, timesToErode);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logln(e.toString());
			}
			
		}
		
		return resultImage.getPixelBoundaryMode(x, y, band);
	}

	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		this.morphology = null;
		return out;
	}

}
