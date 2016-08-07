package registration;

import java.awt.geom.AffineTransform;

import similarity.SimilarityMeasure;

public class RegistrationParameters {

	private boolean isRigid = false, isAffine = false;
	private final AffineTransform[] affineTransforms;
	private final SimilarityMeasure[] measures;
	private final double[] thetas, tx, ty;
	

	/**
	 * Instantiates the parameters for a rigid transform.
	 * @param tx - translation on x
	 * @param ty - translation on y
	 * @param tetha - rotation degree
	 * @param measure - a similarity measure to compare the images being registered
	 */
	public RegistrationParameters(double tx, double ty, double theta, SimilarityMeasure measure, int imgsWidth, int imgsHeight){
		affineTransforms = new AffineTransform[1];
		affineTransforms[0] = new AffineTransform();
		affineTransforms[0].setToRotation(1.57*theta/90, imgsWidth/2, imgsHeight/2);
		double[] m = new double[6];
		affineTransforms[0].getMatrix(m);
		affineTransforms[0] = new AffineTransform(m[0], m[1], m[2], m[3], m[4] + tx, m[5] + ty);
		//affineTransforms[0].translate(tx, ty);
		
		
		thetas = new double[1]; this.tx = new double[1]; this.ty = new double[1];
		this.thetas[0] = theta; this.tx[0] = tx; this.ty[0] = ty;

		this.isRigid = true;
		this.measures = new SimilarityMeasure[1];
		this.measures[0] = measure;
	}
	
	/**
	 * Instantiates the parameters for a rigid transform.
	 * This constructor receives lists, which are used to run the registration tx.length = ty.length = theta.length times, 
	 * where the best parameter set of parameters is chosen (e.g., tx[0], ty[0], theta[0] - the best set of parameters is at index 0).
	 * The size of the measure array can be different from tx, ty and theta.
	 * @param tx - a list of translations on x
	 * @param ty - a list of translations on y
	 * @param theta - a list of rotation degrees
	 * @param measure - a similarity measure to compare the images being registered
	 */
	public RegistrationParameters(double[] tx, double[] ty, double[] theta, SimilarityMeasure measure, int imgsWidth, int imgsHeight){
		if (!(tx.length == ty.length && tx.length == theta.length))
			System.out.printf("The array sizes of the parameters must be the same! [tx: %d != ty: %d != theta: %d] \n", tx.length, ty.length, theta.length);
		
		this.thetas = new double[theta.length]; this.tx = new double[tx.length]; this.ty = new double[ty.length];
		affineTransforms = new AffineTransform[tx.length];
		
		for (int k=0; k<affineTransforms.length; k++){
			affineTransforms[k] = new AffineTransform();
			affineTransforms[k].setToRotation(1.57*theta[k]/90, imgsWidth/2, imgsHeight/2);
			//affineTransforms[k].concatenate(AffineTransform.getTranslateInstance(tx[k], ty[k]));
			double[] m = new double[6];
			affineTransforms[k].getMatrix(m);
			affineTransforms[k] = new AffineTransform(m[0], m[1], m[2], m[3], m[4] + tx[k], m[5] + ty[k]);
			
			this.thetas[k] = theta[k];
			this.tx[k] = tx[k];
			this.ty[k] = ty[k];
		}
		
		this.isRigid = true;
		this.measures = new SimilarityMeasure[1];
		this.measures[0] = measure;
	}
	
