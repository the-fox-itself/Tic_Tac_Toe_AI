package Main;

import org.ejml.simple.SimpleMatrix;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static Main.MinimaxAlgorithm.*;

public class Main {
    final public static int inputs = 9;
    final public static int outputs = 9;
    final public static int[] structure = {inputs, 8, 8, outputs};
    
    public static double trainingFactor = 1;
    public static int batchNumber = 20;
    
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

        for (int batch = 0; batch < 100; batch++) {
            ArrayList<ArrayList<SimpleMatrix>> batchDerivativeMatrices = new ArrayList<>();
            
            for (int example = 0; example < batchNumber; example++) {
                State[] trainingExample = getRandomNonTerminalGameState();                                              //Generating a random game state as a training example
                
                inputTrainingExample(trainingExample);
                getPerfectOutput(trainingExample);
                forwardPropagation();
                
                
                ArrayList<SimpleMatrix> derivativesMatrices = new ArrayList<>();                                        //Backpropagation
                for (int layer = 0; layer < synapseWeightsMatrices.size(); layer++)
                    derivativesMatrices.add(null);
                
                ArrayList<SimpleMatrix> zsPrime = new ArrayList<>();
                for (SimpleMatrix matrix : zs) {
                    SimpleMatrix zPrime = matrix.copy();
                    zPrime.equation("A = (1 / (1 + (e .^ -A))) .* (1 - (1 / (1 + (e .^ -A))))");                //Applying a sigmoid derivative function to all z values
                    zsPrime.add(zPrime);
                }
                
                int layerIndex = neuronValuesMatrices.size() - 1;                                                       //The index of the current neuron layer
                
                SimpleMatrix sigmaOutput = perfectOutput.minus(neuronValuesMatrices.get(layerIndex)).negative().elementMult(zsPrime.get(layerIndex - 1));  //Output layer backpropagation
                SimpleMatrix dJdW3 = neuronValuesMatrices.get(layerIndex - 1).transpose().mult(sigmaOutput);
                derivativesMatrices.set(layerIndex - 1, dJdW3);
                
                SimpleMatrix sigmaPrevious = sigmaOutput;
                layerIndex--;
                for (; layerIndex > 0; layerIndex--) {
                    sigmaPrevious = sigmaPrevious.mult(synapseWeightsMatrices.get(layerIndex).transpose()).elementMult(zsPrime.get(layerIndex - 1));
                    SimpleMatrix dJdW = neuronValuesMatrices.get(layerIndex - 1).transpose().mult(sigmaPrevious);
                    derivativesMatrices.set(layerIndex - 1, dJdW);
                }
                
                batchDerivativeMatrices.add(derivativesMatrices);
            }
            
            ArrayList<SimpleMatrix> meanDerivativesMatrices = new ArrayList<>();                                        //Calculating the mean derivatives from all training examples in a batch
            for (SimpleMatrix synapseWeightsMatrix : synapseWeightsMatrices)
                meanDerivativesMatrices.add(SimpleMatrix.filled(synapseWeightsMatrix.getNumRows(), synapseWeightsMatrix.getNumCols(), 0));
            for (ArrayList<SimpleMatrix> singleBatch : batchDerivativeMatrices) {
                for (int matrix = 0; matrix < meanDerivativesMatrices.size(); matrix++) {
                    meanDerivativesMatrices.set(matrix, meanDerivativesMatrices.get(matrix).plus(singleBatch.get(matrix)));
                }
            }
            for (SimpleMatrix matrix : meanDerivativesMatrices) {
                matrix.divide(batchNumber);
            }
            
            for (SimpleMatrix derivatives : meanDerivativesMatrices) {                                                  //Multiplication by the training factor
                derivatives.equation("A = A * B", trainingFactor, "B");
            }
            
            for (int layer = 0; layer < meanDerivativesMatrices.size(); layer++) {                                      //Applying the derivatives
//                meanDerivativesMatrices.get(layer).print();
                synapseWeightsMatrices.set(layer, synapseWeightsMatrices.get(layer).minus(meanDerivativesMatrices.get(layer)));
            }
            
            System.out.println(testNeuralNetwork());
        }
        
        
        while (true) {                                                                                                  //Playing a game with the trained neural network
            State[] board = getEmptyBoard();
            printGameState(board);
            State user;
            int rand = new Random().nextInt(2);
            if (rand == 0) {
                user = State.X;
            } else {
                user = State.O;
            }
            while (Value(board) == -2) {
                int move;
                if (getPlayer(board) == user) {
                    BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
                    try {
                        move = Integer.parseInt(stream.readLine()) - 1;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    
                } else {
                    inputTrainingExample(board);
                    forwardPropagation();
                    move = getNeuralNetworkAnswer();
                }
                board = Result(board, move, getPlayer(board));
                printGameState(board);
            }
            int value = Value(board);
            if (value == 1)
                System.out.println(BOT + " is the winner!");
            else if (value == -1)
                System.out.println(ENEMY + " is the winner!");
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
    public static int getNeuralNetworkAnswer() {
        double max = 0;
        int answer = 0;
        for (int neuron = 0; neuron < outputs; neuron++) {
            if (neuronValuesMatrices.get(0).get(neuron) != 0)
                continue;
            double value = neuronValuesMatrices.get(neuronValuesMatrices.size()-1).get(neuron);
            if (value > max || neuron == 0) {
                max = value;
                answer = neuron;
            }
        }
        return answer;
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