/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.ArrayList;
import java.util.Random;
/**
 *
 * @author admin
 */
public class Frequencylayer 
{
    private Double [] input;
    private Double [] errors;
    private Double [] output;
    private Double [][] weights;
    private Double [][] oldWeights;

    private int countInputs;
    private int countOutputs;

    private Double label;

    public Frequencylayer()
    {     

    }

    public Frequencylayer(Bat conv)
    {

    }

    public  void readInputFeature(  ArrayList<PoolMap> poolMaps )
    {

        for(int i = 0; i <  poolMaps.size(); i++)
        {
            PoolMap plate = poolMaps.get(i);
            int dimPlate = plate.getOutVol();
            int sizePlate = dimPlate * dimPlate ;
            Double [][] plateOut  = plate.getOutput();

            for( int j = 0 ; j < plateOut.length; j++)
            {
                System.arraycopy(plateOut[j],0, input, (i*sizePlate)  + (j * dimPlate) , plateOut[j].length);
            }
        }        
        input[input.length-1] = 1.0; //bias unit;
    }
    
    public  void train(Fitness pool)
    {
        ArrayList<PoolMap> poolMaps = pool.get_P_maps();
        this.label = pool.getLabel();
        readInputFeature(poolMaps);
        
        for(int j = 0; j < weights.length; j++)
        {       
            output[j] = 0.0;
        		
            for(int i = 0; i < input.length; i++)
            {
       		output[j] += weights[j][i]*input[i];
            }
        }        
        calcOutputs(output);        

    }

    public  void trainwithDropOut(Fitness pool)
    {

        ArrayList<PoolMap> poolMaps = pool.get_P_maps();
        this.label = pool.getLabel();
        readInputFeature(poolMaps);


        for(int j = 0; j < weights.length; j++)
        {
            output[j] = 0.0;
            for(int i = 0; i < input.length; i++)
            {
                output[j] += weights[j][i]*input[i];
            }
        }
        
        calcOutputsWithDropout(output);
    }
    
    public void calcOutputs(Double [] out)
    {
    	for(int i = 0; i < out.length; i++)
        {
            out[i] = LeakyReLU(out[i]);
    	}
    }

    public void calcOutputsWithDropout(Double [] out)
    {

        Random rand = new Random();
        for(int i = 0; i < out.length; i++)
        {
            out[i] = LeakyReLU(out[i]);
        }
    }
    
    public Double ReLU(Double  out)
    {    
    	return out = ((out>= 0) ? out : 0);
    		
    }

    public Double LeakyReLU(Double  out)
    {
    	return out = ((out>= 0) ? out : 0.1*out);

    }
    
    public Frequencylayer( Fitness pool, boolean debugSwitch)
    {
       
        int   countPoolMap    = pool.countPoolMaps();
        int  dimPlate         = pool.outputVolume();
        int numHiddenUnits = 25;//250;

        this.label = pool.getLabel();

        
        countInputs = countPoolMap*dimPlate*dimPlate;
        countOutputs = numHiddenUnits;

        input = new Double[countInputs+1];
        errors = new Double[numHiddenUnits];
        weights = new Double[numHiddenUnits][countInputs+1]; 
        oldWeights = new Double[numHiddenUnits][countInputs+1]; 
        output = new Double[countOutputs];

        initWeights();               

    }
    
    public void initWeights()
    {

        Random rand = new Random(1);
        Random randSign = new Random(1);

        for(int i = 0; i < weights.length; i++)
        {
            for(int j = 0; j < weights[0].length; j++)
            {
                int sign = randSign.nextInt() % 2;
                weights[i][j] =   rand.nextDouble();

                if( sign == 0)
                    weights[i][j] *= -1;
            }
        }
    }

    public int getCountInputs()
    {
        return countInputs;
    }
    
    public int getCountOutputs()
    {
    	
    	return countOutputs;
    }

    public void printAct(){

        for(int i =0 ; i < input.length; i++)
            System.out.println(" FlatLayer : "+i+"  "+input[i]);
    }

    public Double [] getInput ()
    {

        return input;
    }
    
    public Double [] getOutput()
    {
    	
    	return output;
    }

    public Double getLabel ()
    {

        return this.label;
    }

    public void backpropagate (PulseEmission out)
    {

        Double [] err = out.getErrors();
        Double [][] outWeights = out.getOldWeights();
        Double learningRate = 0.001;
       
        Double sum; 
        
        for(int i = 0; i < countOutputs; i++)
        {       
            sum = 0.0;
        	
            for(int j = 0; j < outWeights.length; j++)
            {        	
        	sum += outWeights[j][i] * err[j] ;
            }
            if(output[i] > 0)
            {
        	errors[i] = sum; 
            }
            else
            {
        	errors[i] = 0.1*sum;
            }
        		
        }
        
        for(int i = 0; i<weights.length; i ++)
        {
            for(int j = 0; j<weights[0].length; j++ )
            {
        	oldWeights[i][j] = weights[i][j];
        	weights[i][j] += errors[i]*input[j]*learningRate;
            }
        }
    }

    public Double [] getErrors()
    {
        return  errors;
    }
    
    public Double [][] getOldWeights()
    {
    	return oldWeights;
    }

}
