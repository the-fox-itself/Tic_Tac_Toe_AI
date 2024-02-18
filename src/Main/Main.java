package Main;

import org.ejml.simple.SimpleMatrix;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import static Main.MinimaxAlgorithm.*;
import static Main.GraphFrame.*;

public class Main {
    final public static int inputs = 9;
    final public static int outputs = 9;

    final public static String SIGMOID = "sigmoid";
    final public static String RELU = "relu";
    final public static String TANH = "tanh";

    final public static int hiddenNeurons = 10;
    final public static int[] structure = {inputs, hiddenNeurons, hiddenNeurons, outputs};
    public static double trainingFactor = 7.5;
    public static String activationFunction = SIGMOID;
    public static boolean presenceOfBiasNeurons = true;
    public static int batchNumber = 30;
    
    public static ArrayList<SimpleMatrix> neuronValuesMatrices = new ArrayList<>();
    public static ArrayList<SimpleMatrix> synapseWeightsMatrices = new ArrayList<>();
    public static SimpleMatrix perfectOutput = new SimpleMatrix(1, outputs);
    public static ArrayList<SimpleMatrix> zs;

    public static ArrayList<Double> biases = new ArrayList<>();
    public static ArrayList<SimpleMatrix> biasWeightsMatrices = new ArrayList<>();

    public static ArrayList<SimpleMatrix> randomWeightAssignment = new ArrayList<>();
    public static ArrayList<Double> randomBiases = new ArrayList<>();
    public static ArrayList<SimpleMatrix> randomBiasWeightAssignment = new ArrayList<>();

