package similarity.distances;

import java.math.BigDecimal;

public class EuclideanDistance implements Distance{
	
	private int g = 2;
	
	public EuclideanDistance(int expoentParameter){
		this.g = expoentParameter;
	}
	
	public void setExpoentParameter(int expoentParameter){
		this.g = expoentParameter;
	}

	@Override
	public double compute(double x1, double y1, double x2, double y2) {
		return Math.pow(Math.pow(x2 - x1, g) + Math.pow(y2 - y1, g), 1f/g);
	}

	@Override
	public double compute(double[] x, double[] y) {
		BigDecimal bd = BigDecimal.valueOf(0);
		for (int k=0; k<x.length; k++){
			bd = bd.add(BigDecimal.valueOf(Math.pow(y[k] - x[k], g)));
		}
		//bd = bd.pow(1f/g);
		return Math.pow(bd.doubleValue(), 1f/g);
	}

	
	
}
