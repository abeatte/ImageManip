package com.artbeatte.tardigrade;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author art.beatte
 * @version 3/31/16
 */
public class Tardigrade {

    private static final String WORKING_FILE = Paths.get("").toAbsolutePath()
            .resolve("src/main/res/tardigrade.png").toString();
    private static final String PROCESSED_FILE = Paths.get("").toAbsolutePath()
            .resolve("processed.png").toString();

    private static final double GROUP_VARIANCE = 0.1;

    private BufferedImage mImage;

    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[] { WORKING_FILE };
        }
        new Tardigrade(args[0]);
    }

    public Tardigrade(String imageFilePath) {
        mImage = getImage(imageFilePath);

        if (mImage != null) {
            findImageObjects(true);
            saveProcessedImg();
        }
    }

    private void findImageObjects(boolean showProgress) {
        Set<PixelGroup> pixelGroups = new HashSet<>();
        Set<Pixel> seen = new HashSet<>();
        Queue<Pixel> working = new LinkedList<>();

        WritableRaster raster = mImage.getRaster();

        int[] pixel = new int[raster.getNumDataElements()];
        raster.getPixel(0, 0, pixel);
        working.add(new Pixel(0, 0, pixel));
        while (!working.isEmpty()) {
            Pixel p = working.remove();
            if (!seen.contains(p)) {
                pixelGroups.add(getPixelGroup(p, raster, seen, working));
                if (showProgress) {
                    showProgress(seen.size(), raster.getWidth() * raster.getHeight());
                }
            }
        }

        for (PixelGroup pg : pixelGroups) {
            for (Pixel p : pg.getPixels()) {
                raster.setPixel(p.x, p.y, pg.getColorAverage());
            }
        }
    }

    private PixelGroup getPixelGroup(Pixel p, Raster raster, Set<Pixel> seen, Queue<Pixel> working) {
        PixelGroup pixelGroup = new PixelGroup(GROUP_VARIANCE, raster.getNumDataElements());
        Queue<Pixel> groupWorking = new LinkedList<>();

        groupWorking.add(p);
        while (!groupWorking.isEmpty()) {
            Pixel gp = groupWorking.remove();
            if (!seen.contains(gp)) {
                if (pixelGroup.isPart(gp)) {
                    seen.add(gp);
                    pixelGroup.addPixel(gp);
                    groupWorking.addAll(getNeighbors(gp, raster));
                } else {
                    working.add(gp);
                }
            }
        }
        return pixelGroup;
    }

    private Collection<Pixel> getNeighbors(Pixel p, Raster raster) {
        Collection<Pixel> collection = new HashSet<>();
        int x = p.x - 1;
        int y = p.y - 1;
        int[] pixel;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x-= 2;
        y++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        // skip self
        x+= 2;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x-= 2;
        y++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        x++;
        if (isInBounds(x, y)) {
            pixel = raster.getPixel(x, y, (int[])null);
            collection.add(new Pixel(x, y, pixel));
        }
        return collection;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < mImage.getWidth() && y < mImage.getHeight();
    }

    private void showProgress(int position, int total) {
        StringBuilder sb = new StringBuilder();
        System.out.print("\r");
        for (int i = 0; i < 50; i++) {
            if (i/50.0 <= ((double)position)/total) {
                sb.append("#");
            } else {
                sb.append(" ");
            }
        }
        sb.append(String.format(" (%d%%)", new Double(((double)position)/total*100).intValue()));
        System.out.print(sb.toString());
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
