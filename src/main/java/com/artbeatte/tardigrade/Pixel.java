package com.artbeatte.tardigrade;

/**
 * @author art.beatte
 * @version 4/1/16
 */
public class Pixel {
    public int x, y;
    public int[] pixelData;

    public Pixel(int x, int y, int[] pixelData) {
        this.x = x;
        this.y = y;
        this.pixelData = pixelData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pixel) {
            Pixel o = (Pixel) obj;
            return this.x == o.x &&
                    this.y == o.y &&
                    this.pixelData[0] == o.pixelData[0] &&
                    this.pixelData[1] == o.pixelData[1] &&
                    this.pixelData[2] == o.pixelData[2];
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return "{ x: " + this.x +
                ", y: " + this.y +
                ", red: " + this.pixelData[0] +
                ", green: " + this.pixelData[1] +
                ", blue: " + this.pixelData[2] +
                " }";
    }
}

