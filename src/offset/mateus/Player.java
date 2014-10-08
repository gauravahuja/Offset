package offset.mateus;
import offset.common.GridGraph;
import offset.common.GridGraph.MovePairTime;

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
        
        ArrayList<MovePairTime> steals = advGridGraph.movePairByTime();
        ArrayList<MovePairTime> builds = myGridGraph.movePairByTime();
        
        MovePairTime bestSteal = null;
        for(Iterator<MovePairTime> it = steals.iterator(); it.hasNext(); ) {
        	MovePairTime mpt = it.next();
        	
        	if(moveWillCreateAdvMoves(mpt.movepr)) {
        		continue;
        	}
    		bestSteal = mpt;
        }
        MovePairTime bestBuild = null;
        for(Iterator<MovePairTime> it = builds.iterator(); it.hasNext(); ) {
        	MovePairTime mpt = it.next();
        	
        	if(moveWillCreateAdvMoves(mpt.movepr)) {
        		continue;
        	}
    		bestBuild = mpt;
        }
        
        
        movePair movepr = new movePair();
        if (bestSteal != null && bestBuild != null) {
        	if (bestSteal.moves == bestBuild.moves) {
        		//protect
        		Point myGridPointSrc = myGridGraph.getGraphGridPoint(bestSteal.movepr.src.x, bestSteal.movepr.src.y);
        		Point myGridPointTarget = myGridGraph.getGraphGridPoint(bestSteal.movepr.target.x, bestSteal.movepr.target.y);
        		
        		if(myGridGraph.edgesByPoint.get(myGridPointTarget).size() > 0) {
        			System.out.printf("Protect 1 - target\n");
        			Point target = myGridGraph.edgesByPoint.get(myGridPointTarget).iterator().next();
        			movepr.target = new Point(target);
        			movepr.src = new Point(myGridPointTarget);
        			movepr.move = true;
        		} else if(myGridGraph.edgesByPoint.get(myGridPointSrc).size() > 0) {
        			System.out.printf("Protect 1 - src\n");
        			Point target = myGridGraph.edgesByPoint.get(myGridPointSrc).iterator().next();
        			movepr.target = new Point(target);
        			movepr.src = new Point(myGridPointSrc);
        			movepr.move = true;
        		} else {
        			System.out.printf("Failed to Protect 1\n");
        		}
        	} else if (bestSteal.moves < bestBuild.moves) {
        		//protect
        		Point myGridPointSrc = myGridGraph.getGraphGridPoint(bestSteal.movepr.src.x, bestSteal.movepr.src.y);
        		Point myGridPointTarget = myGridGraph.getGraphGridPoint(bestSteal.movepr.target.x, bestSteal.movepr.target.y);
        		
        		if(myGridGraph.edgesByPoint.get(myGridPointTarget).size() > 0) {
        			System.out.printf("Protect 2 - target\n");
        			Point target = myGridGraph.edgesByPoint.get(myGridPointTarget).iterator().next();
        			movepr.target = new Point(target);
        			movepr.src = new Point(myGridPointTarget);
        			movepr.move = true;
        		} else if(myGridGraph.edgesByPoint.get(myGridPointSrc).size() > 0) {
        			System.out.printf("Protect 2 - src\n");
        			Point target = myGridGraph.edgesByPoint.get(myGridPointSrc).iterator().next();
        			movepr.target = new Point(target);
        			movepr.src = new Point(myGridPointSrc);
        			movepr.move = true;
        		} else {
        			System.out.printf("Failed to Protect 2\n");
        		}
        	} else {
        		//build
        		movepr.src = bestBuild.movepr.src;
        		movepr.target = bestBuild.movepr.target;
        		movepr.move = true;
        		System.out.printf("Build \n");
        	}
        }
        
        
        
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
	
	public boolean moveWillCreateAdvMoves(movePair movepr) {
		Point advGridPoint = advGridGraph.getGraphGridPoint(movepr.target.x, movepr.target.y);
		
		advGridGraph.updateGraphWithMovePair(movepr, id);
    	if(advGridGraph.doesPointHasEdges(advGridPoint)) {
    		advGridGraph.undoGraphByOneMovePair();
    		return true;
    	}
    	advGridGraph.undoGraphByOneMovePair();
    	return false;
	}
}