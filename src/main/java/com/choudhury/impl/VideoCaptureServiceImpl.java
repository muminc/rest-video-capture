package com.choudhury.impl;

import com.choudhury.capture.VideoCapture;
import com.choudhury.service.VideoCaptureService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Muminur Choudhury
 */
@Service("videoCaptureService")
public class VideoCaptureServiceImpl implements VideoCaptureService {

    protected static Logger logger = Logger.getLogger(VideoCaptureServiceImpl.class.getName());

    private Map<String, VideoCapture> map = new ConcurrentHashMap<String, VideoCapture>();

    public void stopRecording(String fileName, boolean passed) throws Exception {
        VideoCapture videoCapture = map.get(fileName);
        videoCapture.setPassed(passed);
        videoCapture.outputExit();
    }


    public void startRecording(String projectName, String title, String fileName) throws Exception {
        VideoCapture videoCapture = new VideoCapture(projectName, title, fileName);
        map.put(fileName, videoCapture);
        videoCapture.outputIntro();
        videoCapture.captureScreen();
    }

    public void deleteRecording(String fileName) throws Exception {
        VideoCapture videoCapture = map.remove(fileName);
        videoCapture.close();
    }
}


