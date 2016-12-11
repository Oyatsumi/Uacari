package filters.misc;

import java.util.HashMap;

import filters.Filter;
import image.Image;
import similarity.distances.Distance;
import similarity.distances.EuclideanDistance;

/**
 * Entropy: draws a circle of radius r around each pixel; gets the histogram of that circle split in numBins chunks; then calculates the entropy as \sum_{p~\mathrm{in}~\mathrm{histogram}} -p*\mathrm{log}_2(p), where p is the probability of each chunk in the histogram..
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class EntropyFilter extends Filter {
	private float radius = 7;
	private int numOfChunks = 16;
	private Distance distanceMeasure = new EuclideanDistance();
	
	public EntropyFilter(){
		
	}
	
	public EntropyFilter(final int numOfChunks, final int kernelRadius, final Distance distance){
		this.setNumOfChunks(numOfChunks);
		this.setKernelRadius(kernelRadius);
		this.setDistance(distance);
	}
	
	public void setNumOfChunks(final int numOfChunks){
		this.numOfChunks = numOfChunks;
	}
	public void setKernelRadius(final int kernelRadius){
		this.radius = kernelRadius;
	}
	public void setDistance(final Distance distance){
		this.distanceMeasure = distance;
	}

	@Override
	public double getFilteredPixel(Image image, int x, int y, int band) {
		HashMap<Double, Integer> histogram = new HashMap<Double, Integer>();
		
		double minPixel = Integer.MAX_VALUE, maxPixel = Integer.MIN_VALUE;
		int counter = 0;
		//constructing the radial histogram
		for (int i=y - Math.round(radius); i<= y + radius; i++){
			for (int j=x - Math.round(radius); j<= x + radius; j++){
				
				if (distanceMeasure.compute(j, i, x, y) > radius) continue;
				
				final double pixel = image.getPixelBoundaryMode(j, i, band);
				counter ++;
				
				if (pixel > maxPixel) maxPixel = pixel;
				if (pixel < minPixel) minPixel = pixel;
				
				if (!histogram.containsKey(pixel)) histogram.put(pixel, 1);
				else{
					histogram.put(pixel, histogram.remove(pixel) + 1);
				}
				
			}
		}
		
		if (numOfChunks > counter) numOfChunks = counter;
		final int chunkSize = (int)Math.floor(counter/numOfChunks);
		
		double entropy = 0; double probChunk = 0;
		int pCounter = 0; int gCounter = 0;
		for (double pixel : histogram.keySet()){
			
			if (pCounter < chunkSize && gCounter < counter - 1){
				probChunk += histogram.get(pixel)/(float)counter;
				pCounter++;
				gCounter ++;
				continue;
			}
			gCounter++;
			
			pCounter = 0;
			
			entropy += -probChunk * (Math.log(probChunk)/(float)Math.log(2));
			
		    probChunk = 0;
		}
		
		return entropy;
	}
	
	public Image applyFilter(final Image image) {
		Image out = super.applyFilter(image);
		out.stretchOrShrinkRange(0, 255);
		return out;
	}

}
