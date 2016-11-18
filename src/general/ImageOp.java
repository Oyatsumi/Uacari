package general;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public abstract class ImageOp {
	/*
	public static final int BICUBIC = AffineTransformOp.TYPE_BICUBIC,
			BILINEAR = AffineTransformOp.TYPE_BILINEAR,
			NEAREST_NEIGHBOR = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
			*/
	public static final Object BICUBIC = RenderingHints.VALUE_INTERPOLATION_BICUBIC,
			BILINEAR = RenderingHints.VALUE_INTERPOLATION_BILINEAR,
			NEAREST_NEIGHBOR = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

	public static BufferedImage cloneImage(BufferedImage img){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.drawImage(img, null, 0, 0);
		g2d.dispose();
		return outputImg;
	}
	
	public static BufferedImage translate(int x, int y, BufferedImage img) {
	    BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
	    Graphics2D g2d = outputImg.createGraphics();
	    g2d.translate(x, y);
	    g2d.drawImage(img, null, 0, 0);
	    g2d.dispose();
	    return outputImg;
	}
	public static BufferedImage scale(double x, double y, BufferedImage img){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.scale(x, y);
		g2d.drawImage(img, null, 0, 0);
		g2d.dispose();
		return outputImg;
	}
	public static BufferedImage transform(BufferedImage img, AffineTransform t, Object interpolation){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		//AffineTransformOp atop = new AffineTransformOp(t, interpolation); JRE7
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.transform(t);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return outputImg;
	}
	
	public static BufferedImage drawLine(BufferedImage img, Vector p1, Vector p2, Color c){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.drawImage(img, null, 0, 0);
		g2d.setColor(c);
		g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
		g2d.dispose();
		return outputImg;
	}
	
	public static BufferedImage blendImages(BufferedImage backgroundImg, BufferedImage topImg, Point2D topImgPosition){
		BufferedImage combined = new BufferedImage(backgroundImg.getWidth(), backgroundImg.getHeight(), topImg.getType());
		// paint both images, preserving the alpha channels
		Graphics2D g2d = (Graphics2D) combined.getGraphics();
		g2d.drawImage(backgroundImg, 0, 0, null);
		g2d.drawImage(topImg, (int)topImgPosition.getX(), (int)topImgPosition.getY(), null);
		g2d.dispose();
		
		return combined;
	}
	
	public static BufferedImage invert(BufferedImage img){
		BufferedImage out = cloneImage(img);
		WritableRaster r = out.getRaster();
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				for (int b=0; b<r.getNumBands(); b++){
					r.setSample(j, i, b, 255 - r.getSample(j, i, b));
				}
			}
		}
		return out;
	}
	
	public static Image intersect(Image image1, Image image2){
		Image out = new Image(image1.getWidth(), image1.getHeight());
		int value = 0;
		for (int b=0; b<image1.getNumBands(); b++){
			for (int i=0; i<image1.getHeight(); i++){
				for (int j=0; j<image1.getWidth(); j++){
					value = (image1.getPixel(j, i, b) == image2.getPixel(j, i, b)) ? image1.getPixel(j, i, b) : 0;
					out.setPixel(j, i, b, value);
				}
			}
		}
		return out;
	}
	public static Image getMaskedImage(Image image, Image mask){
		Image out = new Image(image.getWidth(), image.getHeight());
		for (int b=0; b<image.getNumBands(); b++){
			for (int i=0; i<image.getHeight(); i++){
				for (int j=0; j<image.getWidth(); j++){
					if (mask.getPixel(j, i, b) > 0){
						out.setPixel(j, i, b, image.getPixel(j, i, b));
					}
				}
			}
		}
		return out;
	}
	
	public static Image addBrightness(Image image, int addValue){
		Image out = new Image(image.getWidth(), image.getHeight());
		for (int b=0; b<image.getNumBands(); b++){
			for (int i=0; i<image.getHeight(); i++){
				for (int j=0; j<image.getWidth(); j++){
					out.setPixel(j, i, b, image.getPixel(j, i, b) + addValue);
				}
			}
		}
		return out;
	}
	
	public static Image subtract(Image img1, Image img2){
		Image out = new Image(img1.getWidth(), img1.getHeight());
		out.setNumBands(img1.getNumBands());
		if (!img1.isGray()){
			for (int b=0; b<img1.getNumBands(); b++){
				for (int i=0; i<img1.getHeight(); i++){
					for (int j=0; j<img1.getWidth(); j++){
						out.setPixel(j, i, b, Math.abs(img1.getPixel(j, i, b) - img2.getPixel(j, i, b)));
					}	
				}
			}
		}else{
			for (int i=0; i<img1.getHeight(); i++){
				for (int j=0; j<img1.getWidth(); j++){
					out.setPixel(j, i, img1.getPixel(j, i) - img2.getPixel(j, i));
				}	
			}
		}
		return out;
	}
	

	public static Image threshold(Image in, int level){
		Image out = new Image(in.getWidth(), in.getHeight());
		for (int i=0; i<in.getHeight(); i++){
			for (int j=0; j<in.getWidth(); j++){
				if (in.getPixel(j, i) > level)
					out.setPixel(j, i, 255);
			}
		}
		return out;
	}
	public static Image threshold(Image in, int downLevel, int upLevel){
		Image out = new Image(in.getWidth(), in.getHeight());
		for (int i=0; i<in.getHeight(); i++){
			for (int j=0; j<in.getWidth(); j++){
				if (in.getPixel(j, i) > downLevel && in.getPixel(j, i) < upLevel)
					out.setPixel(j, i, 255);
			}
		}
		return out;
	}
	public static Image greyThreshold(Image in, int downLevel, int upLevel){
		Image out = new Image(in.getWidth(), in.getHeight());
		for (int i=0; i<in.getHeight(); i++){
			for (int j=0; j<in.getWidth(); j++){
				if (in.getPixel(j, i) > downLevel && in.getPixel(j, i) < upLevel)
					out.setPixel(j, i, in.getPixel(j, i));
			}
		}
		return out;
	}
	
	public static Image toGrayLevel(Image coloredImg){
		Image out = new Image(coloredImg.getWidth(), coloredImg.getHeight());
		int value = 0;
		for (int i=0; i<coloredImg.getHeight(); i++){
			for (int j=0; j<coloredImg.getWidth(); j++){
				/*
				value = 0;
				for (int b=0; b<coloredImg.getNumBands(); b++){
					value += coloredImg.getPixel(j, i, b);
				}
				value /= coloredImg.getNumBands();
				out.setPixel(j, i, value);
				*/
				out.setPixel(j, i, (int)(0.2126f*coloredImg.getPixel(j, i, 0) + 0.7152f*coloredImg.getPixel(j, i, 1) + 
						0.0722f*coloredImg.getPixel(j, i, 2)));
			}
		}
		return out;
	}
	
	public static Image antiAlias(Image image){
		Image out = image.clone();
		int count = 0, outValue = 0;
		//"bicubic"
		for (int b=0; b<image.getNumBands(); b++){
			for (int i=1; i<image.getHeight()-1; i++){
				for (int j=1; j<image.getWidth()-1; j++){
					count = 1;
					outValue = image.getPixel(j, i, b);
					if (image.getPixel(j - 1, i, b) == image.getPixel(j, i + 1, b)){
						outValue += image.getPixel(j - 1, i, b);
						count ++;
					}
					if (image.getPixel(j, i + 1, b) == image.getPixel(j + 1, i, b)){
						outValue += image.getPixel(j, i + 1, b);
						count ++;
					}
					if (image.getPixel(j + 1, i, b) == image.getPixel(j, i - 1, b) ){
						outValue += image.getPixel(j + 1, i, b);
						count ++;
					}
					if (image.getPixel(j, i - 1, b) == image.getPixel(j - 1, i, b)){
						outValue += image.getPixel(j, i - 1, b);
						count ++;
					}
					if (count > 1){
						out.setPixel(j, i, b, outValue/count);
					}
				}
			}
		}
		
		return out;
	}
}
