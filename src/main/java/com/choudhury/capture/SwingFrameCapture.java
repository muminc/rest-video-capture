package com.choudhury.capture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public class SwingFrameCapture implements Runnable{

    private AtomicBoolean captureVideo;
    private GraphicsDevice screen;
    private Lock videoCaptureLock;
    private Image cursorImg;
    private Collection<ImageTime> imagesQueue;


    public SwingFrameCapture(AtomicBoolean captureVideo, GraphicsDevice screen, Lock videoCaptureLock, Image cursorImg, Collection<ImageTime> imagesQueue) {
        this.captureVideo = captureVideo;
        this.screen = screen;
        this.videoCaptureLock = videoCaptureLock;
        this.cursorImg = cursorImg;
        this.imagesQueue = imagesQueue;
    }


    public void run() {
        try {
            DisplayMode mode = screen.getDisplayMode();
            int displayWidth = mode.getWidth();
            int displayHeight = mode.getHeight();
            //Rectangle bounds = new Rectangle(0, 0, displayWidth, displayHeight);
            videoCaptureLock.lock();
            long lastCaptureTime;
            do {
                BufferedImage screenCapture = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphics = screenCapture.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0,0, displayWidth, displayHeight);
                paintWindows(graphics, Frame.getFrames());
                lastCaptureTime = System.nanoTime();
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                Point location = pointerInfo.getLocation();

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

    private void paintWindows(final Graphics graphics,final Window[] windows) {
        Runnable runnable=new Runnable()
        {
            public void run()
            {
                for (Window window : windows) {
                    Point location = window.getLocation();
                    Graphics windowGraphics=graphics.create();
                    windowGraphics.translate(location.x,location.y);
                    window.paint(windowGraphics);
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
