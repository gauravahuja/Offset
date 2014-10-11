package offset.oct8_group8;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class GameState {

	private Point[][] grid;
	public Pair playerPair;
	public Pair opponentPair;
	public int playerId;
	public int opponentId;
	public int playerScore;
	public int opponentScore;
	public MovePackage playerMoves;
	public MovePackage opponentMoves;
	private Point lastPoint;
	
	public static int size = 32;
	
	public GameState (Point[] oneDGrid, Pair playerPair, Pair opponentPair, int playerId, int opponentId, movePair lastMove) {
		grid = new Point[size][size];
		for (Point point : oneDGrid) {
			grid[point.x][point.y] = point;
		}
		this.playerPair = playerPair;
		this.opponentPair = opponentPair;
		this.playerId = playerId;
		this.opponentId = opponentId;
		this.playerScore = calculateScore(playerId);
		this.opponentScore = calculateScore(opponentId);
		playerMoves = new MovePackage(playerId, opponentId, playerPair, grid);
		opponentMoves = new MovePackage(opponentId, playerId, opponentPair, grid);
		if (lastMove != null)
			lastPoint = lastMove.target;
	}
	
	public GameState(GameState oldGame) {
		grid = new Point[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				grid[i][j] = new Point(oldGame.grid[i][j]);
			}
		}
		this.playerPair = oldGame.playerPair;
		this.opponentPair = oldGame.opponentPair;
		this.playerMoves = oldGame.playerMoves.clone();
		this.opponentMoves = oldGame.opponentMoves.clone();
		playerMoves.grid = grid;
		opponentMoves.grid = grid;
		lastPoint = null;
	}
	
	public void makeMove(movePair movepr, int playerId) {
		Point target = movepr.target;
		Point src = movepr.src;
		if (src.owner != playerId && target.owner != playerId) {
			playerScore += src.value * 2;
			opponentScore -= src.value * 2;
		}
		else if (src.owner != playerId || target.owner != playerId) {
			playerScore += src.value;
			opponentScore -= src.value;
		}
		grid[target.x][target.y].value = grid[target.x][target.y].value + grid[src.x][src.y].value;
		grid[src.x][src.y].value = 0; 
		grid[target.x][target.y].owner = playerId;
		grid[src.x][src.y].owner = -1;
		playerMoves.registerChange(movepr);
		opponentMoves.registerChange(movepr);
		lastPoint = movepr.target;
	}
	
	private int calculateScore(int id) {
    	int score =0;
    	for (int i=0; i<size; i++) {
    		for (int j =0; j<size; j++) {
    			if (grid[i][j].owner ==id) {
    				score = score+grid[i][j].value;
    			}
    		}
    	}
    	return score;
    }
	
	public List<movePair> getAvailableMoves(int id) {
		if (id == playerId)
			return playerMoves.getPossibleMovesByPriority(Player.MAX_MOVES_TO_CHECK, lastPoint);
		else
			return opponentMoves.getPossibleMovesByPriority(Player.MAX_MOVES_TO_CHECK, lastPoint);
	}
	
	
	public movePair lowerOpponentMoves(Pair pr, Pair pr0) {
		movePair next=new movePair();
		if(!possibleMoves(grid, pr).isEmpty()){
		 next= possibleMoves(grid, pr).get(0);
		}else{
			next=getAnyMove(grid, pr);
		}
		next.move = false;
		
		Comparator<movePair>mPairComparator=new Comparator<movePair>(){
			public int compare(movePair p1, movePair p2){
					if(p1.src.value<p2.src.value){
						return 1;
					}else if(p1.src.value==p2.src.value){
						return 0;
					}else{
						return -1;
					}
				
			}
			
			
		};
		
		PriorityQueue<movePair>maxHeap=new PriorityQueue<>(7,mPairComparator);
			
		int leastOpponentMove = Integer.MAX_VALUE;
		for (movePair mp : possibleMoves(grid, pr)) {
			Point[][] newGrid = gridAfterMove(grid, mp, this.playerId);
			int t=opponentPossibleMoves(newGrid, pr0);
			if (t < leastOpponentMove&&t!=0){
				leastOpponentMove = t;
				maxHeap.add(mp);
				next = mp;
				next.move = true;
			} else if(t==0){
				if((mp.src.owner!=playerId || mp.target.owner!=playerId)){
					
					if((next.src.owner!=playerId || next.target.owner!=playerId)&&next.src.value>mp.src.value){
						continue;
					}else{
						next=mp;
					}		
				}
				
			}
		}
		/*
		while(!maxHeap.isEmpty()){
			movePair temp=maxHeap.poll();
			if(!isStealable(grid, temp.target, pr0)){
				return maxHeap.poll();
			}
			
		}
		*/
		return next;
	}
	
	public boolean isStealable(Point [][] grid, Point spot, Pair pr0) {
		for(Pair direction : MovePackage.moveForPair(pr0)) {
			if (MovePackage.isValidBoardIndex(spot.x + direction.p, spot.y + direction.q)) {
				Point possibleMatch = pointAtIndex(grid, spot.x + direction.p, spot.y + direction.q);
				if (spot.value * 2 == possibleMatch.value) {
					return true;
				}
			}
		}

		return false;
	}

	public movePair maximizeMyMoves(Pair pr) {
		movePair next=new movePair();
			
		int myMaxMove = Integer.MIN_VALUE;
		
		for (movePair mp : possibleMoves(grid, pr)) {
			Point[][] newGrid = gridAfterMove(grid, mp, this.playerId);
			int t=opponentPossibleMoves(newGrid, pr); // my moves
			if (t > myMaxMove){
				myMaxMove = t;
				next = mp;
				next.move = true;
			} 
		}
		return next;
	}
	
	
	
	public List<movePair> getAvailableMoves(Pair pr, int playerId) {
		int opponentId = 0;
		if (playerId == 0)
			opponentId = 1;
		List<movePair>rtn = new ArrayList<movePair>();				
		List<movePair>p1 = new ArrayList<movePair>();
		List<movePair>p2 = new ArrayList<movePair>();
		List<movePair>p3 = new ArrayList<movePair>();
		List<movePair>p4 = new ArrayList<movePair>();		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int i_pr=0; i_pr<size; i_pr++) {
					for (int j_pr=0; j_pr <size; j_pr++) {
						movePair movepr = new movePair();
						movepr.move = false;
						movepr.src = grid[i][j];
						movepr.target = grid[i_pr][j_pr];
						if (MovePackage.validateMove(movepr, pr)) {
							movepr.move = true;						
							if(movepr.src.owner == opponentId && movepr.target.owner == opponentId)
								p1.add(movepr);
							else if(movepr.src.owner != movepr.target.owner && movepr.src.value !=1)
								p2.add(movepr);
							else if(movepr.src.owner != movepr.target.owner)
								p3.add(movepr);
							else p4.add(movepr);		
						}
					}
				}
			}
		}
		
		//Create the rtn list in decreasing order of priority
		if(p1.size()>0)
			rtn.addAll(p1);
		if(p2.size()>0)
			rtn.addAll(p2);
		if(p3.size()>0)
			rtn.addAll(p3);
		if(p4.size()>0)
			rtn.addAll(p4);
		return rtn;
	}
	
	
	public movePair lowerOpponentMoves(Point[][] grid, Pair pr, Pair pr0) {
		movePair next = new movePair();
		next.move = false;
		
		int leastOpponentMove = Integer.MAX_VALUE;
		
		//for (movePair mp : getAvailableMoves(pr, this.playerId)) {
		for (movePair mp : possibleMoves(grid, pr)) {
			Point[][] newGrid = gridAfterMove(grid, mp, this.playerId);
			int t=opponentPossibleMoves(newGrid, pr0);
			
			
			if (t < leastOpponentMove){
				leastOpponentMove = t;
				next = mp;
				next.move = true;
			}
		}
		return next;
	}
	
	public static int opponentPossibleMoves(Point[][] grid, Pair pr) {
		int availableMoves=0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Point currentPoint = pointAtIndex(grid, i, j);
				if (currentPoint.value == 0) {
					continue;
				}
				for (Pair d : MovePackage.moveForPair(pr)) {
					if (MovePackage.isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = pointAtIndex(grid, i + d.p, j + d.q);
						if (currentPoint.value == possiblePairing.value) {
							availableMoves+=2;
						}
					}
					
				}
			}
		}
		
		return availableMoves;
	}
	
	


	
	
	public static Point[][] gridAfterMove(Point[][] grid, movePair move, int newOwner) {
		Point[][] newGrid = new Point[grid.length][grid.length];
		for (int i = 0; i < grid.length; i++) {
			for(int j=0;j<grid.length;j++){
				Point newPoint = new Point();
				newPoint.change = grid[i][j].change;
				newPoint.owner = grid[i][j].owner;
				newPoint.value = grid[i][j].value;
				newPoint.x = grid[i][j].x;
				newPoint.y = grid[i][j].y;
				newGrid[i][j] = newPoint;
				
			}
			
		}
		
		Point src = move.src;
		Point target = move.target;
			
		Point newSrc = pointAtIndex(newGrid, src.x, src.y);
		Point newTarget = pointAtIndex(newGrid, target.x, target.y);
		
		newTarget.value += newSrc.value;
		newTarget.owner = newOwner;
		newTarget.change = true;
		newSrc.value = 0;
		newSrc.owner = -1;
		
		return newGrid;
	}

	public static Point pointAtIndex(Point[][] grid, int i, int j) {
		//return grid[i*size + j];
		return grid[i][j];
	}
	
	
	ArrayList<movePair> possibleMoves(Point[][] grid, Pair pr) {
		ArrayList<movePair> possible = new ArrayList<movePair>();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Point currentPoint = pointAtIndex(grid, i, j);
				if (currentPoint.value == 0) {
					continue;
				}
				for (Pair d : MovePackage.moveForPair(pr)) {
					if (MovePackage.isValidBoardIndex(i + d.p, j + d.q)){
						Point possiblePairing = pointAtIndex(grid, i + d.p, j + d.q);
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
	public static movePair getAnyMove(Point[][] grid,Pair pr){
		movePair movepr=new movePair();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int i_pr=0; i_pr<size; i_pr++) {
				for (int j_pr=0; j_pr <size; j_pr++) {
					movepr.move = false;
					movepr.src = grid[i][j];
					movepr.target = grid[i_pr][j_pr];
					if (MovePackage.validateMove(movepr, pr)) {
						movepr.move = true;
						return movepr;
					}
				}
				}
			
			}
		}
		return movepr;
		
	}

	
}