    public static long startTime;
    public static double cost;
    
    
    public static void main(String[] args) {
//        Toolkit.getDefaultToolkit().beep();

        for (int i = 0; i < structure.length - 1; i++) {
            SimpleMatrix randomWeights = new SimpleMatrix(structure[i], structure[i + 1]);                               //Assigning initial random weights to all synapses
            for (int j = 0; j < randomWeights.getNumRows(); j++) {
                for (int j1 = 0; j1 < randomWeights.getNumCols(); j1++) {
                    randomWeights.set(j, j1, Math.random() * 2 - 1);
                }
            }
            randomWeightAssignment.add(randomWeights);

            if (presenceOfBiasNeurons) {
                randomBiases.add(Math.random() * 2 - 1);
                SimpleMatrix randomBiasWeights = new SimpleMatrix(1, structure[i + 1]);
                for (int j = 0; j < randomBiasWeights.getNumCols(); j++) {
                    randomBiasWeights.set(0, j, Math.random() * 2 - 1);
                }
                randomBiasWeightAssignment.add(randomBiasWeights);
            }
        }

//        displayGraph();
//        Loop loop = new Loop();
//        loop.start();

        loop: for (int i = 0; i < 1000; i++) {
            neuronValuesMatrices = new ArrayList<>();
            synapseWeightsMatrices = new ArrayList<>();

            if (presenceOfBiasNeurons) {
                biases = new ArrayList<>();
                biasWeightsMatrices = new ArrayList<>();
            }

            //Error Reduction Training
            for (int layer : structure) {                                                                                   //Neural network structure initiation
                neuronValuesMatrices.add(new SimpleMatrix(1, layer));
            }

            synapseWeightsMatrices = (ArrayList<SimpleMatrix>) randomWeightAssignment.clone();

            if (presenceOfBiasNeurons) {
                biases = (ArrayList<Double>) randomBiases.clone();
                biasWeightsMatrices = (ArrayList<SimpleMatrix>) randomBiasWeightAssignment.clone();
            }

            startTime = (long) (System.nanoTime()/Math.pow(10, 9));

            while (true) {
                ArrayList<ArrayList<SimpleMatrix>> batchSynapseDerivativesMatrices = new ArrayList<>();
//                ArrayList<ArrayList<Double>> batchBiasDerivatives = new ArrayList<>();
                ArrayList<ArrayList<SimpleMatrix>> batchBiasSynapseDerivativesMatrices = new ArrayList<>();

                for (int example = 0; example < batchNumber; example++) {
                    State[] trainingExample = getRandomNonTerminalGameState();                                              //Generating a random game state as a training example

                    inputTrainingExample(trainingExample);
                    getPerfectOutput(trainingExample);
                    forwardPropagation();

                    ArrayList<SimpleMatrix> synapseDerivativesMatrices = new ArrayList<>();                                        //Backpropagation
                    ArrayList<Double> biasDerivatives = new ArrayList<>();
                    ArrayList<SimpleMatrix> biasSynapseDerivativesMatrices = new ArrayList<>();
                    for (int layer = 0; layer < synapseWeightsMatrices.size(); layer++) {
                        synapseDerivativesMatrices.add(null);
                        if (presenceOfBiasNeurons) {
                            biasDerivatives.add(Double.NaN);
                            biasSynapseDerivativesMatrices.add(null);
                        }
                    }

                    ArrayList<SimpleMatrix> zsPrime = new ArrayList<>();
                    for (int layer = 0; layer < zs.size(); layer++) {
                        SimpleMatrix zPrime = zs.get(layer).copy();
                        if (layer >= zs.size()-2) {
                            zPrime.equation("A = (1 / (1 + (e .^ -A))) .* (1 - (1 / (1 + (e .^ -A))))");
                        } else {
                            switch (activationFunction) {
                                case SIGMOID:
                                    zPrime.equation("A = (1 / (1 + (e .^ -A))) .* (1 - (1 / (1 + (e .^ -A))))");   //Applying a sigmoid derivative function to all z values
                                    break;
                                case RELU:
                                    for (int row = 0; row < zPrime.getNumRows(); row++) {
                                        for (int col = 0; col < zPrime.getNumCols(); col++) {
                                            if (zPrime.get(row, col) < 0)
                                                zPrime.set(row, col, 0);
                                            else
                                                zPrime.set(row, col, 1);
                                        }
                                    }
                                    break;
                                case TANH:
                                    for (int row = 0; row < zPrime.getNumRows(); row++) {
                                        for (int col = 0; col < zPrime.getNumCols(); col++) {
                                            double value = zPrime.get(row, col);
                                            double result = (1 - Math.pow((Math.pow(Math.E, value) - Math.pow(Math.E, -value)) / (Math.pow(Math.E, value) + Math.pow(Math.E, -value)), 2));
                                            zPrime.set(row, col, result);
                                        }
                                    }
                                    break;
                            }
                        }
                        zsPrime.add(zPrime);
                    }

                    int layerIndex = neuronValuesMatrices.size() - 1;                                                       //The index of the current neuron layer

                    SimpleMatrix sigmaOutput = perfectOutput.minus(neuronValuesMatrices.get(layerIndex)).negative().elementMult(zsPrime.get(layerIndex - 1));  //Output layer backpropagation
                    SimpleMatrix dJdWOutput = neuronValuesMatrices.get(layerIndex - 1).transpose().mult(sigmaOutput);
                    synapseDerivativesMatrices.set(layerIndex - 1, dJdWOutput);

                    if (presenceOfBiasNeurons) {
                        SimpleMatrix dJdWBiasOutput = sigmaOutput.scale(biases.get(layerIndex - 1));
                        biasSynapseDerivativesMatrices.set(layerIndex - 1, dJdWBiasOutput);
                    }

                    SimpleMatrix sigmaPrevious = sigmaOutput;
                    layerIndex--;
                    for (; layerIndex > 0; layerIndex--) {
                        sigmaPrevious = sigmaPrevious.mult(synapseWeightsMatrices.get(layerIndex).transpose()).elementMult(zsPrime.get(layerIndex - 1));
                        SimpleMatrix dJdW = neuronValuesMatrices.get(layerIndex - 1).transpose().mult(sigmaPrevious);
                        synapseDerivativesMatrices.set(layerIndex - 1, dJdW);

                        if (presenceOfBiasNeurons) {
                            SimpleMatrix dJdWBias = sigmaPrevious.scale(biases.get(layerIndex - 1));
                            biasSynapseDerivativesMatrices.set(layerIndex - 1, dJdWBias);
                        }
                    }

                    batchSynapseDerivativesMatrices.add(synapseDerivativesMatrices);
                    if (presenceOfBiasNeurons) {
                        batchBiasSynapseDerivativesMatrices.add(biasSynapseDerivativesMatrices);
//                        batchBiasDerivatives.add(biasDerivatives);
                    }
                }

                ArrayList<SimpleMatrix> meanSynapseDerivativesMatrices = new ArrayList<>();                                        //Calculating the mean derivatives from all training examples in a batch
                ArrayList<SimpleMatrix> meanBiasSynapseDerivativesMatrices = new ArrayList<>();
                for (SimpleMatrix synapseWeightsMatrix : synapseWeightsMatrices) {
                    meanSynapseDerivativesMatrices.add(SimpleMatrix.filled(synapseWeightsMatrix.getNumRows(), synapseWeightsMatrix.getNumCols(), 0));
                    if (presenceOfBiasNeurons)
                        meanBiasSynapseDerivativesMatrices.add(SimpleMatrix.filled(1, synapseWeightsMatrix.getNumCols(), 0));
                }

                for (ArrayList<SimpleMatrix> singleBatch : batchSynapseDerivativesMatrices) {
                    for (int matrix = 0; matrix < meanSynapseDerivativesMatrices.size(); matrix++) {
                        meanSynapseDerivativesMatrices.set(matrix, meanSynapseDerivativesMatrices.get(matrix).plus(singleBatch.get(matrix)));
                    }
                }
                for (int matrix = 0; matrix < meanSynapseDerivativesMatrices.size(); matrix++) {
                    meanSynapseDerivativesMatrices.set(matrix, meanSynapseDerivativesMatrices.get(matrix).divide(batchNumber));
                }
                for (SimpleMatrix derivatives : meanSynapseDerivativesMatrices) {                                                  //Multiplication by the training factor
                    derivatives.equation("A = A * B", trainingFactor, "B");
                }
                for (int layer = 0; layer < meanSynapseDerivativesMatrices.size(); layer++) {                                      //Applying the derivatives
                    synapseWeightsMatrices.set(layer, synapseWeightsMatrices.get(layer).minus(meanSynapseDerivativesMatrices.get(layer)));
                }

                if (presenceOfBiasNeurons) {
                    for (ArrayList<SimpleMatrix> singleBatch : batchBiasSynapseDerivativesMatrices) {
                        for (int matrix = 0; matrix < meanBiasSynapseDerivativesMatrices.size(); matrix++) {
                            meanBiasSynapseDerivativesMatrices.set(matrix, meanBiasSynapseDerivativesMatrices.get(matrix).plus(singleBatch.get(matrix)));
                        }
                    }
                    for (int matrix = 0; matrix < meanBiasSynapseDerivativesMatrices.size(); matrix++) {
                        meanBiasSynapseDerivativesMatrices.set(matrix, meanBiasSynapseDerivativesMatrices.get(matrix).divide(batchNumber));
                    }
                    for (SimpleMatrix biasDerivatives : meanBiasSynapseDerivativesMatrices) {
                        biasDerivatives.equation("A = A * B", trainingFactor, "B");
                    }
                    for (int layer = 0; layer < meanBiasSynapseDerivativesMatrices.size(); layer++) {
                        biasWeightsMatrices.set(layer, biasWeightsMatrices.get(layer).minus(meanBiasSynapseDerivativesMatrices.get(layer)));
                    }
                }

                cost = testNeuralNetwork();
//                System.out.println(cost);
//                if (cost < 0.7)
//                    break;
                if (cost < 0.7) {
                    double time = (double) (Math.round((System.nanoTime()/Math.pow(10, 9)-startTime)*1000))/1000;
                    System.out.println(time);
                    continue loop;
                }
            }


//            while (true) {                                                                                                  //Playing a game with the trained neural network
//                State[] board = getEmptyBoard();
//                printGameState(board);
//                State user;
//                int rand = new Random().nextInt(2);
//                if (rand == 0) {
//                    user = State.X;
//                } else {
//                    user = State.O;
//                }
//                while (Value(board) == -2) {
//                    int move;
//                    if (getPlayer(board) == user) {
//                        BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
//                        try {
//                            move = Integer.parseInt(stream.readLine()) - 1;
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    } else {
//                        inputTrainingExample(board);
//                        forwardPropagation();
//                        move = getNeuralNetworkAnswer();
//                    }
//                    board = Result(board, move, getPlayer(board));
//                    printGameState(board);
//                }
//                int value = Value(board);
//                if (value == 1)
//                    System.out.println(BOT + " is the winner!");
//                else if (value == -1)
//                    System.out.println(ENEMY + " is the winner!");
//            }
        }
        Toolkit.getDefaultToolkit().beep();
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
            if (presenceOfBiasNeurons)
                z = z.plus(biasWeightsMatrices.get(layer).scale(biases.get(layer)));
            zs.add(z);
            SimpleMatrix finalZ = z.copy();
            if (layer >= structure.length-3) {
                finalZ.equation("1 / (1 + (e .^ -A))");
            } else {
                switch (activationFunction) {
                    case SIGMOID:
                        finalZ.equation("1 / (1 + (e .^ -A))");                                         //Applying a sigmoid function to all z values
                        break;
                    case RELU:
                        for (int row = 0; row < finalZ.getNumRows(); row++) {
                            for (int col = 0; col < finalZ.getNumCols(); col++) {
                                finalZ.set(row, col, Math.max(0, finalZ.get(row, col)));
                            }
                        }
                        break;
                    case TANH:
                        for (int row = 0; row < finalZ.getNumRows(); row++) {
                            for (int col = 0; col < finalZ.getNumCols(); col++) {
                                double value = finalZ.get(row, col);
                                double result = (Math.pow(Math.E, value) - Math.pow(Math.E, -value)) / (Math.pow(Math.E, value) + Math.pow(Math.E, -value));
                                finalZ.set(row, col, result);
                            }
                        }
                        break;
                }
            }
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