package offset.mateus;
import offset.common.GridGraph;

import java.util.*;
import java.util.Map.Entry;

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
        	// adv might have played more than once in a row
        	int beginAdvMoveInHistoryIndex = history.size() - 1;
        	while(beginAdvMoveInHistoryIndex >= 0 && advId == (int) history.get(beginAdvMoveInHistoryIndex).get(0)) {
        		beginAdvMoveInHistoryIndex--;
        	}
        	// went too far
        	beginAdvMoveInHistoryIndex++;
        	
        	// update graphs
        	for(int i = beginAdvMoveInHistoryIndex; i < history.size(); i++) {
                movePair advMovePair = (movePair) history.get(i).get(1);
                if(advMovePair.move == true) {
                    advGridGraph.updateGraphWithMovePair(advMovePair, advId);
                    myGridGraph.updateGraphWithMovePair(advMovePair, advId);
                }
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
            // update graphs with my move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
        }
        return movepr;
	}
}