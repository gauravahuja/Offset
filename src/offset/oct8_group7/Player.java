package offset.oct8_group7;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
    int size_total = 1024;
	int N=100;

    Point[] grid_;
    Pair our_pair;
    Pair their_pair;
    ArrayList<ArrayList> history_;

    ArrayList<ArrayList<Point>> our_moves;
    ArrayList<ArrayList<Point>> their_moves;
    boolean initialized = false;

	public Player(Pair prin, int idin) {
		super(prin, idin);
	}

   	public void init() { }
	public void init(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        grid_ = grid;
        history_ = history;
        if (this.initialized) return;
        this.initialized = true;
        our_pair = pr;
        their_pair = pr0;

        movePair movepr = new movePair();
        our_moves = new ArrayList<ArrayList<Point>>();
        their_moves = new ArrayList<ArrayList<Point>>();

        for (int i = 0; i < size_total; i++) {
            movepr.src = toPoint(i);
            our_moves.add(new ArrayList<Point>());
            their_moves.add(new ArrayList<Point>());
            for (int j = 0; j < size_total; j++) {
                movepr.target = toPoint(j);
                if (checkMove(movepr, our_pair, true)) {
                    our_moves.get(i).add(toPoint(j));
                }
                if (checkMove(movepr, their_pair, true)) {
                    their_moves.get(i).add(toPoint(j));
                }
            }
        }
	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        init(grid, pr, pr0, history);

        movePair movepr = new movePair();

        Scored scored = doGreedy();
        System.out.printf("SCORED: %d\n", scored.score);

        if (scored.score == 0) {
            movepr = minimizeMoves();
            if (movepr.src == null) {
        		movepr = SetNextStep(grid,pr,pr0,32);
        		if(movepr.move) return movepr;

        		movepr = SetNextStep(grid,pr,pr0,16);
        		if(movepr.move) return movepr;
        		
        		movepr = SetNextStep(grid,pr,pr0,8);
        		if(movepr.move) return movepr;
        		
        		movepr = SetNextStep(grid,pr,pr0,4);
        		if(movepr.move) return movepr;
        		
        		movepr = SetNextStep(grid,pr,pr0,2);
        		if(movepr.move) return movepr;
        		
        		movepr = SetNextStep(grid,pr,pr0,1);
        		if(movepr.move) return movepr;
            }
        } else {
            movepr = scored.movepr;
        }

        return movepr;
	}

	Scored doGreedy() {
        int tmpscore = 0;
        Scored scored = new Scored();

		movePair movepr = new movePair();
		movePair movepr2 = new movePair();
        Point tmppoint;
		
		for (int i = 0; i < size_total; i++) {
            // Only look at "owned" piles
            if (toPoint(i).value <= 1) continue;
            System.out.printf("Looking at %d %d\n", toPoint(i).x, toPoint(i).y);
            movepr.src = toPoint(i);
            if (toPoint(i).owner == id) {
                System.out.println("It's owned by us");
                // It's our pile, let's see if it's threatened
                for (int j = 0; j < their_moves.get(i).size(); j++) {
                    movepr.target = grid_[their_moves.get(i).get(j).x*size+their_moves.get(i).get(j).y];
                    if (!checkMove(movepr, their_pair)) continue; //Not threatened here
                    System.out.printf("It's being threatened\n");
                    // It is being threatened.. Can we do anything about it?
                    for (int k = 0; k < our_moves.get(i).size(); k++) {
                        movepr.target = grid_[our_moves.get(i).get(k).x*size+our_moves.get(i).get(k).y];
                        if (!checkMove(movepr, our_pair)) {
                            // We can't do anything now, but can we set something up for later? Set up 2
                            movepr2.target = movepr.target;
                            for (int h = 0; h < our_moves.get(movepr2.target.x*size+movepr2.target.y).size(); h++) {
                                tmppoint = our_moves.get(movepr2.target.x*size+movepr2.target.y).get(h);
                                movepr2.src = tmppoint;
                                if (movepr.target.value == movepr2.src.value*2 && checkMove(movepr2, our_pair)) {                  	
                                    if (scored.score==0 && scored.score >= movepr.src.value) continue;
                                    if (scored.score>0 && scored.score+1 >= movepr.src.value) continue;
                	
                                    scored.score = movepr2.src.value;
                                    scored.movepr = new movePair(true, movepr2.src, movepr2.target);
                                }
                            }
                        } else {
                            // We can do something about it now
                            // But we might already have found something better..
                            if (scored.score > movepr.src.value) continue;
                            // Nothing better, let's do it!
                            scored.score = movepr.src.value;
                            scored.movepr = new movePair(true, movepr.src, movepr.target);
                        }
                    }
                }
            } else {
                System.out.println("It's owned by opponent");
                // It's opponent pile, let's see if we can steal it
                for (int k = 0; k < our_moves.get(i).size(); k++) {
                    movepr.target = grid_[our_moves.get(i).get(k).x*size+our_moves.get(i).get(k).y];
                    if (!checkMove(movepr, our_pair)) {
                        System.out.printf("We can't steal it here\n");
                        // We can't do anything now, but can we set something up for later?
                        if (movepr.src.value != 2*movepr.target.value) continue; // No we can't
                        // Do we have two half values that we can combine to steal this value?
                        for (int h = 0; h < our_moves.get(movepr.target.x*size+movepr.target.y).size(); h++) {
                            if (movepr.target.value == our_moves.get(movepr.target.x*size+movepr.target.y).get(h).value) {
                                // Found a valid pile to combine
                                if (scored.score==0 && scored.score >= movepr.src.value) continue;
                                if (scored.score>0 && scored.score+1 >= movepr.src.value) continue;
                            	//if (scored.score+1 >= movepr.src.value) continue;
                                scored.score = movepr.target.value;
                                scored.movepr = new movePair(true, our_moves.get(movepr.target.x*size+movepr.target.y).get(h), movepr.target);
                            }
                        }
                        // Do we have a half of this value in a valid position and can we set up same value to combine with that position?
                        //     2   1  1
                        // 4   
                        // vs
                        //     2   2
                        // 4   
                    } else {
                        // We can do something about it now
                        // But we might already have found something better..
                        tmpscore = movepr.src.value;
                        if (movepr.target.owner != id) tmpscore += movepr.target.value;
                        if (scored.score > tmpscore) continue;
                        // Nothing better, let's do it!
                        scored.score = tmpscore;
                        scored.movepr = new movePair(true, movepr.src, movepr.target);
                    }
                }
            }
        }
        return scored;

    }


	movePair minimizeMoves() {
        int tmpmoves = 0;
        int min_moves = Integer.MAX_VALUE;
        movePair min_movepair = new movePair();

        Point src, target;
        Point[] gridtmp = new Point[1024];
        for (int i = 0; i < size_total; i++) {
            gridtmp[i] = new Point(grid_[i].x, grid_[i].y, grid_[i].value, grid_[i].owner);
        }
        Scored scored = new Scored();
        movePair movepr = new movePair();
        ArrayList<movePair> validMoves = new ArrayList<movePair>();
        // Populate validMoves array
		for (int i = 0; i < size_total; i++) {
            movepr.src = gridtmp[i];

            for (int j = 0; j < our_moves.get(i).size(); j++) {
                movepr.target = gridtmp[our_moves.get(i).get(j).x*size+our_moves.get(i).get(j).y];
                if (checkMove(movepr, our_pair, false)) validMoves.add(new movePair(false, movepr.src, movepr.target));
            }
        }

        System.out.printf("VALIDMOVES SIZE: %d\n", validMoves.size());

        // Go through our valid moves and minimize opponents moves
		for (int i = 0; i < validMoves.size(); i++) {
            // Store before src/target
            src = validMoves.get(i).src;
            target = validMoves.get(i).target;
            // Simulate when we have changed points
            gridtmp[src.x*size+src.y] = new Point(src.x, src.y, 0, -1);
            gridtmp[target.x*size+target.y] = new Point(target.x, target.y, target.value*2, id);
            tmpmoves = countOpponentMoves(gridtmp);

            // Change the src/target it back
            gridtmp[src.x*size+src.y] = src;
            gridtmp[target.x*size+target.y] = target;
            // System.out.printf("min_moves: %d\n", tmpmoves);

            // Do comparison
            if (min_moves > tmpmoves) {
                min_moves = tmpmoves;
                min_movepair = validMoves.get(i);
            }
        }
        if (tmpmoves > 0) 
            return new movePair(true, grid_[min_movepair.src.x*size+min_movepair.src.y], grid_[min_movepair.target.x*size+min_movepair.target.y]);
        else {
            System.out.println("Didn't find any moves...");
            return new movePair();
        }
    }

    int countOpponentMoves(Point[] grid) {
        int moves = 0;
        movePair movepr = new movePair();
		for (int i = 0; i < size_total; i++) {
            movepr.src = grid[i];
            for (int j = 0; j < their_moves.get(i).size(); j++) {
                movepr.target = grid[their_moves.get(i).get(j).x*size+their_moves.get(i).get(j).y];
                if (checkMove(movepr, their_pair)) moves++;
            }
        }
        return moves;
    }


    public Point toPoint(int x) {
        return grid_[x];
    }

    public Point toPoint(int x, int y) {
        return grid_[x*size+y];
    }

    boolean checkMove(movePair movepr, Pair pr, boolean skipvaluecheck) {
    	Point src = movepr.src;
    	Point target = movepr.target;
    	boolean rightposition = false;

    	if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    		rightposition = true;
    	}
    	if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
    		rightposition = true;
    	}

        if (rightposition) {
         if (src.value == target.value && src.value > 0) return true;
         if (skipvaluecheck) return true;
        }
        return false;
    }

    boolean checkMove(movePair movepr, Pair pr) {
        return checkMove(movepr, pr, false);
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

							if (checkMove(movepr, pr, true)) {
								//System.out.println("FOUND A VALID SPOT");

								// Found a spot that is a valid move from the 2+
								for (int iii = 0; iii < size; iii++) {
									for (int iij = 0; iij < size; iij++) {
										movepr.move = false;	
										movepr.target = grid[ii*size+ij];
										movepr.src = grid[iii*size+iij];
										if (checkMove(movepr, pr, false)) 
												{
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
}


