/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan.DeepNeuralNetwork;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Giang
 */
public class NNNet {
     
   final double learningRate = 0.05f;
    final double momentum = 0.7f;
    NNLayer inputNetwork;
    NNLayer outputNetwork;
      ArrayList<NNLayer> hiddenLayers ;
      int numOfLayer; // 1 input, 1 output, n-2 hiddenlayer
      
     int tologogy[]; // define number of node of each layer
     
     NNNet ( int numInput, int _topology[], int numOutput) {
     
         this.numOfLayer = _topology.length;
         tologogy = new int[this.numOfLayer];
         tologogy = Arrays.copyOf(_topology, _topology.length);
     
         inputNetwork = new NNLayer(numInput);
         outputNetwork = new NNLayer(numOutput);
         
         hiddenLayers = new ArrayList<NNLayer>();
         
          for(int i =0; i < numOfLayer; i ++) 
           hiddenLayers.add(new NNLayer(tologogy[i]));
       
     }
     
     void initialNeuralNetwork () {
        
        // input layer
        inputNetwork.setInputOutputLayer(null, hiddenLayers.get(0));
        inputNetwork.initital();
        // hidden layre connect to input
        hiddenLayers.get(0).setInputOutputLayer(inputNetwork, hiddenLayers.get(1));
        hiddenLayers.get(0).initital();
        
        
        
        // hidden layer
        for (int i =1; i < numOfLayer-1; i++){
            hiddenLayers.get(i).setInputOutputLayer(hiddenLayers.get(i-1), hiddenLayers.get(i+1));
            hiddenLayers.get(i).initital();
        }
         
        
        // hidden layre connect to output
        
        hiddenLayers.get( numOfLayer-1).setInputOutputLayer( hiddenLayers.get(numOfLayer-2),outputNetwork);
        hiddenLayers.get( numOfLayer-1).initital();
        //output layer
        outputNetwork.setInputOutputLayer(hiddenLayers.get(numOfLayer-1), null);
        outputNetwork.initital();
     }
    
      
     
     void canculateNetwork () {
     // CANCULATE INPUT FOR THE FIRST HDDIEDN
        for (int i =0; i < this.hiddenLayers.get(0).numOfNode; i ++) {
             NNNode currentNode = this.hiddenLayers.get(0).listOfNode.get(i);
             double valueInput = 0;
           
            // for each input
            for (int j =0; j < inputNetwork.numOfNode; j++)
             valueInput +=  currentNode.layerInput.listOfNode.get(j).outputValue* currentNode.weights.get(j).weight ;
            
             currentNode.inputValue = valueInput;
             currentNode.outputValue = currentNode.sigmoid(currentNode.inputValue);
        
        }
         
         
         for (int i =1; i < this.numOfLayer; i ++) {
             for (int j=0; j < this.hiddenLayers.get(i).numOfNode; j ++) {
                 NNNode currentNode = this.hiddenLayers.get(i).listOfNode.get(j);
                 double valueInput = 0;
                 
                for (int k=0; k < currentNode.weights.size(); k ++)
                        valueInput +=  currentNode.layerInput.listOfNode.get(k).outputValue* currentNode.weights.get(k).weight ;
             
                currentNode.inputValue = valueInput;
                currentNode.outputValue = currentNode.sigmoid(currentNode.inputValue);
             }
             
         }
         
         // CALCUALTE OUTPUT
        for (int i =0; i < outputNetwork.numOfNode; i ++) {
             NNNode currentNode =outputNetwork.listOfNode.get(i);
             double valueInput = 0;
           
            // for each input
            for (int j =0; j < this.hiddenLayers.get(this.numOfLayer-1).numOfNode; j++)
            valueInput +=  currentNode.layerInput.listOfNode.get(j).outputValue* currentNode.weights.get(j).weight ;
             
            currentNode.inputValue = valueInput;
            currentNode.outputValue = valueInput;
        
        }
     
        outputValue = outputNetwork.listOfNode.get(0).inputValue;
     }
     
     
     void generateInput () {
     
            for (int i =0; i < inputNetwork.numOfNode; i ++){
                Random R = new Random();
                inputNetwork.listOfNode.get(i).inputValue = R.nextInt(10);
                inputNetwork.listOfNode.get(i).outputValue = inputNetwork.listOfNode.get(i).inputValue/10;
            }
     }
     
     
      void backpropagateOutput (double error) {
         
        for (int i =0;i <  outputNetwork.numOfNode; i++ ) 
            outputNetwork.listOfNode.get(i).dOutdIn = error;
        
        
    }
     
     
  
     
    void backpropagateHiddenLayer () {
    
        for (int i =this.numOfLayer-1;  i>=0; i--) {
          NNLayer currentLayer = this.hiddenLayers.get(i);
        // just 1 output
        for (int j =0;j <  currentLayer.numOfNode; j++ ) {
            NNNode currentNode = currentLayer.listOfNode.get(j);
            currentNode.dOutdIn = currentNode.outputValue *(1-currentNode.outputValue);
            
            double sumdInOut = 0;
            
            for (int k=0; k < currentNode.layerOutput.numOfNode; k++) {
                sumdInOut+= currentNode.layerOutput.listOfNode.get(k).dOutdIn*currentNode.layerOutput.listOfNode.get(k).weights.get(j).weight;
            
            }
            currentNode.dOutdIn*=sumdInOut;
        
        }
         
        }
    
    }
    
  
     
     
     
     public void print () {
         for (int i =0; i <numOfLayer; i ++)
             hiddenLayers.get(i).print();
     
     }
     
     double error;
     double outputValue;
     
     double getOutput () {
         outputValue = 0;
         for (int i =0; i < outputNetwork.numOfNode;i ++)
             outputValue += outputNetwork.listOfNode.get(i).outputValue;
         
         return outputValue;
     }
     
      double getError () {
         error = 0;
         error = getExactvalue() - getOutput();
         
         return error;
     }
     
    double getExactvalue () {
     
         double result  =0 ;
         
         int listP[] = new int[] {1,1,1};
         
         
         for (int i =0; i < this.inputNetwork.numOfNode; i ++)
                result += listP[i]*this.inputNetwork.listOfNode.get(i).inputValue;
         
         
     return result;
     
     }
    
    
    
    void updateWeight (){
        
        for (int i =0; i < numOfLayer; i ++) {
           NNLayer currentLayer = this.hiddenLayers.get(i);
            for (int j =0; j < currentLayer.numOfNode; j ++) {
                NNNode currentNode = currentLayer.listOfNode.get(j);
                
                for (int k=0; k < currentNode.weights.size(); k++ )
                        currentNode.weights.get(k).updateWeight();
           }
        
        }
        
        // weight for outout
        
         for (int j =0; j < outputNetwork.numOfNode; j ++) {
                NNNode currentNode = outputNetwork.listOfNode.get(j);
                
                for (int k=0; k < currentNode.weights.size(); k++ )
                        currentNode.weights.get(k).updateWeight();
           }
        
        
    
    }
    
     void backpropagate () {
        double err = getError();
        // back prrpagattion
         backpropagateOutput(error);
         backpropagateHiddenLayer();
    
         
         // get update
          updateWeight () ;
         
    }
   
}
