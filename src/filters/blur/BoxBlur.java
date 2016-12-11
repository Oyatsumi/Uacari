package filters.blur;

import static similarity.distances.Distance.*;


import image.Image;
import similarity.distances.Distance;

public class BoxBlur extends MeanBlur{

	public BoxBlur(){
		super();
		super.setRadialDistance(CHEBYSHEV_DISTANCE);
	}
	
	public BoxBlur(final int kernelSize, final AverageType operationType){
		super(kernelSize, operationType);
	}
	public BoxBlur(final Image image, final int kernelSize, final AverageType operationType){
		super(image, kernelSize, operationType);
	}
	public BoxBlur(final Image image, final int kernelSize, final AverageType operationType, final Distance radialDistance){
		super(image, kernelSize, operationType, radialDistance);
	}
	public BoxBlur(final Image image, final int kernelWidth, final int kernelHeight, final AverageType operationType, final Distance radialDistance){
		super(image, kernelWidth, kernelHeight, operationType, radialDistance);
	}
	
}
