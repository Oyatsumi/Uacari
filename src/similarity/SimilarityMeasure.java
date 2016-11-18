package similarity;

import general.Image;

public interface SimilarityMeasure {
	public static final int MEAN_DIFFERENCE = 1;

	public int getSimilarityMeasureIndex();
	public double compare(Image img1, Image img2);
}
