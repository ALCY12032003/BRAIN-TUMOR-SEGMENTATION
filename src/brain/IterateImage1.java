/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;

import com.sun.media.jai.codecimpl.util.RasterFactory;
import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Random;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;

/**
 *
 * @author admin
 */
public class IterateImage1 extends ImageProcessingTask
{
 
    private PlanarImage pInput;
  
    private int width,height,numBands;
  
    private int maxIterations,numClusters;
  
    private float fuzziness; // "m"
    private float[][][] membership;
  
    private int iteration;
  
    private double j = Float.MAX_VALUE;
    private double epsilon;
  
    private boolean hasFinished = false;
    private long position;
  
    private float[][] clusterCenters;
  
    private int[] inputData;
    private float[] aPixel;
  
    private short[][] outputData;
  
 
    public IterateImage1(PlanarImage pInput,int numClusters,int maxIterations, float fuzziness,double epsilon)
    {
        this.pInput = pInput;
    
        width = pInput.getWidth();
        height = pInput.getHeight();
        numBands = pInput.getSampleModel().getNumBands();
    
        this.numClusters = numClusters;
        this.maxIterations = maxIterations;
        this.fuzziness = fuzziness;
        this.epsilon = epsilon;
        iteration = 0;
        
        clusterCenters = new float[numClusters][numBands];
        membership = new float[width][height][numClusters];
        
        Raster raster = pInput.getData();
        
        inputData = new int[width*height*numBands];
        aPixel = new float[numBands];
        
        outputData = new short[width][height];
        raster.getPixels(0,0,width,height,inputData);
        
        Random generator = new Random(); 
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {                
                float sum = 0f;
                for(int c=0;c<numClusters;c++)
                {
                    membership[w][h][c] = 0.01f+generator.nextFloat();
                    sum += membership[w][h][c];
                }
       
                for(int c=0;c<numClusters;c++) membership[w][h][c] /= sum;
            }
    
        position = 0;
    }

 
    public void run()
    {
	try
	{
            double lastJ;
    
            lastJ = calculateObjectiveFunction();
    
            for(iteration=0;iteration<maxIterations;iteration++)
            {    
                calculateClusterCentersFromMFs();
    
                calculateMFsFromClusterCenters();
    
                j = calculateObjectiveFunction();
                if (Math.abs(lastJ-j) < epsilon) break;
                lastJ = j;
		Thread.currentThread().sleep(3000);
            } 
            hasFinished = true;
    
            position = getSize();   
	}
	catch(Exception e)
	{
            e.printStackTrace();
	}
    }

 
    private void calculateClusterCentersFromMFs()
    {
        float top,bottom;
    
        for(int b=0;b<numBands;b++)
            for(int c=0;c<numClusters;c++)
            {
                top = bottom = 0;
                for(int h=0;h<height;h++)
                    for(int w=0;w<width;w++)
                    {   
                        int index = (h*width+w)*numBands;
                        top += Math.pow(membership[w][h][c],fuzziness)*inputData[index+b];
                        bottom += Math.pow(membership[w][h][c],fuzziness);
                    }
        
                clusterCenters[c][b] = top/bottom;
        
                position += width*height;
            }
    }

 
    private void calculateMFsFromClusterCenters()
    {
        float sumTerms;
    
        for(int c=0;c<numClusters;c++)
            for(int h=0;h<height;h++)
                for(int w=0;w<width;w++)
                {    
                    int index = (h*width+w)*numBands;
                    for(int b=0;b<numBands;b++)
                        aPixel[b] = inputData[index+b];
          
                    float top = calcDistance(aPixel,clusterCenters[c]);
          
                    sumTerms = 0f;
                    for(int ck=0;ck<numClusters;ck++)
                    {
                        float thisDistance = calcDistance(aPixel,clusterCenters[ck]);
                        sumTerms += Math.pow(top/thisDistance,(2f/(fuzziness-1f)));
                    }
          
                    membership[w][h][c] =    (float)(1f/sumTerms);          
                    position += (numBands+numClusters);
                }
    }
  
 
    private double calculateObjectiveFunction()
    {
        double j = 0;
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
                for(int c=0;c<numClusters;c++)
                {
                    int index = (h*width+w)*numBands;
                    for(int b=0;b<numBands;b++)
                        aPixel[b] = inputData[index+b];
          
                    float distancePixelToCluster = calcDistance(aPixel,clusterCenters[c]);
                    j += distancePixelToCluster*Math.pow(membership[w][h][c],fuzziness);        
                    position += (2*numBands);
                }
        return j;
    }
  
 
    private float calcDistance(float[] a1,float[] a2)
    {
        float distance = 0f;
        for(int e=0;e<a1.length;e++) distance += (a1[e]-a2[e])*(a1[e]-a2[e]);
            return (float)Math.sqrt(distance);
    }

 
    public long getSize()
    {
    
        return (long)maxIterations* 
        (
            (numClusters*width*height*(2*numBands))+ 
            (width*height*numBands*numClusters)+ 
            (numClusters*width*height*(numBands+numClusters))+ 
            (numClusters*width*height*(2*numBands))  
        );
    }
  
 
    public long getPosition()
    {
        return position;
    }

