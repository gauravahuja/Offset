package offset.oct13_group6;

import offset.oct13_group6.GridGraph;
import offset.oct13_group6.GridGraph.PointPath;

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
	int advId;
	movePair advLastMovePair;
	
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
            int advId = (int) history.get(history.size() - 1).get(0);
            if (advId != id) {
                advLastMovePair = (movePair) history.get(history.size() - 1).get(1);
                advGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
                myGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
            }
        }
        // SANITY CHECK
        for(int i = 0; i < SIZE*SIZE; i++) {
        	if(grid[i].value != myGridGraph.grid[i].value) {
        		System.out.printf("BUG! (%d, %d) %d != %d\n", i/SIZE, i%SIZE, grid[i].value, myGridGraph.grid[i].value);
    //     		try {
				// 	Thread.sleep(5000);
				// } catch (InterruptedException e) {
				// 	e.printStackTrace();
				// }
        	}
        }
        
        
        
        ArrayList<PointPath> protects = advGridGraph.movePairByTime();
        Collections.sort(protects, myComparators.MOVES_VALUE);
        ArrayList<PointPath> builds = myGridGraph.movePairByTime();
        Collections.sort(builds, myComparators.ADVMOVE_MOVES_VALUE_ADVPILES);
        
        HashMap<Point, Point> buildsDelayed = new HashMap<Point, Point>();
        
        movePair movepr = new movePair();
        Iterator<PointPath> protectIt = protects.iterator();
        Iterator<PointPath> buildIt = builds.iterator();
        while(movepr.move == false && (protectIt.hasNext() || buildIt.hasNext())) {
        	PointPath bestProtect = null;
            PointPath bestBuild = null;
        	boolean isBuild = false;
        	boolean isProtect = false;
        	while (bestProtect == null && protectIt.hasNext()) {
	        	bestProtect = protectIt.next();
	        	if (bestProtect.src.owner != id) {
	        		bestProtect = null;
	        	}
        	}
        	if (bestBuild == null && buildIt.hasNext()) {
	        	bestBuild = buildIt.next();
//	        	System.out.printf("bestBuild moves %d value %d\n", bestBuild.moves, bestBuild.src.value);
        	}
        	
	        if (bestProtect != null && bestBuild != null) {
	        	if (bestProtect.moves <= bestBuild.moves) {
	        		isProtect = true;
	        	} else {
	        		isBuild = true;
	        	}
	        } else if(bestBuild != null) {
	        	isBuild = true;
    		} else if(bestProtect != null) {
	        	isProtect = true;
	        }
	        
	        if(isProtect) {
	        	//protect by breaking the adversary path to build
        		int n = 0;
        		for(Point p : bestProtect.path) {
        			if(movepr.move == true) {
        				break;
        			}

        			// 2 gives a better result
        			if(p.value <= 2) {
        				continue;
        			}
        			
        			HashSet<Point> edges = myGridGraph.getEdgesFromPoint(p);
        			for(Point neighbor : edges) {
        				if(movepr.move == true) {
            				break;
            			}

        				movepr.src = new Point(p);
        				movepr.target = new Point(neighbor);
        				if(!moveWillCreateAdvMoves(movepr)) {
        					//System.out.printf("Protecting (%d,%d) [%d] by doing (%d,%d)->(%d,%d) %d/%d\n", bestProtect.path.get(0).x, bestProtect.path.get(0).y, bestProtect.moves, movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, n, bestProtect.path.size());
        					movepr.move = true;
        					PlayerStatistics.PROTECT.counter++;
        					break;
        				}
        			}
        			n++;
        		}
        		if(movepr.move == false) {
        			// try build a value to protect the pile (almost never executed, who knows if ever)
            		int maxMoves = bestProtect.path.size()-1;
            		protectpathloop:
            		for(Point p : bestProtect.path) {
            			if(movepr.move == true) {
            				break;
            			}
            			
            			ArrayList<Point> path = new ArrayList<Point>();
            			HashSet<Point> edges = myGridGraph.getEdgesFromPoint(p);
            			for(Point neighbor : edges) {
            				if(movepr.move == true) {
                				break;
                			}
            				
            				if (neighbor.value != p.value/2) {
            					continue;
            				}
            				
            				path.clear();
            				path.add(neighbor);
            				myGridGraph.getPointValueIncreaseOfPoint(neighbor, path);
            				int numberOfMovesToMakeATargetBigger = path.size() -1;
            				if(numberOfMovesToMakeATargetBigger+1 <= maxMoves) {
            					//do it
            					movepr.src = new Point(path.get(path.size()-1));
            					movepr.target = new Point(path.get(path.size()-2));
            				}
            				
            				if(!moveWillCreateAdvMoves(movepr)) {
            					//System.out.printf("Protecting (%d,%d) [%d] in the future by doing (%d,%d)->(%d,%d) %d/%d\n", bestProtect.path.get(0).x, bestProtect.path.get(0).y, bestProtect.moves, movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, n, bestProtect.path.size());
            					movepr.move = true;
            					PlayerStatistics.PROTECT2.counter++;
            					break protectpathloop;
            				}
            			}
            			maxMoves--;
            		}
        		}
        		if(movepr.move == false) {
        			//System.out.printf("Failed to Protect (%d, %d) v(%d) m(%d)\n", bestProtect.src.x, bestProtect.src.y, bestProtect.src.value, bestProtect.moves);
        			// try a different bestProtect
        			bestProtect = null;
        		}
	        } else if (isBuild) {
	        	//build: wait to build piles >= 4, those piles will be combined when about to be stolen
	        	// allow to build piles >= 4 if they are not both mine or target has a neighbor of higher value
	        	Point src = bestBuild.path.get(bestBuild.path.size() - 1);
        		Point target = bestBuild.path.get(bestBuild.path.size() - 2);
        		
        		boolean bothAreMine = src.owner == id && target.owner == id;
        		
        		boolean targetHasHigherValueNeighbor = false;
        		HashSet<Point> edges = myGridGraph.getEdgesFromPoint(target);
    			for(Point neighbor : edges) {
    				if(neighbor.value > target.value) {
    					targetHasHigherValueNeighbor = true;
    					PlayerStatistics.HIGHERVALUENEIGHBOR.counter++;
    					break;
    				}
    			}
        		
	        	if (!bothAreMine || src.value <= 2 || targetHasHigherValueNeighbor) {
	        		movepr.src = new Point(src);
	        		movepr.target = new Point(target);
	        		if(!moveWillCreateAdvMoves(movepr)) {
	        			movepr.move = true;
	        			PlayerStatistics.BUILD.counter++;
	        			//System.out.printf("Build (%d, %d) -> (%d, %d) (%d+%d) moves %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value,bestBuild.moves);
	        		}
	        	} else {
	        		buildsDelayed.put(getGridPoint(src), getGridPoint(target));
	        		PlayerStatistics.DELAYED.counter++;
	        		//System.out.printf("New Delayed: (%d, %d) -> (%d, %d) (%d+%d)\n", src.x, src.y, target.x, target.y, src.value, target.value);
	        	}
	        	if(movepr.move == false) {
        			// try a different bestBuild
	        		bestBuild = null;
        		}
        	}
        }
        
        //garauv_2
        if(movepr.move == false) {
        	int my_moves = myGridGraph.getNumberOfEdges();
            int adv_moves = advGridGraph.getNumberOfEdges();
            int cummulative = my_moves*adv_moves;
            //System.out.printf("[A2] Me:%d, Adv:%d, Cummulative:%d, MyScore: %d, AdvScore: %d\n", my_moves, adv_moves, cummulative, myGridGraph.getScore(), advGridGraph.getScore());
            
            boolean verbose = false;
            // if (my_moves < 20)
            // {
            //     verbose = true;
            // }

            int minimax_depth = 1;        
            minimax_run(0, minimax_depth, true, movepr, verbose);
            
            my_moves = myGridGraph.getNumberOfEdges();
            adv_moves = advGridGraph.getNumberOfEdges();
            cummulative = my_moves*adv_moves;
            int my_score = myGridGraph.getScore();
            int adv_score = advGridGraph.getScore();

            PlayerStatistics.GAURAV.counter++;
            //System.out.printf("[B2] Me:%d, Adv:%d, Cummulative:%d, MyScore: %d, AdvScore: %d\n", my_moves, adv_moves, cummulative, my_score, adv_score);

//            total_moves++;
//            System.out.printf("[LOG2],%d,%d,%d,%d,%d,%d,%d\n", total_moves, my_moves*2, adv_moves*2, my_score, adv_score, (my_moves - adv_moves)*2, my_score-adv_score);
        }
        
        
        // move remover
        if(movepr.move == false) {
        	List<Point> allPointsDifferentThanZero = advGridGraph.getPointsDifferentThanZero();
        	Collections.sort(allPointsDifferentThanZero, Collections.reverseOrder(myComparators.SMARTEDGES_AND_VALUE));
    		for(Point p : allPointsDifferentThanZero) {
    			if (movepr.move == true) {
    				break;
    			}
    			
    			if(myGridGraph.getEdgesFromPoint(p).size() >= advGridGraph.getEdgesFromPoint(p).size()) {
    				continue;
    			}
    			
            	HashSet<Point> edges = myGridGraph.getEdgesFromPoint(p);
    			for(Point neighbor : edges) {
    				if (movepr.move == true) {
        				break;
        			}
    				
    				movepr.src = new Point(p);
    				movepr.target = new Point(neighbor);
    				
    				boolean moveWasDelayed = buildsDelayed.containsKey(getGridPoint(movepr.src)) && buildsDelayed.get(getGridPoint(movepr.src)).equals(getGridPoint(movepr.target));
    				if(moveWasDelayed) {
    					//System.out.printf("(%d, %d) -> (%d, %d) (%d+%d) was delayed don't use it to break edges\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value);
    				}
    				if(!moveWasDelayed && !moveWillCreateAdvMoves(movepr)) {
    					movepr.move = true;
    					PlayerStatistics.MOVEREMOVER.counter++;
    					//System.out.printf("Remove edges (%d, %d) -> (%d, %d) (%d+%d) edges from src %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value, advGridGraph.getEdgesFromPoint(movepr.src).size());
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
        			PlayerStatistics.BUILD2ndPHASE.counter++;
        			//System.out.printf("Build Phase 2 (%d, %d) -> (%d, %d) (%d+%d) moves %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value,bestBuild.moves);
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
    					PlayerStatistics.SEQUENTIAL.counter++;
    					//System.out.printf("Sequential (%d, %d) -> (%d, %d) (%d+%d). Edges from src %d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value, advGridGraph.getEdgesFromPoint(movepr.src).size());
    					break;
    				}
    			}
            }
        }
        if (movepr.move == false) {
        	//do half dumb (might create bad moves, but will get a valid move)
        	//System.out.printf("Last option\n");
        	for (int i = 0; i < SIZE*SIZE; i++) {
            	Point p = myGridGraph.grid[i];
            	if (myGridGraph.doesPointHasEdges(p)) {
            		Point otherP = myGridGraph.edgesByPoint.get(p).iterator().next();
            		if (advGridGraph.doesPointHasEdges(otherP)) {
                		movepr.src = new Point(otherP);
                        movepr.target = new Point(p);
                        movepr.move = true;      
                        PlayerStatistics.DUMB.counter++;
            		} else {
                		movepr.src = new Point(p);
                        movepr.target = new Point(otherP);
                        movepr.move = true;
                        PlayerStatistics.DUMB.counter++;
            		}

                    break;
            	}
            }
        }
        
        if (movepr.move == true) {
            // update graphs with my move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
            //System.out.printf("My move (%d, %d) -> (%d, %d) (%d+%d)\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y, movepr.src.value, movepr.target.value);
//            System.out.printf("Confirm values %d = %d && %d = %d\n", grid[32*movepr.src.x + movepr.src.y].value, movepr.src.value,grid[32*movepr.target.x + movepr.target.y].value, movepr.target.value);
        }
        PlayerStatistics.printStats();
        return movepr;
	}
	
	public Point getGridPoint(Point p) {
		return currentGrid[SIZE*p.x + p.y];
	}
	
	public boolean moveWillCreateAdvMoves(movePair movepr) {
		Point advGridPoint = advGridGraph.getGraphGridPoint(movepr.target.x, movepr.target.y);
		
		// TODO play with this value: currently it means that will ignore stealable piles of 2
		if(movepr.target.value <= 1) {
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
	
	void print_minimax_summary(int current_depth, int max_depth, boolean my_turn)
    {
        System.out.printf("[Summary] current_depth = %d, max_depth = %d, my_turn = %b, my_score = %d, adv_score = %d\n", 
            current_depth,
            max_depth,
            my_turn,
            myGridGraph.getScore(),
            advGridGraph.getScore());
    }

    int minimax_run(int current_depth, int max_depth, boolean my_turn, movePair next_best_move, boolean verbose)
    {
        movePair best_movepr = new movePair();
        best_movepr.move = false;
        best_movepr.src = new Point();
        best_movepr.target = new Point();

        if((current_depth == max_depth) /*|| no moves left*/ )
        {
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            //print_minimax_summary(current_depth, max_depth, my_turn);
            return (myGridGraph.getNumberOfEdges()-advGridGraph.getNumberOfEdges());
        }

        HashSet<Point> keys = new HashSet<Point>();
        HashSet<Point> neighbors = new HashSet<Point>();
        Iterator<Point> it;
        Iterator<Point> it_n;
        Point p;
        Point p_n;
        int best_value;
        int value;
        int current_delta = myGridGraph.getNumberOfEdges() - advGridGraph.getNumberOfEdges();
       
        movePair child_best_move = new movePair();

        movePair movepr = new movePair();
        movepr.move = false;

        if (my_turn)
        {
            best_value = -1000000; //Because I have to maximize
            //keys = myGridGraph.edgesByPoint.keySet();
            keys.addAll(myGridGraph.edgesByPoint.keySet());
            it = keys.iterator();


            while(it.hasNext())
            {
                p = it.next();
                //neighbors = myGridGraph.edgesByPoint.get(p);
                //best_value = -1;
                neighbors.clear();
                neighbors.addAll(myGridGraph.edgesByPoint.get(p));
                if(neighbors == null)
                    continue;
                
                it_n = neighbors.iterator();
                while(it_n.hasNext())
                {
                    p_n = it_n.next();
                    //p <- p_n
                    movepr.src = new Point(p_n);
                    movepr.target = new Point(p);

                    myGridGraph.updateGraphWithMovePair(movepr, id);
                    advGridGraph.updateGraphWithMovePair(movepr, id);
                    //print_minimax_summary(current_depth, max_depth, my_turn);

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move, verbose) - current_delta;
                    if(verbose)
                    {
                        System.out.printf("[Move] (%d, %d) -> (%d, %d) => Adv Moves=%d\n",
                            movepr.src.x,
                            movepr.src.y,
                            movepr.target.x,
                            movepr.target.y,
                            value);
                    }
                    if (value >= best_value)//Maximize best value
                    {
                        best_movepr.src = movepr.src;
                        best_movepr.target = movepr.target;
                        best_movepr.move = true;
                        best_value = value;
                    }

                    myGridGraph.undoGraphByOneMovePair();
                    advGridGraph.undoGraphByOneMovePair();
                }
            }
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            if(verbose)
            {
                System.out.printf("[Chosen Move] (%d, %d) -> (%d, %d) => Adv Moves=%d\n",
                        best_movepr.src.x,
                        best_movepr.src.y,
                        best_movepr.target.x,
                        best_movepr.target.y,
                        best_value);
            }
            return best_value;
        }
        else
        {
            best_value = 1000000;//Because Adv has to minimize
            //keys = advGridGraph.edgesByPoint.keySet();
            keys.addAll(advGridGraph.edgesByPoint.keySet());
            it = keys.iterator();
            
            while(it.hasNext())
            {
                p = it.next();
                //neighbors = advGridGraph.edgesByPoint.get(p);
                neighbors.clear();
                neighbors.addAll(advGridGraph.edgesByPoint.get(p));
                if(neighbors == null)
                    continue;
                
                it_n = neighbors.iterator();
                while(it_n.hasNext())
                {
                    p_n = it_n.next();
                    //p <- p_n
                    movepr.src = new Point(p_n);
                    movepr.target = new Point(p);

                    myGridGraph.updateGraphWithMovePair(movepr, advId);
                    advGridGraph.updateGraphWithMovePair(movepr, advId);
                    //print_minimax_summary(current_depth, max_depth, my_turn);

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move, verbose) - current_delta;
                    if (value <= best_value)//Minimize best value
                    {
                        best_movepr.src = movepr.src;
                        best_movepr.target = movepr.target;
                        best_movepr.move = true;
                        best_value = value;
                    }

                    myGridGraph.undoGraphByOneMovePair();
                    advGridGraph.undoGraphByOneMovePair();
                }
            }
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            return best_value;
        }
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
        public Comparator<PointPath> MOVES = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
                return o1.moves - o2.moves;
            }
        };
        public Comparator<PointPath> VALUE_REVERSE_POINTPATH = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
                return o2.path.get(o2.path.size()-1).value - o1.path.get(o1.path.size()-1).value;
            }
        };
        // adversary piles comes before mine piles
        public Comparator<PointPath> MY_PILES_FIRST = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
            	if(o1.src.owner == o2.src.owner) {
            		return 0;
            	} else if(o1.src.owner == id) {
            		return -1;
            	} else {
            		return 1;
            	}
            }
        };
        public Comparator<PointPath> MOVES_VALUE = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
            	int i = MOVES.compare(o1, o2);
                if (i == 0) {
                    return VALUE_REVERSE_POINTPATH.compare(o1, o2);
                }
                return i;
            }
        };
        public Comparator<PointPath> MOVES_VALUE_MYPILES = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
            	int i = MOVES.compare(o1, o2);
                if (i == 0) {
                    i = VALUE_REVERSE_POINTPATH.compare(o1, o2);
                    if (i == 0) {
                    	return MY_PILES_FIRST.compare(o1, o2);
                    }
                }
                return i;
            }
        };
        public Comparator<PointPath> MOVES_VALUE_ADVPILES = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
            	int i = MOVES.compare(o1, o2);
                if (i == 0) {
                    i = VALUE_REVERSE_POINTPATH.compare(o1, o2);
                    if (i == 0) {
                    	return -MY_PILES_FIRST.compare(o1, o2);
                    }
                }
                return i;
            }
        };
        public Comparator<PointPath> ADVMOVE_MOVES_VALUE_ADVPILES = new Comparator<PointPath>() {
            @Override
            public int compare(PointPath o1, PointPath o2) {
            	Point advLastTarget = getGridPoint(advLastMovePair.target);
            	boolean o1Has = false;
            	for(Point p : o1.path) {
            		if(advLastTarget.equals(getGridPoint(p))) {
            			o1Has = true;
            			break;
            		}
            	}
            	boolean o2Has = false;
            	for(Point p : o2.path) {
            		if(advLastTarget.equals(getGridPoint(p))) {
            			o2Has = true;
            			break;
            		}
            	}
            	if(o1Has == o2Has) {
            		return MOVES_VALUE_ADVPILES.compare(o1, o2);
            	} else if (o1Has) {
            		return -1;
            	} else {
            		return 1;
            	}
            	
            }
        };
    }
	public Comparators myComparators = new Comparators();
	
	public enum PlayerStatistics {
		BUILD,
		PROTECT,
		PROTECT2,
		HIGHERVALUENEIGHBOR,
		DELAYED,
		GAURAV,
		MOVEREMOVER,
		BUILD2ndPHASE,
		SEQUENTIAL,
		DUMB;
		
		public int counter = 0;
		public static void printStats() {
			for(PlayerStatistics s : PlayerStatistics.values()) {
				System.out.printf("%s: %d,", s, s.counter);
			}
			System.out.printf("\n");
		}
	}
}
