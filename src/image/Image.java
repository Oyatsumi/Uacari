package image;


import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import morphology.Morphology;
import similarity.SimilarityMeasure;



/**
 * Class representing an image.
 * @author �rick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class Image implements ImageConstants{
	private List<TreeMap<Double, Integer>> intensities = null; //ascendingly ordered
	//private int[][] pixelValue;
	private PixelMap pixMap = null;
	private BufferedImage bImg = null;
	private boolean updateBuffered = true, updateHistogram = true, updateMean = true;
	//private boolean[][] binaryImgs;
	private byte bands = 1;
	private int type = -1;
	//
	private ImageOperation op = new ImageOperation(this); //low memory instance
	private ImageDisplay display = null;
	private Morphology m = new Morphology(this);
	
	//auxiliary variables
	private WritableRaster raster = null;
	
	public Image set(Image img){
		pixMap = new PixelMap(img.getWidth(), img.getHeight(), img.getNumBands(), img.getBitDepth());
		for (int b=0; b<img.getNumBands(); b++){
			if (img.getBitDepth() == 1)
				pixMap.setPixelData(img.pixMap.getPixelData(b).clone(), b, 1);
			else if (img.getBitDepth() <= 8)
				pixMap.setPixelData(img.pixMap.getPixelData(b).clone(), b, 8);
			else if (img.getBitDepth() <= 32)
				pixMap.setPixelData(img.pixMap.getPixelData(b).clone(), b, 32);
			else if (img.getBitDepth() <= 64)
				pixMap.setPixelData(img.pixMap.getPixelData(b).clone(), b, 64);
		}
		this.type = img.getType();
		this.bands = (byte) img.getNumBands();
		return this;
	}

	/**
	 * @param width
	 * @param height
	 * @throws Exception 
	 */
	public Image(int width, int height, int numBands){
		pixMap = new PixelMap(width, height, numBands, 8);
		this.bands = (byte) numBands;
	}
	/**
	 * Assumes the image is grey and the image values are at most 32 bits depth and creates a new image based on the values of matrix img.
	 * @param img - input matrix
	 */
	public Image(int[][] img){
		pixMap = new PixelMap(img[0].length, img.length, 1, 32);
		pixMap.setPixelData(img, 0);
	}
	/**
	 * Instantiates a new 8-bits depth grey image
	 * @param width - width of the image
	 * @param height - height of the image
	 */
	public Image(int width, int height){
		pixMap = new PixelMap(width, height, 1, 8);
	}
	/**
	 * Creates a new image that is 8-bits depth with the values of the matrix img
	 * @param img - input matrix
	 */
	public Image(short[][] img){
		pixMap = new PixelMap(img[0].length, img.length, 1, 8);
		pixMap.setPixelData(img, 0);
	}
	/**
	 * Clones a image of type Image, creates a new object that is identical to the previous image but is a different object
	 * @param img - the image to be cloned
	 * @throws CloneNotSupportedException 
	 */
	public Image(Image img) throws CloneNotSupportedException{
		this.set(img);
	}
	/**
	 * If the image is RGB-layered it will be converted to one single layer as the mean of every layer.
	 * @param img - The image to be loaded
	 */
	public Image(BufferedImage img){
		setImageFromBufferedImage(img);
	}
	public Image(String imagePath) throws IOException{
		setImageFromBufferedImage(ImageIO.read(new File(imagePath)));
	}
	public Image(File imgFile) throws IOException{
		setImageFromBufferedImage(ImageIO.read(imgFile));
	}
	public void updateImage(BufferedImage img, int width, int height) throws Exception{
		if (width != this.getWidth() || height != this.getHeight()){
			setImageFromBufferedImage(createBufferedImage(width, height));
		}else updateImage(img);
	}
	public void updateImage(BufferedImage img){
		raster = img.getRaster();
		if (this.getType() == img.getType() && raster.getNumBands() == this.getNumBands()) {
			setImageFromBufferedImage(img);
			return;
		}
		
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				for (int b=0; b<raster.getNumBands() && b < this.getNumBands(); b++){
					this.setPixel(j, i, b, raster.getSample(j, i, b));
				}
			}
		}
		this.setToUpdateBuffers();
		this.updateBuffered = false;
	}

	


	public void resize(int width, int height) throws Exception{
		if (!this.hasBufferedImage()){//if there is no bufferedImage
			createBufferedImage();
		}
		
		this.scale(width/(float)this.getWidth(), height/(float)this.getHeight());
		updateImage(this.getBufferedImage(), width, height);
	}
	private BufferedImage createBufferedImage() throws Exception{return createBufferedImage(this.getWidth(), this.getHeight());}
	private BufferedImage createBufferedImage(int width, int height) throws Exception{
		int type = (this.getNumBands() == 1) ? BufferedImage.TYPE_BYTE_GRAY : (this.getNumBands() == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		this.bImg = new BufferedImage(width, height, type);
		this.raster = bImg.getRaster();
		for (int i=0; i<height && i < this.getHeight(); i++){
			for (int j=0; j<width && j < this.getWidth(); j++){
				for (int b=0; b<this.raster.getNumBands() && b < this.getNumBands(); b++){
					this.raster.setSample(j, i, b, this.getPixel(j, i, b));
				}
			}
		}
		raster = null;
		return bImg;
	}
	

	private ArrayList<TreeMap<Double,Integer>> getHistogram(){
		if (!this.updateHistogram) return (ArrayList<TreeMap<Double, Integer>>) intensities;

		intensities = new ArrayList<TreeMap<Double, Integer>>(this.getNumBands());
		for (int k=0; k<this.getNumBands(); k++) intensities.add(new TreeMap<Double, Integer>());
		for (int b=0; b<this.getNumBands(); b++){
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					if (!intensities.get(b).containsKey(this.getPixel(j, i)))
						intensities.get(b).put(this.getPixel(j, i), 1);
					else{
						intensities.get(b).put(this.getPixel(j, i), intensities.get(b).remove(this.getPixel(j, i)) + 1);
					}
				}
			}
		}
		this.updateHistogram = false;
		return (ArrayList<TreeMap<Double, Integer>>) intensities;
	}
	
	
	//get
	public double getPixel(int x, int y){
		return this.pixMap.get(x, y);
	}
	public double getPixel(int x, int y, int band){
		return this.pixMap.get(x, y, band);
	}

	public int[][] getMatrixImage() throws Exception{
		return this.pixMap.getIntegerPixelData(0);
	}
	public Set<Double> getIntensities(int band){
		return getHistogram().get(band).keySet();
	}
	public int getHeight(){return this.pixMap.getHeight();}
	public int getWidth(){return this.pixMap.getWidth();}
	public float getDiagonalLength(){return (float) Math.pow(Math.pow(this.getWidth(), 2) + Math.pow(this.getHeight(), 2), 1/2d);}
	public boolean containsIntensity(double intensity, int band){
		if (this.getHistogram().get(band).containsKey(intensity)) 
			return this.getHistogram().get(band).get(intensity) >= 1; 
		return false;
	}
	public byte getNumBands(){return bands;}
	/**
	 * This method should be taken care because if the maximum value of the image exceeds 255, then it will be painted 255. If the minimal value is under 0, then it is also painted 0.
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getBufferedImage() throws Exception{
		if (!this.updateBuffered) return this.bImg;
		
		if (!this.hasBufferedImage()) createBufferedImage();
		
		int type = (this.isGray()) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB;
		if (this.getNumBands() == 4) type = BufferedImage.TYPE_INT_ARGB;
		
		bImg = new BufferedImage(this.getWidth(), this.getHeight(), type);
		raster = bImg.getRaster();
		
		int pixelValue = 0;
		
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				for (int b=0; b<this.getNumBands() && b<raster.getNumBands(); b++){
					pixelValue = (int) this.getPixel(j, i, b);
					if (pixelValue < 0) pixelValue = 0; if (pixelValue > 255) pixelValue = 255;
					raster.setSample(j, i, b, pixelValue);
				}
			}
		}
		
		this.setToUpdateBuffers();
		this.updateBuffered = false;
		raster = null;
		return bImg;
	}
	public int getType(){
		if (type == -1)
			switch (this.getNumBands()){
				case 1:
					return BufferedImage.TYPE_BYTE_GRAY;
				case 3:
					return BufferedImage.TYPE_INT_RGB;
				case 4:
					return BufferedImage.TYPE_INT_ARGB;
			}
		return type;
	}
	private Vector minMax = null; 
	public Vector getMinMaxIntensity(int band){
		if (this.updateHistogram) {minMax = null; this.updateHistogram = false;}
		if (minMax == null){
			double min = Long.MAX_VALUE, max = Long.MIN_VALUE;
			double value = 0;
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					if (this.isGray()) value = this.getPixel(j, i);
					else value = this.getPixel(j, i, band);
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
			}
			minMax = new Vector((float)min, (float)max);
		}
		return minMax;
	}
	public double getMinimalIntesity(int band){
		return this.getMinMaxIntensity(band).x;
	}
	public double getMaximalIntensity(int band){
		return this.getMinMaxIntensity(band).y;
	}
	public int getBitDepth(){return this.pixMap.getBitDepth();}//acertar dps
	private int getAssociatedNumBands(int imgType){return (imgType == BufferedImage.TYPE_BYTE_GRAY) ? 1 : (imgType == BufferedImage.TYPE_INT_RGB) ? 3 : 4;}
	private int getAssociatedType(int numBands){return (numBands == 1) ? BufferedImage.TYPE_BYTE_GRAY : (numBands == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;}
	double mean = 0;
	byte meanBandBuffer = 0;
	public double getMeanIntensity(int band){
		if (!this.updateMean && meanBandBuffer == band) return mean;
		BigDecimal bd = BigDecimal.valueOf(0);
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				bd = bd.add(BigDecimal.valueOf(this.getPixel(j, i, band)));
			}
		}
		bd = bd.divide(BigDecimal.valueOf(this.getWidth()*this.getHeight()));
		this.updateMean = false;
		this.meanBandBuffer = (byte) band;
		this.mean = bd.doubleValue();
		return mean;
	}
	
	/**
	 * Cluster binary images
	 * @return
	 * @throws Exception 
	 */
	public Image clusterImage() throws Exception{
		return this.set(m.cluster(this, Morphology.PRIMARY_STRUCT));
	}
	/**
	 * Converts a binary image to an grey image, where at every white pixel the image receives the index of the pixel.
	 * @throws Exception 
	 */
	public Image convertToIndexedImage() throws Exception{
		if (!this.isBinary()) throw new Exception("Image must be binary in order to convert it to an indexed image.");
		
		boolean[][] img = new boolean [this.getHeight()][this.getWidth()];
		for (int i=0; i<img.length; i++) for (int j=0; j<img[0].length; j++) img[i][j] = this.getPixel(j, i) > 0;
		
		this.convertToGray(64);
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				if (img[i][j]) this.setPixel(j, i, i*this.getWidth() + j);
			}
		}
		img = null;
		return this;
	}
	
	/**
	 * Contrast the image according to a 8-bit depth image (from 0 to 255). In other words, stretches the image values to within this range: [0,255].
	 * @throws CloneNotSupportedException 
	 */
	public Image contrast(){
		this.changeRange(0, 255);
		return this;
	}
	
	/**
	 * Linearly convert the image values to a new range. If your images goes from -10 to 80, and your pass as parameter newMinimum = 0 and newMaximum = 255, your
	 * image is stretched out to fir the range [0,255], for instance.
	 * @param newMinimum
	 * @param newMaximum
	 */
	public Image changeRange(double newMinimum, double newMaximum){
		for (int b=0; b<this.getNumBands(); b++){
			double max = this.getMaximalIntensity(b), min = this.getMinimalIntesity(b);
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					double r = ((newMinimum - newMaximum)/(min - max))*this.getPixel(j, i, b) +  newMaximum -(((newMinimum - newMaximum)/(min - max))*max);
					this.setPixel(j, i, b, r);
				}
			}
		}
		return this;
	}
	
	//set
	public void setToUpdateBuffers(){this.updateBuffered = true; this.updateHistogram = true; this.updateMean = true;}
	public void setPixelAllBands(int x, int y, double value){
		for (int b=0; b<this.getNumBands(); b++) this.pixMap.set(x, y, b, value); setToUpdateBuffers();
	}
	public void setPixel(int x, int y, double value){this.pixMap.set(x, y, value); setToUpdateBuffers();}
	public void setPixel(int x, int y, int band, double value){
		//this.updateBuffered = true;
		setToUpdateBuffers();
		this.pixMap.set(x, y, band, value);
	}
	public void setNumBands(int num) throws Exception{this.bands = (byte) num; setConfiguration(num, this.getBitDepth());}
	public void setType(int type) throws Exception{this.type = type; setConfiguration(this.getNumBands(), getAssociatedNumBands(type));}
	
	public Image convertToGray() throws Exception{return convertToGray(8);}
	public Image convertToGray(int bitDepth) throws Exception{
		PixelMap pm = new PixelMap(this.getWidth(), this.getHeight(), 1, bitDepth);
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				pm.set(j, i, this.getPixel(j, i, 0));
			}
		}
		this.setNumBands(1);
		this.setType(BufferedImage.TYPE_BYTE_GRAY);
		//this.updateBuffered = true;
		this.setToUpdateBuffers();
		this.pixMap = pm;
		return this;
	}
	public Image convertToBinary(float threshold) throws Exception{
		PixelMap pm = new PixelMap(this.getWidth(), this.getHeight(), 1, 1);
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				pm.set(j, i, this.getPixel(j, i, 0) >= threshold ? 255 : 0);
			}
		}
		this.setNumBands(1);
		this.setType(BufferedImage.TYPE_BYTE_BINARY);
		//this.updateBuffered = true;
		this.setToUpdateBuffers();
		this.pixMap = pm;
		return this;
	}
	public void setRenderingAntiAliasing(boolean setOn){op.setAntiAliasing(setOn);} 
	/**
	 * Sets the interpolation when the image is rendered using Java's Graphics2D
	 * @param interpolationType - the types can be Image.BICUBIC, Image.BILINEAR or Image.NEAREST_NEIGHBOR
	 */
	public void setInterpolation(Object interpolationType){this.op.setInterpolation(interpolationType);}
	public void setConfiguration(int numBands, int bitDepth) throws Exception{ //TERMINAR
		PixelMap pMap = this.pixMap;
		this.bands = (byte) numBands;
		this.type = getAssociatedType(bitDepth / 8);
		this.pixMap = new PixelMap(this.getWidth(), this.getHeight(), numBands, bitDepth);
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				for (int b=0; b<numBands; b++){
					this.setPixel(j, i, b, pMap.get(j, i, b));
				}
			}
		}
		pMap.dispose();
		pMap = null;
		if (this.hasBufferedImage()){
			//this.setNumBands(numBands);
			//int type = (this.getNumBands() == 1) ? BufferedImage.TYPE_BYTE_GRAY : (this.getNumBands() == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
			//bImg = new BufferedImage(this.getWidth(), this.getHeight(), type);
			this.createBufferedImage();
		}
			
		//if (numBands == 1) aux = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.);
		
	}
	public void setImageFromBufferedImage(BufferedImage img){
		if (pixMap != null) pixMap.dispose();
		//update objects
		this.setToUpdateBuffers(); this.updateBuffered = false;
		this.bImg = img;
		this.type = img.getType();
		
		raster = img.getRaster();
		
		boolean grey = true;
		for (int i=0; i<img.getHeight() && raster.getNumBands() > 1; i++){
			for (int j=0; j<img.getWidth(); j++){
				for (int b=0; b<raster.getNumBands(); b++)
					if (b >= 1) grey &= raster.getSample(j, i, b) == raster.getSample(j, i, b-1);
			}
		}
		if (!grey)
			pixMap = new PixelMap(img.getWidth(), img.getHeight(), raster.getNumBands(), 8);
		else
			pixMap = new PixelMap(img.getWidth(), img.getHeight(), 1, 8);
		
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				if (grey)
					pixMap.set(j, i, (raster.getSample(j, i, 0)));
				else{
					for (int b=0; b<raster.getNumBands(); b++){
						pixMap.set(j, i, b, raster.getSampleDouble(j, i, b));
					}
				}
			}
		}
		
		if (!grey) this.bands = (byte) raster.getNumBands();
		else this.bands = 1;
		raster = null;
	}
	

	
	//is
	public boolean isGray(){return bands == 1;}
	public boolean isBinary(){return this.getBitDepth() == 1;}
	
	//has
	public boolean hasBufferedImage(){return this.bImg != null;}
	
	//others
	public void dispose(){if (this.intensities != null) this.intensities.clear(); this.intensities = null; this.pixMap = null;}
	public void disposeBufferedImage(){this.bImg = null;}
	
	
	public Image clone() throws CloneNotSupportedException{
		return new Image(this);
	}
	
	public boolean equals(Image comparedImg, SimilarityMeasure sm) throws Exception{
		sm.setImages(this, comparedImg);
		return sm.isEqual(this, comparedImg);
	}
	/**
	 * Assumes you are comparing the images using the mean difference measure. May not be threadsafe if multiple Images are accessing this method.
	 * @param comparedImg
	 * @return
	 * @throws Exception 
	 */
	public boolean equals(Image comparedImg) throws Exception{
		SimilarityMeasure.MEAN_DIFFERENCE.setImages(this, comparedImg);
		return SimilarityMeasure.MEAN_DIFFERENCE.isEqual(this, comparedImg);
	}
	
	
	/**
	 * Compares this image with an imaged passed as parameter according to a certain similarity measure, returns a double value representing the similarity.
	 * The correspondence of the value depends on the employed measure, but usually, the lower the returned result the better.
	 * @param comparedImg
	 * @param sm
	 * @return
	 * @throws Exception 
	 */
	public double compare(Image comparedImg, SimilarityMeasure sm) throws Exception{
		sm.setImages(this, comparedImg);
		return sm.compare();
	}
	
	public void print(){
		for (int b=0; b<this.getNumBands(); b++){
			System.out.printf("Band (layer): %d \n", b);
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					if (!this.isGray())
						System.out.printf("%d ", this.getPixel(j, i, b));
					else
						System.out.printf("%d ", this.getPixel(j, i));
				}
				System.out.printf("\n");
			}
		}
	}
	
	public void exportImage(String outputPath, String formatName) throws Exception{
		ImageIO.write(this.getBufferedImage(), formatName.toUpperCase(), new File(outputPath));
	}
	
	
	/**
	 * Opening and closing operations. Dilation + erosion - original image.
	 * @param structuringElement
	 * @param times - times to dilate and erode
	 * @return
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 */
	public Image skeletonize(Image structuringElement, int times) throws IOException, CloneNotSupportedException{
		Image aux = new Image(this);
		for (int k=0; k<times; k++) this.op.dilateOrErode(structuringElement, true);
		for (int k=0; k<times; k++) this.op.dilateOrErode(structuringElement, false);
		this.subtract(aux);
		this.contrast();
		return this;
	}
	/**
	 * Opening and closing operations. Dilation + erosion - original image.
	 * @param times
	 * @return
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 */
	public Image skeletonize(int times) throws IOException, CloneNotSupportedException{
		if (this.isBinary()) return this.skeletonize(Morphology.SIMPLE_BINARY_RECT, times);
		else return this.skeletonize(Morphology.PRIMARY_STRUCT, times);
	}
	
	
	//transforms
	private Vector auxVec1 = new Vector(0, 0), auxVec2 = new Vector(0, 0);
	public Image translate(int x, int y) throws Exception{op.translate(x, y); return this;}
	public Image scale(double x, double y) throws Exception{op.scale(x, y); return this;}
	public Image scaleWithFixedSize(double x, double y) throws Exception{op.scaleWithFixedSize(x, y); return this;}
	public Image transform(AffineTransform at) throws Exception{op.transform(at); return this;}

	
	//
	public Image drawLine(int x1, int y1, int x2, int y2, Color lineColor) throws Exception{auxVec1.x = x1; auxVec1.y = y1; auxVec2.x = x2; auxVec2.y = y2; op.drawLine(auxVec1, auxVec2, lineColor); return this;}
	public Image drawLine(Vector p1, Vector p2, Color lineColor) throws Exception{op.drawLine(p1, p2, lineColor); return this;}
	public Image blendImages(Image topImg, int posX, int posY) throws Exception{op.blendImages(topImg.getBufferedImage(), posX, posY); return this;}
	public Image invert(){op.invert(); return this;}
	public Image intersect(Image imgToIntersect){op.intersect(imgToIntersect); return this;}
	public Image maskedImage(Image mask){op.getMaskedImage(mask); return this;}
	public Image addBrightness(int valueToAdd){op.addBrightness(valueToAdd); return this;}
	public Image subtract(Image imgToSubtract){op.subtract(imgToSubtract); return this;}
	public Image threshold(int thresholdLevel){op.threshold(thresholdLevel); return this;}
	public Image threshold(int lowerThresholdLevel, int upperThresholdLevel){op.threshold(lowerThresholdLevel, upperThresholdLevel); return this;}
	public Image smoothThreshold(int lowerThresholdLevel, int upperThresholdLevel){op.smoothThreshold(lowerThresholdLevel, upperThresholdLevel); return this;}
	//public Image toGray(){op.toGray(); return this;}
	public Image antiAlias(){op.antiAlias(); return this;}
	/**
	 * Dilates the image based on the structuring element passed as parameter. You can create a new structuring element or use one of the Morphology class ({@link Morphology}). 
	 * E.g., {@link Morphology.PRIMARY_STRUCT} for grey images, {@link Morphology.SIMPLE_BINARY_RECT} for binary images, etc.
	 * If the desired dilation is a binary dilation, do not forget to convert the image to binary first. Otherwise that could introduce problems related to the upper and lower limits of the image.
	 * @param structuringElement - the structuring element
	 * @param times - how many times the dilation will be performed
	 * @param contrast - whether or not to contrast the image (if not enabled, the image may very easily exceed the 255 value (max of 8bits depth images)
	 * @return - the dilated image
	 * @throws Exception
	 */
	public Image dilate(Image structuringElement, int times, boolean contrast) throws Exception{for (int k=0; k<times; k++) op.dilateOrErode(structuringElement, true); if(contrast) this.contrast(); return this;}
	/**
	 * Dilates the image based on the structuring element passed as parameter. You can create a new structuring element or use one of the Morphology class ({@link Morphology}).
	 * E.g., {@link Morphology.PRIMARY_STRUCT} for grey images, {@link Morphology.SIMPLE_BINARY_RECT} for binary images, etc.
	 * If the desired dilation is a binary dilation, do not forget to convert the image to binary first. Otherwise that could introduce problems related to the upper and lower limits of the image.
	 * @param structuringElement - the structuring element
	 * @param times - how many times the dilation will be performed
	 * @return - the dilated image
	 * @throws Exception
	 */
	public Image dilate(Image structuringElement, int times) throws Exception{return dilate(structuringElement, times, true);}
	/**
	 * Erodes the image based on the structuring element passed as parameter. You can create a new structuring element or use one of the Morphology class ({@link Morphology}). .
	 * E.g., {@link Morphology.PRIMARY_STRUCT} for grey images, {@link Morphology.SIMPLE_BINARY_RECT} for binary images, etc.
	 * If the desired dilation is a binary dilation, do not forget to convert the image to binary first. Otherwise that could introduce problems related to the upper and lower limits of the image.
	 * @param structuringElement - the structuring element
	 * @param times - how many times the dilation will be performed
	 * @return - the dilated image
	 * @throws Exception
	 */
	public Image erode(Image structuringElement, int times) throws Exception{return erode(structuringElement, times, true);}
	/**
	 * Erodes the image based on the structuring element passed as parameter. You can create a new structuring element or use one of the Morphology class ({@link Morphology}). 
	 * E.g., {@link Morphology.PRIMARY_STRUCT} for grey images, {@link Morphology.SIMPLE_BINARY_RECT} for binary images, etc.
	 * If the desired dilation is a binary dilation, do not forget to convert the image to binary first. Otherwise that could introduce problems related to the upper and lower limits of the image.
	 * @param structuringElement - the structuring element
	 * @param times - how many times the dilation will be performed
	 * @param contrast - whether or not to contrast the image (if not enabled, the image may very easily exceed the 255 value (max of 8bits depth images)
	 * @return - the dilated image
	 * @throws Exception
	 */
	public Image erode(Image structuringElement, int times, boolean contrast) throws Exception{for (int k=0; k<times; k++) op.dilateOrErode(structuringElement, false); if(contrast) this.contrast(); return this;}
	
	
	
	
	public void showImage() throws Exception{
		if (display == null) display = new ImageDisplay();
		//if (this.hasBufferedImage()) createBufferedImage();
		display.setImage(this);
	}

}