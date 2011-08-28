package com.choudhury.capture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Please make sure you have xuggle installed
 */
public class VideoCapture {
    public static final int NANO_IN_SECOND = 1000000000;
    public static final int FPS = 12;
    private String projectName;
    private String testName;
    private String videoOutputFile;
    private GraphicsDevice[] screens;
    private BufferedImage temporaryImage;
    private BufferedImage cursorImg;
    private boolean passed;
    private Lock videoCaptureLock;
    private Lock videoDumperLock;
    private AtomicBoolean captureVideo = new AtomicBoolean(false);
    private VideoWriter writer;
    private int maxFrameCapacity=Integer.MAX_VALUE;
    private VideoCaptureType videoCaptureType;

    private static Logger log=Logger.getLogger(VideoCapture.class.getName());


    //Before running this program, make sure you have xuggle installed.


    public VideoCapture(String projectName, String testName, String videoOutputFile, VideoCaptureType videoCaptureType) throws IOException
    {
        this(projectName,testName,videoOutputFile,videoCaptureType,Integer.MAX_VALUE);
    }

    public VideoCapture(String projectName, String testName, String videoOutputFile, VideoCaptureType videoCaptureType,int maxFrameCapacity) throws IOException {
        this.projectName = projectName;
        this.testName = testName;
        this.videoCaptureType = videoCaptureType;
        this.maxFrameCapacity = maxFrameCapacity;
        this.videoOutputFile = videoOutputFile.replaceAll("\\|", "/");
        this.videoCaptureLock = new ReentrantLock();
        this.videoDumperLock = new ReentrantLock();
        this.prepareVideo();
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
        writer = new VideoWriter(videoOutputFile, bounds);
        temporaryImage = new BufferedImage(mode.getWidth(), mode.getHeight(), BufferedImage.TYPE_INT_RGB);
        try{
            cursorImg = ImageIO.read(this.getClass().getResourceAsStream("/images/cursor.png"));
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"Unable to to load mouse cursor png",e);
        }

    }

    public void close() throws Exception {
        try {
            videoCaptureLock.lock();
            videoDumperLock.lock();
            if (writer != null) {
                writer.close();
            }
            writer = null;
            System.out.println("Video Generated, file = " + videoOutputFile);

        } finally {
            videoCaptureLock.unlock();
            videoDumperLock.unlock();
        }
    }

    public void outputIntro() throws Exception {
        int width = temporaryImage.getWidth();
        int height = temporaryImage.getHeight();
        writeIntroScreen(writer, temporaryImage, width, height);
    }


    public void outputExit() throws Exception {
        captureVideo.set(false);
        try {
            videoCaptureLock.lock();
            videoDumperLock.lock();
            writeExitScreen(writer);
        } finally {
            videoCaptureLock.unlock();
            videoDumperLock.unlock();
        }
    }


    private void writeExitScreen(VideoWriter writer) throws Exception {
        int width = temporaryImage.getWidth();
        int height = temporaryImage.getHeight();
        Image image;
        try
        {
        if (passed) {
            image = ImageIO.read(this.getClass().getResourceAsStream("/images/greentick.png"));
        } else {
            image = ImageIO.read(this.getClass().getResourceAsStream("/images/redx.png"));
        }
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"Unable to load status image",e);
            return;
        }
        int imageWith = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        boolean transitionFrameOutputted=false;
        int counter = 24;
        for (int i = 0; i < counter; i++) {
            Graphics2D graphics = paintWhiteBackground(temporaryImage, width, height);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width, height);
            float percent = Math.min(0.3f+(i * 0.1f), 1.0f);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
            graphics.drawImage(image, (width - imageWith) / 2, (height - imageHeight) / 2, null);
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Veranda", Font.BOLD, 18));
            String testStatus = passed ? "Test Passed" : "Test Failed";
            drawCenteredString(testName, testStatus, 1.0f, width, height, graphics, 10);
            BufferedImage bufferedImage = convertToType(temporaryImage, BufferedImage.TYPE_3BYTE_BGR);
            if (!transitionFrameOutputted)
            {
                generateTransition(bufferedImage,TransitionDirection.BACKWARD,SlideType.SLIDE_OVER);
                transitionFrameOutputted=true;
            }

            long time = writer.getLastFrameTime() + (NANO_IN_SECOND / FPS);
            writer.encodeVideo(bufferedImage, time);
        }
    }

    private void writeIntroScreen(VideoWriter writer, BufferedImage img, int width, int height) throws Exception {
        int counter = 30;
        for (int i = 0; i < counter; i++) {
            float percent = Math.min((i / (float) counter), 1.0f);
            Graphics2D graphics = paintWhiteBackground(img, width, height);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(0, 0, width, height);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
            float textPercentage = Math.min(i * 0.1f, 1.0f);
            drawCenteredString(this.projectName, this.testName, textPercentage, width, height, graphics, 10);
            BufferedImage bufferedImage = convertToType(temporaryImage, BufferedImage.TYPE_3BYTE_BGR);
            long time = writer.getLastFrameTime() + (NANO_IN_SECOND / FPS);

            writer.encodeVideo(bufferedImage, time);
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


    public void drawCenteredString(String title, String s, float percent, int w, int h, Graphics2D gIn, int newLineGap) {
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
            int centeredY = (((fm.getAscent() + newLineGap) * (i + 2)) + (h - (fm.getAscent() + fm.getDescent())) / 2);
            int animatedY = (int) (h - (h - (centeredY)) * percent);
            g.drawString(msgs[i], x, animatedY);
        }
    }

    public void captureScreen() {
        System.out.println("Starting video capture");
        captureVideo.set(true);
        final LinkedBlockingDeque<ImageTime> imagesQueue = new LinkedBlockingDeque<ImageTime>(maxFrameCapacity);

        Runnable videoCaptureRunnable;
        switch (videoCaptureType)
        {
            case ROBOT:
                videoCaptureRunnable=new RobotVideoCapture(captureVideo,screens[0],videoCaptureLock,cursorImg,imagesQueue);
                break;
            case SWING:
                videoCaptureRunnable=new SwingFrameCapture(captureVideo,screens[0],videoCaptureLock,cursorImg,imagesQueue);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported video capture type "+videoCaptureType.name());
        }

        Runnable consumerRunnable = new Runnable() {
            public void run() {
                try {
                    videoDumperLock.lock();
                    ImageTime take = imagesQueue.take();
                    generateTransition(take.getBufferedImage(),TransitionDirection.FORWARD,SlideType.SLIDE_OVER);
                    while (!imagesQueue.isEmpty() || captureVideo.get()) {
                        processImage(imagesQueue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    videoDumperLock.unlock();
                }
                System.out.println("Capture processing complete");
            }
        };
        new Thread(videoCaptureRunnable).start();
        new Thread(consumerRunnable).start();
    }

    private void generateTransition(BufferedImage secondImage,TransitionDirection direction, SlideType slideType) {
        TransitionGenerator generator = new TransitionGenerator(writer.getLastImage(), secondImage,direction,slideType);
        List<ImageTime> imageTimes = generator.generateImages(24);
        for (ImageTime imageTime : imageTimes) {
            BufferedImage tempImage = convertToType(imageTime.getBufferedImage(), BufferedImage.TYPE_3BYTE_BGR);
            long time = writer.getLastFrameTime() + (NANO_IN_SECOND / FPS);
            writer.encodeVideo(tempImage, time);
        }
    }

    private void processImage(LinkedBlockingDeque<ImageTime> imagesQueue) {
        try {
            ImageTime take = imagesQueue.poll(5, TimeUnit.SECONDS);
            if (take != null) {
                BufferedImage bufferedImage = take.getBufferedImage();
                BufferedImage tempImage = convertToType(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);
                writer.encodeVideo(tempImage, take.getTime());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        BufferedImage image;
        if (sourceImage.getType() == targetType)
            image = sourceImage;
        else {
            image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }
}
