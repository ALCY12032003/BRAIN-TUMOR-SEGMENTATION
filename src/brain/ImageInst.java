/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
/**
 *
 * @author admin
 */
public class ImageInst 
{
    private BufferedImage image;
    private int width, height;
    
    int gray_image[][];
    
    ImageInst(BufferedImage img)
    {
        image=img;
        width  = image.getWidth();
        height = image.getHeight();

        gray_image = null;
    }
    
    public int[][] getGrayImage() 
    {
        if(gray_image != null) 
        {
            return gray_image;
        }

        gray_image = new int[height][width];

        
        for(int row = 0; row < height; ++row) 
        {
            for(int col = 0; col < width; ++col) 
            {
                int rgb = image.getRGB(col, row) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >>  8) & 0xFF;
                int b = (rgb        & 0xFF);
                gray_image[row][col] = (r + g + b) / 3;
            }
        }
        return gray_image;
    }



    public int getWidth() 
    {
        return width;
    }

    public int getHeight() 
    {
        return height;
    }

    public void display2D(int[][] img)
    {
        BufferedImage bufferedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int row = 0; row < height; ++row) 
        {
            for(int col = 0; col < width; ++col) 
            {
                int c = img[row][col] << 16 | img[row][col] << 8 | img[row][col];
                bufferedImg.setRGB(col, row, c);
            }
        }        
    }
}
