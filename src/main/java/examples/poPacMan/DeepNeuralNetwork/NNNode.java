/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan.DeepNeuralNetwork;

import java.util.ArrayList;
import javax.swing.text.Position;

/**
 *
 * @author Giang
 */
public class NNNode {
    final double momentum = 0.05f;
     double inputValue;
    double outputValue;
    boolean isHidden;
    boolean isInput;
    boolean isOutput;
    int index ; // index of this node on this layer
    
    double dErrordOut; // until the current node
    double dOutdIn;
    double dErrordIn;
    double dIndOut; // next node to current node
    
    
    NNLayer layerInput;
    NNLayer layerOutput;
    NNLayer layerCurrent;
    ArrayList<NNConnection> weights ; // wiehgt of the input layer 
                                 // weight.at(i) : weight of input i from #layer-1
   
    
    NNNode ( ) {
     
    isHidden = false;
    isInput = false;
    isOutput = false;
    
    inputValue = 0;
    outputValue = 0;
    weights = new ArrayList<NNConnection>();
    }
    
    
    void initial (int _index, boolean _isInput, boolean _isHidden,  boolean _isOutput,NNLayer _layerInput,NNLayer _layerCurrent ,NNLayer _layerOutput) {
     
    isHidden = _isHidden; 
    isInput = _isInput;
    isOutput = _isOutput;
    
     
    layerInput =_layerInput;
    layerOutput = _layerOutput;
    layerCurrent = _layerCurrent;
    
    index = _index;
    
    // create weight
    if (layerInput!=null)
    for (int i =0; i < layerInput.numOfNode; i ++)
            weights.add(new NNConnection(layerInput.listOfNode.get(i), this));
      
    
    }
    
    
     
    void initialBias () {
        outputValue = 1;
    
    }
     
    void canculateInput() {
        inputValue =0 ;
        for(int i =0; i <layerInput.numOfNode; i++)
            inputValue += layerInput.listOfNode.get(i).outputValue*weights.get(i).weight;
        
    };
    
    void canculateOutput() {
    // sigmoi fuction
     double s = 0;
        
     for (int i =0; i < weights.size(); i++){
            NNNode leftNeuron = weights.get(i).getFromNeuron();
            double weight = weights.get(i).getWeight();
            double a = leftNeuron.getOutput(); //output from previous layer
             
            s = s + (weight*a);
        }
     
         
        outputValue = sigmoid(s);
    
    }
    
    double sigmoid(double x) {
        return 1.0 / (1.0 +  (Math.exp(-x)));
    }
    
    void print () {
        System.out.println(inputValue + " - " + outputValue);
    
    }
    
    double getOutput () {
        return outputValue;
    }
    
    
    void calculatedErrordOut () {
        
        
    
    }
    
    void calculateddOutdIn (){
        dOutdIn = outputValue*(1-outputValue);
    }
    
    void calculatedOutIn () {
        
        dOutdIn = this.outputValue * (1-this.outputValue);
        double sumLastStep = 0;
        for (int i =0; i < this.layerOutput.numOfNode; i ++)
              sumLastStep +=  this.layerOutput.listOfNode.get(i).dOutdIn;
       
        dOutdIn*=sumLastStep;
    
    }
    
}
