package com.choudhury.capture;

public class TestRobotVideoCapture {
      public static void main(String[] args) {
        try {
            VideoCapture videoCapture = new VideoCapture("My Project", "Super Duper Test", "c:\\temp\\testvideo.mp4",VideoCaptureType.ROBOT,1000);
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
}
