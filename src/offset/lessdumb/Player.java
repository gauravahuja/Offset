package offset.lessdumb;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {

	}

    public Point getFromGrid(Point[] grid, int row, int col) {
        return grid[row * size + col];
    }

    public movePair getMovePairFromSrcTarget(Point[] grid, int start_row, int start_col, int end_row, int end_col) {
        movePair result = new movePair();

        result.src = getFromGrid(grid, start_row, start_col);
        result.target = getFromGrid(grid, end_row, end_col);

        return result;
    }

    public boolean onGrid(int row_or_col) {
        return row_or_col > 0 && row_or_col < size;
    }

    public List<movePair> getMovesFromSquare(Point[] grid, int row, int col, Pair pr) {
        List<movePair> moves = new ArrayList<movePair>();

        int p = pr.p;
        int q = pr.q;

        if (onGrid(row + p)) {
            if (onGrid(col + q)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row + p, col + q));
            }
            if (onGrid(col - q)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row + p, col - q));
            }
        }
        if (onGrid(row - p)) {
            if (onGrid(col + q)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row - p, col + q));
            }
            if (onGrid(col - q)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row - p, col - q));
            }
        }

        if (onGrid(row + q)) {
            if (onGrid(col + p)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row + q, col + p));
            }
            if (onGrid(col - p)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row + q, col - p));
            }
        }
        if (onGrid(row - q)) {
            if (onGrid(col + p)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row - q, col + p));
            }
            if (onGrid(col - p)) {
                moves.add(getMovePairFromSrcTarget(grid, row, col, row - q, col - p));
            }
        }

        return moves;
    }

    public List<movePair> getMoves(Point[] grid, Pair pr) {
        List<movePair> moves = new ArrayList<movePair>();

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
                moves.addAll(getMovesFromSquare(grid, i, j, pr));
            }
        }

        return moves;
    }

    public int scoreBoardAfterMove(Point[] grid, movePair move) {
        int score_before_move = 0;
        for (Point p : grid) {
            if (p.owner == id) {
                score_before_move += p.value;
            }
        }

        int score_from_move = 0;
        if (move.src.owner != id) {
            score_from_move += move.src.value;
        }
        if (move.target.owner != id) {
            score_from_move += move.target.value;
        }

        return score_from_move;
        //return score_before_move + score_from_move;
    }

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        List<movePair> moves = getMoves(grid, pr);

        movePair best  = null;
        int best_score = 0;
        for (movePair move : moves) {
            if (validateMove(move, pr)) {
                int score = scoreBoardAfterMove(grid, move);
                if (best == null || score > best_score) {
                    best_score = score;
                    best = move;
                    best.move = true;
                }
            }
        }

        if (best == null) {
            best = new movePair();
            best.move = false;
        }

        return best;
	}


    boolean validateMove(movePair movepr, Pair pr) {
    	
    	Point src = movepr.src;
    	Point target = movepr.target;
    	boolean rightposition = false;
    	if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    		rightposition = true;
    	}
    	//if (Math.abs(target.x-src.x)==Math.abs(pr.y) && Math.abs(target.y-src.y)==Math.abs(pr.x)) {
    		//rightposition = true;
    	//}
        if (rightposition  && src.value == target.value && src.value >0) {
        	return true;
        }
        else {
        	return false;
        }
    }
}
