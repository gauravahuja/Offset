package offset.mateus;
import offset.common.GridGraph;

import java.util.*;

import offset.common.GridGraph;
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
	int advId;
	
	boolean playerInitialized = false;

	public Player(Pair prin, int idin) { super(prin, idin);	}
	public void init() {}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		currentGrid = grid;
        
        if (!playerInitialized) {
            myPair = pr;
            advPair = pr0;
            advId = id + 1 % 2;
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
        
        movePair movepr = new movePair();
        // get a node in adversary graph with max number of edges and higher value
        ArrayList<Point> pointsByValueByEdges = advGridGraph.getPointsByNumberOfEdgesByValue();
        for (int i = 0; i < pointsByValueByEdges.size(); i++) {
        	Point p = pointsByValueByEdges.get(i);
        	if (myGridGraph.doesPointHasEdges(p)) {
        		movepr.src = new Point(p);
                movepr.target = new Point(myGridGraph.edgesByPoint.get(myGridGraph.getGraphGridPoint(p.x, p.y)).iterator().next());
                movepr.move = true;
                break;
    		}
        }
        if (movepr.move == false) {
        	//do half dumb
        	for (int i = 0; i < SIZE*SIZE; i++) {
            	Point p = myGridGraph.grid[i];
            	if (myGridGraph.doesPointHasEdges(p)) {
            		Point otherP = myGridGraph.edgesByPoint.get(p).iterator().next();
            		if (advGridGraph.doesPointHasEdges(otherP)) {
                		movepr.src = new Point(otherP);
                        movepr.target = new Point(p);
                        movepr.move = true;            			
            		} else {
                		movepr.src = new Point(p);
                        movepr.target = new Point(otherP);
                        movepr.move = true;
            		}

                    break;
            	}
            }
        }
        
        if (movepr.move == true) {
            // update graphs with adversary last move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
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