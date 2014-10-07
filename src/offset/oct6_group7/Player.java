package offset.oct6_group7;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
	int N=100;
	int largestCombine = 0;
	int largestLoss=0;
	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {

	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		movePair movepr = new movePair();
		movePair movepr1 = new movePair();
		int comparisionCombine = 0;
		int comparisionProctect = 0;
	
		if(comparisionCombine==0)
		{
		movepr1 = GreedyCaculation(grid,pr,pr0,64);
		if(movepr.move) { 
			comparisionCombine = largestCombine;
		}
		}
		
		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,32);
		if(movepr.move) { 
			comparisionCombine = largestCombine;
		}
		}
		
		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,16);
		if(movepr.move) { 
			comparisionCombine = largestCombine;
			}
		}
		
		
		if(comparisionCombine!=0)
			return movepr;
		
		
		movepr1 = Protect(grid,largestlosspile(grid,pr0),pr);
		comparisionProctect = largestLoss;

		movepr = SetNextStep(grid,pr,pr0,64);
		if(movepr.move) return movepr;
		
		movepr = SetNextStep(grid,pr,pr0,32);
		if(movepr.move) return movepr;

		movepr = SetNextStep(grid,pr,pr0,16);
		if(movepr.move) return movepr;
		
		if(movepr1.move && comparisionProctect > 4)
		{
			System.out.println(comparisionProctect);
			return movepr1;
		}
		
		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,8);
		if(movepr.move) { 
			comparisionCombine = largestCombine;
			}
		}
		
		movepr = SetNextStep(grid,pr,pr0,8);
		
		if(movepr.move) return movepr;	
		
		if(movepr1.move && comparisionProctect > 2)
		{
			System.out.println(comparisionProctect);
			return movepr1;
		}
		
		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,4);
		if(movepr.move) { 
			comparisionCombine = largestCombine;
			}
		}
		
		movepr = SetNextStep(grid,pr,pr0,4);
		if(movepr.move) return movepr;
		
				
		movepr = SetNextStep(grid,pr,pr0,2);
		if(movepr.move) return movepr;
		
		movepr = SetNextStep(grid,pr,pr0,1);
		if(movepr.move) return movepr;
				
		
