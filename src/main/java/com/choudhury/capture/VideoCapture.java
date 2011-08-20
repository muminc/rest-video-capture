package com.choudhury.capture;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class VideoCapture {
    private String projectName;
    private String testName;
    private String videoOutputFile;
    private GraphicsDevice[] screens;
    private BufferedImage temporaryImage;
    private BufferedImage cursorImg;
    private boolean passed;
    private Lock videoCaptureLock;
    private Lock videoDumperLock;
    private boolean captureVideo = false;
    private IMediaWriter writer = null;
    long startTime = -1;

    public static void main(String[] args) {
        try {
            VideoCapture videoCapture = new VideoCapture("My Project", "Super Duper Test", "c:\\temp\\testvideo.mp4");
            videoCapture.outputIntro();
            videoCapture.captureScreen();
            Thread.sleep(8000);
            videoCapture.setPassed(true);
            videoCapture.outputExit();
            videoCapture.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public VideoCapture(String projectName, String testName, String videoOutputFile) throws IOException {
        this.projectName = projectName;
        this.testName = testName;
        this.videoOutputFile = videoOutputFile.replaceAll("\\|", "/");
        this.videoCaptureLock = new ReentrantLock();
        this.videoDumperLock = new ReentrantLock();
        this.prepareVideo();
        startTime = System.nanoTime();
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    private void prepareVideo() throws IOException {
        GraphicsEnvironment graphenv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        screens = graphenv.getScreenDevices();
        GraphicsDevice firstScreen = screens[0];
        DisplayMode mode = firstScreen.getDisplayMode();

        Rectangle bounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
        IRational FRAME_RATE = IRational.make(12, 1);
        writer = ToolFactory.makeWriter(videoOutputFile);
        // We tell it we're going to add one video stream, with id 0,
        // at position 0, and that it will have a fixed frame rate of
        // FRAME_RATE.
        writer.addVideoStream(0, 0, FRAME_RATE, bounds.width, bounds.height);

        temporaryImage = new BufferedImage(mode.getWidth(), mode.getHeight(), BufferedImage.TYPE_INT_RGB);
        cursorImg = ImageIO.read(this.getClass().getResourceAsStream("/images/cursor.png"));

    }

    public void close() throws Exception {
        try {
            videoCaptureLock.lock();
            if (writer != null) {
                writer.close();
            }
            writer = null;
            System.out.println("Video Generated, file = " + videoOutputFile);

        } finally {
            videoCaptureLock.unlock();
        }
    }

    public void outputIntro() throws Exception {
        int width = temporaryImage.getWidth();
        int height = temporaryImage.getHeight();
        writeIntroScreen(writer, temporaryImage, width, height);
    }


    public void outputExit() throws Exception {
        captureVideo = false;
        try {
            videoCaptureLock.lock();
            videoDumperLock.lock();
            writeExitScreen(writer);
        } finally {
            videoCaptureLock.unlock();
            videoDumperLock.unlock();
        }

    }


    private void writeExitScreen(IMediaWriter out) throws Exception {
        int width = temporaryImage.getWidth();
        int height = temporaryImage.getHeight();
        Image image;
        if (passed) {
            image = ImageIO.read(this.getClass().getResourceAsStream("/images/greentick.png"));
        } else {
            image = ImageIO.read(this.getClass().getResourceAsStream("/images/redx.png"));
        }
        int imageWith = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        int counter = 30;
        for (int i = 0; i < counter; i++) {
            Graphics2D graphics = paintWhiteBackground(temporaryImage, width, height);
            float opacity = Math.min((i / (float) counter), 1.0f);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            graphics.drawImage(image, (width - imageWith) / 2, (height - imageHeight) / 2, null);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Veranda", Font.BOLD, 18));
            String testStatus = passed ? "Test Passed" : "Test Failed";
            drawCenteredString(testName, testStatus, width, height, graphics, 10);

            BufferedImage bufferedImage = convertToType(temporaryImage, BufferedImage.TYPE_3BYTE_BGR);
            out.encodeVideo(0, bufferedImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            Thread.sleep((int) ((1.0 / 12.0) * 1000.0));
        }
    }

    private void writeIntroScreen(IMediaWriter writer, BufferedImage img, int width, int height) throws Exception {
        int counter = 30;
        for (int i = 0; i < counter; i++) {
            float percent = Math.min((i / (float) counter), 1.0f);
            Graphics2D graphics = paintWhiteBackground(img, width, height);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
            drawCenteredString(this.projectName, this.testName, width, height, graphics, 10);
            BufferedImage bufferedImage = convertToType(temporaryImage, BufferedImage.TYPE_3BYTE_BGR);
            writer.encodeVideo(0, bufferedImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            Thread.sleep((int) ((1.0 / 12.0) * 1000.0));
        }
    }


    private Graphics2D paintWhiteBackground(BufferedImage img, int width, int height) {
        Graphics2D graphics = img.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        graphics.setColor(Color.WHITE);

        graphics.fillRect(0, 0, width, height);
        return graphics;
    }


    public void drawCenteredString(String title, String s, int w, int h, Graphics2D gIn, int newLineGap) {
        String[] msgs = s.split("\\n");
        Graphics g = gIn.create();
        g.setColor(Color.BLACK);
        {
            g.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(title)) / 2;
            int y = (((fm.getAscent() + newLineGap)) + (h - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(title, x, y);
        }
        g.setFont(new Font("Arial", Font.BOLD, 30));
        for (int i = 0; i < msgs.length; i++) {
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(msgs[i])) / 2;
            int y = (((fm.getAscent() + newLineGap) * (i + 2)) + (h - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(msgs[i], x, y);
        }
    }

    public void captureScreen() {
        captureVideo = true;
        final LinkedBlockingDeque<BufferedImage> imagesQueue = new LinkedBlockingDeque<BufferedImage>();

        Runnable captureRunnable = new Runnable() {
            public void run() {
                try {
                    GraphicsDevice screen = screens[0];
                    DisplayMode mode = screen.getDisplayMode();
                    Rectangle bounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
                    videoCaptureLock.lock();
                    do {
                        BufferedImage screenCapture = new Robot(screen).createScreenCapture(bounds);
                        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                        Point location = pointerInfo.getLocation();
                        Graphics2D graphics = screenCapture.createGraphics();
                        graphics.drawImage(cursorImg, location.x, location.y, null);
                        imagesQueue.add(screenCapture);
                        Thread.sleep(100);
                    } while (captureVideo);

                } catch (Exception e) {

                    e.printStackTrace();
                } finally {
                    videoCaptureLock.unlock();
                }
            }
        };


        Runnable consumerRunnable = new Runnable() {
            public void run() {
                try {
                    videoDumperLock.lock();
                    do {
                        try {
                            BufferedImage bufferedImage = imagesQueue.take();
                            for (int i = 0; i < 12; i++) {
                                BufferedImage tempImage = convertToType(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);
                                writer.encodeVideo(0, tempImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (captureVideo);
                    while (!imagesQueue.isEmpty()) {
                        try {
                            BufferedImage bufferedImage = imagesQueue.take();
                            for (int i = 0; i < 12; i++) {
                                BufferedImage tempImage = convertToType(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);
                                writer.encodeVideo(0, tempImage, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    videoDumperLock.unlock();
                }
            }
        };
        new Thread(captureRunnable).start();
        new Thread(consumerRunnable).start();
    }


    public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        BufferedImage image;
        if (sourceImage.getType() == targetType)
            image = sourceImage;
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }
}
