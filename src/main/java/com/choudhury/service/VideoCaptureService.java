package com.choudhury.service;

import java.io.IOException;

/**
 * @author Muminur Choudhury
 */
public interface VideoCaptureService {

    public void stopRecording(String filename, boolean passed) throws IOException;

    public void startRecording(String projectName, String titleName,String fileName) throws IOException;

    public void deleteRecording(String filename) throws Exception;

}
