package com.artbeatte.imagemanip;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author art.beatte
 * @version 3/31/16
 */
public class ImageManip {

    private static final String WORKING_FILE = Paths.get("").toAbsolutePath().resolve("src/main/res/tardigrade.png").toString();
    private static final String PROCESSED_FILE = Paths.get("").toAbsolutePath().resolve("processed.png").toString();

    private BufferedImage mImage;

    public static void main(String[] args) {
        if (args.length < 1) {
            args = new String[] { WORKING_FILE };
        }
        new ImageManip(args[0]);

    }

    public ImageManip(String imageFilePath) {
        mImage = getImage(imageFilePath);

        if (mImage != null) {
            processImg();
            saveProcessedImg();
        }
    }

    private void processImg() {
        // TODO: implement!!!
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
