package filters.blur;

import filters.Filter;
import image.Image;
import similarity.distances.Distance;

import static similarity.distances.Distance.*;

import java.util.ArrayList;


interface MeanBlurConstants {
	public static enum AverageType{TYPE_ARITHMETIC_MEAN, TYPE_GEOMETRIC_MEAN, TYPE_MEDIAN, TYPE_MAX, TYPE_MIN;}
	public static final AverageType TYPE_MAX = AverageType.TYPE_MAX, TYPE_ARITHMETIC_MEAN = AverageType.TYPE_ARITHMETIC_MEAN,
			TYPE_GEOMETRIC_MEAN = AverageType.TYPE_GEOMETRIC_MEAN, TYPE_MEDIAN = AverageType.TYPE_MEDIAN,
			TYPE_MIN = AverageType.TYPE_MIN;
}

public class MeanBlur extends Filter implements MeanBlurConstants{
	private int kernelWidth = 7, kernelHeight = 7;
	
	private AverageType operationType = TYPE_MAX;
	private Distance distance = EUCLIDEAN_DISTANCE;
	private boolean update = true;
	
	public MeanBlur(){
		
	}
	public MeanBlur(final int kernelSize, final AverageType operationType){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
	}
	public MeanBlur(final Image image, final int kernelSize, final AverageType operationType){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
		this.setImage(image);
	}
	public MeanBlur(final Image image, final int kernelSize, final AverageType operationType, final Distance radialDistance){
		this.setKernelSize(kernelSize);
		this.setOperationType(operationType);
		this.setImage(image);
		this.setRadialDistance(radialDistance);
	}
	public MeanBlur(final Image image, final int kernelWidth, final int kernelHeight, final AverageType operationType, final Distance radialDistance){
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

	public void setOperationType(final AverageType operationType){
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
		
		for (int i= y - sY; i<= y + sY; i++){
			for (int j= x - sX; j<= x + sX; j++){
				
				if (distance.compute(x, y, j, i) > (kernelWidth/2f > kernelHeight/2f ? kernelWidth/2f : kernelHeight/2f)) continue;
				
				switch(operationType){
				case TYPE_ARITHMETIC_MEAN:
					result += image.getPixelBoundaryMode(j, i, band);
					break;
				case TYPE_GEOMETRIC_MEAN:
					if (update) {result = 1; update = false;}
					result *= image.getPixelBoundaryMode(j, i, band);
					break;
				case TYPE_MAX:
					if (update) {result = Integer.MIN_VALUE; update = false;}
					result = (image.getPixelBoundaryMode(j, i, band) > result) ? image.getPixelBoundaryMode(j, i, band) : result;
					break;
				case TYPE_MIN:
					if (update) {result = Integer.MAX_VALUE; update = false;}
					result = (image.getPixelBoundaryMode(j, i, band) < result) ? image.getPixelBoundaryMode(j, i, band) : result;
					break;
				case TYPE_MEDIAN:
					if (values.size() == 0) values.add(image.getPixelBoundaryMode(j, i, band));
					else{
						For:
						for (int k=0; k<values.size(); k++){
							if (values.get(k) > image.getPixelBoundaryMode(j, i, band)){
								values.add(k, image.getPixelBoundaryMode(j, i, band));
								break For;
							}
						}
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
	
	public Image applyFilter(final Image image) {
		update = true;
		return super.applyFilter(image);
	}
}
