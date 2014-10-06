package offset.mateus;
import offset.common.GridGraph;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	static final int SIZE = 32;
	
	Point[] currentGrid;
	Pair myPair;
	Pair advPair;
	GridGraph myGridGraph;
	GridGraph advGridGraph;
	
	boolean playerInitialized = false;

	public Player(Pair prin, int idin) { super(prin, idin);	}
	public void init() {}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		currentGrid = grid;
		myPair = pr;
		advPair = pr0;

		if (!playerInitialized) {
			int advId = id + 1 % 2;
			myGridGraph = new GridGraph(pr, id);
			advGridGraph = new GridGraph(pr0, advId);
			playerInitialized = true;
		}
		
		// update graphs with adversary last move
		if (history.size() >= 1) {
			int advId = (int) history.get(history.size() - 1).get(0);
			if (advId != id) {
				movePair advLastMovePair = (movePair) history.get(history.size() - 1).get(1);
				advGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
				myGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
			}
		}
		
		System.out.printf("Possibles moves for me: %d\n", myGridGraph.getNumberOfEdges());
		System.out.printf("Possibles moves for adversary: %d\n", advGridGraph.getNumberOfEdges());
		
		movePair movepr = new movePair();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				for (int i_pr=0; i_pr<SIZE; i_pr++) {
					for (int j_pr=0; j_pr <SIZE; j_pr++) {
						movepr.move = false;
						movepr.src = grid[i*SIZE+j];
						movepr.target = grid[i_pr*SIZE+j_pr];
						if (validateMove(movepr, pr)) {
							movepr.move = true;
							// update graphs with adversary last move
							advGridGraph.updateGraphWithMovePair(movepr, id);
							myGridGraph.updateGraphWithMovePair(movepr, id);
							return movepr;
						}
					}
				}
			}
		}
		
		// update graphs with adversary last move
		advGridGraph.updateGraphWithMovePair(movepr, id);
		myGridGraph.updateGraphWithMovePair(movepr, id);
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