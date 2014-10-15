package offset.oct13_group4;


import java.util.*;

import offset.oct13_group4.Board;
import offset.oct13_group4.MoveSequence;
import offset.oct13_group4.Move;
import offset.oct13_group4.Coord;
import offset.sim.Pair;
import offset.sim.Point;

public class MoveSequenceAnalysis {
	final static int MAX_DEPTH = 2;
	
	private Board board;

	private ArrayList<MoveSequence> allMoveSequences;
	private HashMap<String, ArrayList<MoveSequence>> moveSequencesByStart;
	private HashMap<String, ArrayList<MoveSequence>> moveSequencesByEnd;
	
	public MoveSequenceAnalysis(Board board) {
		this.board = new Board(board);
		this.allMoveSequences = new ArrayList<MoveSequence>();
		this.moveSequencesByStart = new HashMap<String, ArrayList<MoveSequence>>();
		this.moveSequencesByEnd = new HashMap<String, ArrayList<MoveSequence>>();
	}
	
	// Generate all of the sequences that double each cell's value, and store them
	public void analyze(int playerId, Pair pr) {
		for (int x = 0; x < board.size; x++) {
			for (int y = 0; y < board.size; y++) {
				ArrayList<MoveSequence> moveSequences = movesToDoubleValue(board, new Coord(x,y), pr, playerId, 0);
				
				if (moveSequences != null) {
					for (MoveSequence moveSequence : moveSequences) {
						moveSequence.coinSwing = (moveSequence.board.scores[playerId] - board.scores[playerId]) - (moveSequence.board.scores[1-playerId] - board.scores[1-playerId]);
						
						Move firstMove = moveSequence.moves.get(0);
						Move lastMove = moveSequence.moves.get(moveSequence.moves.size() - 1);

						ArrayList<MoveSequence> moveSequencesStartingAt = moveSequencesByStart.get(firstMove.toString());
						if (moveSequencesStartingAt == null)
							moveSequencesStartingAt = new ArrayList<MoveSequence>();
						
						moveSequencesStartingAt.add(moveSequence);
						moveSequencesByStart.put(firstMove.toString(), moveSequencesStartingAt);
						
						ArrayList<MoveSequence> moveSequencesEndingAt = moveSequencesByEnd.get(lastMove.toString());
						if (moveSequencesEndingAt == null)
							moveSequencesEndingAt = new ArrayList<MoveSequence>();
						
						moveSequencesEndingAt.add(moveSequence);
						moveSequencesByEnd.put(lastMove.toString(), moveSequencesEndingAt);

						allMoveSequences.add(moveSequence);
					}
				}
			}
		}
	}
	
	public ArrayList<MoveSequence> getAllMoveSequences() {
		return allMoveSequences;
	}
	
	public ArrayList<MoveSequence> getAllDisruptibleMoveSequences(Move move, Pair pair) {
		ArrayList<MoveSequence> moveSequencesFiltered = new ArrayList<MoveSequence>();
		
		for (MoveSequence moveSequence : allMoveSequences) {
			if (moveSequence.isDisruptedBy(board, move))
				moveSequencesFiltered.add(moveSequence);
		}
		
		return moveSequencesFiltered;
	}
	
	public ArrayList<MoveSequence> getMoveSequencesByStart(Move move) {
		return moveSequencesByStart.get(move.toString());
	}
	
	public ArrayList<MoveSequence> getMoveSequencesByEnd(Move move) {
		return moveSequencesByEnd.get(move.toString());
	}
	
	public ArrayList<MoveSequence> getNonDisruptibleMoveSequencesByStart(Move move, Pair pairOpponent) {
		ArrayList<MoveSequence> moveSequences = getMoveSequencesByStart(move);
		ArrayList<MoveSequence> nonDisruptibleMoveSequences = new ArrayList<MoveSequence>();
		
		for (MoveSequence moveSequence : moveSequences)
			if (!moveSequence.isDisruptible(board, pairOpponent))
				nonDisruptibleMoveSequences.add(moveSequence);
		
		return nonDisruptibleMoveSequences;
	}
	