/*		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,4);
		if(movepr.move) { comparisionCombine = largestCombine;}
		}
		
		if(comparisionCombine==0)
		{
		movepr = GreedyCaculation(grid,pr,pr0,2);
		if(movepr.move) { comparisionCombine = largestCombine;}
		}
		

		
		movepr1 = Protect(grid,largestlosspile(grid,pr0),pr);
		comparisionProctect = largestLoss;

		if(movepr1.move && comparisionProctect*2 > comparisionCombine)
		{
			return movepr1;
		}*/
		
		return movepr;		
	}
	
	movePair SetNextStep(Point[] grid,Pair pr,Pair pr0,int number)
	{
		movePair movepr = new movePair();
		// Set up next move for greedy algorithm
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (grid[i*size+j].value == number) {
					// Found a 2+
					//System.out.println("FOUND ..");
					//System.out.println(grid[i*size+j].value);
					for (int ii = 0; ii < size; ii++) {
						for (int ij = 0; ij < size; ij++) {
							movepr.move = false;	
							movepr.src = grid[ii*size+ij];
							movepr.target = grid[i*size+j];

							if (validateMove(movepr, pr, true)) {
								//System.out.println("FOUND A VALID SPOT");

								// Found a spot that is a valid move from the 2+
								for (int iii = 0; iii < size; iii++) {
									for (int iij = 0; iij < size; iij++) {
										movepr.move = false;	
										movepr.target = grid[ii*size+ij];
										movepr.src = grid[iii*size+iij];
										if (validateMove(movepr, pr, false)) 
												{
									//		System.out.println("YES FOUND IT");
									//		System.out.println(ii);
									//		System.out.println(ij);
											movepr.move = true;
											return movepr;
										}
									}
								}
								
							}
						}
					}
				}
			}
		}
		return movepr;
	}

	movePair safe(Point p, Pair pr0, Point[] grid )	
	{
		
		movePair movepr = new movePair();
		movepr.move = false;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				movepr.move = false;
				movepr.src = p;
				movepr.target = grid[i*size+j];				
				if(validateMove(movepr,pr0,false))
				{
					movepr.move = true;
					return movepr;
				}
			}//end i
		}//end j
		return movepr;
	}
	

	movePair largestlosspile(Point[] grid, Pair pr0)
	{
		Point lossPoint=new Point(); //
		Point lossCombin = new Point();
		Point p=new Point();
		int loss = 0;
		movePair safe = new movePair();
		movePair result = new movePair();
		//movepr.move = false;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				//movepr.move = false;
				p = grid[i*size+j];
				safe = safe(p,pr0,grid);
				if(safe.move&&p.value>loss)
					{
					loss=p.value;
					lossPoint =p;
					lossCombin = safe.target;
					}				
			}
		}
		result.src = lossPoint;
		result.target = lossCombin;
		largestLoss = loss;
		return result;		
	}
	
	movePair Protect(Point[] grid,movePair loss,Pair pr)
	{
		movePair movepr = new movePair();
		movepr.move = false;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				movepr.move = false;
				movepr.src = loss.src;
				movepr.target = grid[i*size+j];	
				if (validateMove(movepr, pr, false)) {
					movepr.move = true;
					return movepr;				
				}
			}
		}
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				movepr.move = false;
				movepr.src = loss.target;
				movepr.target = grid[i*size+j];	
				if (validateMove(movepr, pr, false)) {
					movepr.move = true;
					return movepr;				
				}
			}
		}
		return movepr;
	}
	
	movePair Greedy(Point[] grid,Pair pr,Pair pr0,int number)
	{
		movePair movepr = new movePair();
		movepr.move = false;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(grid[i*size+j].value >= number)
				{
					for (int i_pr=0; i_pr<size; i_pr++) {
						for (int j_pr=0; j_pr <size; j_pr++) {
							movepr.move = false;
							movepr.src = grid[i*size+j];
							movepr.target = grid[i_pr*size+j_pr];
							if (validateMove(movepr, pr, false)) {
								//check out the things for player 0!!
								movepr.move = true;
						//		System.out.println("GOOD!We find something!");
								return movepr;				
							}
						}
					}
				}					
			}
		}
		return movepr;
	}
	
	movePair GreedyCaculation(Point[] grid,Pair pr,Pair pr0,int number)
	{
		int score = 0;
		int largestScore = -1;
		int count = 0;
		
		movePair movepr = new movePair();
		movePair bestMovepr = new movePair();
		movepr.move = false;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(grid[i*size+j].value == number)
				{
					for (int i_pr=0; i_pr<size; i_pr++) {
						for (int j_pr=0; j_pr <size; j_pr++) {
							movepr.move = false;
							movepr.src = grid[i*size+j];
							movepr.target = grid[i_pr*size+j_pr];
							if (validateMove(movepr, pr, false)) {
								//check out the things for player 0!!
								score = movepr.src.value + movepr.target.value;
								if (movepr.src.owner == id)
									score -= movepr.src.value;
								if (movepr.target.owner == id)
									score -= movepr.target.value;
								if(score>largestScore)
								{
									largestScore = score;
									bestMovepr.src = movepr.src;
									bestMovepr.target = movepr.target;
								}
							}
						}
					}
				}					
			}
		}
		
		if(largestScore>0)
		{
		bestMovepr.move = true;
		largestCombine = largestScore;
		}
		return bestMovepr;
		
	}
	
	movePair largestCombination(Point[] grid,Pair pr,Pair pr0)
	{
		movePair movepr = new movePair();
		int largestScores=0;
		
		movepr = Greedy(grid,pr,pr0,32);
		if(movepr.move)
		{
			if(movepr.src.owner == id)
			{ }
		}
		
		return movepr;
	}

boolean validateMove(movePair movepr, Pair pr, boolean skipvaluecheck) {
    	
    	Point src = movepr.src;
    	Point target = movepr.target;
    	boolean rightposition = false;
    	if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    		rightposition = true;
    	}
    	if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
    		rightposition = true;
    	}
    	if (skipvaluecheck && rightposition) return true;
    	//if (Math.abs(target.x-src.x)==Math.abs(pr.y) && Math.abs(target.y-src.y)==Math.abs(pr.x)) {
    		//rightposition = true;
    	//}
        if (rightposition  && src.value == target.value && src.value > 0) {
        	return true;
        }
        else {
        	return false;
        }
    }


/*boolean validateMovePlyer0(movePair movepr, Pair pr0,Point[] grid) {
	Point target = movepr.target;
	movePair temp = new movePair();

	for (int i = 0; i < size; i++) {
		for (int j = 0; j < size; j++) {
			{
						movepr.move = false;
						temp.src = target;
						temp.target = grid[i*size+j];
						if (validateMove(temp, pr0, false)) {
							return false;				
						}
					}
				}
	}
					
	return true;
}*/
			
}


