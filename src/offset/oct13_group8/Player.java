package offset.oct13_group8;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	static int size = 32;
	static int MAX_DEPTH = 2;
	static int opponent_id;
	static int MAX_MOVES_TO_CHECK = 10;
	Pair opponentPr;
	boolean initiated = false;
	int expandedNodes = 0;
	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {
		if (id == 1)
			opponent_id = 0;
		else
			opponent_id = 1;
	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		if (!initiated) {
			init();
		}
		GameState startState = new GameState(grid, pr, pr0, id, opponent_id);
		movePair rtn=null;
		
		// when it reaches 125 ticks, each tick adds two input to the history
		if(history.size()<250){
			rtn=startState.lowerOpponentMoves(grid, pr, pr0);
		}else{
			rtn = makeDecision(startState, pr, pr0);
		}
		
		//sometimes makeDecision returns null, it has a bug that's why this added
		if(rtn==null){
			rtn=startState.lowerOpponentMoves(grid, pr, pr0);
		}
		System.out.println(expandedNodes);
		return rtn;
	}
	
	public movePair makeDecision(GameState startState, Pair pr, Pair pr0) {
		opponentPr = pr0;
        movePair result = null;
        int resultValue = Integer.MIN_VALUE;
        List<movePair> moves = startState.getAvailableMoves(pr, id);
        int moveNo = 0;
        for (movePair move : moves) {
        	moveNo++;
        	if (moveNo > MAX_MOVES_TO_CHECK)
        		break;
        	GameState newState = new GameState(startState);
        	newState.makeMove(move, id);
            int value = minValue(newState, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            if (value > resultValue) {
                    result = move;
                    resultValue = value;
            }
        }
        return result;
	}
	
	public int maxValue(GameState startState, double alpha, double beta, int depth) {
		expandedNodes++;
        if (depth == MAX_DEPTH)
            return startState.playerScore;
        int value = Integer.MIN_VALUE;
        List<movePair> moves = startState.getAvailableMoves(pr, id);
        if (moves.isEmpty())
        	return startState.playerScore;
        int moveNo = 0;
        for (movePair move : moves) {
        	moveNo++;
        	if (moveNo > MAX_MOVES_TO_CHECK)
        		break;
        	GameState newState = new GameState(startState);
        	newState.makeMove(move, id);
            value = Math.max(value, minValue(newState, alpha, beta, depth + 1));
            if (value >= beta)
                    return value;
            alpha = Math.max(alpha, value);
        }
        return value;
	}

	public int minValue(GameState startState, double alpha, double beta, int depth) {
		expandedNodes++;
		if (depth == MAX_DEPTH)
			return startState.opponentScore;
        int value = Integer.MAX_VALUE;
        List<movePair> moves = startState.getAvailableMoves(pr, id);
        if (moves.isEmpty())
        	return startState.opponentScore;
        int moveNo = 0;
        for (movePair move : moves) {
        	moveNo++;
        	if (moveNo > MAX_MOVES_TO_CHECK)
        		break;
        	GameState newState = new GameState(startState);
        	newState.makeMove(move, id);
            value = Math.min(value, maxValue(newState, alpha, beta, depth + 1));
            if (value <= alpha)
                    return value;
            beta = Math.min(beta, value);
        }
        return value;
	}
}
