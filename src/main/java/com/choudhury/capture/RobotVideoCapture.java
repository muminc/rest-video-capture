package com.choudhury.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public class RobotVideoCapture implements Runnable {

    private AtomicBoolean captureVideo;
    private GraphicsDevice screen;
    private Lock videoCaptureLock;
    private Image cursorImg;
    private Collection<ImageTime> imagesQueue;


    public RobotVideoCapture(AtomicBoolean captureVideo, GraphicsDevice screen, Lock videoCaptureLock, Image cursorImg, Collection<ImageTime> imagesQueue) {
        this.captureVideo = captureVideo;
        this.screen = screen;
        this.videoCaptureLock = videoCaptureLock;
        this.cursorImg = cursorImg;
        this.imagesQueue = imagesQueue;
    }


    public void run() {
        try {
            DisplayMode mode = screen.getDisplayMode();
            Rectangle bounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
            videoCaptureLock.lock();
            long lastCaptureTime;
            do {
                BufferedImage screenCapture = new Robot(screen).createScreenCapture(bounds);
                lastCaptureTime = System.nanoTime();
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                Point location = pointerInfo.getLocation();
                Graphics2D graphics = screenCapture.createGraphics();
                if (cursorImg != null) {
                    graphics.drawImage(cursorImg, location.x, location.y, null);
                }
                imagesQueue.add(new ImageTime(screenCapture, lastCaptureTime));
                Thread.sleep(50);
            } while (captureVideo.get());
            System.out.println("Finished video capturing, waiting for video encoding to complete.");

        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            videoCaptureLock.unlock();
        }
    }


}
