/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
/**
 *
 * @author admin
 */
public class Bias1 
{
    Bias1()
    {
        
    }
    
    public BufferedImage getBias(BufferedImage bi)
    {
        BufferedImage bi1=null;
        try
        {
            double filter[][] ={{0,0,0}, {0,1,0}, {0,0,0} }; 
		
            double factor = 1.5; 
            double bias = 0.0; 
            int w=bi.getWidth();
            int h=bi.getHeight();
		
            bi1=new BufferedImage(w,h,1);
		
            int clr[][]=new int[w][h];
            int res[][]=new int[w][h];
            
            for(int x = 0; x < w; x++) 
		{
			for(int y = 0; y < h; y++) 
			{
				clr[x][y]=bi.getRGB(x,y);
			}
		}	
		
		
		for(int x = 0; x < w; x++) 
		{
			for(int y = 0; y < h; y++) 
			{ 
				double red = 0.0, green = 0.0, blue = 0.0; 
         
				//multiply every value of the filter with corresponding image pixel 
				for(int filterX = 0; filterX < 3; filterX++) 
				{
					for(int filterY = 0; filterY < 3; filterY++) 
					{ 
						int imageX = (x - 3 / 2 + filterX + w) % w; 
						int imageY = (y - 3 / 2 + filterY + h) % h;

						Color cc=new Color(clr[imageX][imageY]);
						red=red+cc.getRed()* filter[filterX][filterY]; 
						green=green+cc.getGreen()*filter[filterX][filterY]; 
						blue=blue+cc.getBlue()*filter[filterX][filterY]; 
						
						
					} 
				}
				int r1=Math.min(Math.max((int)(factor * red + bias), 0), 255); 
				int g1 = Math.min(Math.max((int)(factor * green + bias), 0), 255); 
				int b1 = Math.min(Math.max((int)(factor * blue + bias), 0), 255);
				Color c=new Color(r1,g1,b1);
				res[x][y]=c.getRGB();
				//truncate values smaller than zero and larger than 255 
				
			}    
		}

		for(int x = 0; x < w; x++) 
		{
			for(int y = 0; y < h; y++) 
			{
				bi1.setRGB(x,y,res[x][y]);
			}
		}
            
         /*  float data[] = { 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f,
            0.2f, 0.2f, 0.2f };
            
            //float data[] = { 0.0625f, 0.125f, 0.0625f, 0.125f, 0.25f, 0.125f,
        //0.0625f, 0.125f, 0.0625f };
            
            Kernel kernel = new Kernel(3, 3, data);
            ConvolveOp convolve = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            convolve.filter(bi, bi1);
               */   
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return bi1;
    }
    
    public BufferedImage normalize1(BufferedImage sourceImage)
    {
        int h=sourceImage.getHeight();
        int w=sourceImage.getWidth();
            
        //BufferedImage bi1=null;
        BufferedImage bi1=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
        try
        {
           // int[][] greyScale = RGBToGrey(sourceImage);
            //greyScale = normalizeMagic(greyScale,sourceImage.getWidth(),sourceImage.getHeight());
            //bi1 = makeNewBufferedImage(greyScale, sourceImage.getWidth(), sourceImage.getHeight());
            
            
            
            for(int i=0;i<w;i++)
            {
		for(int j=0;j<h;j++)
            	{
                    Color c1=new Color(sourceImage.getRGB(i,j));
                    int r=c1.getRed();
                    int b=c1.getBlue();
                    int g=c1.getGreen();
                    int y = (int)(0.2989 * r + 0.5870 * g + 0.1140 * b);
			
                    
                    Color gray = new Color(y, y, y);
                    
                    bi1.setRGB(i,j,gray.getRGB());
		}
            }
           
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return bi1;
    }
    
    public int[][] RGBToGrey(BufferedImage source)
    {
	int greyScale[][] = new int[source.getWidth()][source.getHeight()];
	for(int x=0; x<source.getWidth(); x++)
        {
            for(int y=0; y<source.getHeight(); y++)
            {
		int c = source.getRGB(x, y);
		float r = (c&0x00ff0000)>>16;
		float g = (c&0x0000ff00)>>8;
		float b = c&0x000000ff;
		greyScale[x][y] = (int)(0.3*r + 0.59*g + 0.11*b);
            }
	}
	return greyScale;  
    }
    public int findMinimum(int[][] input, int width, int height)
    {
	int min = input[0][0];
	for(int x=0; x<width; x++)
        {
            for(int y=0; y<height; y++)
            {
		int n = input[x][y];
		if(n<min)
                {
                    min = n;
		}
            }
	}
	return min;
    }

    public int findMaximum(int[][] input, int width, int height)
    {
	int max = input[0][0];
	for(int x=0; x<width; x++)
        {
            for(int y=0; y<height; y++)
            {
		int n = input[x][y];
		if(n>max)
                {
                    max = n;
		}
            }
	}
	return max;
    }
    public int[][] normalizeMagic(int[][] input, int width, int height){
	   int[][] output = new int[width][height];
	   int a=0, b=255;
	   int c = findMaximum(input,width,height)-findMinimum(input,width,height);
	   for(int x=0; x<width; x++){
		   for(int y=0; y<height; y++){
			   a = (input[x][y])-findMinimum(input,width,height);
			   int e =b/c;
			   output[x][y] = e*a;
		   }
	   }
	   return output;
}
        
        public BufferedImage makeNewBufferedImage(int[][] gs, int width, int height){
	   BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	   int[] iArray = {0,0,0,255};
	   WritableRaster r = image.getRaster();
	   for(int x=0; x<width; x++){
		   for(int y=0; y<height; y++){
			   int v = gs[x][y];
			   iArray[0] = v;
			   iArray[1] = v;
			   iArray[2] = v;
			   r.setPixel(x, y, iArray);
		   }
	   }
	   image.setData(r);
	   return image;
   }

        
}
