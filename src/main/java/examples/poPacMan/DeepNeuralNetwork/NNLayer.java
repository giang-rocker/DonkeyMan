/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan.DeepNeuralNetwork;

import java.util.ArrayList;

/**
 *
 * @author Giang
 */
public class NNLayer {
    // Node of currunt layer
    ArrayList<NNNode> listOfNode ;
     NNLayer inputLayer;
    NNLayer outputLayer;
    
    int numOfNode;
    
     NNLayer(int _numOfNode) {
        this.numOfNode = _numOfNode;
        listOfNode = new ArrayList<NNNode>();
         
        for (int i =0; i < numOfNode; i++){
             System.out.println("create Layer" + i);
           listOfNode.add(new NNNode());
        }
     
    }
    
    void setInputOutputLayer(NNLayer _inputLayer,NNLayer _outputLayer){
    this.inputLayer = _inputLayer;
    this.outputLayer = _outputLayer;
    }
    
    
    void initital() {
          boolean isInput = false,isHidden = false,isOutput = false;
          
          if (inputLayer==null) isInput = true;
          else if (outputLayer==null) isOutput = true;
          else isHidden = true;
          
        
        for (int i =0; i < numOfNode; i++){
            listOfNode.get(i).initial(i,isInput,isHidden,isOutput,inputLayer,this,outputLayer);
        }
    
        
    }
    
    
   
    void print () {
        for (int i =0; i < numOfNode; i ++)
            listOfNode.get(i).print();
    
    }
    
  
    
     
}
