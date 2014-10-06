package offset.common;

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

		return this;
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
	
	private Point getGraphGridPoint(int x, int y) {
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

}
