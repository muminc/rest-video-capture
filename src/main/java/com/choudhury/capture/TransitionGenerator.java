package com.choudhury.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TransitionGenerator {

    private BufferedImage firstImage;
    private BufferedImage secondImage;

    public TransitionGenerator(BufferedImage firstImage, BufferedImage secondImage) {
        this.firstImage = firstImage;
        this.secondImage = secondImage;
    }

    public List<ImageTime> generateImages(int imageCount)
    {
        int width=firstImage.getWidth();
        int height=firstImage.getHeight();

        ArrayList<ImageTime> images=new ArrayList<ImageTime>();
        for (int i=0; i<imageCount; i++)
        {
            BufferedImage image = createImage(width, height);
            Graphics2D graphics = image.createGraphics();
            float percentage=i/(float)imageCount;
            int startX=(int)(width*percentage);
            graphics.drawImage(secondImage,0,0,null);
            graphics.drawImage(firstImage,startX,0,null);
            graphics.setColor(Color.BLACK);
            graphics.drawString("Muminxxxxxxxxxxxxxxxxxxxx  "+i,10,100);
            //long timeIndex=(long)(i*1000000000*(1/(float)framePS));
            images.add(new ImageTime(image,i));
        }
       return images;
    }

    public BufferedImage createImage(int w, int h)
    {
        return new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    }
}
