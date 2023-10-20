package Main;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

import static Main.MinimaxAlgorithm.*;

public class Main {
    public static void main(String[] args) {
        //Error Reduction Training
        int inputs = 9;                                                                                                 //Neural network structure initialization
        int outputs = 9;
        int[] structure = {inputs, 16, 16, outputs};
        int layers = structure.length;
        
        ArrayList<SimpleMatrix> neuronValuesMatrices = new ArrayList<>();
        for (int layer : structure) {
            neuronValuesMatrices.add(new SimpleMatrix(1, layer));
        }
        
        ArrayList<SimpleMatrix> synapseWeightsMatrices = new ArrayList<>();
        for (int i = 0; i < structure.length-1; i++) {
            SimpleMatrix simpleMatrix = new SimpleMatrix(structure[i], structure[i+1]);                                 //Assigning initial random weights to all synapses
            for (int j = 0; j < simpleMatrix.getNumRows(); j++) {
                for (int j1 = 0; j1 < simpleMatrix.getNumCols(); j1++) {
                    simpleMatrix.set(j, j1, Math.random()*2-1);
                }
            }
            synapseWeightsMatrices.add(simpleMatrix);
        }

        while (true) {
            State[] trainingExample = getRandomNonTerminalGameState();                                                  //Generating a random game state as a training example
            printGameState(trainingExample);
            for (int i = 0; i < trainingExample.length; i++) {                                                          //Inputting the training example into the neural network
                if (trainingExample[i] == State.X)
                    neuronValuesMatrices.get(0).set(0, i, 1);
                else if (trainingExample[i] == State.O)
                    neuronValuesMatrices.get(0).set(0, i, -1);
                else if (trainingExample[i] == State.I)
                    neuronValuesMatrices.get(0).set(0, i, 0);
            }

            ArrayList<Integer> optimalMoveSet = findOptimalMoveSet(trainingExample);
            SimpleMatrix perfectOutput = new SimpleMatrix(1, outputs);
            perfectOutput.fill(0);
            for (int output = 0; output < outputs; output++) {
                if (optimalMoveSet.contains(output))
                    perfectOutput.set(0, output, 1);
            }

            ArrayList<SimpleMatrix> zs = new ArrayList<>();
            for (int layer = 0; layer < structure.length - 1; layer++) {                                                //Forward propagation
                SimpleMatrix z = neuronValuesMatrices.get(layer).mult(synapseWeightsMatrices.get(layer));
                zs.add(z);
                SimpleMatrix finalZ = z.copy();
                for (int column = 0; column < z.getNumCols(); column++) {
                    if (layer == structure.length - 2)
                        finalZ.set(0, column, sigmoid(z.get(0, column)));                                          //Use the ReLU function for the last layer
                    else
                        finalZ.set(0, column, sigmoid(z.get(0, column)));                                          //Use the sigmoid function for all the other layers
                }
                neuronValuesMatrices.set(layer + 1, finalZ);
            }

            System.out.println("The resultant output layer neuron values:");
            neuronValuesMatrices.get(neuronValuesMatrices.size() - 1).print();

            double cost = 0;                                                                                            //Calculating the total cost for the processed training example
            for (int output = 0; output < outputs; output++) {
                cost += 0.5 * Math.pow(perfectOutput.get(output) - neuronValuesMatrices.get(neuronValuesMatrices.size() - 1).get(0, output), 2);
            }
            System.out.println("Cost: " + cost);


            ArrayList<SimpleMatrix> synapseDerivativesMatrices = new ArrayList<>();                                     //Backpropagation
            SimpleMatrix dWLastLayer = synapseWeightsMatrices.get(synapseWeightsMatrices.size()-1).createLike();

            SimpleMatrix zSigmoidDerivative = zs.get(zs.size()-1);
            for (int column = 0; column < zSigmoidDerivative.getNumCols(); column++)
                zSigmoidDerivative.set(0, column, sigmoid_derivative(zs.get(0, column)));
            double lastLayerDerivative = (neuronValuesMatrices.get(layers-1).minus(perfectOutput)).mult(zSigmoidDerivative).mult(neuronValuesMatrices.get());
        }
        
//        while (true) {
//            State[] randomState = getRandomNonTerminalGameState();
//            printGameState(randomState);
//            findOptimalMoveSet(randomState);
//            userConfirmation();
//        }
        
//        State[] gameState = new State[]{State.I, State.I, State.I,
//                                        State.I, State.I, State.I,
//                                        State.I, State.I, State.I};
//        printGameState(gameState);
//        while (Value(gameState) == -2) {
//            int move;
//            if (getPlayer(gameState) == State.X) {
//                BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
//                try {
//                    move = Integer.parseInt(stream.readLine())-1;
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//            } else {
//                ArrayList<Integer> optimalMoveSet = MinimaxAlgorithm.findOptimalMoveSet(gameState);
//                move = optimalMoveSet.get(new Random().nextInt(optimalMoveSet.size()));
//            }
//            gameState = Result(gameState, move, getPlayer(gameState));
//            printGameState(gameState);
//        }
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
