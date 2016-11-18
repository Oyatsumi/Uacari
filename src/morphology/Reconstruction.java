package morphology;

import java.io.IOException;

import similarity.SimilarityMeasuresOp;
import general.Image;
import general.ImageOp;

public class Reconstruction {
	private Image src, mask = null, marker, structuringElement;

	
	public Reconstruction(Image src, Image mask, Image marker, Image structuringElement){
		this.src = src;
		this.mask = mask;
		this.marker = marker;
		this.structuringElement = structuringElement;
	}
	public Reconstruction(Image src, Image marker, Image structuringElement){
		this.src = src;
		this.marker = marker;
		this.structuringElement = structuringElement;
	}
	public Reconstruction(Image src, Image structuringElement){
		this.src = src;
		this.structuringElement = structuringElement;
	}
	
	
	//set
	public void setStructuringElement(Image structuringElement){
		this.structuringElement = structuringElement;
	}
	
	//has
	public boolean hasMask(){return mask != null;}
	
	
	//get
	public Image getMask(){if (this.mask != null) return this.mask; return this.getInputImage();}
	public Image getInputImage(){return this.src;}
	public Image getStructuringElement(){return this.structuringElement;}
	public Image getMarker(){if (this.marker == null) return this.getInputImage(); return this.marker;}
	
	public Image getReconstructedImage() throws Exception{
		return reconstructImage(this.getMarker(), this.getMask(), this.getStructuringElement());
	}
	public Image getClusteredImage() throws Exception{
		this.getInputImage().parseGrey();
		return reconstructImage(MorphologyOp.getIndexedImage(this.getInputImage(), 0), this.getMask(), this.getStructuringElement());
	}
	private static Image reconstructImage(Image marker, Image mask, Image structElement) throws Exception{
		Image itImg = marker.clone(), itImg2 = null;
		int count = 0;
		System.out.println("Processing reconstruction...");
		long startTime = System.nanoTime();
		
		do {
			itImg2 = itImg.clone();
			itImg = MorphologyOp.dilateOrErode(itImg, structElement, true);
			itImg = ImageOp.getMaskedImage(itImg, mask);
			count ++;
		}while(!itImg.equals(itImg2, SimilarityMeasuresOp.MEAN_DIFFERENCE));
		
		System.out.println("| Total of iterations during reconstruction: " + count + " (in " + (System.nanoTime() - startTime) + " nano seconds).");
		
		return itImg;
	}


	
}
