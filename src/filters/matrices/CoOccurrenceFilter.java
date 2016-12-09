package filters.matrices;

import filters.Filter;
import image.Image;
import matrices.CoOccurrenceMatrix;
import similarity.distances.Distance;

import static similarity.distances.Distance.*;

public class CoOccurrenceFilter extends Filter{
	private int kernelSizeX = 7, kernelSizeY = 7;
	private Distance kernelRadialDistance = CHEBYSHEV_DISTANCE;
	private boolean computeBothOrientations = false;
	public int deltaX = 1, deltaY = 0;
	private float momentDegree = 2;
	
	private int operation = TYPE_ENTROPY;
	
	public static final int TYPE_ENERGY = 0, TYPE_CONTRAST = 1, TYPE_HOMOGENEITY = 2, 
			TYPE_ENTROPY = 3, TYPE_MOMENT = 4;
	
	public CoOccurrenceFilter(){
		
	}
	public CoOccurrenceFilter(final Image image){
		this.setImage(image);
	}

	public CoOccurrenceFilter(final Image image, final int operationType, final int kernelRadius, final Distance kernelDistanceMeasure, final int deltaX,
			final int deltaY){
		this.setImage(image);
		this.setOperationType(operationType);
		this.setKernelSize(kernelRadius);
		this.setKernelRadialDistanceMeasure(kernelDistanceMeasure);
		this.setDelta(deltaX, deltaY);
	}
	
	public CoOccurrenceFilter(final Image image, final int operationType, final int kernelRadius, final Distance kernelDistanceMeasure, final int deltaX,
			final int deltaY, final boolean computeBothOrientations){
		this.setImage(image);
		this.setOperationType(operationType);
		this.setKernelSize(kernelRadius);
		this.setKernelRadialDistanceMeasure(kernelDistanceMeasure);
		this.setDelta(deltaX, deltaY);
		this.setToComputeToBothOrientations(computeBothOrientations);
	}
	
	public CoOccurrenceFilter(final Image image, final int operationType, final int deltaX, final int deltaY){
		this.setImage(image);
		this.setOperationType(operationType);
		this.setDelta(deltaX, deltaY);
		this.setToComputeToBothOrientations(computeBothOrientations);
	}
	
	public CoOccurrenceFilter(final Image image, final int operationType, final int kernelSize, final int deltaX, final int deltaY){
		this.setImage(image);
		this.setOperationType(operationType);
		this.setKernelSize(kernelSize);
		this.setDelta(deltaX, deltaY);
		this.setToComputeToBothOrientations(computeBothOrientations);
	}
	
	public CoOccurrenceFilter(final Image image, final int operationType, final int kernelWidth, final int kernelHeight, final int deltaX, final int deltaY){
		this.setImage(image);
		this.setOperationType(operationType);
		this.setKernelHeight(kernelHeight);
		this.setKernelWidth(kernelWidth);
		this.setDelta(deltaX, deltaY);
		this.setToComputeToBothOrientations(computeBothOrientations);
	}
	
	
	/**
	 * Sets the equation to be computed from the local co-occurrence matrices.
	 * @param type
	 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
	 */
	public void setOperationType(final int type){
		this.operation = type;
	}
	/**
	 * Sets the neighbourhood size around the processed pixel.
	 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
	 */
	public void setKernelWidth(final int kernelWidth){
		this.kernelSizeX = kernelWidth % 2 == 0 ? kernelWidth + 1 : kernelWidth;
	}
	public void setKernelHeight(final int kernelHeight){
		this.kernelSizeX = kernelHeight % 2 == 0 ? kernelHeight + 1 : kernelHeight;
	}
	public void setKernelSize(final int kernelSize){
		this.setKernelHeight(kernelSize);
		this.setKernelWidth(kernelSize);
	}
	public void setKernelRadialDistanceMeasure(final Distance distance){
		this.kernelRadialDistance = distance;
	}
	public void setToComputeToBothOrientations(final boolean computeBothOrientations){
		this.computeBothOrientations = computeBothOrientations;
	}
	public void setDelta(final int deltaX, final int deltaY){
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	public void setMomentDegree(final float degree){
		this.momentDegree = degree;
	}
	
	
	@Override
	public double getFilteredPixel(Image image, int x, int y, int band) {
		Image neighImage = new Image(kernelSizeX, kernelSizeY, 1, 32, true);
		
		final int sX = kernelSizeX/2, sY = kernelSizeY/2;
		
		for (int i=y - sY; i <= y + sY; i++){
			for (int j=x - sX; j <= x + sX; j++){
				neighImage.setPixel(j - x + sX, i - y + sY, band, image.getPixelBoundaryMode(j, i, band));
			}
		}
		
		CoOccurrenceMatrix cMatrix = new CoOccurrenceMatrix(neighImage);
		cMatrix.setBand(band);
		cMatrix.setKernelRadialDistanceMeasure(kernelRadialDistance);
		cMatrix.setToComputeBothOrientations(computeBothOrientations);
		cMatrix.setDelta(deltaX, deltaY);
		
		double result = 0;
		
		switch(operation){
		case TYPE_ENERGY:
			result = cMatrix.getEnergy();
			break;
		case TYPE_CONTRAST:
			result = cMatrix.getContrast();
			break;
		case TYPE_HOMOGENEITY:
			result = cMatrix.getHomogeneity();
			break;
		case TYPE_ENTROPY:
			result = cMatrix.getEntropy();
			break;
		case TYPE_MOMENT:
			result = cMatrix.getMoment(momentDegree);
			break;
		}
		
		return result;
	}
	
	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		out.stretchOrShrinkRange(0, 255);
		return out;
	}

	
}
