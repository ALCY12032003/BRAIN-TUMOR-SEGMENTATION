/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package brain;
import java.util.Vector;
/**
 *
 * @author admin
 */
public class ConvNet 
{
    
    private int countClasses;

    private Bat conv1;
    private Fitness maxPool1;
    private Bat conv2;
    private Fitness maxPool2;
    private Bat conv3;
    private Fitness maxPool3;
    private Frequencylayer flat;
    private PulseEmission out;
    private int bestTune;


   public ConvNet( Vector<Vector<Double>> inputFeatureVectors , int hyperparameters, boolean debugSwitch)
   {      
       conv1        = new Bat(inputFeatureVectors, hyperparameters, false);    
       maxPool1     = new Fitness(conv1, false);                                       
       conv2        = new Bat(maxPool1, hyperparameters,  false);              
       maxPool2     = new Fitness(conv2, false);                                              
       flat         = new Frequencylayer(maxPool2, false);                               
       out          = new PulseEmission(flat, hyperparameters, false);                

   }

   public int trainCNN( Vector<Vector<Double>> trainFeatureVectors) 
   {   
	out.resetCountCorrect();
        int errorCount = 0;
	   
        for (int trainingIpNum = 0; trainingIpNum < trainFeatureVectors.size(); trainingIpNum++) 
        {
            Vector<Double> trainFeatureVector = trainFeatureVectors.get(trainingIpNum);
            conv1.train(trainFeatureVector);
            maxPool1.train(conv1);
            conv2.train(maxPool1);
            maxPool2.train(conv2);           

            flat.trainwithDropOut(maxPool2);
            out.train(flat);
            out.backpropagate();
            flat.backpropagate(out);

            maxPool2.backpropagate(flat);
            conv2.backpropagate(maxPool2);

            maxPool1.backpropagate(conv2);
            conv1.backpropagate(maxPool1);
           
            out.printPrediction();
            errorCount += out.reportPredictionError();
        }
       
        return errorCount;
    }

    

    public int testCNN( Vector<Vector<Double>> testFeatureVectors) 
    {
   	out.resetCountCorrect();
	out.zeroConfusionMatrix();

        int errorCount = 0;
 	   
        for (int testIpNum = 0; testIpNum < testFeatureVectors.size(); testIpNum++) 
        {
            Vector<Double> testFeatureVector = testFeatureVectors.get(testIpNum);

            conv1.train(testFeatureVector);
            maxPool1.train(conv1);
            conv2.train(maxPool1);
            maxPool2.train(conv2);
            flat.train(maxPool2);
            out.train(flat);

            out.printPrediction();
            errorCount += out.reportPredictionError();
        }
        
        out.printConfusion();
        return errorCount;
    }
   
}
