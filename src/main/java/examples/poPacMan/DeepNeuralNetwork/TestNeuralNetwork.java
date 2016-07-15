/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.poPacMan.DeepNeuralNetwork;


import java.awt.Color;
 

/**
 *
 * @author Giang
 */
public class TestNeuralNetwork {
    
    public static void main(String[] args) throws InterruptedException {
       
       NNNet neuralNetwork;
       neuralNetwork = new NNNet( 3, new int[]{3,3},1);
       neuralNetwork.initialNeuralNetwork();
         
       // update
       
      
        
       NeuralNetworkForm neuralForm = new NeuralNetworkForm();
       neuralForm.setSize(1000,500);
       neuralForm.setVisible(true);
       neuralForm.setBackground(Color.black);
       neuralForm.setForeground(Color.black);
       neuralForm.neuralNetwork = neuralNetwork;
       
       do {
            neuralForm.clickUpdate();
            System.out.println(Math.abs(neuralNetwork.error)+" - " + neuralNetwork.getExactvalue()+" - " + neuralNetwork.outputValue);
       }
       while (Math.abs(neuralNetwork.error) > 0.0001f || neuralForm.autoUpdate==true) ;    
      
       System.out.println ("STOP");
       System.out.println(Math.abs(neuralNetwork.error)+" - " + neuralNetwork.getExactvalue()+" - " + neuralNetwork.outputValue);
  
    }
    
}
