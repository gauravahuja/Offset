package offset.oct8_group8;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class MovePackage implements Cloneable{
	private int myId;
	private int opponentId;
	public Point[][] grid;
	Set<movePair> doubleOpponentMoves;
	Set<movePair> singleOpponentMoves;
	Set<movePair> doubleMyMoves;
	Set<movePair> unclaimedMoves;
	Pair myPair;
	boolean initi;
	
	public MovePackage(int myId, int opponentId, Pair myPair, Point[][] grid) {
		this.myId = myId;
		this.opponentId = opponentId;
		doubleOpponentMoves = new HashSet<movePair>();
		singleOpponentMoves = new HashSet<movePair>();
		doubleMyMoves = new HashSet<movePair>();
		unclaimedMoves = new HashSet<movePair>();
		this.myPair = myPair;
		this.grid = grid;
		initi = false;
	}
	
	public MovePackage clone() {
		MovePackage rtn = new MovePackage(myId, opponentId, myPair, grid);
		rtn.doubleOpponentMoves = new HashSet<movePair>(doubleOpponentMoves);
		rtn.singleOpponentMoves = new HashSet<movePair>(singleOpponentMoves);
		rtn.unclaimedMoves = new HashSet<movePair>(unclaimedMoves);
		rtn.doubleMyMoves = new HashSet<movePair>(doubleMyMoves);
		rtn.initi = true;
		return rtn;
	}
	
	public List<movePair> getPossibleMovesByPriority(int numberToReturn, Point lastTarget) {
		List<movePair> rtn = new ArrayList<movePair>();
		if (!initi) {
			initializeMoves();
		}
		ArrayList<movePair> movesNearOpponent = new ArrayList<movePair>();
		//lastTarget is passed in null if we're at the start of a game
		if (lastTarget != null && lastTarget.owner == opponentId) {
			int movesToAdd = 0;
			int i = lastTarget.x;
			int j = lastTarget.y;
			for (Pair d : moveForPair(myPair)) {
				if (isValidBoardIndex(i + d.p, j + d.q)) {
					Point possiblePairing = grid[i+d.p][j+d.q];
					for (Pair d2 : moveForPair(myPair)) {
						if (isValidBoardIndex(i+d.p+d2.p, j +d.q+d2.q)){
							Point possiblePairingPairing = grid[i+d.p+d2.p][j+d.q+d2.q];
							if (possiblePairingPairing.value == possiblePairing.value && possiblePairing.value > 0) {
								movePair movepr = new movePair();
								movepr.src = grid[i+d.p][j+d.q];
								movepr.target = grid[i+d.p+d2.p][j+d.q+d2.q];
								movepr.move = true;
								movesNearOpponent.add(movepr);
								movesToAdd++;
								if (movesToAdd > 3)
									break;
							}
						}
					}
					if (movesToAdd > 3)
						break;
				}
			}
		}
		rtn.addAll(movesNearOpponent);
		rtn.addAll(doubleOpponentMoves);
		if (rtn.size() >= numberToReturn)
			return rtn;
		rtn.addAll(singleOpponentMoves);
		if (rtn.size() >= numberToReturn)
			return rtn;
		rtn.addAll(doubleMyMoves);
		if (rtn.size() >= numberToReturn)
			return rtn;
		rtn.addAll(unclaimedMoves);
		if (rtn.size() >= numberToReturn)
			return rtn;
		return rtn;
	}
	
	public int getNumberOfRemainingMoves() {
		return doubleOpponentMoves.size() + singleOpponentMoves.size() + doubleMyMoves.size() + unclaimedMoves.size();
	}
	
	public void registerChange(movePair oldMove) {
		ArrayList<Set<movePair>> moves = new ArrayList<Set<movePair>>();
		moves.add(doubleOpponentMoves);
		moves.add(singleOpponentMoves);
		moves.add(doubleMyMoves);
		moves.add(unclaimedMoves);
		Point[] changedPoints = new Point[2];
		changedPoints[0] = oldMove.src;
		changedPoints[1] = oldMove.target;
		for (Set<movePair> moveSet : moves) {
			for (Point point : changedPoints) {
				for (Iterator<movePair> i = moveSet.iterator(); i.hasNext();) {
					movePair move = i.next();
					if (move.src.equals(point) || move.target.equals(point)) {
						i.remove();
					}
				}
				int i = point.x;
				int j = point.y;
				Point currentPoint = grid[i][j];
				for (Pair d : moveForPair(myPair)) {
					if (isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = grid[i+d.p][j+d.q];
						if (currentPoint.value == possiblePairing.value && currentPoint.value != 0) {
							movePair movepr = new movePair();
							movepr.src = grid[i][j];
							movepr.target = grid[i+d.p][j+d.q];
							movepr.move = true;						
							if(movepr.src.owner == opponentId && movepr.target.owner == opponentId) {
								doubleOpponentMoves.add(movepr);
							}
							else if(movepr.src.owner != movepr.target.owner && movepr.src.value !=1) {
								singleOpponentMoves.add(movepr);
							}
							else if(movepr.src.owner != movepr.target.owner) {
								doubleMyMoves.add(movepr);
							}
							else {
								unclaimedMoves.add(movepr);
							}
						}
					}
				}
			}
		}
	}
	
	private void initializeMoves() {
		for (int i = 0; i < GameState.size; i++) {
			for (int j = 0; j < GameState.size; j++) {
				Point currentPoint = grid[i][j];
				for (Pair d : moveForPair(myPair)) {
					if (isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = grid[i+d.p][j+d.q];
						if (currentPoint.value == possiblePairing.value && currentPoint.value != 0) {
							movePair movepr = new movePair();
							movepr.src = grid[i][j];
							movepr.target = grid[i+d.p][j+d.q];
							movepr.move = true;						
							if(movepr.src.owner == opponentId && movepr.target.owner == opponentId) {
								doubleOpponentMoves.add(movepr);
							}
							else if(movepr.src.owner != movepr.target.owner && movepr.src.value !=1) {
								singleOpponentMoves.add(movepr);
							}
							else if(movepr.src.owner != movepr.target.owner) {
								doubleMyMoves.add(movepr);
							}
							else {
								unclaimedMoves.add(movepr);
							}
						}
					}
				}
			}
		}
	}
	
	public static boolean isValidBoardIndex(int i, int j) {
		return !(i < 0 || i >= GameState.size || j < 0 || j >= GameState.size);
	}
	
	public static boolean isValidBoardIndex(Point p) {
		return isValidBoardIndex(p.x, p.y);
	}
	
	public static Pair[] moveForPair(Pair pr) {
		Pair[] moves = new Pair[8];
		moves[0] = new Pair(pr); 
		moves[1] = new Pair(pr.p, -pr.q);
		moves[2] = new Pair(-pr.p, -pr.q);
		moves[3] = new Pair(-pr.p, -pr.q);
		moves[4] = new Pair(pr.q, pr.p);
		moves[5] = new Pair(-pr.q, pr.p);
		moves[6] = new Pair(pr.q, -pr.p);
		moves[7] = new Pair(-pr.q, -pr.p);
		return moves;
	}
	
	static boolean validateMove(movePair movepr, Pair pr) {
    	Point src = movepr.src;
    	Point target = movepr.target;
    	boolean rightposition = false;
    	if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    		rightposition = true;
    	}
        if (rightposition  && src.value == target.value && src.value >0) {
        	return true;
        }
        else {
        	return false;
        }
    }
	
}
