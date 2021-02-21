package bsa.java.concurrency.hash;

import bsa.java.concurrency.exception.FileProcessingException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public abstract class Hasher {

    public static long calculateHash(byte[] image) {
        try {
            var bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
            return calculateDHash(bufferedImage);
        } catch (IOException e) {
            throw new FileProcessingException();
        }
    }

    private static long calculateDHash(BufferedImage image) {
        long hash = 0;

        var processedImage = getProcessedImage(image);

        for(int i = 1; i < 9; i++){
            for(int j = 1; j < 9; j++){
                int currBrightness = calculateBrightness(processedImage.getRGB(i, j));
                int prevBrightness = calculateBrightness(processedImage.getRGB(i - 1, j - 1));
                if(currBrightness > prevBrightness){
                    hash |= 1;
                }
                hash <<= 1;
            }
        }
        return hash;
    }

    private static BufferedImage getProcessedImage(BufferedImage image) {
        int width = 9, height = 9;
        var colorless = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        var scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        colorless.getGraphics().drawImage(scaled, 0, 0, null);

        return colorless;
    }

    private static int calculateBrightness(int rgb) {
        return rgb & 0xff;
    }

}
