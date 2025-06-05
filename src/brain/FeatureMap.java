/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.Random;
import java.util.Vector;
/**
 *
 * @author admin
 */
public class FeatureMap 
{
    

    private int width,height,kernel_size;    
    private Double [][] featureMap;
    private Double [][] inputFeature;
    private Double [][] kernel;
    private Double [][] errors;

    private Double label;

    public FeatureMap(int input_size, int kernel_size,int outVol, boolean debugSwitch)
    {
    
        if(input_size <= 0 || kernel_size <=0 || outVol <= 0)
        {
            System.out.println("Inavlid parameter passes to FeatureMap Constructor");
        }
        else 
        {
            width = height = input_size;
            this.kernel_size = kernel_size;
            inputFeature = new Double[input_size][input_size];
            kernel = new Double[kernel_size][kernel_size];
            featureMap = new Double[outVol][outVol];
            errors = new Double[outVol][outVol];
        }

    }

    public Double activation( int row, int col)
    {
        Double val = 0.0;
        //System.out.println(row+" : "+col+" : "+kernel_size);
        for(int i =0; i < kernel_size-1; i++)
        {
            for(int j = 0; j < kernel_size-1; j++)
            {
                int temp1 = i+row;
                int temp2 = j+col;
                
                val += inputFeature[i+row] [j+col] *  kernel[i][j];
            }
        }
        return val;
    }

    public Double LeakyRELU(Double activn)
    {
    	return (activn <= 0 ? 0.1*activn : activn);
    }

    public Double RELU( Double activn)
    {
        return (activn <= 0 ? 0 : activn);
    }

    public void computeFeatureMap(int stride, int padding)
    {
        for(int i = 0; i< featureMap.length ; i++)
        {
            for(int j = 0; j < featureMap[0].length; j++)
            {
                featureMap[i][j] = RELU(activation (i,j) );
            }
        }

    }

    public void readFeatureVector( Vector<Double> featureVector)
    {
        for(int index = 0; index < featureVector.size() - 1 ; index++)
        {
            int xValue = index % width;
            int yValue = index / height;

            inputFeature[yValue][xValue] = featureVector.get(index);
        }

        label = featureVector.get(featureVector.size()-1);  
    }

    public void readFeatureVectorAugmented( Vector<Double> featureVector)
    {
        for(int index = 0; index < featureVector.size() - 1 ; index++)
        {

            int xValue = index % width;
            int yValue = index / height;

            inputFeature[yValue][xValue] = featureVector.get(index);
        }

        label = featureVector.get(featureVector.size()-1);

        dataAugmentation();
        
    }

    public void initKernel()
    {

        Random rand = new Random(3);

        for(int i = 0; i < kernel_size; i++)
        {
            for(int j = 0; j < kernel_size; j++ )
            {           
                kernel[i][j] = rand.nextDouble();
            }
        }
    }

    public Double [][] getErrors()
    {
        return errors;
    }

    public Double [][] getFeatureMap()
    {
        return featureMap;
    }

    public Double [][] getKernel()
    {
        return kernel;
    }

    public Double [][] getInputMap()
    {
        return inputFeature;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Double getLabel()
    {
        return label;
    }

    void dataAugmentation()
    {
        Random rand = new Random();

        int cropKind = rand.nextInt() % 6;

        if(cropKind == 0)
        {
            cropCenter( );
        }
        else if(cropKind ==1)
        {
            cropBottomRight();
        }
        else if(cropKind == 2)
        {
            cropBottomLeft();
        }
        else if (cropKind == 3)
        {
            cropTopRight();
        }
        else if (cropKind == 4)
        {
            cropTopLeft();
        }
        else
        {
            return;
        }
    }

    void cropCenter( )
    {
        int width = inputFeature.length;
        int length = inputFeature[0].length;

        int croppedPixels =  width/8 ;

        for(int i =  0; i < croppedPixels/2; i++)
        {
            for(int j =  0 ; j < length; j++)
            {
                inputFeature[i ][j] = 0.0;
            }
        }

        for(int i =  width - croppedPixels/2 ; i < width; i++)
        {
            for(int j =  0 ; j < length; j++)
            {
                inputFeature[i ][j] = 0.0;
            }
        }

        for(int i =  0 ; i < width; i++)
        {
            for(int j =  0 ; j < croppedPixels/2; j++)
            {
                inputFeature[i ][j] = 0.0;
            }
        }

        for(int i =  0 ; i < width; i++)
        {
            for(int j =  length- croppedPixels/2  ; j < length; j++)
            {
                inputFeature[i ][j] = 0.0;
            }
        }

    }
    void cropBottomLeft(  )
    {

        int width = inputFeature.length;
        int length = inputFeature[0].length;

        int croppedPixels =  width/8 ;

        for(int i =  croppedPixels; i < width; i++)
        {
            for(int j =  length-croppedPixels-1; j >= 0; j--)
            {
                inputFeature[i-croppedPixels ][j + croppedPixels] = inputFeature[i][j];
            }
        }

        for(int i =  0; i < width; i++)
        {
            for(int j = 0; j < croppedPixels; j++)
            {
                inputFeature[i ][j ] = 0.0;
            }
        }

        for(int i =  width - croppedPixels ; i < width; i++)
        {
            for(int j = 0; j < length; j++)
            {
                inputFeature[i ][j ] = 0.0;
            }
        }

    }

    void cropTopRight( )
    {

        int width = inputFeature.length;
        int length = inputFeature[0].length;

        int croppedPixels =  width/8 ;

        for(int i =  width-croppedPixels-1; i >= 0; i--)
        {
            for(int j = croppedPixels ; j < length; j ++)
            {
                inputFeature[i+croppedPixels ][j - croppedPixels] = inputFeature[i][j];
            }
        }

        for(int i =  0; i < croppedPixels; i++)
        {
            for(int j = 0; j < length; j++)
            {
                inputFeature[i ][j ] = 0.0;
            }
        }

        for(int i =  0 ; i < width; i++)
        {
            for(int j = length -croppedPixels; j < length; j++)
            {
                inputFeature[i ][j ] = 0.0;
            }
        }

    }

    void cropTopLeft(  )
    {

        int width = inputFeature.length;
        int length = inputFeature[0].length;

        int croppedPixels =  width/8 ;

        for(int i =  width-croppedPixels-1; i >= 0; i--)
        {
            for(int j =  length -croppedPixels-1 ; j >= 0; j--)
            {
                inputFeature[i+croppedPixels ][j + croppedPixels] = inputFeature[i][j];
            }
        }

        for(int i =  0; i < croppedPixels; i++)
        {

            for(int j = 0; j < length; j++)
            {

                inputFeature[i ][j ] = 0.0;
            }
        }

        for(int i =  0 ; i < width; i++)
        {

            for(int j = 0; j < croppedPixels; j++)
            {

                inputFeature[i ][j ] = 0.0;
            }
        }

    }

    void cropBottomRight(  )
    {

        int width = inputFeature.length;
        int length = inputFeature[0].length;

        int croppedPixels =  width/8 ;

        for(int i =  croppedPixels; i < width; i++)
        {

            for(int j =  croppedPixels; j < length; j++)
            {

                inputFeature[i-croppedPixels ][j - croppedPixels] = inputFeature[i][j];
            }
        }

        for(int i =  0; i < width; i++)
        {

            for(int j =  length-croppedPixels; j < length; j++)
            {

                inputFeature[i ][j ] = 0.0;
            }
        }

        for(int i =  width - croppedPixels ; i < width; i++)
        {

            for(int j = 0; j < length; j++)
            {

                inputFeature[i ][j ] = 0.0;
            }
        }

    }

}
