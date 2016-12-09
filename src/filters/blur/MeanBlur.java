package filters.blur;

import filters.Filter;
import image.Image;
import similarity.distances.Distance;

import static similarity.distances.Distance.*;

import java.util.ArrayList;

public class MeanBlur extends Filter{
	private int kernelWidth = 7, kernelHeight = 7;
	
	private int operationType = TYPE_MAX;
	private Distance distance = EUCLIDEAN_DISTANCE;
	
	public static final int TYPE_ARITHMETIC_MEAN = 0, TYPE_GEOMETRIC_MEAN = 1, TYPE_MEDIAN = 2, TYPE_MAX = 3, TYPE_MIN = 4;
	
	
	public MeanBlur(){
		
	}
	public MeanBlur(final int kernelSize, final int operationType){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
	}
	public MeanBlur(final Image image, final int kernelSize, final int operationType){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
		this.setImage(image);
	}
	public MeanBlur(final Image image, final int kernelSize, final int operationType, final Distance radialDistance){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
		this.setImage(image);
		this.setRadialDistance(radialDistance);
	}
	public MeanBlur(final Image image, final int kernelWidth, final int kernelHeight, final int operationType, final Distance radialDistance){
		this.setKernelWidth(kernelWidth);
		this.setKernelHeight(kernelHeight);
		this.setOperationType(operationType);
		this.setImage(image);
		this.setRadialDistance(radialDistance);
	}
	
	public void setKernelSize(final int kernelSize){
		this.setKernelWidth(kernelSize);
		this.setKernelHeight(kernelSize);
	}
	
	public void setKernelWidth(final int kernelWidth){
		this.kernelWidth = kernelWidth;
		if (kernelWidth % 2 == 0) this.kernelWidth ++;
	}
	
	public void setKernelHeight(final int kernelHeight){
		this.kernelHeight = kernelHeight;
		if (kernelHeight % 2 == 0) this.kernelHeight ++;
	}
	
	public void setRadialDistance(final Distance distance){
		this.distance = distance;
	}

	public void setOperationType(final int operationType){
		this.operationType = operationType;
	}
	
	@Override
	public double getFilteredPixel(Image image, int x, int y, int band) {
		final int sX = kernelWidth/2,
				sY = kernelHeight/2;
		
		ArrayList<Double> values = null;
		if (operationType == TYPE_MEDIAN) values = new ArrayList<Double>(kernelWidth*kernelHeight);
		
		double result = 0;
		int counter = 0;
		boolean firstTime = true;
		for (int i= y - sY; i<= y + sY; i++){
			for (int j= x - sX; j<= x + sX; j++){
				
				if (distance.compute(x, y, j, i) > (kernelWidth/2f > kernelHeight/2f ? kernelWidth/2f : kernelHeight/2f)) continue;
				
				switch(operationType){
				case TYPE_ARITHMETIC_MEAN:
					result += image.getPixelBoundaryMode(j, i, band);
					break;
				case TYPE_GEOMETRIC_MEAN:
					if (firstTime) {result = 1; firstTime = false;}
					result *= image.getPixelBoundaryMode(j, i, band);
					break;
				case TYPE_MAX:
					if (firstTime) {result = Integer.MIN_VALUE; firstTime = false;}
					result = (image.getPixelBoundaryMode(j, i, band) > result) ? image.getPixelBoundaryMode(j, i, band) : result;
					break;
				case TYPE_MIN:
					if (firstTime) {result = Integer.MAX_VALUE; firstTime = false;}
					result = (image.getPixelBoundaryMode(j, i, band) < result) ? image.getPixelBoundaryMode(j, i, band) : result;
					break;
				case TYPE_MEDIAN:
					for (int k=0; k<values.size(); k++){
						if (values.get(k) > image.getPixelBoundaryMode(j, i, band))
							values.add(k, image.getPixelBoundaryMode(j, i, band));
					}
					break;
				}
				
				counter++;
			}
		}
		
		switch(operationType){
		case TYPE_ARITHMETIC_MEAN:
			result /= counter;
			break;
		case TYPE_GEOMETRIC_MEAN:
			result = Math.pow(result, 1f/(counter));
			break;
		case TYPE_MEDIAN:
			result = values.get(values.size()/2);
			break;
		}
		
		return result;
	}
	
}
