package offset.oct6_group5;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	private final int self = id;
	private final int size = 32;

	private final int STEAL_BOTH = 2;
	private final int STEAL_ONE  = 1;
	private final int STEAL_NONE = 0;

	private Point[][] board = new Point[size][size];
	private Pair selfPair = pr;
	private Pair opponentPair;

	List<movePair> my_legal_moves = new ArrayList<>();
	List<movePair> opponent_legal_moves = new ArrayList<>();

	public Player(Pair pair, int id) {
		super(pair, id);
	}

	public void init() {
	}

	// 1. Generate a priority set of moves, A, which depends upon if you take both, either, or neither (worst case) set of coins from the opponent
	// 2. From A, select the top N moves which cut off most of the moves of the opponent
	// 3. From the top N moves generated in step 2, choose the move which gets you the maximum number of coins
	public movePair move(Point[] grid, Pair pair0, Pair pair1, ArrayList<ArrayList> history) {
		updateBoard(grid);
		opponentPair = pair1; // pair0 is the same with selfPair

		Move[] moves = getAllValidMoves();
		moves = getBestStealMoves(moves);
		moves = getCutoffMoves(moves, 10);
		Move move = getLargestMove(moves);

		if (validMove(move))
			move.flag = true;
		return move.toMovePair();
	}

	// get all valid moves
	private Move[] getAllValidMoves(Pair pair) {
		ArrayList<Move> moves = new ArrayList<Move>();
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				Move[] cellMoves = getAllValidMovesFrom(board[i][j], pair);
				for (Move move : cellMoves)
					moves.add(move);
			}
		}
		return moves.toArray(new Move[moves.size()]);
	}

	private Move[] getAllValidMoves() {
		return getAllValidMoves(selfPair);
	}

	// return valid moves from one cell (either as source or as target)
	private Move[] getAllValidMovesFrom(Point p, Pair pair) {
		ArrayList<Move> moves = new ArrayList<Move>();
		final int[] cx = {-1, 1, -1, 1, -1, 1, -1, 1};
		final int[] cy = {-1, -1, 1, 1, -1, -1, 1, 1};
		for (int i = 0; i < 8; ++i) {
			int cp = pair.p, cq = pair.q;
			if (i >= 4) {
				cp = pair.q;
				cq = pair.p;
			}
			int xx = p.x + cx[i] * cp;
			int yy = p.y + cy[i] * cq;
			if (onBoard(xx, yy)) {
				Move move = new Move(board[p.x][p.y], board[xx][yy]);
				if (validMove(move, pair))
					moves.add(move);
			}
		}
		return moves.toArray(new Move[moves.size()]);
	}

	private Move[] getAllValidMovesFrom(Point p) {
		return getAllValidMovesFrom(p, selfPair);
	}

	// given a move, return how many cells from which it steals
	private int getStealScore(Move move) {
		Point src = move.src;
		Point target = move.target;
		if (src.value == 1) // TODO: How should we score the 1's case?
			return STEAL_NONE;
		if (src.owner != self && target.owner != self)
			return STEAL_BOTH;
		else if (src.owner != self || target.owner != self)
			return STEAL_ONE;
		else
			return STEAL_NONE;
	}

	// filter to get moves that steal from most cells
	private Move[] getBestStealMoves(Move[] allMoves) {
		ArrayList<Move> moves = new ArrayList<Move>();
		int bestScore = 0;
		for (Move move : allMoves) {
			int tempScore = getStealScore(move);
			if (tempScore > bestScore) {
				moves.clear();
				bestScore = tempScore;
			}
			if (tempScore == bestScore)
				moves.add(move);
		}
		return moves.toArray(new Move[moves.size()]);
	}

	// filter to get limit number of moves that cutoff the most
	private Move[] getCutoffMoves(Move[] allMoves, int limit) {
		PriorityQueue<Move> queue = new PriorityQueue<Move>(limit, new MoveComparator());
		for (Move move : allMoves) {
			Point src = move.src, target = move.target;
			int oldMoveNum = getAllValidMovesFrom(src, opponentPair).length + getAllValidMovesFrom(target, opponentPair).length;
			board[src.x][src.y].value = 0;
			board[target.x][target.y].value *= 2;
			int newMoveNum = getAllValidMovesFrom(src, opponentPair).length + getAllValidMovesFrom(target, opponentPair).length;
			board[src.x][src.y].value = board[target.x][target.y].value / 2;
			board[target.x][target.y].value /= 2;
			move.delta = oldMoveNum - newMoveNum;
			if (queue.size() < limit || queue.peek().delta < move.delta) {
				if (queue.size() >= limit)
					queue.poll();
				queue.add(move);
			}
		}
		return queue.toArray(new Move[queue.size()]);
	}

	// get the score of a move
	private int scoreMove(Move move) {
		return move.src.value;
	}

	// get the move generating largest score
	private Move getLargestMove(Move[] moves) {
		Move bestMove = new Move();
		int bestScore = 0;
		for (Move move : moves) {
			int tempScore = scoreMove(move);
			if (tempScore > bestScore) {
				bestMove = new Move(move);
			}
		}
		return bestMove;
	}

	/////////// UTILITY METHODS ///////////

	// a better movePair class with copy constructor
	private class Move {
		public Point src;
		public Point target;
		public boolean flag;
		public int delta;

		Move() {
			src = new Point();
			target = new Point();
			flag = false;
		}

		Move(Point src, Point target) {
			this.src = src;
			this.target = target;
			this.flag = false;
		}

		Move(Move move) {
			src = move.src;
			target = move.target;
			flag = move.flag;
		}

		Move(movePair move) {
			src = move.src;
			target = move.target;
			flag = move.move;
		}

		public movePair toMovePair() {
			return new movePair(flag, src, target);
		}
	}

	// comparator for move.delta
	private class MoveComparator implements Comparator<Move> {
		@Override
		public int compare(Move a, Move b) {
			return (a.delta - b.delta);
		}
	}

	// update 2D board[][] with Point array grid[]
	private void updateBoard(Point[] grid) {
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				board[i][j] = grid[i * size + j];
			}
		}
	}

	// check if a point is on board
	private boolean onBoard(int x, int y) {
		return (x >= 0 && x < size && y >= 0 && y < size);
	}

	private boolean onBoard(Point p) {
		return onBoard(p.x, p.y);
	}

	// generic method of checking if a move is valid
	private boolean validMove(Point src, Point target, Pair pair) {
		if (onBoard(src) && onBoard(target) &&
			board[src.x][src.y].value != 0 && board[src.x][src.y].value == board[target.x][target.y].value &&
			(Math.abs(src.x - target.x) == pair.p && Math.abs(src.y - target.y) == pair.q ||
				Math.abs(src.x - target.x) == pair.q && Math.abs(src.y - target.y) == pair.p))
			return true;
		return false;
	}

	// check if a move is valid given a Pair
	private boolean validMove(Move move, Pair pair) {
		return validMove(move.src, move.target, pair);
	}

	// check if a move is valid using selfPair
	private boolean validMove(Move move) {
		return validMove(move, selfPair);
	}
}
