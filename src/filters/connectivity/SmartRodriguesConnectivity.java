package filters.connectivity;

import filters.Filter;
import image.Image;

/**
 * Smart/intelligent connectivity filter that uses the Rodrigues distance. This filter should be run with a large stack size (use -Xss when launching the program).
 * Created by: Erick Oliveira Rodrigues
 */
public class SmartRodriguesConnectivity extends Filter {
    private double threshold = 0;
    private long maxErrorScore = 100; //70
    private double maxDistance = 3;
    private Image kernel = new Image(new int[][]{{0,1,0},{1,1,1},{0,1,0}});

    /**
     * Sets the threshold to consider a pixel for "counting" the connectivity. Standard threshold is 0. If > 0 then it is a pixel to keep counting the connectivity.
     * @param threshold
     */
    public void setThreshold(final double threshold){
        this.threshold = threshold;
    }

    /**
     * Sets the kernel used to connect the points in the connectivity filter
     */
    public void setKernel(final Image kernel){
        this.kernel = kernel;
    }

    /**
     * Sets the maximal error score that can be found before halting the filter.
     * The error score is a sum of pixels below the threshold.
     * @param maxErrorScore
     */
    public void setMaxErrorScore(final long maxErrorScore){
        this.maxErrorScore = maxErrorScore;
    }

    /**
     * Sets the maximal distance between disconnected pixels.
     * @param maxDistance
     */
    public void setMaxDistance(final long maxDistance){
        this.maxDistance = maxDistance;
    }

    @Override
    public double getFilteredPixel(final Image image, final int x, final int y, final int band) {
        boolean[][] visitMap = new boolean[image.getHeight()][image.getWidth()];

        long[] connectivityScore = new long[1]; //array for java to treat it as an object
        long[] errorScore = new long[1]; //array to keep count of not-connected pixels
        checkPixel(image, x, y, band, visitMap, connectivityScore, errorScore);

        return connectivityScore[0];
    }

    private void checkPixel(final Image image, final int x, final int y, final int band, final boolean[][] visitMap, final long[] connectivityScore, final long[] errorScore){
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight())
            return;

        if (image.getPixel(x, y, band) > this.threshold && !visitMap[y][x]) {
            errorScore[0] = 0; //reset the errors once a new connection pixel has been found
            connectivityScore[0]++;
            visitMap[y][x] = true; //mark as visited

            //kernel-based vicinity access
            final int midX = kernel.getWidth()/2, midY = kernel.getHeight()/2;
            for (int i=0; i<kernel.getHeight(); i++){
                for (int j=0; j<kernel.getWidth(); j++){
                    int posX = -midX + x + j,
                            posY = -midY + y + i;

                    if (kernel.getPixel(j, i) > 0) {
                        checkPixel(image, posX, posY, band, visitMap, connectivityScore, errorScore);

                    /*
                    if ((connectivityScore[0] - currentScore) >= 2 && errorScore[0] > 0 && value <= this.threshold) {
                        connectivityScore[0] = 0;
                        return;
                    }
                    */
                    }
                }
            }

        }else if (errorScore[0] <= this.maxErrorScore && !visitMap[y][x]){
            errorScore[0]++;
            //connectivityScore[0]++;
            //visitMap[y][x] = true; //mark as visited

            rodriguesErrorSum(image, x, y, band, visitMap, connectivityScore, errorScore);

        }else
            return;


        //percorrer um SUP ao redor do pixel e fazer o sum numa variável (chamar recursivo só depois de achar o pixel branco)
        //olhar o artigo do combining minkowski e chebyshev


        //acho que tem que deixar o próximo código



