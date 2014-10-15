package offset.oct13_group4;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;
import offset.oct13_group4.Board;
import offset.oct13_group4.Move;
import offset.oct13_group4.MoveSequence;

public class Player extends offset.sim.Player {
	private int size = 32;
	
	public Player(Pair prin, int idin) {
		super(prin, idin);
	}
	
	///////////////////////// PRIVATE CLASSES /////////////////////////

	
	///////////////////////// PRIVATE VARIABLES /////////////////////////
	Board board;
	boolean didSetup = false;
	int idOpponent;
	Pair pairSelf;
	Pair pairOpponent;
	
	///////////////////////// SETUP /////////////////////////
	public void init() {
		
	}
	
	///////////////////////// STATEGY: RANDOM ///////////////////////////
	private Move randomStrategy() {
		ArrayList<Move> validMoves = board.validMoves(pairSelf, id);
		
		if (validMoves.isEmpty())
			return null;
		
		Random random = new Random();
		System.out.printf("Valid moves remaining: %d\n", validMoves.size());
		return validMoves.get(random.nextInt(validMoves.size()));
	}
	
	///////////////////////// STRATEGY: CORNERS /////////////////////////
	// Choose move based on shortest Manhattan distance from the corners
	private Move cornerStrategy() {
		for (int d = 0; d < size; d++) {
			for (int n = 0; n <= d; n++) {
				int x = d - n;
				int y = n;
				
				for (int c = 0; c < 4; c++) {					
					ArrayList<Coord> validMoves = board.validMovesFrom(x, y, pairSelf);
					
					// Return any valid moves from that point
					if (!validMoves.isEmpty())
						return new Move(validMoves.get(0).x, validMoves.get(0).y, x, y, id);
					
					// Rotate to the next corner
					int temp = y;
					y = x;
					x = size - temp - 1;
				}
			}
		}
		
		return null;
	}
	
	///////////////////////// STRATEGY: RECURSIVE MOVE SEARCH /////////////////////////
	private MoveSequence getMoveSequenceWithMaxCoinSwingPerMove(ArrayList<MoveSequence> moveSequences) {
		MoveSequence bestMoveSequence = null;
		double maxCoinSwingPerMove = 0;
		
		for (MoveSequence moveSequence : moveSequences) {
			if (moveSequence.coinSwing / moveSequence.moves.size() > maxCoinSwingPerMove) {
				bestMoveSequence = moveSequence;
				maxCoinSwingPerMove = moveSequence.coinSwing / moveSequence.moves.size();
			}
		}
		
		return bestMoveSequence;
	}
	
	private Move searchStrategy() {
		MoveSequenceAnalysis analysisSelf = new MoveSequenceAnalysis(board);
		MoveSequenceAnalysis analysisOpponent = new MoveSequenceAnalysis(board);
		
		analysisSelf.analyze(id, pairSelf);
		analysisOpponent.analyze(idOpponent, pairOpponent);

		// Generate all possible valid moves
		ArrayList<Move> validMoves = board.validMoves(pairSelf, id);
		
		if (validMoves.isEmpty())
			return null;
		
		// From among the possible valid moves, choose the one with the highest value according to our valuation methodology
		Move bestMove = null;
		double bestMoveScore = 0;
		
		for (Move move : validMoves) {
			// Weighted 'score' of the move in terms of its aggressiveness, defensiveness, and flexibility effects
			double score = 0;
			double agg = 0;
			double def = 0;
			double flex = 0;
			
			// Aggressiveness: Determine the sequence starting with this move with the highest ratio of coin swing / # moves
			ArrayList<MoveSequence> moveSequencesByStartSelf = analysisSelf.getNonDisruptibleMoveSequencesByStart(move, pairOpponent);
			MoveSequence moveSequenceWithMaxCoinSwingPerMove = getMoveSequenceWithMaxCoinSwingPerMove(moveSequencesByStartSelf);
			
			if (moveSequenceWithMaxCoinSwingPerMove != null)
				agg = (double) (moveSequenceWithMaxCoinSwingPerMove.coinSwing) / (double) moveSequenceWithMaxCoinSwingPerMove.moves.size();
			
			// Defensiveness: Determine the opponent move sequence that this move disrupts with the highest ratio of coin swing / # moves
			ArrayList<MoveSequence> moveSequencesOpponentDisruptible = analysisOpponent.getAllDisruptibleMoveSequences(move, pairSelf);
			MoveSequence moveSequenceOpponentWithMaxCoinSwingPerMove = getMoveSequenceWithMaxCoinSwingPerMove(moveSequencesOpponentDisruptible);
			
			if (moveSequenceOpponentWithMaxCoinSwingPerMove != null)
				def = ((double) (moveSequenceOpponentWithMaxCoinSwingPerMove.coinSwing) / (double) moveSequenceOpponentWithMaxCoinSwingPerMove.moves.size()) / ((double) analysisOpponent.getMoveSequencesByEnd(moveSequenceOpponentWithMaxCoinSwingPerMove.lastMove()).size());
			
			// Flexibility: Determine net # move impact to us and our opponent
			int deltaMovesSelf = board.numMovesDelta(move, pairSelf, id);
			int deltaMovesOpponent = board.numMovesDelta(move, pairOpponent, idOpponent);
			
			flex = (double) (-deltaMovesOpponent - -deltaMovesSelf);
			
			score = 1.00*agg + 0.00*def + 0.05*flex;
			
			if (score > bestMoveScore) {
				bestMove = move;
				bestMoveScore = score;
			}
		}
		
		if (bestMove == null) {
			return randomStrategy();
		} else {
			return bestMove;
		}
	}
	
	
	///////////////////////// IMPLEMENT ABSTRACT CLASS MOVE METHOD /////////////////////////	
	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		// Setup our board class if this is the first time we have been called
		if (!didSetup) {
			board = new Board(size, grid);
			pairSelf = pr;
			pairOpponent = pr0;
			idOpponent = 1 - id;
			didSetup = true;
		} else {
			/*// Keep our board up to date by processing the most recent moves made by opponent (probably faster than copying the whole grid again)
			// An element in the history ArrayList is itself an ArrayList where the first element is the player id and the second is a movePair (each of which must be cast)
			int i = history.size() - 1;
			while (i >= 0 && (int) history.get(i).get(0) != id) {
				int player = (int) history.get(i).get(0);
				movePair mp = (movePair) history.get(i).get(1);
				if (mp.move)
					board.processMove(new Move(mp.src.x, mp.src.y, mp.target.x, mp.target.y, player));
				
				i--;
			}*/
			board.updateGrid(grid);
		}
		
		// Call a strategy to actually determine the move to make
		Move move = searchStrategy();
		//Move move = randomStrategy();

		// Transform the resulting move from our representation to the simulator representation
		movePair movepr = new movePair();
		if (move != null) {
			System.out.printf("Playing as %d, playing move %s\n", id, move);
			board.processMove(move);
	
			movepr.move = true;
			movepr.src = grid[move.src.x*size + move.src.y];
			movepr.target = grid[move.target.x*size + move.target.y];
			return movepr;
		} else {
			System.out.printf("Playing as %d, passing\n", id);
			movepr.move = false;
			return movepr;
		}
	}
}
