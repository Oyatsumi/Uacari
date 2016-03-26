package morphology;

import image.Image;
import image.ImageOperation;

/**
 * General class for morphology, including dilations, erosions, reconstructions, etc.
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 */
public class Morphology {
	public static final Image PRIMARY_STRUCT = new Image(new short[][]{{0,255,0},{255,255,255},{0,255,0}}),
			PRIMARY_GREY_STRUCT = new Image(new short[][]{{0,30,0},{30,255,30},{0,30,0}}),
			SIMPLE_BINARY_RECT = new Image(new short[][]{{1,1,1},{1,1,1},{1,1,1}});
	
	private ImageOperation op = null;
	private MorphologicalReconstruction mr = null;
	
	
	public Morphology(Image img){
		this.op = new ImageOperation(img);
	}
	public Morphology(){}
	
	/**
	 * Returns a new dilated image.
	 * @param img
	 * @param structuringElement
	 * @param timesToDilate
	 * @return
	 * @throws Exception
	 */
	public Image dilate(Image img, Image structuringElement, int timesToDilate) throws Exception {
		if (this.op == null) op = new ImageOperation(img);
		op.setImage(img);
		Image out = new Image(img);
		return out.dilate(structuringElement, timesToDilate);
	}
	
	/**
	 * Returns a new eroded image.
	 * @param img
	 * @param structuringElement
	 * @param timesToDilate
	 * @return
	 * @throws Exception
	 */
	public Image erode(Image img, Image structuringElement, int timesToErode) throws Exception {
		if (this.op == null) op = new ImageOperation(img);
		op.setImage(img);
		Image out = new Image(img);
		return out.dilate(structuringElement, timesToErode);
	}
	
	
	/**
	 * Performs a morphological reconstruction.
	 * @return
	 * @throws Exception 
	 */
	public Image reconstruct(Image marker, Image mask, Image structuringElement) throws Exception{
		if (this.mr == null) this.mr = new MorphologicalReconstruction(marker, mask, structuringElement);
		this.mr.setStructuringElement(structuringElement); this.mr.setMarker(marker); this.mr.setMask(mask);
		return mr.getReconstructedImage();
	}
	
	
	/**
	 * Cluster a binary image
	 * @param img
	 * @param structuringElement
	 * @return
	 * @throws Exception
	 */
	public Image cluster(Image img, Image structuringElement) throws Exception{
		if (this.mr == null) this.mr = new MorphologicalReconstruction();
		this.mr.setStructuringElement(structuringElement); this.mr.setMask(img);
		return mr.getClusteredImage();
	}
}
