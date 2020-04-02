package filters.connectivity;

import filters.Filter;
import image.Image;

public class SupConnectivity extends Filter {
    private double threshold = 0;

    /**
     * Sets the threshold to consider a pixel for "counting" the connectivity. Standard threshold is 0. If > 0 then it is a pixel to keep counting the connectivity.
     * @param threshold
     */
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public double getFilteredPixel(Image image, int x, int y, int band) {
        boolean[][] visitMap = new boolean[image.getHeight()][image.getWidth()];

        double[] score = new double[1]; //array for java to treat it as an object
        score[0] = 0;
        checkPixel(image, x, y, band, visitMap, score);

        return score[0];
    }

    private void checkPixel(Image image, int x, int y, int band, boolean[][] visitMap, double[] score){
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight())
            return;

        if (image.getPixel(x,y,band) > this.threshold && !visitMap[y][x]) {
            score[0]++;
            visitMap[y][x] = true; //mark as visited
        }else
            return;

        //cross
        checkPixel(image, x-1, y, band, visitMap, score);
        checkPixel(image, x+1, y, band, visitMap, score);
        checkPixel(image, x, y-1, band, visitMap, score);
        checkPixel(image, x, y+1, band, visitMap, score);

        //diagonals
        checkPixel(image, x+1, y+1, band, visitMap, score);
        checkPixel(image, x-1, y+1, band, visitMap, score);
        checkPixel(image, x+1, y-1, band, visitMap, score);
        checkPixel(image, x-1, y-1, band, visitMap, score);

        return;
    }

    public Image applyFilter(final Image image) {
        Image out = super.applyFilter(image);
        out.stretchOrShrinkRange(0, 255);
        return out;
    }

}