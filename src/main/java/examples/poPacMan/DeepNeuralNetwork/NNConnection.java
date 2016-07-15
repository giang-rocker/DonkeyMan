/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan.DeepNeuralNetwork;

/**
 *
 * @author Giang
 */
public class NNConnection {
    double learningRate = 0.0025f;
     double weight = 0;
    double prevDeltaWeight = 0; // for momentum
    double deltaWeight = 0;
 
    final NNNode leftNeuron;
    final NNNode rightNeuron;
    static int counter = 0;
  
    public NNConnection(NNNode fromN, NNNode toN) {
        leftNeuron = fromN;
        rightNeuron = toN;
       
    }
    
    
    public void calculateDeltaWeight  () {
        
        deltaWeight = leftNeuron.outputValue *  rightNeuron.dOutdIn;
    }
    
    
    public double getWeight() {
        return weight;
    }
 
    public void setWeight(double w) {
        weight = w;
    }
 
    public void setDeltaWeight(double w) {
        prevDeltaWeight = deltaWeight;
        deltaWeight = w;
    }
 
    public double getPrevDeltaWeight() {
        return prevDeltaWeight;
    }
 
    public NNNode getFromNeuron() {
        return leftNeuron;
    }
 
    public NNNode getToNeuron() {
        return rightNeuron;
    }
    
    public void updateWeight() {
        calculateDeltaWeight();
        weight+= learningRate*deltaWeight;
    }
 
}