        return;
    }

    /**
     * This method is based on the neighborhood iteration of the Rodrigues distance
     * https://www.sciencedirect.com/science/article/abs/pii/S0167865518301004
     * @param image
     * @param xc
     * @param yc
     * @param band
     * @return
     */
    public void rodriguesErrorSum(final Image image, final int xc, final int yc, final int band, final boolean[][] visitMap, final long[] connectivityScore, final long[] errorScore){

        int d = 1, dist = d;
        final int LIMIT = (int) Math.ceil(this.maxDistance/2f);

        long sum = 0;

        while (d < LIMIT){

            if (dist > this.maxDistance || errorScore[0] > this.maxErrorScore) return;
            sum = 0;

            //1
            if (image.getPixelBoundaryMode(xc + d, yc, band) > this.threshold) {
                checkPixel(image, xc + d, yc, band, visitMap, connectivityScore, errorScore);
                sum++;
            }else
                errorScore[0]++;
            //2
            if (image.getPixelBoundaryMode(xc + d, yc, band) > this.threshold) {
                checkPixel(image, xc + d, yc, band, visitMap, connectivityScore, errorScore);
                sum++;
            }else
                errorScore[0]++;
            //3
            if (image.getPixelBoundaryMode(xc, yc + d, band) > this.threshold) {
                checkPixel(image, xc, yc + d, band, visitMap, connectivityScore, errorScore);
                sum++;
            }else
                errorScore[0]++;
            //4
            if (image.getPixelBoundaryMode(xc, yc - d, band) > this.threshold) {
                checkPixel(image, xc, yc - d, band, visitMap, connectivityScore, errorScore);
                sum++;
            }else
                errorScore[0]++;

            if (sum > 1) return;


            for (int l=1; l <= d; l++){

                if (dist + l > this.maxDistance || errorScore[0] > this.maxErrorScore) return;
                sum = 0;

                //1
                if (image.getPixelBoundaryMode(xc + d, yc - l, band) > this.threshold) {
                    checkPixel(image, xc + d, yc - l, band, visitMap, connectivityScore, errorScore);
                    sum++;
                }else
                    errorScore[0]++;
                //2
                if (image.getPixelBoundaryMode(xc + d, yc + l, band) > this.threshold) {
                    checkPixel(image, xc + d, yc + l, band, visitMap, connectivityScore, errorScore);
                    sum++;
                }else
                    errorScore[0]++;
                //3
                if (image.getPixelBoundaryMode(xc - d, yc - l, band) > this.threshold) {
                    checkPixel(image, xc - d, yc - l, band, visitMap, connectivityScore, errorScore);
                    sum++;
                }else
                    errorScore[0]++;
                //4
                if (image.getPixelBoundaryMode(xc - d, yc + l, band) > this.threshold) {
                    checkPixel(image, xc - d, yc + l, band, visitMap, connectivityScore, errorScore);
                    sum++;
                }else
                    errorScore[0]++;

                if (l != d){

                    //1
                    if (image.getPixelBoundaryMode(xc - l, yc + d, band) > this.threshold) {
                        checkPixel(image, xc - l, yc + d, band, visitMap, connectivityScore, errorScore);
                        sum++;
                    }else
                        errorScore[0]++;
                    //2
                    if (image.getPixelBoundaryMode(xc + l, yc + d, band) > this.threshold) {
                        checkPixel(image, xc + l, yc + d, band, visitMap, connectivityScore, errorScore);
                        sum++;
                    }else
                        errorScore[0]++;
                    //3
                    if (image.getPixelBoundaryMode(xc - l, yc - d, band) > this.threshold) {
                        checkPixel(image, xc - l, yc - d, band, visitMap, connectivityScore, errorScore);
                        sum++;
                    }else
                        errorScore[0]++;
                    //4
                    if (image.getPixelBoundaryMode(xc + l, yc - d, band) > this.threshold) {
                        checkPixel(image, xc + l, yc - d, band, visitMap, connectivityScore, errorScore);
                        sum++;
                    }else
                        errorScore[0]++;

                }

                if (sum > 1) return;

            }

            dist += d + 1;
            d++;

        }

        return;
    }

    public Image applyFilter(final Image image) {
        Image out = super.applyFilter(image);
        out.stretchOrShrinkRange(0, 255);
        return out;
    }

}
