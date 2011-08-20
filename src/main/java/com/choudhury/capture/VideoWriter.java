package com.choudhury.capture;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class VideoWriter {
    private IMediaWriter writer = null;
    private long startTime = -1;
    private long lastFrameTime = -1;
    private BufferedImage lastImage;

    public VideoWriter(String videoOutputFile, Rectangle bounds) {
        IRational FRAME_RATE = IRational.make(12, 1);
        writer = ToolFactory.makeWriter(videoOutputFile);
        startTime = System.nanoTime();
        lastFrameTime = startTime;
        writer.addVideoStream(0, 0, FRAME_RATE, bounds.width, bounds.height);
    }

    public void encodeVideo(BufferedImage image, long nanoTime) {
        lastFrameTime = nanoTime;
        lastImage = image;
        writer.encodeVideo(0, image, nanoTime, TimeUnit.NANOSECONDS);
    }


    public long getStartTime() {
        return startTime;
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public BufferedImage getLastImage() {
        return lastImage;
    }

    public void close() {
        writer.close();
    }
}
