package morphology;

import general.Image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class MorphologyOp {
	public static final Image PRIMARY_STRUCT = new Image(new short[][]{{0,255,0},{255,255,255},{0,255,0}}),
			PRIMARY_GREY_STRUCT = new Image(new short[][]{{0,30,0},{30,255,30},{0,30,0}}),
			SIMPLE_RECT = new Image(new short[][]{{1,1,1},{1,1,1},{1,1,1}});

	
	public static Image dilateOrErode(String sourceImgPath, String structElementPath, boolean dilation) throws IOException{
		//instanciando novos arquivos
		File srcFile = new File(sourceImgPath),
				structElementFile = new File(structElementPath);
		//instanciando novas imagens com base nos arquivos acima
		BufferedImage src = ImageIO.read(srcFile),
				structElement = ImageIO.read(structElementFile);
		//instanciando novos rasters (a matriz da imagem - � tamb�m chamado de rasteriza��o), aqui que os valores da imagens devem ser salvos
		//Raster rasterSrc = src.getRaster(),
			//	rasterstructElement = structElement.getRaster();
		
		return dilateOrErode(new Image(src), new Image(structElement), dilation);
	}
	public static void dilateOrErode(String sourceImgPath, String structElementPath, boolean dilation, String outFilePath) throws IOException{
		//instanciando novos arquivos
		File srcFile = new File(sourceImgPath),
				structElementFile = new File(structElementPath);
		//instanciando novas imagens com base nos arquivos acima
		BufferedImage src = ImageIO.read(srcFile),
				structElement = ImageIO.read(structElementFile);
		//instanciando novos rasters (a matriz da imagem - � tamb�m chamado de rasteriza��o), aqui que os valores da imagens devem ser salvos
		//Raster rasterSrc = src.getRaster(),
			//	rasterstructElement = structElement.getRaster();
		
		dilateOrErodeAndExport(new Image(src), new Image(structElement), dilation, outFilePath, "PNG");
	}
	public static void dilateOrErodeAndExport(Image src, Image structElement, boolean dilation, String outFilePath, String outFileFormat) throws IOException{
		BufferedImage outImg = dilateOrErode(src, structElement, dilation).getBufferedImage();
		
		//instanciando o arquivo de sa�da
		File outFile = new File(outFilePath);
		
		//escrevendo o arquivo de sa�da e verificando se tudo ocorreu bem
		boolean ok = ImageIO.write(outImg, outFileFormat.toUpperCase(), outFile);
		System.out.println("Successfully exported to: " + outFile.getPath());
	}
	public static Image dilateOrErode(Image src, Image structElement, boolean dilation) throws IOException{
		//instanciando a imagem de sa�da e adicionando a uma vari�vel o raster dessa imagem de sa�da
		//BufferedImage outImg = new BufferedImage(src.getWidth(), src.getHeight(), src.getType()); //o tipo da imagem � cinza com um byte por pixel
		//WritableRaster outRaster = outImg.getRaster();
		Image outImg = new Image(src.getWidth(), src.getHeight());
		
		int value = 0;
		//iteranto todos os pixels da imagem src
		for (int i=0; i<src.getHeight(); i++){
			for (int j=0; j<src.getWidth(); j++){
				for (int b=0; b<src.getNumBands(); b++){
					value = getCentralValue(src, structElement, j, i, b, dilation);
					//colocamos o valor (na posi��o j,i e na "primeira banda/layer" da imagem 0) o valor do pixel que desejamos
					//outRaster.setSample(j, i, b, value);
					outImg.setPixel(j, i, b, value);
				}
			}
		}
		
		return outImg;

	}
	/*
	private static int getCentralValue(Image src, Image structElement, int x, int y, int band, boolean dilate){
		int min = 0; //o valor minimo da vizinha � 0 (pixel preto)
		
		//esse for itera os valores i e j de acordo com a posi��o do structElement(structuring element) em cima da imagem source
		for (int i=(int) -Math.floor(structElement.getHeight()/2)+y; i<=Math.floor(structElement.getHeight()/2)+y; i++){
			for (int j=(int) -Math.floor(structElement.getWidth()/2)+x; j<=Math.floor(structElement.getWidth()/2)+x; j++){
				//se as vari�veis i e j n�o estiverem extrapolando os limites da imagem src e n�o forem igual �s coordenadas do pixel central do structuring element
				if (j >= 0 && i >=0 && j < src.getWidth() && i < src.getHeight() /*&& !(i == y && j == i)/){
					int sub = 0; //subtra��o
					if (!dilate) sub = src.getPixel(j, i, band) - structElement.getPixel(j-x+(int)(Math.floor(structElement.getWidth()/2)), 
							i-y+(int)(Math.floor(structElement.getHeight()/2)), band); //se for pra eros�o
					else sub = (255-structElement.getPixel(j-x+(int)(Math.floor(structElement.getWidth()/2)), 
							i-y+(int)(Math.floor(structElement.getHeight()/2)), band)) - src.getPixel(j, i, band); //se for pra dilatar
					if (sub < min)
						min = sub;
				}
			}
		}
		int result = min+255; //para imagens bin�rias 'min' pode ser -255 ou 0
		if (dilate) result = 255-result; //inverter a imagem se for pra dilatar

		return result;
	}
	*/
	private static int getCentralValue(Image src, Image structElement, int x, int y, int band, boolean dilate){
		int min = 0; //o valor minimo da vizinha � 0 (pixel preto)
		int max = src.getMaximumIntensity(band);
		if (max < 255) max = 255;
		
		
		//esse for itera os valores i e j de acordo com a posi��o do structElement(structuring element) em cima da imagem source
		for (int i=(int) -Math.floor(structElement.getHeight()/2)+y; i<=Math.floor(structElement.getHeight()/2)+y; i++){
			for (int j=(int) -Math.floor(structElement.getWidth()/2)+x; j<=Math.floor(structElement.getWidth()/2)+x; j++){
				//se as vari�veis i e j n�o estiverem extrapolando os limites da imagem src e n�o forem igual �s coordenadas do pixel central do structuring element
				if (j >= 0 && i >=0 && j < src.getWidth() && i < src.getHeight() /*&& !(i == y && j == i)*/){
					int sub = 0; //subtra��o
					int structValue = structElement.getPixel(j-x+(int)(Math.floor(structElement.getWidth()/2)), 
							i-y+(int)(Math.floor(structElement.getHeight()/2)), band);
					if (structValue == 255) structValue = max;
					if (!dilate) sub = src.getPixel(j, i, band) - structValue; //se for pra eros�o
					else sub = (max-structValue) - src.getPixel(j, i, band); //se for pra dilatar
					if (sub < min)
						min = sub;
				}
			}
		}
		int result = min+max; //para imagens bin�rias 'min' pode ser -255 ou 0
		if (dilate) result = max-result; //inverter a imagem se for pra dilatar

		//System.out.println(result);
		return result;
	}
	
	
	public static Image getIndexedImage(Image img, int threshold){
		Image out = new Image(img.getWidth(), img.getHeight());
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				if (img.getPixel(j, i) > threshold){
					out.setPixel(j, i, i*img.getWidth() + j);
				}
			}
		}
		return out;
	}
	
}
