package offset.oct8_group6;
import offset.oct8_group6.GridGraph;
import offset.oct8_group6.GridGraph.MovePairTime;

import java.util.*;
import java.util.Map.Entry;

import offset.oct8_group6.GridGraph;
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
	int lastSeenAdvHistoryIndex = -1;
	
	boolean playerInitialized = false;


	public Player(Pair prin, int idin) { super(prin, idin);	}
	public void init() {}
	
	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		currentGrid = grid;
        
        if (!playerInitialized) {
            myPair = pr;
            advPair = pr0;
            advId = (id + 1) % 2;
            myGridGraph = new GridGraph(pr, id);
            advGridGraph = new GridGraph(pr0, advId);
            playerInitialized = true;
        }
        
        // update graphs with adversary last move
        if (history.size() >= 1) {
        	// SANITY CHECK
        	if (lastSeenAdvHistoryIndex > history.size() - 1) {
        		System.out.printf("BUG! history smaller than before\n");
        		try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	// update graphs
        	for(int i = lastSeenAdvHistoryIndex + 1; i < history.size(); i++) {
        		if (advId != (int) history.get(i).get(0)) {
        			continue;
        		}
        		
                movePair advMovePair = (movePair) history.get(i).get(1);
                if(advMovePair.move == true) {
//                	System.out.printf("New target (%d, %d) %d\n", advMovePair.target.x, advMovePair.target.y, advMovePair.target.value);
                    advGridGraph.updateGraphWithMovePair(advMovePair, advId);
                    myGridGraph.updateGraphWithMovePair(advMovePair, advId);
                }
        	}
            lastSeenAdvHistoryIndex = history.size() -1;
        }
        // SANITY CHECK
        for(int i = 0; i < SIZE*SIZE; i++) {
        	if(grid[i].value != myGridGraph.grid[i].value) {
        		System.out.printf("BUG! (%d, %d) %d != %d\n", i/SIZE, i%SIZE, grid[i].value, myGridGraph.grid[i].value);
        		try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        ArrayList<MovePairTime> steals = advGridGraph.movePairByTime();
        ArrayList<MovePairTime> builds = myGridGraph.movePairByTime();
        
        movePair movepr = new movePair();
        Iterator<MovePairTime> stealIt = steals.iterator();
        Iterator<MovePairTime> buildIt = builds.iterator();
        while(movepr.move == false && (stealIt.hasNext() || buildIt.hasNext())) {
        	MovePairTime bestSteal = null;
            MovePairTime bestBuild = null;
        	boolean isBuild = false;
        	boolean isSteal = false;
        	while (stealIt.hasNext()) {
	        	bestSteal = stealIt.next();
	        	if(!moveWillCreateAdvMoves(bestSteal.movepr)) {
	        		break;
	        	}
	        	bestSteal = null;
        	}
        	while (buildIt.hasNext()) {
	        	bestBuild = buildIt.next();
	        	if(!moveWillCreateAdvMoves(bestBuild.movepr)) {
	        		break;
	        	}
	        	bestBuild = null;
        	}
        	
	        if (bestSteal != null && bestBuild != null) {
	        	//System.out.printf("bs %d bb %d\n", bestSteal.moves, bestBuild.moves);
	        	if (bestSteal.moves <= bestBuild.moves) {
	        		isSteal = true;
	        	} else {
	        		isBuild = true;
	        	}
	        } else if(bestBuild != null) {
	        	isBuild = true;
    		} else if(bestSteal != null) {
	        	isSteal = true;
	        }
	        
	        if(isSteal) {
	        	//protect
        		Point myGridPointSrc = myGridGraph.getGraphGridPoint(bestSteal.movepr.src.x, bestSteal.movepr.src.y);
        		Point myGridPointTarget = myGridGraph.getGraphGridPoint(bestSteal.movepr.target.x, bestSteal.movepr.target.y);
        		
        		if(myGridGraph.edgesByPoint.get(myGridPointTarget).size() > 0) {
//        			System.out.printf("Protect 1 - target\n");
        			Point targetNeighbor = myGridGraph.edgesByPoint.get(myGridPointTarget).iterator().next();
        			movepr.target = new Point(targetNeighbor);
        			movepr.src = new Point(myGridPointTarget);
        			movepr.move = true;
        		} else if(myGridGraph.edgesByPoint.get(myGridPointSrc).size() > 0) {
//        			System.out.printf("Protect 1 - src\n");
        			Point srcNeighbor = myGridGraph.edgesByPoint.get(myGridPointSrc).iterator().next();
        			movepr.target = new Point(srcNeighbor);
        			movepr.src = new Point(myGridPointSrc);
        			movepr.move = true;
        		} else {
//        			System.out.printf("Failed to Protect 1\n");
        			
        			// TODO play with this part: what if we cannot protect a steal?
        			if(bestBuild != null) {
        				movepr.src = new Point(bestBuild.movepr.src);
                		movepr.target = new Point(bestBuild.movepr.target);
                		movepr.move = true;
        			}
        		}
	        } else if (isBuild) {
	        	//build
        		movepr.src = new Point(bestBuild.movepr.src);
        		movepr.target = new Point(bestBuild.movepr.target);
        		movepr.move = true;
//        		System.out.printf("Build (%d, %d) -> (%d, %d) (%d+%d) moves %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value,bestBuild.moves);
//        		System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
	        }
        }
        
        // TODO play with this part: what to do when previous strategy didn't find anything good
        // get a node in adversary graph with max number of edges and higher value
        if(movepr.move == false) {
//        	System.out.printf("Remove edges\n");
        	ArrayList<Point> pointsByValueByEdges = advGridGraph.getPointsByNumberOfEdgesByValue();
            for (int i = 0; i < pointsByValueByEdges.size(); i++) {
            	Point p = pointsByValueByEdges.get(i);
//            	System.out.printf("(%d,%d) %d\n", p.x, p.y, p.value);
            	if (myGridGraph.doesPointHasEdges(p)) {
            		movepr.src = new Point(p);
                    movepr.target = new Point(myGridGraph.edgesByPoint.get(myGridGraph.getGraphGridPoint(p.x, p.y)).iterator().next());
                    movepr.move = true;
                    break;
        		}
            }
        }
        if (movepr.move == false) {
        	//do half dumb
//        	System.out.printf("Last option\n");
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
//            System.out.printf("Moving (%d, %d) -> (%d, %d) (%d+%d)\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value);
//            System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
        }
        return movepr;
	}
	
	public boolean moveWillCreateAdvMoves(movePair movepr) {
		Point advGridPoint = advGridGraph.getGraphGridPoint(movepr.target.x, movepr.target.y);
		
		// TODO play with this value: ignore if we will create a stealiable pile of value 2
		if(movepr.target.value == 1) {
			return false;
		}
		
		advGridGraph.updateGraphWithMovePair(movepr, id);
    	if(advGridGraph.doesPointHasEdges(advGridPoint)) {
    		advGridGraph.undoGraphByOneMovePair();
    		return true;
    	}
    	advGridGraph.undoGraphByOneMovePair();
    	return false;
	}
}
