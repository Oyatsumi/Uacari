package similarity;

import java.math.BigDecimal;

import general.Image;

public class MeanDifference implements SimilarityMeasure{
	private float g = 1;
	private boolean boost = false;

	public MeanDifference(float expoentParameter){
		this.g = expoentParameter;
	}
	
	@Override
	public int getSimilarityMeasureIndex() {
		return SimilarityMeasure.MEAN_DIFFERENCE;
	}

	@Override
	public double compare(Image img1, Image img2){
		if (this.boost)
			return SumOfDifferences.fastSumOfDifferences(img1, img2, this.g)/(img2.getHeight()*img2.getWidth());
		else{
			BigDecimal bd = SumOfDifferences.sumOfDifferences(img1, img2, this.g);
			bd = bd.divide(BigDecimal.valueOf(img2.getHeight()*img2.getWidth()));
			return bd.doubleValue();
		}
	}

	//set
	public void setExpoentParameter(float parameter){
		this.g = parameter;
	}
	public void setSpeedUp(boolean activate){this.boost = activate;}

	
	
	
}
