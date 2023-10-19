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
        
        State[] trainingExample = getRandomNonTerminalGameState();                                                      //Generating a random game state as a training example
        for (int i = 0; i < trainingExample.length; i++) {                                                              //Inputting the training example into the neural network
            if (trainingExample[i] == State.X)
                neuronValuesMatrices.get(0).set(0, i, 1);
            else if (trainingExample[i] == State.O)
                neuronValuesMatrices.get(0).set(0, i, -1);
            else if (trainingExample[i] == State.I)
                neuronValuesMatrices.get(0).set(0, i, 0);
        }
        
        ArrayList<Integer> optimalMoveSet = findOptimalMoveSet(trainingExample);
        
        SimpleMatrix z1 = neuronValuesMatrices.get(0).mult(synapseWeightsMatrices.get(0));
//        for (int i)
        neuronValuesMatrices.set(1, );
        
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
}
