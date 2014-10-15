package offset.oct13_group1;

import java.util.*;
import java.io.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	
	int size = 32;

  int p;
  int q;

  int opp_p;
  int opp_q;

  int opp_id;

  int opponentMoveDelta;
  int playerMoveDelta;

  File f1; 
  File f2;
  PrintWriter writer1;
  PrintWriter writer2;

  Point[] grid;

	public Player(Pair prin, int idin) throws Exception {
		super(prin, idin);

    // write training files for Sameer
    // f1 = new File("x.txt");
    // f2 = new File("y.txt");
    // writer1 = new PrintWriter(f1);
    // writer2 = new PrintWriter(f2);

		// TODO Auto-generated constructor stub
	}

	public void init() {

	}

	public movePair move(Point[] initGrid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {

    // initialize the grid, p, and q at the class level so we don't have to
    // pass them as parameters to helper functions
    p = pr.p;
    q = pr.q;
    opp_p = pr0.p;
    opp_q = pr0.q;
    opp_id = this.id == 0 ? 1 : 0;
    grid = initGrid;

    // log moves for sameer
    // logPossibleOpponentMoves();

    //movePair nextMove = oneLevelMove(grid, pr, pr0);

    movePair nextMove = returnFilteredMove(grid, pr);

  	return nextMove == null ? new movePair() : nextMove;
  }

  public movePair returnFilteredMove(Point[] grid, Pair pr) {
    ArrayList<movePair> possibleMoves = possibleMoves(grid, pr);
    ArrayList<movePair> opponentMinMoves = opponentMinimizationMoves(grid, possibleMoves);
    ArrayList<movePair> playerMaxMoves = playerMaximizationMoves(grid, opponentMinMoves);
    ArrayList<movePair> scoreMinMoves = scoreMinimizationMoves(grid, playerMaxMoves);
    //if (playerMaxMoves.size() == 0)
    //  return null;
    //return playerMaxMoves.get(0);
    if (scoreMinMoves.size() == 0)
      return null;
    return scoreMinMoves.get(0);
  }

  /**
   * return an ArrayList of all possible movePairs for a given grid, p, q
   */
  public ArrayList<movePair> possibleMoves(Point[] grid, Pair pr) {
  	ArrayList<movePair> possible = new ArrayList<movePair>();
  	for (int i = 0; i < size; i++) {
  		for (int j = 0; j < size; j++) {
  			Point currentPoint = pointAtGridIndex(grid, i, j);
  			if (currentPoint.value == 0)
  				continue;
  			for (Pair d : directionsForPair(pr)) {
  				if (isValidBoardIndex(i + d.p, j + d.q)) {
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

  /**
   * method borrowed from group3
   * returns a new grid with a move applied to it
   */
  Point[] applyMoveToGrid(Point[] grid, movePair move, int newOwner) {
    Point[] newGrid = new Point[grid.length];
  	for (int i = 0; i < grid.length; i++) {
  		Point newPoint = new Point(grid[i].x, grid[i].y, grid[i].value, grid[i].owner);
      newPoint.change = grid[i].change;
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

  /**
   * make a move, looking one move ahead to minimize opponent's moves
   */
  public movePair oneLevelMove(Point[] grid, Pair pr, Pair pr0) {
  	movePair nextMove = null;
  	int fewestCompetitorMoves = Integer.MAX_VALUE;
    int maxOurMoves = Integer.MIN_VALUE;
    ArrayList<movePair> possibleMoves = possibleMoves(grid, pr);
    System.out.println("my possible moves: " + possibleMoves.size());
    ArrayList<movePair> opponentMoves = possibleMoves(grid, pr0);
    System.out.println("opponent's possible moves: " + opponentMoves.size());
  	for (movePair mp : possibleMoves) {
  		Point[] newGrid = applyMoveToGrid(grid, mp, this.id);
  		ArrayList<movePair> possibleOpponentMoves = possibleMoves(newGrid, pr0);
  		if (possibleOpponentMoves.size() <= fewestCompetitorMoves) {
  			fewestCompetitorMoves = possibleOpponentMoves.size();
  			nextMove = mp;
  			nextMove.move = true;
        // if we can decrease our opponent moves by 64 (the practical maximum),
        // we break
        if (opponentMoves.size() - possibleOpponentMoves.size() == 64)
          break;
  		} 
  	}
  	return nextMove;
  }

  public ArrayList<movePair> scoreMinimizationMoves(Point[] grid, ArrayList<movePair> possibleMoves) {
    ArrayList<movePair> scoreMinimizationMoves = new ArrayList<movePair>();
    int minScore = Integer.MAX_VALUE;
    System.out.println("moves before score optimization: " + possibleMoves.size());
    for (movePair mp : possibleMoves) {
      if (mp.src.value == minScore) {
        scoreMinimizationMoves.add(mp);
      } else if (mp.src.value < minScore) {
        minScore = mp.src.value;
        scoreMinimizationMoves.clear();
        scoreMinimizationMoves.add(mp);
      }
    }
    System.out.println("moves after score optimization: " + scoreMinimizationMoves.size());
    return scoreMinimizationMoves;
  }

  public ArrayList<movePair> opponentMinimizationMoves(Point[] grid, ArrayList<movePair> possibleMoves) {
    ArrayList<movePair> opponentMinimizationMoves = new ArrayList<movePair>();
    int fewestCompetitorMoves = Integer.MAX_VALUE;
    ArrayList<movePair> opponentMoves = possibleMoves(grid, new Pair(opp_p, opp_q));
    System.out.println("opponent moves: " + opponentMoves.size());
    for (movePair mp : possibleMoves) {
  		Point[] newGrid = applyMoveToGrid(grid, mp, this.id);
      int numPossibleOpponentMoves = possibleMoves(newGrid, new Pair(opp_p, opp_q)).size();
  		if (numPossibleOpponentMoves == fewestCompetitorMoves) {
        opponentMinimizationMoves.add(mp);
      } else if (numPossibleOpponentMoves < fewestCompetitorMoves) {
  		  fewestCompetitorMoves = numPossibleOpponentMoves;
        opponentMinimizationMoves.clear();
        opponentMinimizationMoves.add(mp);
      }
    }
    opponentMoveDelta = fewestCompetitorMoves - opponentMoves.size();
    System.out.println("opponent move delta: " + opponentMoveDelta);
    return opponentMinimizationMoves;
  }

  public ArrayList<movePair> playerMaximizationMoves(Point[] grid, ArrayList<movePair> possibleMoves) {
    ArrayList<movePair> playerMaximizationMoves = new ArrayList<movePair>();
    int mostPlayerMoves = Integer.MIN_VALUE;
    ArrayList<movePair> playerMoves = possibleMoves(grid, new Pair(p, q));
    System.out.println("player moves: " + playerMoves.size());
    for (movePair mp : possibleMoves) {
  		Point[] newGrid = applyMoveToGrid(grid, mp, this.id);
      int numPossiblePlayerMoves = possibleMoves(newGrid, new Pair(p, q)).size();
  		if (numPossiblePlayerMoves == mostPlayerMoves) {
        playerMaximizationMoves.add(mp);
      } else if (numPossiblePlayerMoves > mostPlayerMoves) {
  		  mostPlayerMoves = numPossiblePlayerMoves;
        playerMaximizationMoves.clear();
        playerMaximizationMoves.add(mp);
      }
    }
    playerMoveDelta = mostPlayerMoves - playerMoves.size();
    System.out.println("player move delta: " + playerMoveDelta);
    return playerMaximizationMoves;
  }
  
  /**
   * check all 8 possible moves
   */
  Pair[] directionsForPair(Pair pr) {
  	Pair[] directions = new Pair[8];
  	directions[0] = new Pair(pr); 
  	directions[1] = new Pair(pr.p, -pr.q);
    directions[2] = new Pair(-pr.p, pr.q);
    directions[3] = new Pair(-pr.p, -pr.q);
    directions[4] = new Pair(pr.q, pr.p);
    directions[5] = new Pair(pr.q, -pr.p);
    directions[6] = new Pair(-pr.q, pr.p);
    directions[7] = new Pair(-pr.q, -pr.p);
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

  /**
   * validate a move on the grid, passed in as a movePair
   */
  boolean validateMove(movePair movepr) {
    	
  	Point src = movepr.src;
  	Point target = movepr.target;
  	boolean rightposition = false;
  	if (Math.abs(target.x - src.x) == Math.abs(p) && Math.abs(target.y - src.y) == Math.abs(q)) {
  		rightposition = true;
  	}
  	if (Math.abs(target.x - src.x) == Math.abs(q) && Math.abs(target.y - src.y) == Math.abs(p)) {
  		rightposition = true;
  	}

    if (rightposition && src.value == target.value && src.value > 0) {
    	return true;
    } else {
    	return false;
    }
  }

}

