package offset.mateus;
import offset.common.GridGraph;
import offset.common.GridGraph.Comparators;
import offset.common.GridGraph.PointPath;

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
					Thread.sleep(5000);
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
                    System.out.printf("Adversary move (%d, %d) -> (%d, %d) (%d+%d)\n", advMovePair.src.x, advMovePair.src.y, advMovePair.target.x, advMovePair.target.y, advMovePair.src.value, advMovePair.target.value);
                }
        	}
            lastSeenAdvHistoryIndex = history.size() -1;
        }
        // SANITY CHECK
        for(int i = 0; i < SIZE*SIZE; i++) {
        	if(grid[i].value != myGridGraph.grid[i].value) {
        		System.out.printf("BUG! (%d, %d) %d != %d\n", i/SIZE, i%SIZE, grid[i].value, myGridGraph.grid[i].value);
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        
        
        ArrayList<PointPath> steals = advGridGraph.movePairByTime();
        ArrayList<PointPath> builds = myGridGraph.movePairByTime();
        
        HashMap<Point, Point> buildsDelayed = new HashMap<Point, Point>();
        
        movePair movepr = new movePair();
        Iterator<PointPath> stealIt = steals.iterator();
        Iterator<PointPath> buildIt = builds.iterator();
        while(movepr.move == false && (stealIt.hasNext() || buildIt.hasNext())) {
        	PointPath bestSteal = null;
            PointPath bestBuild = null;
        	boolean isBuild = false;
        	boolean isSteal = false;
        	while (bestSteal == null && stealIt.hasNext()) {
	        	bestSteal = stealIt.next();
	        	if (bestSteal.src.owner != id) {
	        		bestSteal = null;
	        	}
        	}
        	while (bestBuild == null && buildIt.hasNext()) {
	        	bestBuild = buildIt.next();
        	}
        	
	        if (bestSteal != null && bestBuild != null) {
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
	        	//protect by breaking the adversary path to steal
        		Iterator<Point> it = bestSteal.path.iterator();
        		int n = 0;
        		while(movepr.move == false && it.hasNext()) {
        			Point srcN = it.next();

        			// TODO try with 1
        			if(srcN.value <= 2) {
        				continue;
        			}
        			
        			HashSet<Point> edges = myGridGraph.getEdgesFromPoint(srcN);
        			Iterator<Point> possibleTargets = edges.iterator();
        			while(movepr.move == false && possibleTargets.hasNext()) {
        				Point aTarget = possibleTargets.next();
        				movepr.src = new Point(srcN);
        				movepr.target = new Point(aTarget);
        				if(!moveWillCreateAdvMoves(movepr)) {
        					System.out.printf("Protecting (%d,%d) [%d] by doing (%d,%d)->(%d,%d) %d/%d\n", bestSteal.path.get(0).x, bestSteal.path.get(0).y, bestSteal.moves, movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, n, bestSteal.path.size());
        					movepr.move = true;
        					break;
        				}
        			}
        			n++;
        		}
        		if(movepr.move == false) {
        			// try build a value to protect the pile
        			Iterator<Point> it1 = bestSteal.path.iterator();
            		int maxMoves = bestSteal.path.size()-1;
            		stealpathloop:
            		while(movepr.move == false && it1.hasNext()) {
            			Point srcN = it1.next();

            			ArrayList<Point> path = new ArrayList<Point>();
            			HashSet<Point> edges = myGridGraph.getEdgesFromPoint(srcN);
            			Iterator<Point> possibleTargets = edges.iterator();
            			while(movepr.move == false && possibleTargets.hasNext()) {
            				Point aTarget = possibleTargets.next();
            				
            				if (aTarget.value != srcN.value/2) {
            					continue;
            				}
            				
            				path.clear();
            				path.add(aTarget);
            				myGridGraph.getPointValueIncreaseOfPoint(aTarget, path);
            				int numberOfMovesToMakeATargetBigger = path.size() -1;
            				if(numberOfMovesToMakeATargetBigger+1 <= maxMoves) {
            					//do it
            					movepr.src = new Point(path.get(path.size()-1));
            					movepr.target = new Point(path.get(path.size()-2));
            				}
            				
            				// TODO play with this if
            				if(!moveWillCreateAdvMoves(movepr)) {
            					System.out.printf("Protecting (%d,%d) [%d] in the future by doing (%d,%d)->(%d,%d) %d/%d\n", bestSteal.path.get(0).x, bestSteal.path.get(0).y, bestSteal.moves, movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, n, bestSteal.path.size());
            					movepr.move = true;
            					break stealpathloop;
            				}
            			}
            			maxMoves--;
            		}
        		}
        		
        		if(movepr.move == false) {
        			System.out.printf("Failed to Protect (%d, %d) v(%d) m(%d)\n", bestSteal.src.x, bestSteal.src.y, bestSteal.src.value, bestSteal.moves);
        			// try a different bestSteal
        			bestSteal = null;
        		}
	        } else if (isBuild) {
	        	//build: wait to build piles >= 4, those piles will be combined when about to be stolen
	        	// allow to build piles >= 4 if they are not both mine or target has a neighbor of higher value
	        	Point src = bestBuild.path.get(bestBuild.path.size() - 1);
        		Point target = bestBuild.path.get(bestBuild.path.size() - 2);
        		
        		boolean bothAreMine = src.owner == id && target.owner == id;
        		
        		boolean targetHasHigherValueNeighbor = false;
        		HashSet<Point> edges = myGridGraph.getEdgesFromPoint(target);
    			Iterator<Point> possibleTargets = edges.iterator();
    			while(possibleTargets.hasNext()) {
    				Point aNeighbor = possibleTargets.next();
    				if(aNeighbor.value > target.value) {
    					targetHasHigherValueNeighbor = true;
    					break;
    				}
    			}
        		
	        	if (!bothAreMine || src.value <= 2 || targetHasHigherValueNeighbor) {
	        		movepr.src = new Point(src);
	        		movepr.target = new Point(target);
	        		if(!moveWillCreateAdvMoves(movepr)) {
	        			movepr.move = true;
	        			
	        			System.out.printf("Build (%d, %d) -> (%d, %d) (%d+%d) moves %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value,bestBuild.moves);
//		        		System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
	        		}
	        	} else {
	        		buildsDelayed.put(getGridPoint(src), getGridPoint(target));
	        		System.out.printf("New Delayed: (%d, %d) -> (%d, %d) (%d+%d)\n", src.x, src.y, target.x, target.y, src.value, target.value);
	        	}
	        	if(movepr.move == false) {
//        			System.out.printf("Failed to Protect (%d, %d) v(%d) m(%d)\n", bestSteal.src.x, bestSteal.src.y, bestSteal.src.value, bestSteal.moves);
        			// try a different bestBuild
	        		bestBuild = null;
        		}
        	}
        }
        
        // TODO play with this part: what to do when previous strategy didn't find anything good
        // get a node in adversary graph with max number of edges and higher value
        if(movepr.move == false) {
        	List<Point> pointsByEdges = advGridGraph.getPointsDifferentThanZero();
        	Collections.sort(pointsByEdges, Collections.reverseOrder(myComparators.SMARTEDGES_AND_VALUE));
        	Iterator<Point> it = pointsByEdges.iterator();
    		while(movepr.move == false && it.hasNext()) {
    			Point srcN = it.next();
    			
//    			if(myGridGraph.getEdgesFromPoint(srcN).size() >= advGridGraph.getEdgesFromPoint(srcN).size()) {
//    				continue;
//    			}
    			
            	HashSet<Point> edges = myGridGraph.getEdgesFromPoint(srcN);
    			Iterator<Point> possibleTargets = edges.iterator();
    			while(movepr.move == false && possibleTargets.hasNext()) {
    				Point aTarget = possibleTargets.next();
    				
    				movepr.src = new Point(srcN);
    				movepr.target = new Point(aTarget);
    				
    				boolean moveWasDelayed = buildsDelayed.containsKey(getGridPoint(movepr.src)) && buildsDelayed.get(getGridPoint(movepr.src)).equals(getGridPoint(movepr.target));
    				if(moveWasDelayed) {
    					System.out.printf("(%d, %d) -> (%d, %d) (%d+%d) was delayed don't use it to break edges\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value);
    				}
    				if(!moveWasDelayed && !moveWillCreateAdvMoves(movepr)) {
    					movepr.move = true;
    					System.out.printf("Remove edges (%d, %d) -> (%d, %d) (%d+%d) edges from src %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value, advGridGraph.getEdgesFromPoint(movepr.src).size());
    					break;
    				}
    			}
            }
        }
        if (movepr.move == false) {
        	// build second phase
        	buildIt = builds.iterator();
            while(movepr.move == false && buildIt.hasNext()) {
            	PointPath bestBuild = buildIt.next();
            	
            	Point src = bestBuild.path.get(bestBuild.path.size() - 1);
        		Point target = bestBuild.path.get(bestBuild.path.size() - 2);
        		
        		movepr.src = new Point(src);
        		movepr.target = new Point(target);
        		if(!moveWillCreateAdvMoves(movepr)) {
        			movepr.move = true;
        			System.out.printf("Build Phase 2 (%d, %d) -> (%d, %d) (%d+%d) moves %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value,bestBuild.moves);
//		        		System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
        		}
            }
        }
        if (movepr.move == false) {
        	// sequential (just don't create bad move)
        	for (int i = 0; i < SIZE*SIZE; i++) {
            	Point srcN = myGridGraph.grid[i];
            	
            	HashSet<Point> edges = myGridGraph.getEdgesFromPoint(srcN);
    			Iterator<Point> possibleTargets = edges.iterator();
    			while(movepr.move == false && possibleTargets.hasNext()) {
    				Point aTarget = possibleTargets.next();
    				
    				movepr.src = new Point(srcN);
    				movepr.target = new Point(aTarget);
    				if(!moveWillCreateAdvMoves(movepr)) {
    					movepr.move = true;
    					System.out.printf("Sequential (%d, %d) -> (%d, %d) (%d+%d). Edges from src %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value, advGridGraph.getEdgesFromPoint(movepr.src).size());
    					break;
    				}
    			}
            }
        }
        if (movepr.move == false) {
        	//do half dumb (might create bad moves, but will get a valid move)
        	System.out.printf("Last option\n");
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
            System.out.printf("My move (%d, %d) -> (%d, %d) (%d+%d)\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value);
//            System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
        }
        return movepr;
	}
	
	public Point getGridPoint(Point p) {
		return currentGrid[SIZE*p.x + p.y];
	}
	
	public boolean moveWillCreateAdvMoves(movePair movepr) {
		Point advGridPoint = advGridGraph.getGraphGridPoint(movepr.target.x, movepr.target.y);
		
		// TODO play with this value: currently it means that will ignore stealiable piles of 2
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
	
	public class Comparators {
        public Comparator<Point> SMARTEDGES = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int diff = (advGridGraph.getEdgesFromPoint(o1).size() - advGridGraph.getEdgesFromPoint(o2).size());
                if (diff == 0) {
                	return (myGridGraph.getEdgesFromPoint(o2).size() - myGridGraph.getEdgesFromPoint(o1).size());
                }
                return diff;
            }
        };
        public Comparator<Point> SMARTEDGES_AND_VALUE = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int i = SMARTEDGES.compare(o1, o2);
                if (i == 0) {
                	return o2.value - o1.value;
                }
                return i;
            }
        };
    }
	public Comparators myComparators = new Comparators();
}