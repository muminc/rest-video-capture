package com.choudhury.client;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VideoRestClient {

    public static void main(String[] args)
    {
        try
        {
            RestTemplate restTemplate=new RestTemplate();
            MarshallingHttpMessageConverter httpMessageConverter=new MarshallingHttpMessageConverter();
            XStreamMarshaller marshaller=new XStreamMarshaller();
            httpMessageConverter.setMarshaller(marshaller);
            httpMessageConverter.setUnmarshaller(marshaller);
            List<HttpMessageConverter<?>> messageConvertors=new ArrayList<HttpMessageConverter<?>>();
            messageConvertors.add(httpMessageConverter);
            restTemplate.setMessageConverters(messageConvertors);


            String hostnamePost="localhost:8080";
            String fileName="c:|temp||outputvideo.mp4";

            URI recordURL = new URI(
                    "http",
                    hostnamePost,
                    "/rest/videocapture/record/My Project/My Test/"+fileName,
                    "",
                    null);

            String result = restTemplate.postForObject(recordURL, null, String.class);
            System.out.println("result = " + result);

            Thread.sleep(4000);

             URI stopURI = new URI(
                    "http",
                    hostnamePost,
                    "/rest/videocapture/stop/failed/"+fileName,
                    "",
                    null);

            restTemplate.put(stopURI,null);
            System.out.println("done put ....");

             URI deleteURI = new URI(
                    "http",
                    hostnamePost,
                    "/rest/videocapture/delete/"+fileName,
                    "",
                    null);

            restTemplate.delete(deleteURI);
            System.out.println("done delete ....");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
