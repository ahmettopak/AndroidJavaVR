package com.ahmet.androidjavavr;

/**
 * Author: Ahmet TOPAK
 * Since: 2/13/2024
 */
public class BarrelHelper {


    protected static int[] correction2(int[] pixels, int width, int height) {
        int[] pixelsCopy = pixels.clone();
        // parameters for correction
        double paramA = -0.007715; // affects only the outermost pixels of the image
        double paramB = 0.026731; // most cases only require b optimization
        double paramC = 0.0; // most uniform correction
        double paramD = 1.0 - paramA - paramB - paramC; // describes the linear scaling of the image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int d = Math.min(width, height) / 2;    // radius of the circle
                // center of dst image
                double centerX = (width - 1) / 4.0;
                double centerY = (height - 1) / 4.0;
                // cartesian coordinates of the destination point (relative to the centre of the image)
                double deltaX = (x - centerX) / d;
                double deltaY = (y - centerY) / d;
                // distance or radius of dst image
                double dstR = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                // distance or radius of src image (with formula)
                double srcR = (paramA * dstR * dstR * dstR + paramB * dstR * dstR + paramC * dstR + paramD) * dstR;
                // comparing old and new distance to get factor
                double factor = Math.abs(dstR / srcR);
                // coordinates in source image
                double srcXd = centerX + (deltaX * factor * d);
                double srcYd = centerY + (deltaY * factor * d);
                // no interpolation yet (just nearest point)
                int srcX = (int) srcXd;
                int srcY = (int) srcYd;
                if (srcX >= 0 && srcY >= 0 && srcX < width && srcY < height) {
                    int dstPos = y * width + x;
                    pixels[dstPos] = pixelsCopy[srcY * width + srcX];
                }
            }
        }
        return pixels;
    }
    protected int[] correction(int[] pixels, int width, int height) {
        //
        int[] pixelsCopy = pixels.clone();
        // parameters for correction
        double paramA = 0.0; // affects only the outermost pixels of the image
        double paramB = -0.02; // most cases only require b optimization
        double paramC = 0.0; // most uniform correction
        double paramD = 1.0 - paramA - paramB - paramC; // describes the linear scaling of the image
        //
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int dstX = x;
                int dstY = y;
                // center of dst image
                double centerX = (width - 1) / 2.0;
                double centerY = (height - 1) / 2.0;
                // difference between center and point
                double diffX = centerX - dstX;
                double diffY = centerY - dstY;
                // distance or radius of dst image
                double dstR = Math.sqrt(diffX * diffX + diffY * diffY);
                // distance or radius of src image (with formula)
                double srcR = (paramA * dstR * dstR * dstR + paramB * dstR * dstR + paramC * dstR + paramD) * dstR;
                // comparing old and new distance to get factor
                double factor = Math.abs(dstR / srcR);
                // coordinates in source image
                double srcXd = centerX + (diffX * factor);
                double srcYd = centerY + (diffX * factor);
                // no interpolation yet (just nearest point)
                int srcX = (int)srcXd;
                int srcY = (int)srcYd;
                if(srcX >= 0 && srcY >= 0 && srcX < width && srcY < height) {
                    int dstPos = dstY * width + dstX;
                    pixels[dstPos] = pixelsCopy[srcY * width + srcX];
                }
            }
        }
        return pixels;
    }
}
