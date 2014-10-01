package offset.mateus;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	static final int SIZE = 32;
	
	Point[] currentGrid;
	Pair myPair;
	Pair advPair;
	ArrayList<ArrayList> currentHistory;

	public Player(Pair prin, int idin) { super(prin, idin);	}
	public void init() {}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		currentGrid = grid;
		myPair = pr;
		advPair = pr0;
		currentHistory = history;
		
		// System.out.println(history.size());
		ArrayList lastMove;
		movePair lastMovePair;
		
		if (history.size() > 0) {
			lastMove = history.remove(history.size() - 1);
			lastMovePair = (movePair) lastMove.remove(1);
				
		} else {
			lastMovePair = new movePair();
			lastMovePair.target = currentGrid[0];
		}
		
		
		
//		System.out.printf("Last move was %s to %s\n", lastMovePair.src, lastMovePair.target);
		movePair movepr = new movePair();
		for (int y1_offset = 0; y1_offset < SIZE; y1_offset++) {
			for (int x1_offset = 0; x1_offset < SIZE; x1_offset++) {
				for (int y2_offset = 0; y2_offset < SIZE; y2_offset++) {
					for (int x2_offset = 0; x2_offset < SIZE; x2_offset++) {
						movepr.move = false;
						movepr.src = grid[(((lastMovePair.target.x + x1_offset) * SIZE) + (lastMovePair.target.y + y1_offset)) % (SIZE*SIZE)];
						movepr.target = grid[SIZE * x2_offset + y2_offset];
//						System.out.printf("Will try %s to %s\n", movepr.src, movepr.target);
						if (validateMove(movepr, pr) && advNextMovesIsNotThatGood(movepr)) {
							movepr.move = true;
							return movepr;
						}
					}
				}
				/*
				 * if (i + pr.x >= 0 && i + pr.x < size) { if (j + pr.y >= 0 &&
				 * j + pr.y < size) {
				 * 
				 * } if (j - pr.y >= 0 && j - pr.y < size) {
				 * 
				 * } } if (i - pr.x >= 0 && i - pr.x < size) { if (j + pr.y >= 0
				 * && j + pr.y < size) {
				 * 
				 * } if (j - pr.y >= 0 && j - pr.y < size) {
				 * 
				 * } } if (i + pr.y >= 0 && i + pr.y < size) { if (j + pr.x >= 0
				 * && j + pr.x < size) {
				 * 
				 * } if (j - pr.x >= 0 && j - pr.x < size) {
				 * 
				 * } } if (i - pr.y >= 0 && i - pr.y < size) { if (j + pr.x >= 0
				 * && j + pr.x < size) {
				 * 
				 * } if (j - pr.x >= 0 && j - pr.x < size) {
				 * 
				 * } }
				 */
			}
		}
		return movepr;
	}

	boolean validateMove(movePair movepr, Pair pr) {

		Point src = movepr.src;
		Point target = movepr.target;
		boolean rightposition = false;
		if (Math.abs(target.x - src.x) == Math.abs(pr.p)
				&& Math.abs(target.y - src.y) == Math.abs(pr.q)) {
			rightposition = true;
		}
		if (Math.abs(target.x - src.x) == Math.abs(pr.p)
				&& Math.abs(target.y - src.y) == Math.abs(pr.q)) {
			rightposition = true;
		}
		if (rightposition && src.value == target.value && src.value > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	boolean advNextMovesIsNotThatGood(movePair movepr) {
		Point[] allAdvMoves = new Point[8];
		for(int i = 0; i < 8; i++) {
			allAdvMoves[i] = new Point();
		}
		setAllNextMoves(allAdvMoves, movepr.target, advPair);
		
		for(int i = 0; i < 8; i++) {
			int x = allAdvMoves[i].x;
			int y = allAdvMoves[i].y;
			if(x == -1 || y == -1) {
				continue;
			}
			
			if (currentGrid[SIZE*y+x].value == movepr.target.value*2) {
				return false;
			}
		}
		return true;
	}
	
	void setAllNextMoves(Point[] pointsBuffer, Point src, Pair pr) {
		assert(pointsBuffer.length == 8);
		assert(pointsBuffer[7] != null);
		
		pointsBuffer[0].x = src.x - pr.p;
		pointsBuffer[0].y = src.y - pr.q;
		
		pointsBuffer[1].x = src.x - pr.p;
		pointsBuffer[1].y = src.y + pr.q;
		
		pointsBuffer[2].x = src.x + pr.p;
		pointsBuffer[2].y = src.y - pr.q;
		
		pointsBuffer[3].x = src.x + pr.p;
		pointsBuffer[3].y = src.y + pr.q;
		
		pointsBuffer[4].x = src.x - pr.q;
		pointsBuffer[4].y = src.y - pr.p;
		
		pointsBuffer[5].x = src.x - pr.q;
		pointsBuffer[5].y = src.y + pr.p;
		
		pointsBuffer[6].x = src.x + pr.q;
		pointsBuffer[6].y = src.y - pr.p;
		
		pointsBuffer[7].x = src.x + pr.q;
		pointsBuffer[7].y = src.y + pr.p;
		
		for(int i = 0; i < 8; i++) {
			int x = pointsBuffer[i].x;
			int y = pointsBuffer[i].y;
			if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
				pointsBuffer[i].x = -1;
				pointsBuffer[i].y = -1;
			}
		}
	}
	
	
}