    public boolean isFinished()
    {
        return (position == getSize());
    }
 
 
    public TiledImage getRankedImage(int rank)
    {
    
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_INT,width,height,numBands);
    
        WritableRaster raster =  RasterFactory.createWritableRaster(sampleModel,new Point(0,0));
    
        int[] pixelArray = new int[numBands];
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {
                int aCluster = getRankedIndex(membership[w][h],rank);
        
                for(int band=0;band<numBands;band++) pixelArray[band] = (int)clusterCenters[aCluster][band];        
                    raster.setPixel(w,h,pixelArray);
            }
    
        TiledImage pOutput = new TiledImage(pInput,false);
        pOutput.setData(raster);
        return pOutput;
    }


    public TiledImage getRankedMFImage(int rank)
    {
    
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width,height,1);
    
        ColorModel colorModel = PlanarImage.createColorModel(sampleModel);
    
        WritableRaster raster = RasterFactory.createWritableRaster(sampleModel,new Point(0,0));
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {      
                int aCluster = (int)(255*getRankedMF(membership[w][h],rank));
                raster.setPixel(w,h,new int[]{aCluster});
            }
    
        TiledImage pOutput = new TiledImage(0,0,width,height,0,0,sampleModel,colorModel);
        pOutput.setData(raster);
        return pOutput;
    }

    private int getRankedIndex(float[] data,int rank)
    {
    
        int[] indexes = new int[data.length];
        float[] tempData = new float[data.length];
    
        for(int i=0;i<indexes.length;i++)
        {
            indexes[i] = i;
            tempData[i] = data[i];
        }
    
        for(int i=0;i<indexes.length-1;i++)
            for(int j=i;j<indexes.length;j++)
            {
                if (tempData[i] < tempData[j])
                {
                    int tempI= indexes[i];
                    indexes[i] = indexes[j];
                    indexes[j] = tempI;
                    float tempD = tempData[i];
                    tempData[i] = tempData[j];
                    tempData[j] = tempD;
                }
            }
    
        return indexes[rank];
    }


    private float getRankedMF(float[] data,int rank)
    {
    
        int[] indexes = new int[data.length];
        float[] tempData = new float[data.length];
    
        for(int i=0;i<indexes.length;i++)
        { 
            indexes[i] = i;
            tempData[i] = data[i];
        }
    
        for(int i=0;i<indexes.length-1;i++)
            for(int j=i;j<indexes.length;j++)
            {
                if (tempData[i] < tempData[j])
                {
                    int tempI= indexes[i];
                    indexes[i] = indexes[j];
                    indexes[j] = tempI;
                    float tempD = tempData[i];
                    tempData[i] = tempData[j];
                    tempData[j] = tempD;
                }
            }
    
        return tempData[rank];
    }

 
    public double getPartitionCoefficient()
    {
        double pc = 0;
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
                for(int c=0;c<numClusters;c++)
                    pc += membership[w][h][c]*membership[w][h][c];
                    pc = pc/(height*width);
        return pc;    
    }
  
 
    public double getPartitionEntropy()
    {
        double pe = 0;
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
                for(int c=0;c<numClusters;c++)
                    pe += membership[w][h][c]*Math.log(membership[w][h][c]);
                    pe = -pe/(height*width);
        return pe;    
    }

    public double getCompactnessAndSeparation()
    {
        double cs = 0;
    
        for(int h=0;h<height;h++)
            for(int w=0;w<width;w++)
            {
                int index = (h*width+w)*numBands;
                for(int b=0;b<numBands;b++)
                    aPixel[b] = inputData[index+b];
                for(int c=0;c<numClusters;c++)
                {
                    float distancePixelToCluster = calcSquaredDistance(aPixel,clusterCenters[c]);
                    cs += membership[w][h][c]*membership[w][h][c]*
                    distancePixelToCluster*distancePixelToCluster;
                }       
            }
        cs /= (height*width);
    
        float minDist = Float.MAX_VALUE;
        for(int c1=0;c1<numClusters-1;c1++)
            for(int c2=c1+1;c2<numClusters;c2++)
            {
                float distance = calcSquaredDistance(clusterCenters[c1],clusterCenters[c2]);
                minDist = Math.min(minDist,distance);
            }
        cs = cs/(minDist*minDist);
        return cs;    
    }
   

    private float calcSquaredDistance(float[] a1,float[] a2)
    {
        float distance = 0f;
        for(int e=0;e<a1.length;e++) distance += (a1[e]-a2[e])*(a1[e]-a2[e]);
        return (float)distance;
    }
       
}
