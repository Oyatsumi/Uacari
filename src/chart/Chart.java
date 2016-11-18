package chart;

import general.Image;
import general.ImageOp;
import general.Vector;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.imageio.ImageIO;

import morphology.MorphologyOp;

public class Chart {
	private boolean[][] chart;
	private boolean connectPoints = false, exportMean = false;
	private BigDecimal sumY = null;
	private long pointsQuantity = 0;
	private int timesToDilate = 0;
	private Image structuringElement = null;
	private String name = null;
	
	private byte padding = 5;
	
	public Chart(int pixelSizeX, int pixelSizeY, boolean exportMean, boolean connectPoints){
		this.chart = new boolean[pixelSizeY][pixelSizeX];
		this.connectPoints = connectPoints;
		this.exportMean = exportMean;
	}
	
	//set
	private int initial = (int) (padding/1.5 + 1);
	public void setPoint(double x, double y){this.setPoint((float)x, (float)y);}
	public void setPoint(float x, float y){
		//if (x < 0 || y < 0 || x > 1 || y > 1) return;
		
		if (!(x < 0 || y < 0 || x > 1 || y > 1)){
			chart[chart.length - (padding + Math.round((chart.length-2*padding)*y))]
					[padding + Math.round((chart[0].length-2*padding)*x)] = true;
		}else{
			System.out.println("The point (" + x + "," + y + ") is not being plotted in the chart.");
		}
		
		if (this.isToExportMean()) {
			if (sumY == null){sumY = BigDecimal.valueOf(0);}
			sumY = sumY.add(BigDecimal.valueOf(y));
		}
		pointsQuantity ++;
	}
	public void setToDilate(Image structElement, int times){this.structuringElement = structElement; timesToDilate = times;}
	
	
	private void buildChartSkeleton(WritableRaster r){
		
		for (int j=initial; j<chart[0].length-2*padding; j++)
			for (int b=0; b<3; b++)
				r.setSample(j, chart.length - padding, b, 100);
		
		for (int i=initial; i<chart.length-2*padding; i++)
			for (int b=0; b<3; b++)
				r.setSample(padding, chart.length - i, b, 100);
		
		int size = 2, count = 0;
		while (size >= 0){
			for (int a=-size; a<=size; a++){
				for (int b=0; b<3; b++){
					r.setSample(-initial + chart[0].length-padding + count, chart.length - padding + a, b, 100);
				}
			}
			size --;
			count ++;
		}
		size = 2; count = 0;
		while (size >= 0){
			for (int a=-size; a<=size; a++){
				for (int b=0; b<3; b++){
					r.setSample(padding + a, initial + padding - count, b, 100);
				}
			}
			size --;
			count ++;
		}
	}
	
	public void exportAsImage(String filePath, String fileFormat) throws IOException{
		
		BufferedImage bi = new BufferedImage(chart[0].length, chart.length, BufferedImage.TYPE_INT_RGB);
		WritableRaster r = bi.getRaster();
		for (int i=0; i<r.getHeight(); i++){
			for (int j=0; j<r.getWidth(); j++){
				if (chart[i][j]){
					for (int b=0; b<3; b++)
						r.setSample(j, i, b, 0);
				}else{
					for (int b=0; b<3; b++)
						r.setSample(j, i, b, 255);
				}
			}
		}
		
		
		//mean line
		if (this.isToExportMean()){
			int auxY = (int) (chart.length - (initial + Math.round((chart.length-2*padding)*this.getMeanY())));
			if (auxY >= r.getHeight() || auxY < 0){
				System.out.println("The mean cannot be plotted because it exceeds the upper y boundary.");
			}else{
				for (int j=padding; j<chart[0].length-2*padding; j++){
					r.setSample(j, auxY, 0, r.getSample(j, auxY, 0)/2 + 100);
					r.setSample(j, auxY, 1, 0);
					r.setSample(j, auxY, 2, 0);
				}
			}
		}
		
		//connecting points
		if (this.isToConnectPoints()){
			int lastX = 0, lastY = 0;
			//find the first point
			firstPoint:
			for (int j=padding; j<chart[0].length; j++){
				for (int i=padding; i<chart.length; i++){
					if (chart[chart.length - i][j]){
						lastX = j; lastY = chart.length - i;
						break firstPoint;
					}
				}
			}
			nextColumn:
			for (int j=padding; j<chart[0].length; j++){
				for (int i=padding; i<chart.length; i++){
					if (chart[i][j] && lastX != j && lastY != i){
						bi = ImageOp.drawLine(bi, new Vector(lastX, lastY), new Vector(j, i), Color.BLACK);
						lastX = j; lastY = i;
						continue nextColumn;
					}
				}
			}
		}
		

		
		
		//dilate
		int dilateCount = timesToDilate;
		//bi = ImageOp.invert(bi);
		while (dilateCount > 0) {
			bi = MorphologyOp.dilateOrErode(new Image(bi), this.structuringElement, false).getBufferedImage();
			dilateCount --;
		}
		
		//bi = ImageOp.antiAlias(new Image(bi)).getBufferedImage();
		
		
		buildChartSkeleton(bi.getRaster());
		
		ImageIO.write(bi, fileFormat.toUpperCase(), new File(filePath));
	}
	
	
	
	
	//is
	public boolean isToExportMean(){return exportMean;}
	public boolean isToConnectPoints(){return connectPoints;}
	
	
	//get
	public int getPadding(){return padding;}
	public double getMeanY(){return sumY.divide(BigDecimal.valueOf(pointsQuantity), 2, RoundingMode.HALF_UP).floatValue();}
	public String getName(){return name;}
}
