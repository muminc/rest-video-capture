package com.choudhury.controller;


import org.junit.Assert;
import org.junit.Test;


/**
 * @author Muminur Choudhury
 */
public class TestRestService extends BaseWebApplicationContextTests {

    private String videoFilename="c:|temp|testing.avi";

    @Test
    public void createRecording() throws Exception {
        request.setMethod("POST");
        request.addHeader("Accept", "application/xml");
        request.setContentType("application/xml");
        request.setRequestURI("/videocapture/record/Our Project/Add Remove Products/"+videoFilename);
        request.addHeader("Content-Type", "application/xml");
        servlet.service(request, response);
        String result = response.getContentAsString();
        int status = response.getStatus();
        Assert.assertEquals(201, status);
    }

    @Test
    public void stopRecording() throws Exception {
        request.setMethod("PUT");
        request.addHeader("Accept", "application/xml");
        request.setContentType("application/xml");
        request.setRequestURI("/videocapture/stop/pass/"+videoFilename);
        request.addHeader("Content-Type", "application/xml");
        servlet.service(request, response);
        String result = response.getContentAsString();
        int status = response.getStatus();
        Assert.assertEquals(200, status);
        Assert.assertEquals("", result);
    }

    @Test
    public void deleteRecording() throws Exception {
        request.setMethod("DELETE");
        request.addHeader("Accept", "application/xml");
        request.setContentType("application/xml");
        request.setRequestURI("/videocapture/delete/"+videoFilename);
        request.addHeader("Content-Type", "application/xml");
        servlet.service(request, response);
        String result = response.getContentAsString();
        int status = response.getStatus();
        Assert.assertEquals(204, status);
        Assert.assertEquals("", result);
    }



}
