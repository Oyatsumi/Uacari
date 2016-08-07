package filters.border;

import filters.Filter;
import image.Image;

/**
 * A modification of the Sobel filter, this algorithm is presented in (http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=7502723&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D7502723)
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class RodriguesSobel extends Filter {
	private int distance = 5;
	private float threshold = -10;
	private boolean shadow = false, light = true, performOnX = true, performOnY = true;
	
	public RodriguesSobel(Image image){this.setImage(image);}
	
	public RodriguesSobel(){}
	
	public RodriguesSobel(int distance, float threshold){
		this.distance = distance;
	}
	
	public RodriguesSobel(Image image, int distance, float threshold){
		this.setImage(image);
		this.distance = distance;
		this.threshold = threshold;
	}
	
	public RodriguesSobel(Image image, int distance, float threshold, boolean paintShadow, boolean paintLight,
			boolean performOnX, boolean performOnY){
		this.distance = distance;
		this.threshold = threshold;
		this.shadow = paintShadow;
		this.light = paintLight;
		this.performOnX = performOnX;
		this.performOnY = performOnY;
	}
	
	public void setShadowLight(boolean paintShadow, boolean paintLight){
		if (!paintShadow && !paintLight) return;
		this.shadow = paintShadow;
		this.light = paintLight;
	}
	public void setDistance(int distance){
		this.distance = distance;
	}
	public void setToPerformOnAxes(boolean performOnX, boolean performOnY){
		if (!performOnX && !performOnY) return;
		this.performOnX = performOnX;
		this.performOnY = performOnY;
	}
	/**
	 * The threshold can be negative
	 * @param threshold
	 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
	 */
	public void setThreshold(float threshold){
		this.threshold = threshold;
	}

	@Override
	public Image apply(Image image) {
		Image out = new Image(image.getWidth(), image.getHeight(), image.getNumBands(), 1);
		for (int b=0; b<image.getNumBands(); b++){
			for(int i=0; i<image.getHeight(); i++){
				for (int j=0; j<image.getWidth(); j++){
					boolean paint = false;
					if (shadow){
						if (performOnX && performOnY){
							if (image.getPixelBoundaryMode(j - distance, i, b) - image.getPixelBoundaryMode(j, i, b) > threshold &&
									image.getPixelBoundaryMode(j + distance, i, b) - image.getPixelBoundaryMode(j, i, b) > threshold &&
									image.getPixelBoundaryMode(j, i - distance, b) - image.getPixelBoundaryMode(j, i, b) > threshold &&
									image.getPixelBoundaryMode(j, i + distance, b) - image.getPixelBoundaryMode(j, i, b) > threshold){
								paint = true;
							}
						}
						else if (performOnX){
							if (image.getPixelBoundaryMode(j - distance, i, b) - image.getPixelBoundaryMode(j, i, b) > threshold &&
									image.getPixelBoundaryMode(j + distance, i, b) - image.getPixelBoundaryMode(j, i, b) > threshold){
								paint = true;
							}
						}else{
							if (image.getPixelBoundaryMode(j, i - distance, b) - image.getPixelBoundaryMode(j, i, b) > threshold &&
									image.getPixelBoundaryMode(j, i + distance, b) - image.getPixelBoundaryMode(j, i, b) > threshold){
								paint = true;
							}
						}
					}
					
					if (light){//light
						if (performOnX && performOnY){
							if (image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j - distance, i, b) > threshold &&
									image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j + distance, i, b)> threshold &&
									image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j, i - distance, b) > threshold &&
									image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j, i + distance, b)> threshold){
								paint = true;
							}
						}
						else if (performOnX){
							if (image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j - distance, i, b) > threshold &&
									image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j + distance, i, b) > threshold){
								paint = true;
							}
						}else{
							if (image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j, i - distance, b) > threshold &&
									image.getPixelBoundaryMode(j, i, b) - image.getPixelBoundaryMode(j, i + distance, b)  > threshold){
								paint = true;
							}
						}
					}
					
					if (paint){
						out.setPixel(j, i, b, 1);
					}
				}
			}
		}
		return out;
	}
	
	
	
}
