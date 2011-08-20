package com.choudhury.capture;

import java.awt.image.BufferedImage;

public class ImageTime {
    BufferedImage bufferedImage;
    long time;

    public ImageTime(BufferedImage bufferedImage, long time) {
        this.bufferedImage = bufferedImage;
        this.time = time;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public long getTime() {
        return time;
    }
}
