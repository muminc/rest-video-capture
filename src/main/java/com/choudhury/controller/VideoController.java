package com.choudhury.controller;

import com.choudhury.service.VideoCaptureService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author Muminur Choudhury
 */
@Controller
@Resource(name="restController")
@RequestMapping("/videocapture")
public class VideoController {


    @Resource(name="videoCaptureService")
	private VideoCaptureService videoCaptureService;



    @RequestMapping(value = "/record/{projectname}/{title}/{filename}.{extension}",
								method = RequestMethod.POST )
    @ResponseStatus(HttpStatus.CREATED)
    public void startRecording(@PathVariable("projectname") String projectname, @PathVariable("title") String title, @PathVariable("filename") String filename,@PathVariable("extension") String extension) {
        try {
            videoCaptureService.startRecording(projectname,title, filename+"."+extension);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/stop/{status}/{filename}.{extension}",
								method = RequestMethod.PUT )
    @ResponseStatus(HttpStatus.OK)
    public void stopRecording(@PathVariable("status") String status, @PathVariable("filename") String filename, @PathVariable("extension") String extension ) {
        try {
            videoCaptureService.stopRecording(filename+"."+extension,status.equalsIgnoreCase("pass"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/delete/{filename}.{extension}",
								method = RequestMethod.DELETE )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecording(@PathVariable("filename") String filename, @PathVariable("extension") String extension ) {
        try {
            videoCaptureService.deleteRecording(filename+"."+extension);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
