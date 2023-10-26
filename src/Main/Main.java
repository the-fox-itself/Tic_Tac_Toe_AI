package Main;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

import static Main.MinimaxAlgorithm.*;

public class Main {
    final public static int inputs = 9;
    final public static int outputs = 9;
    final public static int[] structure = {inputs, 16, 16, outputs};
    
    public static double trainingFactor = 10;
    
    public static ArrayList<SimpleMatrix> neuronValuesMatrices = new ArrayList<>();
    public static ArrayList<SimpleMatrix> synapseWeightsMatrices = new ArrayList<>();
    public static SimpleMatrix perfectOutput = new SimpleMatrix(1, outputs);
    public static ArrayList<SimpleMatrix> zs;
    
    
    
    public static void main(String[] args) {
        //Error Reduction Training
        for (int layer : structure) {                                                                                   //Neural network structure initiation
            neuronValuesMatrices.add(new SimpleMatrix(1, layer));
        }
        
        for (int i = 0; i < structure.length-1; i++) {
            SimpleMatrix simpleMatrix = new SimpleMatrix(structure[i], structure[i+1]);                                 //Assigning initial random weights to all synapses
            for (int j = 0; j < simpleMatrix.getNumRows(); j++) {
                for (int j1 = 0; j1 < simpleMatrix.getNumCols(); j1++) {
                    simpleMatrix.set(j, j1, Math.random()*2-1);
                }
            }
            synapseWeightsMatrices.add(simpleMatrix);
        }

        for (int example = 0; example < 100000; example++) {
            State[] trainingExample = getRandomNonTerminalGameState();                                                      //Generating a random game state as a training example
//            printGameState(trainingExample);
            
            inputTrainingExample(trainingExample);
            getPerfectOutput(trainingExample);
            forwardPropagation();
            
//            System.out.println("The resultant output layer neuron values:");
//            neuronValuesMatrices.get(neuronValuesMatrices.size() - 1).print();
            
//            double cost = getCost();
//            System.out.println("Cost: " + cost);
            
            
            ArrayList<SimpleMatrix> synapseDerivativesMatrices = new ArrayList<>();                                         //Backpropagation
            synapseDerivativesMatrices.add(null);
            synapseDerivativesMatrices.add(null);
            synapseDerivativesMatrices.add(null);
            
            ArrayList<SimpleMatrix> zsPrime = new ArrayList<>();
            for (SimpleMatrix matrix : zs) {
                SimpleMatrix zPrime = matrix.copy();
                zPrime.equation("A = (1 / (1 + (e .^ -A))) .* (1 - (1 / (1 + (e .^ -A))))");                        //Applying a sigmoid derivative function to all z values
                zsPrime.add(zPrime);
            }
            
            
            SimpleMatrix sigma4 = perfectOutput.minus(neuronValuesMatrices.get(3)).negative().elementMult(zsPrime.get(2));
            SimpleMatrix dJdW3 = neuronValuesMatrices.get(2).transpose().mult(sigma4);
            synapseDerivativesMatrices.set(2, dJdW3);
//            dJdW3.print();
            
            SimpleMatrix sigma3 = sigma4.mult(synapseWeightsMatrices.get(2).transpose()).elementMult(zsPrime.get(1));
            SimpleMatrix dJdW2 = neuronValuesMatrices.get(1).transpose().mult(sigma3);
            synapseDerivativesMatrices.set(1, dJdW2);
//            dJdW2.print();
            
            SimpleMatrix sigma2 = sigma3.mult(synapseWeightsMatrices.get(1).transpose()).elementMult(zsPrime.get(0));
            SimpleMatrix dJdW1 = neuronValuesMatrices.get(0).transpose().mult(sigma2);
            synapseDerivativesMatrices.set(0, dJdW1);
//            dJdW1.print();
            
            for (SimpleMatrix derivatives : synapseDerivativesMatrices) {                                                   //Multiplying the derivatives the training factor
                derivatives.equation("A = A * B", trainingFactor, "B");
            }
            
            for (int layer = 0; layer < synapseDerivativesMatrices.size(); layer++) {
//                synapseDerivativesMatrices.get(layer).print();
                synapseWeightsMatrices.set(layer, synapseWeightsMatrices.get(layer).minus(synapseDerivativesMatrices.get(layer)));
            }
            
            System.out.println(testNeuralNetwork());
        }
    }
    
    public static void inputTrainingExample(State[] trainingExample) {
        for (int i = 0; i < trainingExample.length; i++) {                                                              //Inputting the training example into the neural network
            if (trainingExample[i] == State.X)
                neuronValuesMatrices.get(0).set(0, i, 1);
            else if (trainingExample[i] == State.O)
                neuronValuesMatrices.get(0).set(0, i, -1);
            else if (trainingExample[i] == State.I)
                neuronValuesMatrices.get(0).set(0, i, 0);
        }
    }
    public static void getPerfectOutput(State[] trainingExample) {
        ArrayList<Integer> optimalMoveSet = findOptimalMoveSet(trainingExample);
        perfectOutput.fill(0);
        for (int output = 0; output < outputs; output++) {
            if (optimalMoveSet.contains(output))
                perfectOutput.set(0, output, 1);
        }
    }
    public static void forwardPropagation() {
        zs = new ArrayList<>();
        for (int layer = 0; layer < structure.length - 1; layer++) {                                                    //Forward propagation
            SimpleMatrix z = neuronValuesMatrices.get(layer).mult(synapseWeightsMatrices.get(layer));
            zs.add(z);
            SimpleMatrix finalZ = z.copy();
            finalZ.equation("1 / (1 + (e .^ -A))");                                                             //Applying a sigmoid function to all z values
            neuronValuesMatrices.set(layer + 1, finalZ);
        }
    }
    public static double getCost() {
        double cost = 0;                                                                                                //Calculating the total cost for the processed training example
        for (int output = 0; output < outputs; output++) {
            cost += 0.5 * Math.pow(perfectOutput.get(output) - neuronValuesMatrices.get(neuronValuesMatrices.size() - 1).get(0, output), 2);
        }
        return cost;
    }
    
    public static double testNeuralNetwork() {
//        System.out.println("Test initiated");
        ArrayList<State[]> allTrainingExamples = getAllNonTerminalGameStates();
        double totalCost = 0.0;
        for (State[] state : allTrainingExamples) {
            inputTrainingExample(state);
            getPerfectOutput(state);
            forwardPropagation();
            double cost = getCost();
//            System.out.println(cost);
            totalCost += cost;
        }
//        System.out.println("Test completed");
//        System.out.println(totalCost);
        return totalCost/(allTrainingExamples.size());
    }

    public static double sigmoid(double value) {
        return 1/(1+Math.pow(Math.E, -value));
    }
    public static double relu(double value) {
        return Math.max(0, value);
    }

    public static double sigmoid_derivative(double value) {
        return sigmoid(value)*(1-sigmoid(value));
    }
}