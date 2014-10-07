package offset.oct6_group4;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;

public class Board {
	ArrayList<Point> grid;
	int size;
	int[] scores = new int[2];
	
	// CONSTRUCTORS
	Board(int size, Point grid[]) {
		this.size = size;
		
		this.grid = new ArrayList<Point>();
		this.setGrid(grid);
	}
	
	Board(Board board) {
		this.size = board.size;
		this.scores[0] = board.scores[0];
		this.scores[1] = board.scores[1];
		
		this.grid = new ArrayList<Point>();
		for (int i = 0; i < board.grid.size(); i++)
			this.grid.add(new Point(board.grid.get(i)));
	}
	
	// PRIVATE METHODS
	
	
	// PUBLIC METHODS	
	public void setGrid(Point grid[]) {
		this.scores[0] = 0;
		this.scores[1] = 0;
		
		for (int i = 0; i < grid.length; i++) {
			this.grid.add(new Point(grid[i]));
			
			if (grid[i].owner >= 0)
				this.scores[grid[i].owner] += grid[i].value; 
		}
	}
	
	// Updates the grid based on a move performed by a player
	// Assumes that the move is valid
	public void processMove(int xSrc, int ySrc, int xTarget, int yTarget, int playerId) {
		Point src = grid.get(xSrc*size + ySrc);
	
		if (src.owner >= 0)
			scores[src.owner] -= src.value;
		
		src.value = 0;
		src.owner = -1;

		Point target = grid.get(xTarget*size + yTarget);
		
		if (target.owner >= 0)
			scores[target.owner] -= target.value;

		target.value *= 2;
		target.owner = playerId;
		
		scores[playerId] += target.value;
	}
	
	public void processMove(Coord src, Coord target, int player) {
		processMove(src.x, src.y, target.x, target.y, player);
	}
	
	public void processMove(Move move) {
		processMove(move.src, move.target, move.playerId);
	}
	
	public Point getPoint(int x, int y) {
		return new Point(grid.get(x*size + y));		// Return a copy so user cannot modify the board
	}
	
	public Point getPoint(Coord c) {
		return getPoint(c.x, c.y);
	}
	
	// Given a point and an offset pair, returns an ArrayList of neighbors of that point that are on the board
	public ArrayList<Coord> neighborsOf(int x, int y, Pair pr) {
		ArrayList<Coord> neighbors = new ArrayList<Coord>();
		int p;
		int q;

		for (int k = 0; k <= 1; k++) {
			p = k == 0 ? pr.p : pr.q;
			q = k == 0 ? pr.q : pr.p;

			for(int i = -1; i <= 1; i += 2) {
				for(int j = -1; j <= 1; j += 2) {
					if (isInBounds(x + p*i, y + q*j))
						neighbors.add(new Coord(x + p*i, y + q*j));
				}
			}
		}

		return neighbors;
	}
	
	public ArrayList<Coord> neighborsOf(Coord c, Pair pr) {
		return neighborsOf(c.x, c.y, pr);
	}
	
	public ArrayList<Coord> validMovesFrom(int x, int y, Pair pr) {
		ArrayList<Coord> neighbors = neighborsOf(x, y, pr);
		ArrayList<Coord> validMoves = new ArrayList<Coord>();
		
		for (Coord c : neighbors) {
			if (isMoveValid(c.x, c.y, x, y, pr))
				validMoves.add(new Coord(c.x, c.y));
		}
		
		return validMoves;
	}

	public ArrayList<Coord> validMovesFrom(Coord c, Pair pr) {
		return validMovesFrom(c.x, c.y, pr);
	}
	
	public boolean isInBounds(int x, int y) {
		return (x >= 0 && x < this.size && y >= 0 && y < this.size);
	}
	
	public boolean isInBounds(Coord c) {
		return isInBounds(c.x, c.y);
	}
	
	public boolean isMoveValid(int x1, int y1, int x2, int y2, Pair pr) {
		return (isInBounds(x1, y1) && isInBounds(x2, y2) && 
				((Math.abs(x1 - x2) == pr.p && Math.abs(y1 - y2) == pr.q) || (Math.abs(x1 - x2) == pr.q && Math.abs(y1 - y2) == pr.p)) &&
				getPoint(x1, y1).value == getPoint(x2, y2).value &&
				getPoint(x1, y1).value > 0);
	}
	
	public boolean isMoveValid(Coord src, Coord target, Pair pr) {
		return isMoveValid(src.x, src.y, target.x, target.y, pr);
	}
	
	public boolean isMoveValid(Move move, Pair pr) {
		return isMoveValid(move.src, move.target, pr);
	}
	
	public int distBetween(int x1, int y1, int x2, int y2) {
		return (Math.abs(x1 - x2) + Math.abs(y1 - y2));
	}
	
	public int distBetween(Coord c1, Coord c2) {
		return distBetween(c1.x, c1.y, c2.x, c2.y);
	}
		
	public double distFromCenter(int x, int y) {
		return (Math.abs(x - (double) size/2) + Math.abs(y - (double) size/2));
	}
	
	public double distFromCenter(Coord c) {
		return distFromCenter(c.x, c.y);
	}
	
	@Override public String toString() {
		String str = "";
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++)
				str = str + getPoint(x,y).value + " ";
				
			str = str + "\n";
		}
		
		return str;
	}
}
