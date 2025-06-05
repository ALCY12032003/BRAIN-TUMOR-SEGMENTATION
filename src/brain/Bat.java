/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;
/**
 *
 * @author admin
 */
public class Bat 
{
    

    
    private ArrayList<FeatureMap> feature_maps;
    
    private int kernel_size;
    
    private int stride;
    
    private int padding;
    
    public int countFeatureMaps;
    
    private int input_size;
    
    public int outputVol;

    private Double label;


    public Bat()
    {   
        this.feature_maps = new ArrayList<FeatureMap>();
    }


    public Bat( Vector<Vector<Double>> inputFeatureVectors, int hyperparameters, boolean debugSwitch)
    {
        
        setHyperParameters( hyperparameters);

        this.feature_maps = new ArrayList<FeatureMap> ();

        input_size = (int ) Math.round( Math.sqrt(inputFeatureVectors.get(0).size()-1) );

        outputVol = outputVolume();
     
        for(int i =0; i< countFeatureMaps; i++)
        {
            FeatureMap featureMap = new FeatureMap(input_size, kernel_size,outputVol, debugSwitch);
            addFeatureMap(featureMap);
            featureMap.initKernel();
        }
    }


    public void setHyperParameters( int hyperParameter)
    {

        padding                 =  (hyperParameter >>28)  & (0xF);
        stride                  =  (hyperParameter >> 16) & (0xFF);
        kernel_size             =  (hyperParameter >>8)   & (0xFF);
        countFeatureMaps        =   hyperParameter        & (0xFF);

        if(kernel_size <=0 || countFeatureMaps <=0)
        {
            System.out.println("Inavlid parameter passes to Convolution layer constructor");
        }

    }

    public Bat( Fitness poolLayer, int hyperparameters, boolean debugSwitch)
    {
        setHyperParameters( hyperparameters);

        this.feature_maps = new ArrayList<FeatureMap> ();

        input_size = poolLayer.outputVolume();
        outputVol = outputVolume();
       
        for(int i =0; i< countFeatureMaps; i++)
        {
            FeatureMap featureMap = new FeatureMap(input_size, kernel_size,outputVol,debugSwitch);
            addFeatureMap(featureMap);
            featureMap.initKernel();
        }
    }



    public void calcFeatureMaps( )
    {

        for(FeatureMap feature_map: feature_maps)
            feature_map.computeFeatureMap(stride, padding);

    }

    public int countFeatureMaps()
    {
        return feature_maps.size();
    }

   
    public void addFeatureMap(FeatureMap featureMap)
    {
        feature_maps.add(featureMap);
    }

    public ArrayList<FeatureMap> get_fMaps()
    {
        return feature_maps;
    }

    public void readInputFeature(Vector <Double> featureVector)
    {
        for (FeatureMap feature_map: feature_maps)
        {
            feature_map.readFeatureVector(featureVector);
        }

        this.label = featureVector.get(featureVector.size()-1);

    }


    public void readInputFeatureAugmented(Vector <Double> featureVector)
    {

        for (FeatureMap feature_map: feature_maps)
        {
            feature_map.readFeatureVectorAugmented(featureVector);
        }

        this.label = featureVector.get(featureVector.size()-1);        
    }



    public void readInputFeature(ArrayList<PoolMap> pMaps)
    {

        for(int i = 0; i < pMaps.size(); i++)
        {
            PoolMap pMap = pMaps.get(i);

            FeatureMap fMap = feature_maps.get(i);
            Double [][] inp = fMap.getInputMap();

            Double [][] input  = pMap.getOutput();


            for( int j = 0; j< input.length; j++)
                System.arraycopy(input[j],0, inp[j] , 0, input[j].length);

        }

    }


   

    public void train(  Vector<Double> inputFeatureVector)
    {

        readInputFeature(inputFeatureVector);
            calcFeatureMaps();
    }


    public void tune(  Vector<Double> inputFeatureVector)
    {

        readInputFeature(inputFeatureVector); 
        calcFeatureMaps();
    }


    public void train( Fitness pool)
    {

        this.label = pool.getLabel();

        readInputFeature(pool.get_P_maps());
        calcFeatureMaps();


    }

    public int outputVolume()
    {      
     
        int outVolume = ((input_size - kernel_size + 2 * padding)/ stride ) + 1;
        
        return outVolume;
    }

    public Double getLabel()
    {

        return this.label;
    }

    public void backpropagate(Fitness pool)
    {
        Double learning_rate = 0.001;

        ArrayList<PoolMap> pmaps = pool.get_P_maps();

        for ( int i = 0; i < pmaps.size(); i ++)
        {

            PoolMap pm =pmaps.get(i);
            Double [][] err = pm.getErrors();

            FeatureMap fm = feature_maps.get(i);
            Double [][] error = fm.getErrors();
            Double[][] out = fm.getFeatureMap();
        
            for( int x = 0 ;x<error.length ; x++ )
                Arrays.fill(error[x], 0.0);

            for(int j = 0; j < err.length; j++)
            {
                for(int k = 0; k < err[0].length ; k++)
                {
                    int downsample_ratio = input_size / outputVol;

                    int ind1 = -1;
                    int ind2 = -1;
                    Double max_val = Double.MIN_VALUE;

                    for (int l = downsample_ratio * j; l < (downsample_ratio * (j + 1)); l++) 
                    {
                        for (int m = downsample_ratio * k; m < (downsample_ratio * (k + 1)); m++) 
                        {
                            if (out[l][m] > max_val) 
                            {
                                max_val = out[l][m];
                                ind1 = l;
                                ind2 = m;
                            }
                        }

                    }

                    if (ind1 == -1 || ind2 == -1) 
                    {
                        
                    }
                    else
                    {
                        error[ind1][ind2] = err[j][k];
                    }

                }
            }

        }

        for( int i = 0 ; i < feature_maps.size(); i++)
        {
            FeatureMap fm = feature_maps.get(i);

            Double [][] err = fm.getErrors();
            Double [][] out = fm.getFeatureMap();
            Double [][] ker = fm.getKernel();
            Double [][] inp = fm.getInputMap();

            for(int j=0; j < err.length; j++)
            {
                for(int k = 0; k < err[0].length; k++)
                {
                    if(err[j][k] != 0.0) 
                    {
                        for (int p = 0; p < kernel_size; p++) 
                        {
                            for (int q = 0; q < kernel_size; q++) 
                            {
                                ker[p][q] += err[j][k] * learning_rate * inp[p + j][q + k];
                            }
                        }
                    }
                }
            }
        }
    }

    public int getKernelSize()
    {
        return kernel_size;
    }

}
