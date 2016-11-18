package similarity;

import general.Image;

import java.math.BigDecimal;

public class SumOfDifferences {

	
	
	
	public static BigDecimal sumOfDifferences(Image image1, Image image2, float g){
		BigDecimal sum = BigDecimal.valueOf(0);
		for (int b=0; b<image1.getNumBands(); b++){
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image2.getWidth(); j++){
					sum = sum.add(BigDecimal.valueOf(Math.abs(Math.pow(image1.getPixel(j, i, b)-image2.getPixel(j, i, b), g))));
				}
			}
		}
		return sum;
	}
	public static long fastSumOfDifferences(Image image1, Image image2, float g){
		long sum = 0;
		for (int b=0; b<image1.getNumBands(); b++){
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image2.getWidth(); j++){
					sum += (Math.abs(Math.pow(image1.getPixel(j, i, b)-image2.getPixel(j, i, b), g)));
				}
			}
		}
		return sum;
	}
}
