package offset.oct6_group4;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	private int size = 32;
	
	public Player(Pair prin, int idin) {
		super(prin, idin);
	}
	
	///////////////////////// PRIVATE CLASSES /////////////////////////

	
	///////////////////////// PRIVATE VARIABLES /////////////////////////
	Board board;
	boolean didSetup = false;
	
	///////////////////////// SETUP /////////////////////////
	public void init() {
		
	}
	
	///////////////////////// STRATEGY: CORNERS /////////////////////////
	// Choose move based on shortest Manhattan distance from the corners
	private Move cornerStrategy() {
		for (int d = 0; d < size; d++) {
			for (int n = 0; n <= d; n++) {
				int x = d - n;
				int y = n;
				
				for (int c = 0; c < 4; c++) {					
					ArrayList<Coord> validMoves = board.validMovesFrom(x, y, pr);
					
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
	private Move searchStrategy() {
		Move move = null;
		double maxCoinsPerMove = 0;
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Point p = board.getPoint(x, y);
				
				// Only focus on capturing neutral or opponent coins
				if (p.owner != id) {
					ArrayList<Move> moves = movesToDoubleValue(new Board(board), new Coord(x,y), pr, id);
					
					if (moves != null) {
						double coinsPerMove = p.value / moves.size();
						
						if (coinsPerMove > maxCoinsPerMove) {
							maxCoinsPerMove = coinsPerMove;
							move = moves.get(0);			// Actually make the first move in the sequence
						}
					}
				}
			}
		}
		
		// If we can't capture neutral or opponent coins, just make some valid move
		if (move == null) {
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					ArrayList<Coord> validMoves = board.validMovesFrom(x, y, pr);
					
					if (!validMoves.isEmpty()) {
						move = new Move(x, y, validMoves.get(0).x, validMoves.get(0).y, id);
					}
				}
			}
		}
		
		return move;
	}
	
	// Returns a shortest list of moves such that, after the moves, the point at coordinate c has value value
	private ArrayList<Move> movesToMakeValue(Board board, Coord c, int value, Pair pair, int playerId) {
		Point p = board.getPoint(c);
		
		if (p.value == value) {
			return new ArrayList<Move>();				// No moves needed
		} else if (p.value == 0 || p.value > value) {
			return null;								// Impossible to get p to have value
		} else {
			Board newBoard = new Board(board);
			ArrayList<Move> movesList = new ArrayList<Move>();
			int currentValue = p.value;
			
			while (currentValue < value) {
				ArrayList<Move> moves = movesToDoubleValue(newBoard, c, pair, playerId);
				
				if (moves != null) {
					movesList.addAll(moves);
					currentValue = newBoard.getPoint(c).value;
				} else {
					return null;
				}
			}
			
			return movesList;
		}
	}
	
	// Returns a shortest list of moves such that the value of the point at the given coordinate is doubled, i.e., one combination
	// Includes the combination move itself as the last move in the list, with the given point receiving the coins.  (Of course, this final move can be reversed if desired)
	// Applies the moves to the provided board.  Thus when called externally, a copy of the board should be provided
	private ArrayList<Move> movesToDoubleValue(Board board, Coord c, Pair pair, int playerId) {
		Point p = board.getPoint(c);
		
		if (p.value == 0) {
			return null;			// Cannot increase the value if it is already zero
		}
		
		int minMoves = Integer.MAX_VALUE;
		ArrayList<Move> minMovesList = new ArrayList<Move>();
		
		ArrayList<Coord> neighbors = board.neighborsOf(c, pair);
		
		for (Coord n : neighbors) {
			ArrayList<Move> moves = movesToMakeValue(board, n, p.value, pair, playerId);
			
			if (moves != null) {
				moves.add(new Move(n, c, playerId));
				if (moves.size() < minMoves) {
					minMoves = moves.size();
					minMovesList = moves;
				}
			}
		}
		
		if (minMovesList.isEmpty()) {
			return null;
		} else {
			for (Move move : minMovesList) {
				board.processMove(move);
			}
			
			return minMovesList;
		}
	}
	
	
	///////////////////////// IMPLEMENT ABSTRACT CLASS MOVE METHOD /////////////////////////	
	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		// Setup our board class if this is the first time we have been called
		if (!didSetup) {
			board = new Board(size, grid);
			didSetup = true;
		} else {
			// Keep our board up to date by processing the most recent moves made by opponent (probably faster than copying the whole grid again)
			// An element in the history ArrayList is itself an ArrayList where the first element is the player id and the second is a movePair (each of which must be cast)
			int i = history.size() - 1;
			while (i >= 0 && (int) history.get(i).get(0) != id) {
				int player = (int) history.get(i).get(0);
				movePair mp = (movePair) history.get(i).get(1);
				if (mp.move)
					board.processMove(new Move(mp.src.x, mp.src.y, mp.target.x, mp.target.y, player));
				
				i--;
			}
		}
		
		// Call a strategy to actually determine the move to make
		Move move = searchStrategy();

		// Transform the resulting move from our representation to the simulator representation
		movePair movepr = new movePair();
		if (move != null) {
			board.processMove(move);
			
			movepr.move = true;
			movepr.src = grid[move.src.x*size + move.src.y];
			movepr.target = grid[move.target.x*size + move.target.y];
			return movepr;
		} else {
			movepr.move = false;
			return movepr;
		}
	}
}
