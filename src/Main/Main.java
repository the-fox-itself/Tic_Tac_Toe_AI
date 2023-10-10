package Main;

import org.ejml.simple.SimpleMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

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
        
        State[] gameState = new State[]{State.I, State.I, State.I,
                                        State.I, State.I, State.I,
                                        State.I, State.I, State.I};
        printGameState(gameState);
        while (Value(gameState) == -2) {
            int move;
            if (getPlayer(gameState) == State.X) {
                BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
                try {
                    move = Integer.parseInt(stream.readLine())-1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
            } else {
                ArrayList<Integer> optimalMoveSet = MinimaxAlgorithm.findOptimalMoveSet(gameState);
                move = optimalMoveSet.get(new Random().nextInt(optimalMoveSet.size()));
            }
            gameState = Result(gameState, move, getPlayer(gameState));
            printGameState(gameState);
        }
        
//        SimpleMatrix matrix1 = new SimpleMatrix(new double[][]{{1, 2, 3}});
//        SimpleMatrix matrix2 = new SimpleMatrix(new double[][]{{1}, {2}, {3}});
//        matrix1.mult(matrix2).print();
//        matrix2.mult(matrix1).print();
    }
}
