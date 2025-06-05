/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
/**
 *
 * @author admin
 */
public class Fitness
{
    
	
    private ArrayList<PoolMap> poolMaps;
	
    private int countPoolMaps;

    private int plateSize;

    private int outVol;

    private Double label;

    public Fitness()
    {
	poolMaps = new ArrayList<PoolMap>();
    }


    public Fitness(Bat conv, boolean debugSwitch)
    {
	
	poolMaps = new ArrayList<PoolMap>();

	plateSize =  conv.outputVol;
	countPoolMaps = conv.countFeatureMaps;
	outVol = plateSize/2;
    	
	for( int i = 0; i<countPoolMaps; i++) 
        {
            PoolMap poolMap = new PoolMap(plateSize,outVol,debugSwitch);
            addPoolMap(poolMap);
	}

    }



    public int countPoolMaps()
    {
	return poolMaps.size();
    }

    public void addPoolMap(PoolMap poolMap)
    {
	poolMaps.add(poolMap);
    }

    public ArrayList<PoolMap> get_P_maps()
    {
	return poolMaps;
    }

    public void calcPoolMaps( )
    {
	for(PoolMap pool_map: poolMaps)
            pool_map.computePoolMap(); 

    }

    public void readInputFeature(ArrayList<FeatureMap> feature_maps )
    {
	for (int i = 0; i <  feature_maps.size() ; i++)
        {
            FeatureMap feature_map = feature_maps.get(i);
            Double [][]fMap = feature_map.getFeatureMap();

            PoolMap poolMap = poolMaps.get(i);

            Double [][] pMap = poolMap.getInputFeature();

            for(int j = 0 ; j < fMap.length; j++)
            {
		System.arraycopy( fMap[j],0, pMap[j], 0, fMap[j].length);
            }
	}

    }

    public void train(  ArrayList<FeatureMap> feature_maps   )
    {
	readInputFeature(feature_maps);
	calcPoolMaps();
    }

    public void train(  Bat conv   )
    {

	this.label = conv.getLabel();

	ArrayList<FeatureMap> feature_maps = conv.get_fMaps();

	readInputFeature(feature_maps);
	calcPoolMaps();
    }

    public int outputVolume()
    {
	return outVol;
    }

    public void  connectPreConv( Bat conv)
    {
    }

    public Double getLabel()
    {
	return this.label;
    }

    public void backpropagate (Frequencylayer flat)
    {
        Double [] err = flat.getErrors();
	Double [][] weights = flat.getOldWeights();
	Double sum;
	int index;

	for ( int i = 0; i < poolMaps.size() ; i++)
        {
            PoolMap pmap = poolMaps.get(i);

            Double [][] error = pmap.getErrors();

            for(int row = 0; row < error.length; row++)
            {
		for(int col = 0; col < error[0].length; col++)
                {
                    sum = 0.0;
					
                    for(int j = 0; j < weights.length; j++)
                    {			
			index = i * error.length * error[0].length + row * error.length + col;
			sum += weights[j][index]*err[j];
                    }
                    error[row][col] = sum;
		}
            }

	}
    }

	
    public void backpropagate (Bat conv)
    {
	ArrayList<FeatureMap> fmaps = conv.get_fMaps();
	int kernel_size = conv.getKernelSize();

	for(int i = 0; i < fmaps.size(); i++)
        {
            FeatureMap  fm = fmaps.get(i);
            PoolMap pm = poolMaps.get(i);

            Double [][] error = pm.getErrors();

            Double [][] err = fm.getErrors();

            for(int x=0; x< error.length; x++)
		Arrays.fill(error[x], 0.0);

            for(int x = 0; x < err.length; x++)
		System.arraycopy( err[x], 0, error[x+kernel_size/2],kernel_size/2,err[x].length);

	}

    }

}
