/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

/**
 *
 * @author admin
 */
public class RegionGrowing extends ImageProcessingTask
{
  
    private PlanarImage input;
    private int width,height;
    private byte[][] pixels;
    private int[][] labels;
  
    private long position;
  
    private int numberOfRegions;
  
    private Map<Integer,Integer> count;
  
    public RegionGrowing(PlanarImage im,boolean preprocess)
    {
        if (preprocess) 
            input = preprocess(im); 
        else input = im;
            Raster inputRaster = input.getData();
    
        width = input.getWidth();
        height = input.getHeight();
        labels = new int[width][height];
        pixels = new byte[width][height];
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {
                pixels[w][h] = (byte)inputRaster.getSample(w,h,0);
                labels[w][h] = -1;
            }
        position = 0;
        count = new TreeMap<Integer, Integer>();
    }
  

    private PlanarImage preprocess(PlanarImage input)
    {
        if (input.getColorModel() instanceof IndexColorModel)
        {
      
            IndexColorModel icm = (IndexColorModel)input.getColorModel();
     
            int mapSize = icm.getMapSize();
     
            byte[][] lutData = new byte[3][mapSize];
     
            icm.getReds(lutData[0]);
            icm.getGreens(lutData[1]);
            icm.getBlues(lutData[2]);
     
            LookupTableJAI lut = new LookupTableJAI(lutData);
      
            input = JAI.create("lookup", input, lut);
        }
    
        if (input.getNumBands() > 1)
        {
            double[][] matrix = {{ 0.114, 0.587, 0.299, 0 }};
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(input);
            pb.add(matrix);
            input = JAI.create("bandcombine", pb, null);
        }
    
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(input);
        pb.add(null); 
        pb.add(1); pb.add(1);
        pb.add(new int[]{256}); 
        pb.add(new double[]{0});
        pb.add(new double[]{256});
    
        PlanarImage dummyImage = JAI.create("histogram", pb);
        Histogram h = (Histogram)dummyImage.getProperty("histogram");
        double[] thresholds = h.getMinFuzzinessThreshold(); 
    
        pb = new ParameterBlock();
        pb.addSource(input);
        pb.add(thresholds[0]);
        
        input = JAI.create("binarize", pb);
       	float[] kernelMatrix =  { 0, 0, 0, 0,1 , 1, 0, 1, 1};
							  
							  
    	KernelJAI kernel = new KernelJAI(3,3,kernelMatrix);
    
        ParameterBlock p = new ParameterBlock();
        p.addSource(input);
        p.add(kernel);
    
        input = JAI.create("dilate",p,null);
    
        p = new ParameterBlock();
        p.addSource(input);
        p.add(kernel);
        input = JAI.create("erode",p,null);
    
        p = new ParameterBlock();
        p.addSource(input);
        p.add(kernel);
        input = JAI.create("erode",p,null);
        p = new ParameterBlock();
        p.addSource(input);
        p.add(kernel);
        input = JAI.create("dilate",p,null);
    
        return input;
    }


    public void run()
    {
        numberOfRegions = 0;
        Stack<Point> mustDo = new Stack<Point>();
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {
                position++;
                if (labels[w][h] < 0)
                {
                    numberOfRegions++;
                    mustDo.add(new Point(w,h));
                    labels[w][h] = numberOfRegions;
                    count.put(numberOfRegions,1);
                }
      
                while(mustDo.size() > 0)
                {
                    Point thisPoint = mustDo.get(0); mustDo.remove(0);                    
                    for(int th=-1;th<=1;th++)
                        for(int tw=-1;tw<=1;tw++)
                        {
                            int rx = thisPoint.x+tw;
                            int ry = thisPoint.y+th;
                            
                            if ((rx < 0) || (ry < 0) || (ry>=height) || (rx>=width)) continue;
                            if (labels[rx][ry] < 0) 
                                if (pixels[rx][ry] == pixels[thisPoint.x][thisPoint.y])
                                { 
                                    mustDo.add(new Point(rx,ry));
                                    labels[rx][ry] = numberOfRegions;
                                    count.put(numberOfRegions, count.get(numberOfRegions)+1);
                                }
                        } 
                    } 
                } 
            position = width*height;
    }

    public int getNumberOfRegions()
    {
        return numberOfRegions;
    }
  
  
    public int getPixelCount(int region)
    {
        Integer c = count.get(region);
        if (c == null) return -1; else return c;
    }
  
 
    public long getSize()
    {
        return width*height;
    }

 
    public long getPosition()
    {
        return position;
    }

 
    public boolean isFinished()
    {
        return (position == width*height);
    }

 
    public PlanarImage getOutput()
    {
    
        int[] imageDataSingleArray = new int[width*height];
        int count=0;
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
                imageDataSingleArray[count++] = labels[w][h]; 
    
        DataBufferInt dbuffer = new DataBufferInt(imageDataSingleArray,width*height);
    
        SampleModel sampleModel =  RasterFactory.createBandedSampleModel(DataBuffer.TYPE_INT,width,height,1);
    
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
    
        Raster raster = RasterFactory.createWritableRaster(sampleModel,dbuffer,new Point(0,0));
    
        TiledImage tiledImage = new TiledImage(0,0,width,height,0,0,sampleModel,colorModel);
    
        tiledImage.setData(raster);
		
        return tiledImage;
    }

 
    public PlanarImage getInternalImage()
    {
        return input;
    }  
    
}
