package com.choudhury.capture;

import javax.swing.*;
import java.awt.*;

public class TestSwingVideoCapture {
    public static void main(String[] args) {
            try {
               SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       launchSwingUI();
                   }
               });
               VideoCapture videoCapture = new VideoCapture("My Project", "Super Duper Test", "c:\\temp\\testvideo.mp4",VideoCaptureType.SWING,1000);
               videoCapture.outputIntro();
               videoCapture.captureScreen();
               Thread.sleep(8000);
               videoCapture.setPassed(true);
               videoCapture.outputExit();
               videoCapture.close();
               System.exit(0);
           } catch (Exception ex) {
               ex.printStackTrace();
           }
       }

       private static void launchSwingUI() {
           JFrame frame = new JFrame("FrameDemo");
           frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

           JLabel emptyLabel = new JLabel("Some conentent");
           frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
           frame.setLocation(30,200);
           frame.pack();
           frame.setVisible(true);

           JFrame frame2 = new JFrame("FrameDemo");
           frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

           JLabel emptyLabel2 = new JLabel("Another content");
           frame2.getContentPane().add(emptyLabel2, BorderLayout.CENTER);
           frame2.setLocation(50,210);
           frame2.pack();
           frame2.setVisible(true);
       }
}
