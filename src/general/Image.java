package general;



import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import similarity.SimilarityMeasuresOp;



public class Image {
	private ArrayList<Integer> hues = null; //ordenado crescente
	private int[][] pixelValue;
	private byte bands = 1;
	private int type = -1;
	
	public Image(int[][] img){this.pixelValue = img;}
	public Image(int width, int height){this.pixelValue = new int[height][width];}
	public Image(short[][] img){
		pixelValue = new int[img.length][img[0].length];
		for (int i=0; i<img.length; i++){
			for (int j=0; j<img[0].length; j++){
				pixelValue[i][j] = img[i][j];
			}
		}
	}
	public Image(Image img){
		this.pixelValue = new int[img.getHeight()][img.getWidth()];
		for (int i=0; i<pixelValue.length; i++)
			for (int j=0; j<pixelValue[0].length; j++)
				pixelValue[i][j] = img.pixelValue[i][j];
		this.setType(img.getType());
		this.setNumBands(img.getNumBands());
	}
	/**
	 * If the image is RGB-layered it will be converted to one single layer as the mean of every layer.
	 * @param img - The image to be loaded
	 */
	public Image(BufferedImage img){
		constructor(img);
	}
	public Image(String imagePath) throws IOException{
		constructor(ImageIO.read(new File(imagePath)));
	}
	public Image(File imgFile) throws IOException{
		constructor(ImageIO.read(imgFile));
	}
	private void constructor(BufferedImage img){
		this.type = img.getType();
		this.pixelValue = new int[img.getHeight()][img.getWidth()];
		Raster r = img.getRaster();
		boolean grey = true;
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				for (int b=0; b<r.getNumBands(); b++)
					if (b >= 1) grey &= r.getSample(j, i, b) == r.getSample(j, i, b-1);
			}
		}
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				int out = 0;
				if (grey)
					this.pixelValue[i][j] = (r.getSample(j, i, 0));
				else{
					out = r.getSample(j, i, 0);
					for (int l=1; l<r.getNumBands(); l++){
						out = (out << 8 /*byte*/) | (r.getSample(j, i, l) & 0xFF);
					}
					this.pixelValue[i][j] = out;
				}
			}
		}
		
		this.setNumBands(r.getNumBands());
	}

	
	private void buildHues() throws Exception{
		if (!this.isGray()){throw new Exception("The image must be grey in order to access the hues.");}
		
		hues = new ArrayList<Integer>();
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				if (!hues.contains(this.pixelValue[i][j])){
					hues.add(this.pixelValue[i][j]);
				}
			}
		}
		
		//ordenar o vetor de hues/ -pode ser retirado se preferir
		ArrayList<Integer> aux = new ArrayList<Integer>();
		int menor, menori = 0;
		while (hues.size() != 0){
			menor = hues.get(0);
			menori = 0;
			for (short i=0; i<hues.size(); i++){
				if (menor > hues.get(i)) {
					menor = hues.get(i);
					menori = i;
				}
			}
			hues.remove(menori);
			aux.add(menor);
		}
		this.hues.clear();
		this.hues = aux;
	}
	
	
	public int getPixel(int x, int y){
		return this.pixelValue[y][x];
	}
	public int getPixel(int x, int y, int band){
		if (this.isGray()) return this.pixelValue[y][x];
		
		int out = this.pixelValue[y][x];
		for (int k=0; k<this.getNumBands()-1-band; k++) out = out >>> 8 /*byte*/;
		out &= 0xFF;
		return out;
	}
	public short[][] getMatrixShortImage(int band){
		short[][] img = new short[pixelValue.length][pixelValue[0].length];
		for (int i=0; i<img.length; i++){
			for (int j=0; j<img[0].length; j++){
				img[i][j] = (short) this.getPixel(j, i, band);
			}
		}
		return img;
	}
	public int[][] getMatrixImage(){return this.pixelValue;}
	public ArrayList<Integer> getHues() throws Exception{if (this.hues == null) this.buildHues(); return this.hues;}
	public int getHeight(){return this.pixelValue.length;}
	public int getWidth(){return this.pixelValue[0].length;}
	public float getDiagonal(){return (float) Math.pow(Math.pow(this.pixelValue[0].length, 2) + Math.pow(this.pixelValue.length, 2), 1/2d);}
	public boolean containsHue(int hue){return this.hues.contains(hue);}
	public int getNumBands(){return bands;}
	public BufferedImage getBufferedImage(){
		BufferedImage bi = new BufferedImage(pixelValue[0].length, pixelValue.length, 
				(this.isGray()) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB);
		WritableRaster r = bi.getRaster();
		
		for (int i=0; i<pixelValue.length; i++){
			for (int j=0; j<pixelValue[0].length; j++){
				if (this.isGray()){
					r.setSample(j, i, 0, 255 - ((~this.getPixel(j, i) & 255)));
				}else{
					for (int b=0; b<this.getNumBands() && b<r.getNumBands(); b++){
						r.setSample(j, i, b, this.getPixel(j, i, b));
					}
				}
			}
		}
		
		return bi;
	}
	
	//set
	public void setPixel(int x, int y, int value){this.pixelValue[y][x] = value;}
	public void setPixel(int x, int y, int band, int value){
		int bands[] = new int[this.getNumBands()];
		for (int b=0; b<bands.length; b++) bands[b] = this.getPixel(x, y, b);
		if (band + 1 >= this.getNumBands()) this.setNumBands(band + 1);
		int bands2[] = new int[(band + 1 >= this.getNumBands()) ? band + 1 : this.getNumBands()];
		for (int b=0; b<bands.length; b++) bands2[b] = bands[b];
		bands2[band] = value;
		int finalValue = bands2[0];
		for (int b=1; b<this.getNumBands(); b++) finalValue = (finalValue << 8) | (bands2[b] & 0xFF);
		this.pixelValue[y][x] = finalValue;
	}
	public void setNumBands(int num){this.bands = (byte) num;}
	public void setType(int type){this.type = type;}
	
	public void parseGrey(){
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				this.pixelValue[i][j] = this.getPixel(j, i, 0);
			}
		}
		this.setNumBands(1);
		this.setType(BufferedImage.TYPE_BYTE_GRAY);
	}
	
	//is
	public boolean isGray(){return bands == 1;}
	
	//get
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
		if (minMax == null){
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			int value = 0;
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
			minMax = new Vector(min, max);
		}
		return minMax;
	}
	public int getMinimumIntesity(int band){
		return (int) this.getMinMaxIntensity(band).x;
	}
	public int getMaximumIntensity(int band){
		return (int) this.getMinMaxIntensity(band).y;
	}
	public int getMeanIntensity(int band){
		return (int) ((this.getMinMaxIntensity(band).y + this.getMinMaxIntensity(band).x)/2);
	}
	public int getBitDepth(){return 8;}//acertar dps
	
	public void dispose(){if (this.hues != null) this.hues.clear(); this.hues = null;}
	
	
	
	public Image clone(){
		return new Image(this);
	}
	
	public boolean equals(Image comparedImg, int similarityMeasure) throws Exception{
		boolean equals = true;
		for (int b=0; b<this.getNumBands(); b++){
			switch (similarityMeasure){
				case SimilarityMeasuresOp.MEAN_DIFFERENCE:
					if (SimilarityMeasuresOp.sumOfDifferences(this, comparedImg, 1) == 0) return true;
					break;
			}
		}
		
		return false;
	}
	
	
	public void print(){
		for (int b=0; b<this.getNumBands(); b++){
			System.out.println("Band (layer): " + b);
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					if (!this.isGray())
						System.out.print(this.getPixel(j, i, b) + " ");
					else
						System.out.print(this.getPixel(j, i) + " ");
				}
				System.out.println();
			}
		}
	}
	
	public void exportImage(String outputPath, String formatName) throws IOException{
		ImageIO.write(this.getBufferedImage(), formatName.toUpperCase(), new File(outputPath));
	}
	
	

}
