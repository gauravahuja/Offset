package offset.group6;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class GridGraph {
	public static final int SIZE = 32;
	
	public Point[] grid = new Point[SIZE*SIZE];
	public HashMap<Point, HashSet<Point>> edgesByPoint;
	public ArrayList<HistoryRecord> history;
	public Pair pr;
	
	private Point[] pointsBuffer = new Point[8];
	
	public GridGraph(Pair graphPair) {
		edgesByPoint = new HashMap<Point, HashSet<Point>>();
		for (int i=0; i<SIZE; i++) {
			for (int j=0; j<SIZE; j++) {
				grid[i*SIZE+j] = new Point(i, j, 1, -1);
				edgesByPoint.put(grid[i*SIZE+j], new HashSet<Point>());
			}
		}
		history = new ArrayList<HistoryRecord>();
		pr = graphPair;
		for(int i = 0; i < grid.length; i++) {
			Point[] possiblePoints = loadAllNextMoves(grid[i], pr);
			for(int possibleIndex = 0; possibleIndex < possiblePoints.length; possibleIndex++) {
				if(possiblePoints[possibleIndex] == null) {
					continue;
				}
				
				addEdge(grid[i], possiblePoints[possibleIndex]);
			}
		}
	}
	
	public GridGraph UpdateGraphWithMovePair(movePair movepr, int playerId) {
//		System.out.printf("Received following movePair: %d,%d to %d,%d\n", movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y);
		Point src = getGraphGridPoint(movepr.src.x, movepr.src.y);
		Point target = getGraphGridPoint(movepr.target.x, movepr.target.y);
		history.add(new HistoryRecord(playerId, movepr,src.owner, target.owner));
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
	    Point[] possiblePoints = loadAllNextMoves(target, pr);
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
	
	public GridGraph UndoGraphByOneMovePair() {
		HistoryRecord record = history.remove(history.size()-1);
		Point src = getGraphGridPoint(record.movepr.src.x, record.movepr.src.y);
		Point target = getGraphGridPoint(record.movepr.target.x, record.movepr.target.y);
		target.value /= 2;
		src.value = target.value;
		src.owner = record.srcOriginalOwner;
		target.owner = record.targetOriginalOwner;

		HashSet<Point> edgesFromSrc = edgesByPoint.get(src);
		assert(edgesFromSrc != null);
		// src.value now has old value; add new connections
		Point[] possiblePoints = loadAllNextMoves(src, pr);
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
	    possiblePoints = loadAllNextMoves(target, pr);
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
	
	public int GetNumberOfPossibleMoves() {
		int count = 0;
		for(int i = 0; i < grid.length; i++) {
			count += edgesByPoint.get(grid[i]).size();
		}
		return count / 2;
	}

	private Point[] loadAllNextMoves(Point src, Pair pr) {
		assert(pointsBuffer.length == 8);
		
		pointsBuffer[0] = getGraphGridPoint(src.x - pr.p, src.y - pr.q);
		pointsBuffer[1] = getGraphGridPoint(src.x - pr.p, src.y + pr.q);
		pointsBuffer[2] = getGraphGridPoint(src.x + pr.p, src.y - pr.q);
		pointsBuffer[3] = getGraphGridPoint(src.x + pr.p, src.y + pr.q);
		pointsBuffer[4] = getGraphGridPoint(src.x - pr.q, src.y - pr.p);
		pointsBuffer[5] = getGraphGridPoint(src.x - pr.q, src.y + pr.p);
		pointsBuffer[6] = getGraphGridPoint(src.x + pr.q, src.y - pr.p);
		pointsBuffer[7] = getGraphGridPoint(src.x + pr.q, src.y + pr.p);
		return pointsBuffer;
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
}
