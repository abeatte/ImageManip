package com.artbeatte.tardigrade;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author art.beatte
 * @version 4/1/16
 */
public class PixelGroup {
    private double groupVariance;
    private Set<Pixel> pixels;
    private int[] averagePixelData;

    public PixelGroup(double groupVariance, int numDataElements) {
        this.groupVariance = groupVariance;
        this.pixels = new HashSet<>();
        this.averagePixelData = new int[numDataElements];
    }

    public void addPixel(Pixel p) {
        int numPixels = pixels.size();
        this.averagePixelData[0] = (this.averagePixelData[0] * numPixels + p.pixelData[0]) / (numPixels + 1);
        this.averagePixelData[1] = (this.averagePixelData[1] * numPixels + p.pixelData[1]) / (numPixels + 1);
        this.averagePixelData[2] = (this.averagePixelData[2] * numPixels + p.pixelData[2]) / (numPixels + 1);
        pixels.add(p);
    }

    public Set<Pixel> getPixels() {
        return Collections.unmodifiableSet(pixels);
    }

    public boolean isPart(Pixel p) {
        int redDif = Math.abs(this.averagePixelData[0] - p.pixelData[0]);
        int greenDif = Math.abs(this.averagePixelData[1] - p.pixelData[1]);
        int blueDif = Math.abs(this.averagePixelData[2] - p.pixelData[2]);
        return pixels.isEmpty() || (redDif <= this.averagePixelData[0] * groupVariance &&
                greenDif <= this.averagePixelData[1] * groupVariance &&
                blueDif <= this.averagePixelData[2] * groupVariance);
    }

    public int[] getColorAverage() {
        return averagePixelData;
    }

    @Override
    public String toString() {
        return "{ pixels: " + this.pixels.size() +
                ", averageRed: " + this.averagePixelData[0] +
                ", averageGreen: " + this.averagePixelData[1] +
                ", averageBlue: " + this.averagePixelData[2] +
                " }";
    }
}

