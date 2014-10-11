package offset.oct8_group6;

import java.util.*;
import java.util.Map.Entry;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class GridGraph {
	public static final int SIZE = 32;
	
	public Point[] grid = new Point[SIZE*SIZE];
	public HashMap<Point, HashSet<Point>> edgesByPoint;
	public ArrayList<HistoryRecord> history;
	public Pair pr;
	public int graphPlayerId;
	public ArrayList<HashMap<Point, Set<Point>>> maps = new ArrayList<HashMap<Point, Set<Point>>>(); 
	
	private Point[] possiblePoints = new Point[8];
	
	// evaluation and scores variables
	public int score;
	
	public GridGraph(Pair graphPair, int playerId) {
		history = new ArrayList<HistoryRecord>();
		pr = graphPair;
		graphPlayerId = playerId;
		edgesByPoint = new HashMap<Point, HashSet<Point>>();
		
		for (int i=0; i<SIZE; i++) {
			for (int j=0; j<SIZE; j++) {
				grid[i*SIZE+j] = new Point(i, j, 1, -1);
				edgesByPoint.put(grid[i*SIZE+j], new HashSet<Point>());
			}
		}
		for(int i = 0; i < grid.length; i++) {
			loadPossiblePoints(grid[i], pr);
			for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
				if(possiblePoints[possibleIndex] == null) {
					continue;
				}
				
				addEdge(grid[i], possiblePoints[possibleIndex]);
			}
		}
		
		// maps code
		for(int n = 0; n < 8; n++) {
			maps.add(new HashMap<Point, Set<Point>>());
		}
		for(int i = 0; i < SIZE*SIZE; i++) {
			Point p = grid[i];

			// init combinations
			Set<Point> possiblePointsSet = new HashSet<Point>();
			loadPossiblePoints(grid[i], pr);
			for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
				if(possiblePoints[possibleIndex] == null) {
					continue;
				}
				
				possiblePointsSet.add(possiblePoints[possibleIndex]);
			}
			
			for(int n = 0; n < possiblePointsSet.size(); n++) {
				maps.get(n).put(p, new HashSet<Point>(possiblePointsSet));
			}
		}
	}
	
	public GridGraph updateGraphWithMovePair(movePair movepr, int playerId) {
//		System.out.printf("Received following movePair: %d,%d to %d,%d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y);
		Point src = getGraphGridPoint(movepr.src.x, movepr.src.y);
		Point target = getGraphGridPoint(movepr.target.x, movepr.target.y);
		
		history.add(new HistoryRecord(playerId, movepr,src.owner, target.owner));

		//update score
		if (src.owner == graphPlayerId) {
			// player lost src
			score -= src.value;
		}
		if (target.owner == graphPlayerId) {
			// player "lost" target. Will be compensated if necessary (see next if)
			score -= target.value;
		}
		if (playerId == graphPlayerId) {
			// player gains target
			score += target.value*2;
		}
		
		src.value = 0;
		target.value *= 2;
		target.owner = playerId;
		
		HashSet<Point> edgesFromSrc = edgesByPoint.get(src);
		assert(edgesFromSrc != null);
		// src.value now is zero; remove it from graph
		Iterator<Point> it = edgesFromSrc.iterator();
	    while (it.hasNext()) {
	    	Point p = it.next();
	    	
	    	// remove p -> src
	    	edgesByPoint.get(p).remove(src);
			// remove src -> p
	        it.remove();
	    }

		HashSet<Point> edgesFromTarget = edgesByPoint.get(target);
		assert(edgesFromTarget != null);
	    // target has a new value now; remove old edges
	    Iterator<Point> it2 = edgesFromTarget.iterator();
	    while (it2.hasNext()) {
	    	Point p = it2.next();
	    	
	    	// remove p -> target
	    	edgesByPoint.get(p).remove(target);
			// remove target -> p
	        it2.remove();
	    }

	    // target has a new value now; create new edges
	    loadPossiblePoints(target, pr);
		for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
			if(possiblePoints[possibleIndex] == null) {
				continue;
			}
			
			if(target.value == possiblePoints[possibleIndex].value) {
				addEdge(target, possiblePoints[possibleIndex]);
			}
		}
		
		// maps code
		// src cannot construct anything now
