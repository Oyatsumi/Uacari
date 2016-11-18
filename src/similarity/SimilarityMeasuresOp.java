package similarity;

import general.Image;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;



public class SimilarityMeasuresOp{
	public static final int MEAN_DIFFERENCE = 0;

	
	private static void checkViability(Image image1, Image image2) throws Exception{
		/*
		if (image1.getNumBands() != image2.getNumBands()){
			throw new Exception("The images being compared have distinct number of layers.");
		}
		*/
		if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()){
			throw new Exception("The images being compared have distinct sizes.");
		}
	}
	
	//the higher the better (maximization)
	public static long hybridSumOfDifferences(Image image1, Image image2, float g, int threshold) throws Exception{
		int img1MaxIntensity = 0, img2MaxIntensity = 0;

		checkViability(image1, image2);
		
		long blueScore = 0;
		for (int b=0; b<image1.getNumBands(); b++){
			img1MaxIntensity = image1.getMaximumIntensity(b);
			img2MaxIntensity = image2.getMaximumIntensity(b);
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image1.getWidth(); j++){
					int delta = image1.getPixel(j, i, b),
							movingSample = (int) (image2.getPixel(j, i, b)*(img1MaxIntensity/img2MaxIntensity));
					long blueCounter = 1;
					if (image1.getPixel(j, i, b) > -1){// != -1 equals within fixed image boundaries
						if (movingSample > threshold){
							delta -= (1 + img1MaxIntensity)/(1 + movingSample);
							if (delta < 0) delta = 0;
							for (byte a=0; a<g; a++) blueCounter *= delta;
							//blueCounter = delta*delta*delta;
							blueCounter = Math.abs(blueCounter);
						}else{
							delta -= movingSample;
							for (byte a=0; a<g; a++) blueCounter *= delta;
							//blueCounter = delta*delta*delta;
							blueCounter = -Math.abs(blueCounter);
						}
					}else{//if the atlas is out of the fixed image boundaries
						for (byte a=0; a<g; a++) blueCounter *= img1MaxIntensity;
						//blueCounter = maxIntensity*maxIntensity*maxIntensity;
						blueCounter = -blueCounter;
					}
					
					//hybric mean error
					blueScore += blueCounter;
	
				}
			}
		}
		
		return blueScore;
	}
	//maximization
	public static double normalizedCrossCorrelation(Image image1, Image image2) throws Exception{
		double numerator = 0,
				denominator1 = 0, denominator2 = 0;
		int img1MeanIntensity = 0, img2MeanIntensity = 0;
		
		
		checkViability(image1, image2);
		
		for (int b=0; b<image1.getNumBands(); b++){
			img1MeanIntensity = image1.getMeanIntensity(b);
			img2MeanIntensity = image2.getMeanIntensity(b);
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image1.getWidth(); j++){
					numerator += (image1.getPixel(j, i, b)-img1MeanIntensity)*(image2.getPixel(j, i, b)-img2MeanIntensity);
					denominator1 += Math.pow((image1.getPixel(j, i, b)-img1MeanIntensity), 2);
					denominator2 += Math.pow((image2.getPixel(j, i, b)-img2MeanIntensity), 2);
				}
			}
		}
		
		return Math.abs(numerator/(Math.pow(denominator1*denominator2,1/2f)));
	}
	
	public static long sumOfDifferences(Image image1, Image image2, float g) throws Exception{
		checkViability(image1, image2);
		
		long sum = 0;
		for (int b=0; b<image1.getNumBands(); b++){
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image2.getWidth(); j++){
					sum += Math.abs(Math.pow(image1.getPixel(j, i, b)-image2.getPixel(j, i, b), g));
				}
			}
		}
		return sum;
	}
	
	public static double mutualInformation(Image image1, Image image2, String logBase) throws Exception{
		return weightedMutualInformation(image1, image2, logBase, false);
	}
	private static TreeMap<Short, TreeMap<Short, Double>> mutualOccurrence = null;
	private static TreeMap<Short, Double> fixedOccurrence = null, movingOccurrence = null;
	public static double weightedMutualInformation(Image image1, Image image2, String logBase, boolean weighted) throws Exception{
		checkViability(image1, image2);
		
		double mutualInf = 0;
		
		for (int b=0; b<image1.getNumBands(); b++){
			mutualOccurrence = new TreeMap<Short, TreeMap<Short, Double>>();
			fixedOccurrence = new TreeMap<Short, Double>();
			movingOccurrence = new TreeMap<Short, Double>();
			TreeMap<Short, Double> aux = new TreeMap<Short, Double>();
			
			double count = 0;
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image1.getWidth(); j++){
					//mutual occurrence
					if (!mutualOccurrence.containsKey(image1.getPixel(j, i, b))){
						aux = new TreeMap<Short, Double>();
						mutualOccurrence.put((short)image1.getPixel(j, i, b), aux);
					}
					count = 0;
					aux = mutualOccurrence.get(image1.getPixel(j, i, b));
					if (aux.containsKey(image2.getPixel(j, i, b))){
						count = aux.get(image2.getPixel(j, i, b));
						aux.remove(image2.getPixel(j, i, b));
					}
					aux.put((short)image2.getPixel(j, i, b), (count + 1));
					
					
					//fixed occurrence
					if (!fixedOccurrence.containsKey((short)image1.getPixel(j, i, b))){
						fixedOccurrence.put((short)image1.getPixel(j, i, b), (double) 0);
					}
					double value = fixedOccurrence.get((short)image1.getPixel(j, i, b));
					fixedOccurrence.remove((short)image1.getPixel(j, i, b));
					fixedOccurrence.put((short)image1.getPixel(j, i, b), (value + 1));
					
					//moving occurrence
					if (!movingOccurrence.containsKey((short)image2.getPixel(j, i, b))){
						movingOccurrence.put((short)image2.getPixel(j, i, b), (double) 0);
					}
					value = movingOccurrence.get((short)image2.getPixel(j, i, b));
					movingOccurrence.remove((short)image2.getPixel(j, i, b));
					movingOccurrence.put((short)image2.getPixel(j, i, b), (value + 1));
					
				}
			}
			
	
			//divide by the overall number of occurrences to get the probability
			ArrayList<Short> firstValues = new ArrayList<Short>();
			ArrayList<ArrayList<Short>> secondValues = new ArrayList<ArrayList<Short>>();
			for (Entry <Short, TreeMap<Short, Double>> entry : mutualOccurrence.entrySet()){
				firstValues.add(entry.getKey());
				secondValues.add(new ArrayList<Short>());
			}
			int totalMutualOccurrencies = 0;
			for (int k=0; k<firstValues.size(); k++){
				for (Entry <Short, Double> entry : mutualOccurrence.get((short)firstValues.get(k)).entrySet()){
					secondValues.get(k).add(entry.getKey());
					totalMutualOccurrencies += entry.getValue();
				}
			}
			for (int l=0; l<firstValues.size(); l++){
				for (int m=0; m<secondValues.get(l).size(); m++){
					double value = mutualOccurrence.get(firstValues.get(l)).get(secondValues.get(l).get(m));
					mutualOccurrence.get(firstValues.get(l)).remove(secondValues.get(l).get(m));
					mutualOccurrence.get(firstValues.get(l)).put(secondValues.get(l).get(m), (double)value/(totalMutualOccurrencies));
				}
			}
			ArrayList<Short> fixedHues = new ArrayList<Short>();
			int totalFixedOccurrencies = 0;
			for (Entry<Short, Double> entry : fixedOccurrence.entrySet()){
				 fixedHues.add(entry.getKey());
				 totalFixedOccurrencies += entry.getValue();
			}
			for (int k=0; k<fixedHues.size(); k++){
				double value = fixedOccurrence.get(fixedHues.get(k));
				fixedOccurrence.remove(fixedHues.get(k));
				fixedOccurrence.put(fixedHues.get(k), (double)value/(totalFixedOccurrencies));
			}
			ArrayList<Short> movingHues = new ArrayList<Short>();
			int totalMovingOccurrencies = 0;
			for (Entry<Short, Double> entry : movingOccurrence.entrySet()){
				movingHues.add(entry.getKey());
				totalMovingOccurrencies += entry.getValue();
			}
			for (int k=0; k<movingHues.size(); k++){
				double value = movingOccurrence.get(movingHues.get(k));
				movingOccurrence.remove(movingHues.get(k));
				movingOccurrence.put(movingHues.get(k), (double)value/(totalMovingOccurrencies));
			}
			
			
			//computing result
			for (Entry<Short, Double> fixed : fixedOccurrence.entrySet()){
				for (Entry<Short, Double> moving : movingOccurrence.entrySet()){
					if (mutualOccurrence.get(fixed.getKey()).containsKey(moving.getKey())){
						double weight = 1;
						if (weighted)
							weight = (double) 1/(Math.abs(moving.getKey() - fixed.getKey())+1);
						double result = mutualOccurrence.get(fixed.getKey()).get(moving.getKey()),
								resultAux = (double)result/(fixedOccurrence.get(fixed.getKey())*movingOccurrence.get(moving.getKey()));
						if (logBase.equals("e"))
							result *= Math.log(resultAux);
						else if (logBase.equals("2"))
							result *= Math.log(resultAux)/Math.log(2);
						else if (logBase.equals("10"))
							result *= Math.log10(resultAux);
						result *= weight;
						mutualInf += result;
					}
				}
			}
		}

		return mutualInf;
	}
	
	

	public static double greyDiceSimilarity(Image image1, Image image2, int relevance) throws Exception{
		checkViability(image1, image2);
		
		if (image1.getNumBands() > 1){
			return diceColorSimilarity(image1, image2);
		}else{
			int n1 = 0, n2 = 0, nS = 0;
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image1.getWidth(); j++){
					if ((image1.getPixel(j, i) >= image2.getPixel(j, i) - relevance 
							&& image1.getPixel(j, i) <= image2.getPixel(j, i) + relevance)){
						nS ++;
					}
					n1++;
					n2++;
				}
					
			}
			return (2d*nS/(n1 + n2));
		}
	}
	
	
	//dice similarity (colors-segmentation)
	private static double diceColorSimilarity(Image image1, Image image2){
		int n1 = 0, n2 = 0, nS = 0;
		for (int i=0; i<image1.getHeight(); i++){
			for (int j=0; j<image1.getWidth(); j++){
				boolean grey1 = true, grey2 = true;
				if (!(image1.getPixel(j, i, 0) == image1.getPixel(j, i, 1) && image1.getPixel(j, i, 0) == image1.getPixel(j, i, 2))){//if not grey
					grey1 = false;
				}
				if (!(image2.getPixel(j, i, 0) == image2.getPixel(j, i, 1) && image2.getPixel(j, i, 0) == image2.getPixel(j, i, 2))){
					grey2 = false;
				}
				//if (!grey1 || !grey2){
				if (image1.getPixel(j, i, 0) > 0 && image2.getPixel(j, i, 0) > 0 ){
					for (int k=0; k<2; k++){//band amount
						final int variance = 10;
						if ((image1.getPixel(j, i, k) > image2.getPixel(j, i, k) - variance 
								&& image1.getPixel(j, i, k) < image2.getPixel(j, i, k) + variance)
								//&& (!grey1 && !grey2)){
								){
							nS ++;
							n1 ++;
							n2 ++;
						}else{
							if (!grey1) n1++;
							if (!grey2) n2++;
						}
					}
				}
			}
				
		}
		return (2d*nS/(n1 + n2));
	}
}


