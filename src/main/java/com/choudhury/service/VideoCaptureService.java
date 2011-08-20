package com.choudhury.service;

import java.io.IOException;

/**
 * @author Muminur Choudhury
 */
public interface VideoCaptureService {

    public void stopRecording(String filename, boolean passed) throws Exception;

    public void startRecording(String projectName, String titleName,String fileName) throws Exception;

    public void deleteRecording(String filename) throws Exception;

}
