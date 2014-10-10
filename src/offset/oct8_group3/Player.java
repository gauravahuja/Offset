package offset.oct8_group3;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
	
	int ourId, theirId;

	boolean initialized;
	Point[] grid;
	
	ArrayList<ArrayList> history;


	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub

		this.initialized = false;
	}

	public void init() {

	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		if (!this.initialized) {
			this.initialized = true;
			// I assume we'll have to do some stuff here the first time
			
		      if (history.size() % 2 == 1) {
		          ourId = 1;
		          theirId = 0;
		      } else {
		          ourId = 0;
		          theirId = 1;
		      }
		}
		this.grid = grid;
		this.history = history;


		// Everything else here is recycled from dumbPlayer
		//		movePair movepr = new movePair();
		//		for (int i = 0; i < size; i++) {
		//			for (int j = 0; j < size; j++) {
		//				for (int i_pr=0; i_pr<size; i_pr++) {
		//				for (int j_pr=0; j_pr <size; j_pr++) {
		//					movepr.move = false;
		//					movepr.src = grid[i*size+j];
		//					movepr.target = grid[i_pr*size+j_pr];
		//					if (validateMove(movepr, pr)) {
		//						movepr.move = true;
		//						return movepr;
		//					}
		//				}
		//				}
		//
		//			}
		//		}
		//		return movepr;
		return oneLevelMove(grid, pr, pr0);
	}

	movePair oneLevelMove(Point [] grid, Pair pr, Pair pr0) {
		int fewestCompetitorMoves = Integer.MAX_VALUE;

		int lowestAttainableScore = Integer.MAX_VALUE;
		movePair lowestAttainableScoreMove = new movePair();
		int highestAttainableScoreUnstealable = -1;
		movePair highestAttainableScoreUnstealableMove = new movePair();

		for (movePair mp : possibleMoves(grid, pr)) {
			// Create new grid with one of the possible moves
			Point[] newGrid = applyMoveToGrid(grid, mp, this.id);
			// Create ArrayList with all possible opponent moves in new grid 
			int possibleOpponentMovesNum = possibleMovesNum(newGrid, pr0);
			
			boolean opponentCanStealSpot = opponentCanStealSpot(newGrid, mp.target, pr0);
			int pointsObtainedForMove = pointsObtainedForMove(mp);
			
			if (possibleOpponentMovesNum < fewestCompetitorMoves) {
				lowestAttainableScore = pointsObtainedForMove;
				lowestAttainableScoreMove = mp;
				
				if (opponentCanStealSpot) {
					highestAttainableScoreUnstealable = -1;
				} else {
					highestAttainableScoreUnstealable = pointsObtainedForMove;
					highestAttainableScoreUnstealableMove = mp;
				}
				
				fewestCompetitorMoves = possibleOpponentMovesNum;
			} else if (possibleOpponentMovesNum == fewestCompetitorMoves) {
				if (opponentCanStealSpot && 
						pointsObtainedForMove < lowestAttainableScore) {
					lowestAttainableScore = pointsObtainedForMove;
					lowestAttainableScoreMove = mp;
				}
				
				if (!opponentCanStealSpot && // if the opponent cannot steal the spot
						pointsObtainedForMove > highestAttainableScoreUnstealable) { // highest score unstealable
					highestAttainableScoreUnstealable = pointsObtainedForMove;
					highestAttainableScoreUnstealableMove = mp;
				}
				
			}
		}
		
		movePair nextMove = new movePair();
		nextMove.move = false;
		
		System.out.println("highestattainable = " + highestAttainableScoreUnstealable +",lowestattainable=" + lowestAttainableScore);
		if (highestAttainableScoreUnstealable > -1) {
			nextMove = highestAttainableScoreUnstealableMove;
			nextMove.move = true;
			System.out.println("Going with highest attainable unstealable");
		} else if (lowestAttainableScore < Integer.MAX_VALUE) {
			nextMove = lowestAttainableScoreMove;
			nextMove.move = true;
			System.out.println("Going with lowest attainable stealable");
		}
		
		//System.out.println("fewestCompetitorMoves: " + fewestCompetitorMoves);
		return nextMove;
	}
	
	private int pointsObtainedForMove(movePair mp) {
		int pointsObtained = 0;
		
		if(mp.src.owner == theirId || mp.src.owner == -1) {
			pointsObtained += mp.src.value;
		}
		
		if (mp.target.owner == theirId || mp.src.owner == -1) {
			pointsObtained += mp.target.value;
		}
		
		return pointsObtained;
	}

	private boolean opponentCanStealSpot(Point [] grid, Point spot, Pair pr0) {
		for(Pair direction : allDirectionsForPair(pr0)) {
			if (isValidBoardIndex(spot.x + direction.p, spot.y + direction.q)) {
				Point possibleMatch = pointAtGridIndex(grid, spot.x + direction.p, spot.y + direction.q);
				if (spot.value * 2 == possibleMatch.value) {
					return true;
				}
			}
		}

		return false;
	}

	ArrayList<movePair> possibleMoves(Point[] grid, Pair pr) {
		ArrayList<movePair> possible = new ArrayList<movePair>();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Point currentPoint = pointAtGridIndex(grid, i, j);
				if (currentPoint.value == 0) {
					continue;
				}
				for (Pair d : directionsForPair(pr)) {
					if (isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = pointAtGridIndex(grid, i + d.p, j + d.q);
						if (currentPoint.value == possiblePairing.value) {
							possible.add(new movePair(true, currentPoint, possiblePairing));
							possible.add(new movePair(true, possiblePairing, currentPoint));
						}
					}

				}
			}
		}

		return possible;
	}

	int possibleMovesNum(Point[] grid, Pair pr) {
		int possible = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Point currentPoint = pointAtGridIndex(grid, i, j);
				if (currentPoint.value == 0) {
					continue;
				}
				for (Pair d : directionsForPair(pr)) {
					if (isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = pointAtGridIndex(grid, i + d.p, j + d.q);
						if (currentPoint.value == possiblePairing.value) {
							possible = possible + 2;
						}
					}

				}
			}
		}

		return possible;
	}
	Point[] applyMoveToGrid(Point[] grid, movePair move, int newOwner) {
		Point[] newGrid = new Point[grid.length];
		for (int i = 0; i < grid.length; i++) {
			Point newPoint = new Point();
			newPoint.change = grid[i].change;
			newPoint.owner = grid[i].owner;
			newPoint.value = grid[i].value;
			newPoint.x = grid[i].x;
			newPoint.y = grid[i].y;
			newGrid[i] = newPoint;
		}

		Point src = move.src;
		Point target = move.target;

		assert isValidBoardIndex(src) : "Source point out of bounds";
		assert isValidBoardIndex(target) : "Destination point out of bounds";
		assert src.value == target.value : "Cannot combine points with different values";

		Point newSrc = pointAtGridIndex(newGrid, src.x, src.y);
		Point newTarget = pointAtGridIndex(newGrid, target.x, target.y);

		newTarget.value += newSrc.value;
		newTarget.owner = newOwner;
		newTarget.change = true;
		newSrc.value = 0;
		newSrc.owner = -1;

		return newGrid;
	}

	Pair[] directionsForPair(Pair pr) {
		Pair[] directions = new Pair[4];
		directions[0] = new Pair(pr); 
		directions[1] = new Pair(pr.p, -pr.q);
		directions[2] = new Pair(pr.q, pr.p);
		directions[3] = new Pair(pr.q, -pr.p);
		return directions;
	}

	Pair[] allDirectionsForPair(Pair pr) {
		// Going clockwise
		Pair[] directions = new Pair[8];
		directions[0] = new Pair(pr.p, pr.q); 
		directions[1] = new Pair(pr.q, pr.p);
		directions[2] = new Pair(pr.q, -pr.p);
		directions[3] = new Pair(pr.p, -pr.q);
		directions[4] = new Pair(-pr.p, -pr.q);
		directions[5] = new Pair(-pr.q, -pr.p);
		directions[6] = new Pair(-pr.q, pr.p);
		directions[7] = new Pair(-pr.p, pr.q);
		
		return directions;
	}

	Point pointAtGridIndex(Point[] grid, int i, int j) {
		return grid[i*size + j];
	}

	boolean isValidBoardIndex(Point p) {
		return isValidBoardIndex(p.x, p.y);
	}

	boolean isValidBoardIndex(int i, int j) {
		if (i < 0 || i >= size || j < 0 || j >= size) {
			return false;
		}
		return true;
	}

	boolean validateMove(movePair movepr, Pair pr) {
		// This is also from dumbPlayer, and seems like something we won't have to use
		Point src = movepr.src;
		Point target = movepr.target;
		boolean rightposition = false;
		if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
			rightposition = true;
		}
		if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
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
