package com.artbeatte.imagemanip;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * @author art.beatte
 * @version 3/31/16
 */
public class ImageManip {

    private static final String WORKING_FILE = Paths.get("").toAbsolutePath()
            .resolve("src/main/res/tardigrade.png").toString();
    private static final String PROCESSED_FILE = Paths.get("").toAbsolutePath()
            .resolve("processed.png").toString();

    private static final int RGB_PIXEL_OFFSET = 3;
    private static final double GROUP_VARIANCE = 0.05;

    private static final Comparator<Pixel> PIXEL_COMPARATOR = (p1, p2) -> {
        int xDif = p1.x - p2.x;
        int yDif = p1.y - p2.y;
        return xDif == 0 ? yDif : xDif;
    };

    private BufferedImage mImage;

    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[] { WORKING_FILE };
        }
        new ImageManip(args[0]);
    }

    private class Pixel {
        public int x, y, rgb;

        public Pixel(int x, int y, int rgb) {
            this.x = x;
            this.y = y;
            this.rgb = rgb;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pixel) {
                Pixel o = (Pixel) obj;
                return this.x == o.x &&
                        this.y == o.y &&
                        this.rgb == o.rgb;
            }
            return false;
        }
    }

    private class PixelGroup {
        public Set<Pixel> pixels;
        public List<Pixel> boundaryPixels;
        public int averageRGB;

        public PixelGroup() {
            this.pixels = new HashSet<>();
            this.boundaryPixels = new ArrayList<>();
            this.averageRGB = 0;
        }

        public void addPixel(Pixel p) {
            int numPixels = pixels.size();
            this.averageRGB = (averageRGB * numPixels + p.rgb) / (numPixels + 1);
            pixels.add(p);
            updateBoundaryPixels(p);
        }

        public boolean isPart(Pixel p) {
            int redDif = this.averageRGB - p.rgb;
            return redDif <= this.averageRGB * GROUP_VARIANCE && isContiguous(p);
        }

        private void updateBoundaryPixels(Pixel p) {
            int insertionIndex = -Collections.binarySearch(boundaryPixels, p, PIXEL_COMPARATOR) - 1;
            if (insertionIndex >= 0) {
                boundaryPixels.add(insertionIndex, p);
            }
            Iterator<Pixel> itr = boundaryPixels.iterator();
            while (itr.hasNext()) {
                Pixel bp = itr.next();
                if (isEnclosed(bp)) {
                    itr.remove();
                }
            }
        }

        private boolean isEnclosed(Pixel p) {
            Pixel testPixel = new Pixel(p.x - 1, p.y - 1, 0);
            boolean n0 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n1 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n2 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x -=3;
            testPixel.y +=1;
            boolean n3 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            testPixel.x +=1;
            boolean n5 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.y +=1;
            testPixel.x -=3;
            boolean n6 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n7 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n8 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            return n0 && n1 && n2 && n3 && n5 && n6 && n7 && n8;
        }

        private boolean isContiguous(Pixel p) {
            Pixel testPixel = new Pixel(p.x - 1, p.y - 1, 0);
            boolean n0 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n1 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n2 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x -=3;
            testPixel.y +=1;
            boolean n3 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            testPixel.x +=1;
            boolean n5 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.y +=1;
            testPixel.x -=3;
            boolean n6 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n7 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            testPixel.x +=1;
            boolean n8 = Collections.binarySearch(boundaryPixels, testPixel, PIXEL_COMPARATOR) >= 0;
            return n0 || n1 || n2 || n3 || n5 || n6 || n7 || n8;
        }
    }

    public ImageManip(String imageFilePath) {
        mImage = getImage(imageFilePath);

        if (mImage != null) {
            //blueShiftImage();
            findImageObjects(true);
            saveProcessedImg();
        }
    }

    private void findImageObjects(boolean showProgress) {
        Set<PixelGroup> pixelGroups = new HashSet<>();

        for (int i = 0; i < mImage.getWidth(); i++) {
            if (showProgress) {
                showProgress(i, mImage.getWidth());
            }
            for (int j = 0; j < mImage.getHeight(); j++) {
                Pixel p = new Pixel(i, j, mImage.getRGB(i, j));
                boolean added = false;
                for (PixelGroup pg : pixelGroups) {
                    if (pg.isPart(p)) {
                        pg.addPixel(p);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    PixelGroup pg = new PixelGroup();
                    pg.addPixel(p);
                    pixelGroups.add(pg);
                }
            }
        }

        for (PixelGroup pg : pixelGroups) {
            for (Pixel p : pg.pixels) {
                mImage.setRGB(p.x, p.y, pg.averageRGB);
            }
        }
    }

    private void showProgress(int position, int total) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i/25.0 <= ((double)position)/total) {
                sb.append("#");
            } else {
                sb.append(" ");
            }
        }
        sb.append(String.format(" (%d%%)", (int)((double)position)/total));
        System.out.println(sb.toString());
    }

    private void blueShiftImage() {
//        DataBuffer db = mImage.getData().getDataBuffer(); db.
        WritableRaster raster = mImage.getRaster();
        int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(),
                new int[raster.getWidth() * raster.getHeight() * RGB_PIXEL_OFFSET]);
        for (int i = 0; i < raster.getHeight() * RGB_PIXEL_OFFSET; i++) {
            for (int j = 0; j < raster.getWidth(); j+=RGB_PIXEL_OFFSET) {
                if (i % 2 == 0 && j % 2 == 0) {
                    pixels[raster.getWidth() * i + j] = Color.blue.getRed();
                    pixels[raster.getWidth() * i + j + 1] = Color.blue.getGreen();
                    pixels[raster.getWidth() * i + j + 2] = Color.blue.getBlue();
                }
            }
        }
        raster.setPixels(0, 0, raster.getWidth(), raster.getHeight(), pixels);
    }

    private void saveProcessedImg() {
        writeImage(mImage, PROCESSED_FILE);
    }

    private static BufferedImage getImage(String imageFilePath) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new BufferedInputStream(Files.newInputStream(Paths.get(imageFilePath))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    private static void writeImage(BufferedImage img, String imageFilePath) {
        try {
            ImageIO.write(img, "png", Files.newOutputStream(Paths.get(imageFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
