package image;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * Some image operations. Including various Java's Graphics2D derived operations.
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class ImageOperation implements ImageConstants {

	
	private BufferedImage outBuffImg = null;
	private Graphics2D g2d = null;
	
	private Image associatedImg = null;
	
	private Object antiAlias = RenderingHints.VALUE_ANTIALIAS_OFF, interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	
	public ImageOperation(Image associatedImage){
		this.associatedImg = associatedImage;
	}
	public void setImage(Image associatedImg){this.associatedImg = associatedImg;}

	
	public void setAntiAliasing(boolean setOn){
		if (setOn) this.antiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
		else this.antiAlias = RenderingHints.VALUE_ANTIALIAS_OFF;
	}
	private void setRenderingKeys(Graphics2D g2d){
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
	}
	/**
	 * Sets the interpolation when the image is rendered using Java's Graphics2D
	 * @param interpolationType - the types can be Image.BICUBIC, Image.BILINEAR or Image.NEAREST_NEIGHBOR
	 */
	public void setInterpolation(Object interpolationType){
		this.interpolation = interpolationType;
	}
	
	/*
	public static BufferedImage cloneImage(BufferedImage img){
		Graphics2D g2d;
		BufferedImage outBuffImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		g2d = outBuffImg.createGraphics();
		g2d.drawImage(img, null, 0, 0);
		g2d.dispose();
		return outBuffImg;
	}
	*/
	
	public BufferedImage translate(int x, int y) throws Exception {
	    outBuffImg = new BufferedImage(associatedImg.getWidth(), associatedImg.getHeight(), associatedImg.getType());
	    g2d = outBuffImg.createGraphics();
	    setRenderingKeys(g2d);
	    g2d.translate(x, y);
	    g2d.drawImage(associatedImg.getBufferedImage(), null, 0, 0);
	    g2d.dispose();
	    associatedImg.updateImage(outBuffImg);
	    return outBuffImg;
	}
	public BufferedImage scale(double x, double y) throws Exception{
		outBuffImg = new BufferedImage((int)(associatedImg.getWidth()*x), (int)(associatedImg.getHeight()*y), associatedImg.getType());
		g2d = outBuffImg.createGraphics();
		setRenderingKeys(g2d);
		g2d.scale(x, y);
		g2d.drawImage(associatedImg.getBufferedImage(), null, 0, 0);
		g2d.dispose();
		associatedImg.updateImage(outBuffImg);
		return outBuffImg;
	}
	public BufferedImage scaleWithFixedSize(double x, double y) throws Exception{
		outBuffImg = new BufferedImage((int)(associatedImg.getWidth()), (int)(associatedImg.getHeight()), associatedImg.getType());
		g2d = outBuffImg.createGraphics();
		setRenderingKeys(g2d);
		g2d.scale(x, y);
		g2d.drawImage(associatedImg.getBufferedImage(), null, 0, 0);
		g2d.dispose();
		associatedImg.updateImage(outBuffImg);
		return outBuffImg;
	}
	public BufferedImage transform(AffineTransform t/*, Object interpolation*/) throws Exception{
		outBuffImg = new BufferedImage(associatedImg.getWidth(), associatedImg.getHeight(), associatedImg.getType());
		g2d = outBuffImg.createGraphics();
		setRenderingKeys(g2d);
		//AffineTransformOp atop = new AffineTransformOp(t, interpolation); JRE7
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.transform(t);
		g2d.drawImage(associatedImg.getBufferedImage(), 0, 0, null);
		g2d.dispose();
		associatedImg.updateImage(outBuffImg);
		return outBuffImg;
	}
	
	public BufferedImage drawLine(Vector p1, Vector p2, Color c) throws Exception{
		outBuffImg = new BufferedImage(associatedImg.getWidth(), associatedImg.getHeight(), associatedImg.getType());
		g2d = outBuffImg.createGraphics();
		setRenderingKeys(g2d);
		g2d.drawImage(associatedImg.getBufferedImage(), null, 0, 0);
		g2d.setColor(c);
		g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
		g2d.dispose();
		associatedImg.updateImage(outBuffImg);
		return outBuffImg;
	}
	
	public BufferedImage blendImages(BufferedImage topImg, int x, int y) throws Exception{return blendImages(topImg, new Point2D.Float(x, y));}
	public BufferedImage blendImages(BufferedImage topImg, Vector topImgPosition) throws Exception{
		return blendImages(topImg, new Point2D.Float(topImgPosition.x, topImgPosition.y));
	}
	public BufferedImage blendImages(BufferedImage topImg, Point2D topImgPosition) throws Exception{
		outBuffImg = new BufferedImage(associatedImg.getWidth(), associatedImg.getHeight(), topImg.getType());
		// paint both images, preserving the alpha channels
		g2d = (Graphics2D) outBuffImg.getGraphics();
		setRenderingKeys(g2d);
		g2d.drawImage(associatedImg.getBufferedImage(), 0, 0, null);
		g2d.drawImage(topImg, (int)topImgPosition.getX(), (int)topImgPosition.getY(), null);
		g2d.dispose();
		associatedImg.updateImage(outBuffImg);
		return outBuffImg;
	}
	public BufferedImage blendImages(Image topImg, Point2D topImgPosition) throws Exception{
		return blendImages(topImg.getBufferedImage(), topImgPosition);
	}
	
	public Image invert(){
		for (int b=0; b<associatedImg.getNumBands(); b++){
			double maxTone = associatedImg.getMaximalIntensity(b);
				for (int i=0; i<associatedImg.getHeight(); i++){
					for (int j=0; j<associatedImg.getWidth(); j++){
						associatedImg.setPixel(j, i, b, maxTone - associatedImg.getPixel(j, i, b));
					}
				}
		}
		return associatedImg;
		/*
		outBuffImg = new BufferedImage(associatedImg.getWidth(), associatedImg.getHeight(), associatedImg.getType());
		r = outBuffImg.getRaster();
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<r.getNumBands(); b++){
					r.setSample(j, i, b, 255 - r.getSample(j, i, b));
				}
			}
		}
		return outBuffImg;
		*/
	}
	
	public Image intersect(Image imgToIntersect){
		double value = 0;
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					value = (associatedImg.getPixel(j, i, b) == imgToIntersect.getPixel(j, i, b)) ? associatedImg.getPixel(j, i, b) : 0;
					associatedImg.setPixel(j, i, b, value);
				}
			}
		}
		return associatedImg;
	}
	public Image getMaskedImage(Image mask){
		for (int b=0; b<associatedImg.getNumBands(); b++){
			for (int i=0; i<associatedImg.getHeight(); i++){
				for (int j=0; j<associatedImg.getWidth(); j++){
					if (mask.getPixel(j, i, b) <= 0){
						associatedImg.setPixel(j, i, b, 0);
					}
				}
			}
		}
		return associatedImg;
	}
	
	public Image addBrightness(int valueToAdd){
		for (int b=0; b<associatedImg.getNumBands(); b++){
			for (int i=0; i<associatedImg.getHeight(); i++){
				for (int j=0; j<associatedImg.getWidth(); j++){
					associatedImg.setPixel(j, i, b, associatedImg.getPixel(j, i, b) + valueToAdd);
				}
			}
		}
		return associatedImg;
	}
	
	public Image subtract(Image imgToSubtract){
			for (int b=0; b<associatedImg.getNumBands(); b++){
				for (int i=0; i<associatedImg.getHeight(); i++){
					for (int j=0; j<associatedImg.getWidth(); j++){
						associatedImg.setPixel(j, i, b, Math.abs(associatedImg.getPixel(j, i, b) - imgToSubtract.getPixel(j, i, b)));
					}	
				}
			}
		return associatedImg;
	}

	
	
	//threshold values
	int upperValue = 255, lowerValue = 0;
	
	public void setThresholdImageUpperValue(int value){this.upperValue = value;}
	public void setThresholdImageLowerValue(int value){this.lowerValue = value;}

	public Image threshold(int thresholdLevel){
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					if (associatedImg.getPixel(j, i, b) > thresholdLevel)
						associatedImg.setPixel(j, i, b, upperValue /*255*/);
					else
						associatedImg.setPixel(j, i, b, lowerValue /*0*/);
				}
			}
		}
		return associatedImg;
	}
	public Image threshold(int lowerThresholdLevel, int upperThresholdLevel){
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					if (associatedImg.getPixel(j, i, b) > lowerThresholdLevel && associatedImg.getPixel(j, i, b) < upperThresholdLevel)
						associatedImg.setPixel(j, i, b, upperValue /*255*/);
					else
						associatedImg.setPixel(j, i, b, lowerValue /*0*/);
				}
			}
		}
		return associatedImg;
	}
	public Image smoothThreshold(int lowerThresholdLevel, int upperThresholdLevel){
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					if (!(associatedImg.getPixel(j, i, b) > lowerThresholdLevel && associatedImg.getPixel(j, i, b) < upperThresholdLevel))
						associatedImg.setPixel(j, i, b, lowerValue);
				}
			}
		}
		return associatedImg;
	}
	
	public Image toGray(){
		if (associatedImg.getNumBands() == 1 || associatedImg.isGray()) return associatedImg;
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					associatedImg.setPixel(j, i, b, (int)(0.2126f*associatedImg.getPixel(j, i, 0) + 0.7152f*associatedImg.getPixel(j, i, 1) + 
							0.0722f*associatedImg.getPixel(j, i, 2)));
				}
			}
		}
		return associatedImg;
	}
	
	public Image antiAlias(){
		int count = 0;
		double outValue = 0;
		//"bicubic"
		for (int b=0; b<associatedImg.getNumBands(); b++){
			for (int i=1; i<associatedImg.getHeight()-1; i++){
				for (int j=1; j<associatedImg.getWidth()-1; j++){
					count = 1;
					outValue = associatedImg.getPixel(j, i, b);
					if (associatedImg.getPixel(j - 1, i, b) == associatedImg.getPixel(j, i + 1, b)){
						outValue += associatedImg.getPixel(j - 1, i, b);
						count ++;
					}
					if (associatedImg.getPixel(j, i + 1, b) == associatedImg.getPixel(j + 1, i, b)){
						outValue += associatedImg.getPixel(j, i + 1, b);
						count ++;
					}
					if (associatedImg.getPixel(j + 1, i, b) == associatedImg.getPixel(j, i - 1, b) ){
						outValue += associatedImg.getPixel(j + 1, i, b);
						count ++;
					}
					if (associatedImg.getPixel(j, i - 1, b) == associatedImg.getPixel(j - 1, i, b)){
						outValue += associatedImg.getPixel(j, i - 1, b);
						count ++;
					}
					if (count > 1){
						associatedImg.setPixel(j, i, b, outValue/count);
					}
				}
			}
		}
		
		return associatedImg;
	}
	
	
	
	
	//MORPHOLOGY
	public Image dilateOrErode(Image structElement, boolean dilation) throws IOException, CloneNotSupportedException{
		Image aux = new Image(associatedImg);
		
		double value = 0;
		for (int i=0; i<associatedImg.getHeight(); i++){
			for (int j=0; j<associatedImg.getWidth(); j++){
				for (int b=0; b<associatedImg.getNumBands(); b++){
					value = getCentralValue(aux, structElement, j, i, b, dilation);
					associatedImg.setPixel(j, i, b, value);
				}
			}
		}
		
		aux.dispose();
		aux = null;
		return associatedImg;
	}
	private double getCentralValue(Image src, Image structElement, int x, int y, int band, boolean dilate){
		double min = 0;
		double max = src.getMaximalIntensity(band);
		if (max < 255) max = 255;
		
		
		//esse for itera os valores i e j de acordo com a posição do structElement(structuring element) em cima da imagem source
		for (int i=(int) -Math.floor(structElement.getHeight()/2)+y; i<=Math.floor(structElement.getHeight()/2)+y; i++){
			for (int j=(int) -Math.floor(structElement.getWidth()/2)+x; j<=Math.floor(structElement.getWidth()/2)+x; j++){
				//se as variáveis i e j não estiverem extrapolando os limites da imagem src e não forem igual às coordenadas do pixel central do structuring element
				if (j >= 0 && i >=0 && j < src.getWidth() && i < src.getHeight() /*&& !(i == y && j == i)*/){
					double sub = 0; //subtração
					double structValue = structElement.getPixel(j-x+(int)(Math.floor(structElement.getWidth()/2)), 
							i-y+(int)(Math.floor(structElement.getHeight()/2)), band);
					if (structValue == 255) structValue = max;
					if (!dilate) sub = src.getPixel(j, i, band) - structValue; //se for pra erosão
					else sub = (max-structValue) - src.getPixel(j, i, band); //se for pra dilatar
					if (sub < min)
						min = sub;
				}
			}
		}
		double result = min+max; //para imagens binárias 'min' pode ser -255 ou 0
		if (dilate) result = max-result; //inverter a imagem se for pra dilatar

		//System.out.println(result);
		return result;
	}
	
	
	
	
	
	
}