	/**
	 * Instantiates the parameters for a rigid transform.
	 * This constructor receives lists, which are used to run the registration tx.length = ty.length = theta.length times, 
	 * where the best parameter set of parameters is chosen (e.g., tx[0], ty[0], theta[0] - the best set of parameters is at index 0).
	 * The size of the measure array can be different from tx, ty and theta.
	 * @param tx - a list of translations on x
	 * @param ty - a list of translations on y
	 * @param theta - a list of rotation degrees
	 * @param measure - a list of similarity measures to compare the images being registered
	 */
	public RegistrationParameters(double[] tx, double[] ty, double[] theta, SimilarityMeasure[] measure, int imgsWidth, int imgsHeight){
		if (!(tx.length == ty.length && tx.length == theta.length))
			System.out.printf("The array sizes of the parameters must be the same! [tx: %d != ty: %d != theta: %d] \n", tx.length, ty.length, theta.length);
		
		this.thetas = new double[theta.length]; this.tx = new double[tx.length]; this.ty = new double[ty.length];
		affineTransforms = new AffineTransform[tx.length];
		
		for (int k=0; k<affineTransforms.length; k++){
			affineTransforms[k] = new AffineTransform();
			affineTransforms[k].setToRotation(1.57*theta[k]/90, imgsWidth/2, imgsHeight/2);
			//affineTransforms[k].concatenate(AffineTransform.getTranslateInstance(tx[k], ty[k]));
			double[] m = new double[6];
			affineTransforms[k].getMatrix(m);
			affineTransforms[k] = new AffineTransform(m[0], m[1], m[2], m[3], m[4] + tx[k], m[5] + ty[k]);
			
			this.thetas[k] = theta[k];
			this.tx[k] = tx[k];
			this.ty[k] = ty[k];
		}
		
		this.isRigid = true;
		this.measures = measure;
	}
	
	
	/**
	 * Instantiates the parameters for an affine transform.
	 * @param transform - the affine transform
	 * @param measure - a similarity measure to compare the images being registered
	 */
	public RegistrationParameters(AffineTransform transform, SimilarityMeasure measure){
		affineTransforms = new AffineTransform[1];
		affineTransforms[0] = transform;
		
		thetas = new double[1];
		thetas[0] = Double.MIN_VALUE;
		
		this.tx = new double[1]; this.tx[0] = Double.MIN_VALUE;
		this.ty = new double[1]; this.ty[0] = Double.MIN_VALUE;
		
		this.measures = new SimilarityMeasure[1];
		this.measures[0] = measure;
		
		this.isAffine = true;
	}
	
	/**
	 * Instantiates the parameters for an affine transform.
	 * The length of the measure array can be different from the transform array.
	 * @param transform - the affine transform array
	 * @param measure - a similarity measure array to compare the images being registered
	 */
	public RegistrationParameters(AffineTransform[] transform, SimilarityMeasure[] measure){
		this.affineTransforms = transform;
		this.measures = measure;
		this.isAffine = true;
		
		thetas = new double[transform.length]; this.tx = new double[transform.length]; this.ty = new double[transform.length];
		for (int k=0; k<transform.length; k++) {thetas[k] = Double.MIN_VALUE; tx[k] = Double.MIN_VALUE; ty[k] = Double.MIN_VALUE;}
	}
	
	
	public boolean isRigid(){return this.isRigid;}
	public boolean isAffine(){return this.isAffine;}
	
	private int tCounter = 0;
	protected AffineTransform getNextAffineTransform(){
		tCounter ++;
		if (tCounter > affineTransforms.length) {
			tCounter = 0;
			return null;
		}
		return affineTransforms[tCounter - 1];
	}
	
	private int pCounter = 0;
	protected SimilarityMeasure getNextSimilarityMeasure(){
		pCounter ++;
		if (pCounter > measures.length){
			pCounter = 0;
			return null;
		}
		return measures[pCounter - 1];
	}
	
	public String getParametersAsString(int paramIndex){
		
		String params = "[Input Parameters: translationOnX: " + 
		((tx[0] == Double.MIN_VALUE) ? "?" : tx[paramIndex]) + 
		", translationOnY: " +
		((ty[0] == Double.MIN_VALUE) ? "?" : ty[paramIndex]) + 
		", degree of rotation: " +
		((thetas[0] == Double.MIN_VALUE) ? "?" : thetas[paramIndex]) + "]";
		
		params += "[" + affineTransforms[paramIndex] + "]";
		return params;
 	}
	
	/**
	 * Returns the parameters of the affine transformation that is currently being iterated
	 * @return
	 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
	 */
	public String getParametersAsString(){
		int idx = (tCounter - 1 < 0) ? 0 : tCounter - 1;
		return getParametersAsString(idx);
	}
	
	protected SimilarityMeasure[] getSimilarityMeasures(){return this.measures;}
	protected AffineTransform[] getAffineTransforms(){return this.affineTransforms;}
	
	
}