	// Returns all sequences of moves such that, after the moves, the point at coordinate c has value value
	private ArrayList<MoveSequence> movesToMakeValue(Board board, Coord c, int value, Pair pair, int playerId, int depth) {
		Point p = board.getPoint(c);
		
		if (p.value == value) {
			MoveSequence moveSequence = new MoveSequence(board, pair);
			ArrayList<MoveSequence> moveSequences = new ArrayList<MoveSequence>();
			moveSequences.add(moveSequence);			
			return moveSequences;						// No moves needed, so sequences contains a single sequence with no moves
		} else if (p.value == 0 || p.value > value) {
			return null;								// Impossible to get p to have value
		} else {
			ArrayList<MoveSequence> moveSequences = new ArrayList<MoveSequence>();
			int currentValue = p.value;
			
			while (currentValue < value) {	
				if (moveSequences.isEmpty()) {
					ArrayList<MoveSequence> moveSequencesToDoubleValue = movesToDoubleValue(board, c, pair, playerId, depth);
					
					if (moveSequencesToDoubleValue != null) {
						for (MoveSequence moveSequenceToDoubleValue : moveSequencesToDoubleValue)
								moveSequences.add(moveSequenceToDoubleValue);
						
						currentValue *= 2;
					} else {
						return null;
					}
				} else {
					boolean canDoubleValue = false;
					
					ArrayList<MoveSequence> newMoveSequences = new ArrayList<MoveSequence>();
					
					for (MoveSequence moveSequence : moveSequences) {
						ArrayList<MoveSequence> moveSequencesToDoubleValue = movesToDoubleValue(moveSequence.board, c, pair, playerId, depth);
						
						if (moveSequencesToDoubleValue != null) {
							canDoubleValue = true;
							
							for (MoveSequence moveSequenceToDoubleValue : moveSequencesToDoubleValue) {
								MoveSequence newMoveSequence = new MoveSequence(board, pair);
								newMoveSequence.moves.addAll(moveSequence.moves);
								newMoveSequence.moves.addAll(moveSequenceToDoubleValue.moves);
								
								for (Move move : newMoveSequence.moves)
									newMoveSequence.board.processMove(move);
								
								newMoveSequences.add(newMoveSequence);
							}
						}
					}
					
					moveSequences = newMoveSequences;
					
					if (canDoubleValue) {
						currentValue *= 2;
					} else {
						return null;
					}
				}				
			}
			
			return moveSequences;
		}
	}
	
	// Returns an array of all move sequences such that the value of the point at the given coordinate is doubled, i.e., one combination
	// Includes the combination move itself as the last move in each list, with the given point receiving the coins.  (Of course, this final move can be reversed if desired)
	private ArrayList<MoveSequence> movesToDoubleValue(Board board, Coord c, Pair pair, int playerId, int depth) {
		Point p = board.getPoint(c);
		
		if (p.value == 0 || depth > MAX_DEPTH)
			return null;			// Cannot increase the value if it is already zero
		
		ArrayList<MoveSequence> moveSequences = new ArrayList<MoveSequence>();
		
		ArrayList<Coord> neighbors = board.neighborsOf(c, pair);
		
		for (Coord n : neighbors) {
			ArrayList<MoveSequence> moveSequencesToMakeValue = movesToMakeValue(board, n, p.value, pair, playerId, depth + 1);
			
			if (moveSequencesToMakeValue != null) {
				for (MoveSequence moveSequenceToMakeValue : moveSequencesToMakeValue) {
					moveSequenceToMakeValue.moves.add(new Move(n, c, playerId));
					moveSequences.add(moveSequenceToMakeValue);
				}
			}
		}
		
		if (moveSequences.isEmpty()) {
			return null;
		} else {
			for (MoveSequence moveSequence : moveSequences) {
				moveSequence.board = new Board(board);
				
				for (Move move : moveSequence.moves)
					moveSequence.board.processMove(move);
			}
			
			return moveSequences;
		}
	}
}
