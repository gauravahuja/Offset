package offset.oct13_group4;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.oct13_group4.Coord;
import offset.oct13_group4.Move;


public class Board {
	int size;
	int[] scores = new int[2];
	
	int[] owners;
	int[] values;
	
	// CONSTRUCTORS
	Board(int size, Point grid[]) {
		this.size = size;
		
		owners = new int[size*size];
		values = new int[size*size];
		
		this.updateGrid(grid);
	}
	
	Board(Board board) {
		this.size = board.size;
		this.scores[0] = board.scores[0];
		this.scores[1] = board.scores[1];
		
		this.owners = board.owners.clone();
		this.values = board.values.clone();
	}
	
	// PRIVATE METHODS
	
	
	// PUBLIC METHODS
	public void updateGrid(Point grid[]) {
		scores[0] = 0;
		scores[1] = 0;
		
		for (int i = 0; i < grid.length; i++) {
			this.owners[i] = grid[i].owner;
			this.values[i] = grid[i].value;
			
			if (grid[i].owner >= 0)
				this.scores[grid[i].owner] += grid[i].value;
		}	
	}
	
	// Updates the grid based on a move performed by a player
	// Assumes that the move is valid
	public void processMove(int xSrc, int ySrc, int xTarget, int yTarget, int playerId) {
		if (owners[xSrc*size + ySrc] >= 0)
			scores[owners[xSrc*size + ySrc]] -= values[xSrc*size + ySrc];
		
		values[xSrc*size + ySrc] = 0;
		owners[xSrc*size + ySrc] = -1;

		if (owners[xTarget*size + yTarget] >= 0)
			scores[owners[xTarget*size + yTarget]] -= values[xTarget*size + yTarget];

		values[xTarget*size + yTarget] *= 2;
		owners[xTarget*size + yTarget] = playerId;
		
		scores[playerId] += values[xTarget*size + yTarget];
	}
	
	public void processMove(Coord src, Coord target, int player) {
		processMove(src.x, src.y, target.x, target.y, player);
	}
	
	public void processMove(Move move) {
		processMove(move.src, move.target, move.playerId);
	}
	
	public Point getPoint(int x, int y) {
		Point p = new Point();
		
		p.x = x;
		p.y = y;
		p.owner = owners[x*size + y];
		p.value = values[x*size + y];

		return p;
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
	
	public int numValidMovesFrom(int x, int y, Pair pr) {
		ArrayList<Coord> neighbors = neighborsOf(x, y, pr);
		int num = 0;
		
		for (Coord c : neighbors) {
			if (isMoveValid(c.x, c.y, x, y, pr))
				num++;
		}
		
		return num;
	}

	public int numValidMovesFrom(Coord c, Pair pr) {
		return numValidMovesFrom(c.x, c.y, pr);
	}
	
	public ArrayList<Move> validMoves(Pair pr, int playerId) {
		ArrayList<Move> validMoves = new ArrayList<Move>();
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				ArrayList<Coord> validMovesFrom = validMovesFrom(x, y, pr);
				
				for (Coord c : validMovesFrom)
					validMoves.add(new Move(x, y, c.x, c.y, playerId));
			}
		}
		
		return validMoves;
	}
	
	public int numValidMoves(Pair pr, int playerId) {
		int num = 0;
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				num += numValidMovesFrom(x, y, pr);
			}
		}
		
		return num;
	}
	
	public int numMovesDelta(Move move, Pair pr, int playerId) {
		int delta = 0;
		ArrayList<Coord> neighbors;
		
		// For each neighbor of src, we lose two moves (both directions) if the move was valid
		neighbors = neighborsOf(move.src, pr);
		for (Coord c : neighbors)
			if (isMoveValid(c, move.src, pr))
				delta -= 2;
		
		// For each neighbor of target we lose two moves (both directions) if the move was valid before
		neighbors = neighborsOf(move.target, pr);
		for (Coord c : neighbors)
			if (isMoveValid(c, move.target, pr))
				delta -= 2;
		
		// If the move was made by us, we have double counted the reduction in moves between src and target, so add it back
		if (playerId == move.playerId)
			delta += 2;
		
		// For each neighbor of target we gain two moves (both directions) if the move is valid now
		Board newBoard = new Board(this);
		newBoard.processMove(move);
		neighbors = newBoard.neighborsOf(move.target, pr);
		for (Coord c : neighbors)
			if (newBoard.isMoveValid(c, move.target, pr))
				delta += 2;
		
		return delta;
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
				values[x1*size + y1] == values[x2*size + y2] &&
				values[x1*size + y1] > 0);
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