//		for(int n = 0; n < 8; n++) {
//			maps.get(n).remove(src);
//			// any construction that would use src is now impossible
//			removeConstructionsThatUsePointAtLevel(src, n);
//		}
//
//		// get targetNewLevel
//		int targetNewLevel = -1;
//		// fast logarithm base 2 for power of 2s
//		while(2 << targetNewLevel < target.value) {
//			targetNewLevel++;
//		}
//		
//		// target at targetNewLevel has already been built
//		maps.get(targetNewLevel).remove(target);
//		// any construction that would use target at targetNewLevel is gone
//		removeConstructionsThatUsePointAtLevel(target, targetNewLevel);
		

		return this;
	}
	
	private void removeConstructionsThatUsePointAtLevel(Point p, int level) {
		ArrayList<Point> toBeRemoved = new ArrayList<Point>();

		HashMap<Point, Set<Point>> levelNMap = maps.get(level);
		
		loadPossiblePoints(p, pr);
		for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
			if(possiblePoints[possibleIndex] == null) {
				continue;
			}
			
			Set<Point> set = levelNMap.get(possiblePoints[possibleIndex]);
			if(set != null) {
				set.remove(p);
				if(set.size() == 0) {
					levelNMap.remove(possiblePoints[possibleIndex]);
					toBeRemoved.add(possiblePoints[possibleIndex]);
				}
			}
		}
		
		Iterator<Point> it = toBeRemoved.iterator();
		while(it.hasNext()) {
			Point a = it.next();
			for(int n = level + 1; n < 8; n++) {
				maps.get(n).remove(a);
				removeConstructionsThatUsePointAtLevel(a, n);				
			}
		}
	}
	
	public GridGraph undoGraphByOneMovePair() {
		HistoryRecord record = history.remove(history.size()-1);
		Point src = getGraphGridPoint(record.movepr.src.x, record.movepr.src.y);
		Point target = getGraphGridPoint(record.movepr.target.x, record.movepr.target.y);
		
		//update score
		if (target.owner == graphPlayerId) {
			// player "lost" target
			score -= target.value;
		}
		if (record.srcOriginalOwner == graphPlayerId) {
			// player gains src
			score += target.value/2;
		}
		if (record.targetOriginalOwner == graphPlayerId) {
			// player gains target
			score += target.value/2;
		}
		
		target.value /= 2;
		src.value = target.value;
		src.owner = record.srcOriginalOwner;
		target.owner = record.targetOriginalOwner;

		HashSet<Point> edgesFromSrc = edgesByPoint.get(src);
		assert(edgesFromSrc != null);
		// src.value now has old value; add new connections
		loadPossiblePoints(src, pr);
		for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
			if(possiblePoints[possibleIndex] == null) {
				continue;
			}
			
			if(src.value == possiblePoints[possibleIndex].value) {
				addEdge(src, possiblePoints[possibleIndex]);
			}
		}

		HashSet<Point> edgesFromTarget = edgesByPoint.get(target);
		assert(edgesFromTarget != null);
	    // target has a new value now; remove old edges
	    Iterator<Point> it2 = edgesFromTarget.iterator();
	    while (it2.hasNext()) {
	    	Point p = it2.next();
	    	
	    	// remove p -> target
	    	edgesByPoint.get(p).remove(target);
			// remove target -> p
	        it2.remove();
	    }

	    // target has a new value now; create new edges
	    loadPossiblePoints(target, pr);
		for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
			if(possiblePoints[possibleIndex] == null) {
				continue;
			}
			
			if(target.value == possiblePoints[possibleIndex].value) {
				addEdge(target, possiblePoints[possibleIndex]);
			}
		}

		return this;
	}
	
	private void addEdge(Point a, Point b) {
		assert(a.value == b.value);
		edgesByPoint.get(a).add(b);
		edgesByPoint.get(b).add(a);
	}

	private void loadPossiblePoints(Point src, Pair pr) {
		assert(possiblePoints.length == 8);
		
		possiblePoints[0] = getGraphGridPoint(src.x - pr.p, src.y - pr.q);
		possiblePoints[1] = getGraphGridPoint(src.x - pr.p, src.y + pr.q);
		possiblePoints[2] = getGraphGridPoint(src.x + pr.p, src.y - pr.q);
		possiblePoints[3] = getGraphGridPoint(src.x + pr.p, src.y + pr.q);
		possiblePoints[4] = getGraphGridPoint(src.x - pr.q, src.y - pr.p);
		possiblePoints[5] = getGraphGridPoint(src.x - pr.q, src.y + pr.p);
		possiblePoints[6] = getGraphGridPoint(src.x + pr.q, src.y - pr.p);
		possiblePoints[7] = getGraphGridPoint(src.x + pr.q, src.y + pr.p);
	}
	
	public Point getGraphGridPoint(int x, int y) {
		if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
			return null;
		}
		return grid[SIZE*x + y];
	}
	
	private class HistoryRecord {
		public int playerId;
		public movePair movepr;
		public int targetOriginalOwner;
		public int srcOriginalOwner;
		
		public HistoryRecord(int playerId, movePair movepr, int srcOriginalOwner, int targetOriginalOwner) {
			this.playerId = playerId;
			this.movepr = movepr;
			this.targetOriginalOwner = targetOriginalOwner;
			this.srcOriginalOwner = srcOriginalOwner;
		}
	}
	
	public int getNumberOfEdges() {
		int count = 0;

		Iterator<Entry<Point, HashSet<Point>>> it = edgesByPoint.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Point, HashSet<Point>> pairs = (Map.Entry<Point, HashSet<Point>>)it.next();
	        count += pairs.getValue().size();
	    }
	    
		return count / 2;
	}
	
	public int getScore() { return score; }
	
	public boolean doesPointHasEdges(Point p) {
		HashSet<Point> edges = edgesByPoint.get(getGraphGridPoint(p.x, p.y));
		return edges.size() != 0;
	}
	
	public class Comparators {
        public Comparator<Point> VALUE = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.value - o2.value;
            }
        };
        public Comparator<Point> EDGES = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return edgesByPoint.get(o1).size() - edgesByPoint.get(o2).size();
            }
        };
        public Comparator<Point> EDGESANDVALUE = new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int i = EDGES.compare(o1, o2);
                if (i == 0) {
                    return VALUE.compare(o1, o2);
                }
                return i;
            }
        };
        public Comparator<MovePairTime> MOVES = new Comparator<MovePairTime>() {
            @Override
            public int compare(MovePairTime o1, MovePairTime o2) {
                return o1.moves - o2.moves;
            }
        };
    }
	public Comparators myComparators = new Comparators();
	
	
	public ArrayList<Point> getPointsByNumberOfEdgesByValue() {
		ArrayList<Point> points = new ArrayList<Point>(SIZE*SIZE);
		for(int i = 0; i < SIZE*SIZE; i++) {
			if(grid[i].value == 0) {
				continue;
			}
			points.add(grid[i]);
		}
		Collections.sort(points, Collections.reverseOrder(myComparators.EDGESANDVALUE));
//		for (int i = 0; i < points.size(); i++) {
//        	Point p = points.get(i);
//        	System.out.printf("%d-%d, ", edgesByPoint.get(p).size(), p.value);
//        }
//		System.out.printf("\n");
		return points;
	}
	
	public void printLevelN(int n) {
		System.out.printf("Level %d\n", n);
		HashMap<Point, Set<Point>> levelNMap = maps.get(n);
		if(levelNMap.size() == 0) {
			System.out.printf("-- Empty --\n");
			return;
		}
		
		for(int y = 0; y < SIZE; y++) {
			for(int x = 0; x < SIZE; x++) {
				Point p = getGraphGridPoint(x, y);
				
				if(levelNMap.containsKey(p)) {
					System.out.printf("(%d, %d)(%d, %d), ", x,y, levelNMap.get(p).iterator().next().x, levelNMap.get(p).iterator().next().y);
				} else {
					System.out.printf("0, ");
				}
			}
			System.out.printf("\n");
		}
	}
	
	public class MovePairTime {
		public movePair movepr;
		public int moves;
		
		public MovePairTime(movePair movepr, int moves) {
			this.movepr = movepr;
			this.moves = moves;
		}
	}
	
	public ArrayList<MovePairTime> movePairByTime() {
		ArrayList<MovePairTime> list = new ArrayList<MovePairTime>();
		
		for(int i = 0; i < SIZE*SIZE; i++) {
			Point p = grid[i];
			
			// TODO play with this number: shoudl we worry about piles of value 1 and 2?
			if(p.value < 4) {
				continue;
			}

			MovePairTime mpt = getMovePairTimeOfPoint(p,0);
			if (mpt == null) {
				continue;
			}
			list.add(mpt);
		}
		Collections.sort(list, myComparators.MOVES);
		return list;
	}

	public MovePairTime getMovePairTimeOfPoint(Point p, int movesCounter) {
//		System.out.printf("getMovePairTimeOfPoint %d\n", p.value);
		loadPossiblePoints(p, pr);
		for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
			if(possiblePoints[possibleIndex] == null) {
				continue;
			}
			
			if(possiblePoints[possibleIndex].value > p.value) {
				continue;
			} else if(possiblePoints[possibleIndex].value == p.value) {
				movePair movepr = new movePair();
				movepr.src = possiblePoints[possibleIndex];
				movepr.target = p;
				MovePairTime mpt = new MovePairTime(movepr, movesCounter+1);
				return mpt;
			} else if(possiblePoints[possibleIndex].value == p.value/2 && possiblePoints[possibleIndex].value != 0) {
				return getMovePairTimeOfPoint(possiblePoints[possibleIndex], movesCounter + 1);
			} else {
				continue;
			}
		}
		return null;
	}
	
}
