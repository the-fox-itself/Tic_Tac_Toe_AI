package Main;

import java.util.ArrayList;

public abstract class MinimaxAlgorithm {
    public enum State {I, X, O}
    public static State BOT;
    public static State ENEMY;
    
    public static ArrayList<Integer> findOptimalMoveSet(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        
        BOT = getPlayer(gameState);
        ENEMY = (BOT == State.X) ? State.O : State.X;
        
        double value = Value(gameState);
        if (value != -2)
            throw new RuntimeException();
        
        ArrayList<Integer> optimalMoves = new ArrayList<>();
        
        value = -999;
        for (Integer actionIndex : Actions(gameState)) {
            double actionValue = getGameStateValue(Result(gameState, actionIndex, BOT));
            if (actionValue > value) {
                value = actionValue;
                optimalMoves = new ArrayList<>();
                optimalMoves.add(actionIndex);
            } else if (actionValue == value) {
                optimalMoves.add(actionIndex);
            }
        }
        System.out.println("Best value: " + value + ". Optimal moves: " + optimalMoves);
        return optimalMoves;
    }
    
    public static double getGameStateValue(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
    
        double value = Value(gameState);
        if (value != -2) {
            return (value*0.5);
        }
        
        State player = getPlayer(gameState);
        if (player == BOT) {
            value = -999.0;
            for (Integer actionIndex : Actions(gameState)) {
                State[] result = Result(gameState, actionIndex, BOT);
                value = Math.max(value, getGameStateValue(result));
            }
            return (value*0.5);
        }
        if (player == ENEMY) {
            value = 999.0;
            for (Integer actionIndex : Actions(gameState)) {
                State[] result = Result(gameState, actionIndex, ENEMY);
                value = Math.min(value, getGameStateValue(result));
            }
            return (value*0.5);
        }
        
        throw new RuntimeException();
    }
    
    public static int Value(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        
        if (    (gameState[0] == BOT && gameState[1] == BOT && gameState[2] == BOT) ||
                (gameState[3] == BOT && gameState[4] == BOT && gameState[5] == BOT) ||
                (gameState[6] == BOT && gameState[7] == BOT && gameState[8] == BOT) ||
                (gameState[0] == BOT && gameState[3] == BOT && gameState[6] == BOT) ||
                (gameState[1] == BOT && gameState[4] == BOT && gameState[7] == BOT) ||
                (gameState[2] == BOT && gameState[5] == BOT && gameState[8] == BOT) ||
                (gameState[0] == BOT && gameState[4] == BOT && gameState[8] == BOT) ||
                (gameState[2] == BOT && gameState[4] == BOT && gameState[6] == BOT)) {
            return 1;
        } else if ( (gameState[0] == ENEMY && gameState[1] == ENEMY && gameState[2] == ENEMY) ||
                    (gameState[3] == ENEMY && gameState[4] == ENEMY && gameState[5] == ENEMY) ||
                    (gameState[6] == ENEMY && gameState[7] == ENEMY && gameState[8] == ENEMY) ||
                    (gameState[0] == ENEMY && gameState[3] == ENEMY && gameState[6] == ENEMY) ||
                    (gameState[1] == ENEMY && gameState[4] == ENEMY && gameState[7] == ENEMY) ||
                    (gameState[2] == ENEMY && gameState[5] == ENEMY && gameState[8] == ENEMY) ||
                    (gameState[0] == ENEMY && gameState[4] == ENEMY && gameState[8] == ENEMY) ||
                    (gameState[2] == ENEMY && gameState[4] == ENEMY && gameState[6] == ENEMY)) {
            return -1;
        } else if (gameState[0] != State.I && gameState[1] != State.I && gameState[2] != State.I &&
                gameState[3]!= State.I && gameState[4] != State.I && gameState[5] != State.I &&
                gameState[6] != State.I && gameState[7] != State.I && gameState[8] != State.I) {
            return 0;
        } else {
            return -2;
        }
    }
    
    public static State getPlayer(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        
        int totalX = 0;
        int totalO = 0;
        for (State state : gameState) {
            if (state == State.X)
                totalX++;
            if (state == State.O)
                totalO++;
        }
        if (totalX == totalO)
            return State.X;
        if (totalX-totalO == 1)
            return State.O;
        throw new RuntimeException();
    }
    
    public static ArrayList<Integer> Actions(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        
        ArrayList<Integer> actionIndexes = new ArrayList<>();
        for (int i = 0; i < gameState.length; i++) {
            if (gameState[i] == State.I)
                actionIndexes.add(i);
        }
        return actionIndexes;
    }
    
    public static State[] Result(State[] gameState, int moveIndex, State player) {
        if (gameState.length != 9)
            throw new RuntimeException();
        if (moveIndex < 0 || moveIndex > 8)
            throw new RuntimeException();
        if (player == State.I)
            throw new RuntimeException();
        if (gameState[moveIndex] != State.I) {
            throw new RuntimeException();
        }
        
        State[] resultState = cloneGameState(gameState);
        resultState[moveIndex] = player;
        return resultState;
    }
    
    public static State[] cloneGameState(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        State[] cloneState = new State[9];
        System.arraycopy(gameState, 0, cloneState, 0, 9);
        return cloneState;
    }
    
    public static void printGameState(State[] gameState) {
        if (gameState.length != 9)
            throw new RuntimeException();
        for (int i = 0; i < 9; i++) {
            if (gameState[i] == State.I)
                System.out.print(". ");
            else
                System.out.print(gameState[i] + " ");
            if (i % 3 == 2)
                System.out.println();
        }
        System.out.println();
        
    }
}
