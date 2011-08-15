package com.choudhury.capture;


import ch.randelshofer.media.avi.AVIOutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class VideoCapture {

    private AVIOutputStream.VideoFormat format;
    private int depth;
    private float quality;
    private String projectName;
    private String testName;
    private String targetVideoPath;
    private File tempVideoPath;
    private GraphicsDevice[] screens;
    private AVIOutputStream aviOuput;
    private BufferedImage temporaryImage;
    private BufferedImage cursorImg;
    private boolean passed;
    private Lock videoLock;
    private boolean captureVideo = false;
    private boolean useFFMpeg=false;

    public static void main(String[] args) {
        try {
            VideoCapture videoCapture = new VideoCapture(AVIOutputStream.VideoFormat.JPG, 24, 0.9f, "My Project", "Super Duper Test", "c:\\temp\\outputMumin.mp4",false);
            videoCapture.outputIntro();
            videoCapture.captureScreen();
            Thread.sleep(10000);
            videoCapture.outputExit();
            videoCapture.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public VideoCapture(AVIOutputStream.VideoFormat format, int depth, float quality, String projectName, String testName, String targetVideoPath, boolean useFFMpeg) throws IOException {
        this.format = format;
        this.depth = depth;
        this.quality = quality;
        this.projectName = projectName;
        this.testName = testName;
        this.useFFMpeg = useFFMpeg;
        this.targetVideoPath = targetVideoPath.replaceAll("\\|","/");
        this.tempVideoPath = File.createTempFile("atvideo", ".avi");
        this.videoLock = new ReentrantLock();
        this.prepareVideo();
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }



    public void runFFMpeg() throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("cmd", "/C start /B  /WAIT ffmpeg -threads 2 -i " + tempVideoPath.getAbsolutePath() + " -vcodec libx264 -b 3000k -r 24 -y " + this.targetVideoPath);
        pb.start();
        Process start = pb.start();
        pipeOutput(start);
        int result = start.waitFor();
        System.out.println("Exit code " + result);
    }

    private static void pipeOutput(Process process) {
        pipe(process.getErrorStream(), System.err);
        pipe(process.getInputStream(), System.out);
    }

    private static void pipe(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    for (int n = 0; n != -1; n = src.read(buffer)) {
                        dest.write(buffer, 0, n);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void prepareVideo() throws IOException {
        GraphicsEnvironment graphenv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        screens = graphenv.getScreenDevices();
        GraphicsDevice firstScreen = screens[0];
        DisplayMode mode = firstScreen.getDisplayMode();
        aviOuput = null;

        aviOuput = new AVIOutputStream(tempVideoPath, format, depth);
        aviOuput.setVideoCompressionQuality(quality);

        aviOuput.setTimeScale(1);
        aviOuput.setFrameRate(12);

        temporaryImage = new BufferedImage(mode.getWidth(), mode.getHeight(), BufferedImage.TYPE_INT_RGB);
        cursorImg = ImageIO.read(this.getClass().getResourceAsStream("/images/cursor.png"));

    }

    public void close() throws Exception {
        try {
            videoLock.lock();
            if (aviOuput != null) {
                aviOuput.close();
            }
            if (useFFMpeg)
            {
                runFFMpeg();
                this.tempVideoPath.delete();
            }
            else
            {
                tempVideoPath.renameTo(new File(this.targetVideoPath));
            }
            aviOuput = null;

        } finally {
            videoLock.unlock();
        }
    }

    public void outputIntro() throws IOException {
        int width = temporaryImage.getWidth();
        int height = temporaryImage.getHeight();
        writeIntroScreen(aviOuput, temporaryImage, width, height);
    }


    public void outputExit() throws IOException {
        captureVideo = false;
        try {
            videoLock.lock();
            writeExitScreen();
        } finally {
            videoLock.unlock();
        }

    }


    private void writeExitScreen() throws IOException {
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
        int counter = 100;
        for (int i = 0; i < counter; i++) {
            Graphics2D graphics = paintWhiteBackground(temporaryImage, width, height);
            float opacity = Math.min((i / (float) counter), 1.0f);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            graphics.drawImage(image, (width - imageWith) / 2, (height - imageHeight) / 2, null);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Veranda", Font.BOLD, 18));
            String testStatus=passed ? "Test Passed" : "Test Failed";
            drawCenteredString(testName, testStatus, width, height, graphics, 10);
            aviOuput.writeFrame(temporaryImage);
        }
    }

    private void writeIntroScreen(AVIOutputStream out, BufferedImage img, int width, int height) throws IOException {
        int counter = 72;
        for (int i = 0; i < counter; i++) {
            float percent = Math.min((i / (float) counter), 1.0f);
            Graphics2D graphics = paintWhiteBackground(img, width, height);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
            drawCenteredString(this.projectName, this.testName, width, height, graphics, 10);
            out.writeFrame(img);
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
        captureVideo=true;
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    GraphicsDevice screen = screens[0];
                    DisplayMode mode = screen.getDisplayMode();
                    Rectangle bounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
                    videoLock.lock();
                    while (captureVideo) {
                        BufferedImage screenCapture = new Robot(screen).createScreenCapture(bounds);
                        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                        Point location = pointerInfo.getLocation();
                        Graphics2D graphics = screenCapture.createGraphics();
                        graphics.drawImage(cursorImg, location.x, location.y, null);
                        for (int i = 0; i < 12; i++) {
                            aviOuput.writeFrame(screenCapture);
                            aviOuput.writeFrame(screenCapture);
                            aviOuput.writeFrame(screenCapture);
                        }
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                } finally {
                    videoLock.unlock();
                }
            }
        };
        new Thread(runnable).start();
    }
}
