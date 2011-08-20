package com.choudhury.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TransitionGenerator {

    private BufferedImage firstImage;
    private BufferedImage secondImage;
    private TransitionDirection direction;
    private SlideType slideType;

    public TransitionGenerator(BufferedImage firstImage, BufferedImage secondImage, TransitionDirection direction, SlideType slideType) {
        this.firstImage = firstImage;
        this.secondImage = secondImage;
        this.direction = direction;
        this.slideType = slideType;
    }

    public List<ImageTime> generateImages(int imageCount) {
        int width = firstImage.getWidth();
        int height = firstImage.getHeight();
        final BufferedImage bottomImage, topImage;
        if (slideType == SlideType.SLIDE_OVER) {
            bottomImage = secondImage;
            topImage = firstImage;
        } else {
            bottomImage = firstImage;
            topImage = secondImage;
        }

        ArrayList<ImageTime> images = new ArrayList<ImageTime>();
        for (int i = 0; i < imageCount; i++) {
            BufferedImage image = createImage(width, height);
            Graphics2D graphics = image.createGraphics();
            float percentage = i / (float) imageCount;
            int startX;
            if (direction == TransitionDirection.FORWARD) {
                startX = (int) (width * percentage);
                graphics.drawImage(bottomImage, 0, 0, null);
                graphics.drawImage(topImage, startX, 0, null);
            }
            else
            {
                startX=(int)(width-(width*percentage));
                graphics.drawImage(topImage, 0, 0, null);
                graphics.drawImage(bottomImage, startX, 0, null);
            }


            graphics.setColor(Color.BLACK);
            images.add(new ImageTime(image, i));
        }
        return images;
    }

    public BufferedImage createImage(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    }
}
