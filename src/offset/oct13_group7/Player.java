package offset.oct13_group7;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
    int size_total = 1024;
	int N=100;
    int offensive_check = 0;

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
        our_moves = new ArrayList<ArrayList<Point>>(); // our moves for each pile
        their_moves = new ArrayList<ArrayList<Point>>(); // their moves for each pile

        for (int i = 0; i < size_total; i++) {
            movepr.src = toPoint(i);
            our_moves.add(new ArrayList<Point>());
            their_moves.add(new ArrayList<Point>());
            for (int j = 0; j < size_total; j++) {
                movepr.target = toPoint(j);
                if (checkMove(movepr, our_pair, 0)) {
                    our_moves.get(i).add(toPoint(j));
                }
                if (checkMove(movepr, their_pair, 0)) {
                    their_moves.get(i).add(toPoint(j));
                }
            }
        }
	}

    public ArrayList<Point> getOurMovesForPoint(Point p) {
        ArrayList<Point> tmp = new ArrayList<Point>();
        for (int i=0; i < our_moves.get(p.x*size+p.y).size(); i++) {
            tmp.add(grid_[our_moves.get(p.x*size+p.y).get(i).x*size+our_moves.get(p.x*size+p.y).get(i).y]);
        }
        return tmp;
    }

    public ArrayList<Point> getTheirMovesForPoint(Point p) {
        ArrayList<Point> tmp = new ArrayList<Point>();
        for (int i=0; i < their_moves.get(p.x*size+p.y).size(); i++) {
            tmp.add(grid_[their_moves.get(p.x*size+p.y).get(i).x*size+their_moves.get(p.x*size+p.y).get(i).y]);
        }
        return tmp;
    }

    public movePair greeedy() {
        movePair movepr = new movePair();
        movePair movepr2 = new movePair();
        movePair movepr3 = new movePair();
        movePair movepr4 = new movePair();
        // loop over our 2's and see if their threatened
        // if we find one that is threatened, we find something in a safe our offset from it and make a two
        for (int i = 0; i < size_total; i++) {
            if (toPoint(i).value <= 1) continue;
            movepr.src = toPoint(i);
            ArrayList<Point> their_mp = getTheirMovesForPoint(movepr.src);
            // Let's check their moves
            for (int j = 0; j < their_mp.size(); j++) {
                movepr.target = their_mp.get(j);
                if (checkMove(movepr, their_pair)) {
                    // System.out.println("FOUND EQUAL PILE");
                    // There is an equal pile possible to combine by enemy
                    // let's see if we can combine a pile in our distance from that pile
                    offensive_check++;
                    ArrayList<Point> tmp_mp1 = getOurMovesForPoint(movepr.src);
                    movepr2.src = movepr.src;
                    for (int k = 0; k < tmp_mp1.size(); k++) {
                        movepr2.target = tmp_mp1.get(k);
                        if (checkMove(movepr2, our_pair)) {
                            // We have an equal pile away from ours, so we can just compile them, but to which one?
                            ArrayList<Point> tmp_mp6 = getOurMovesForPoint(movepr2.src);
                            for (int n = 0; n < tmp_mp6.size(); n++) {
                                // System.out.printf("movepr2 src value %d - tmp_mp6 value %d\n", movepr2.src.value, tmp_mp6.get(n).value/2);
                                if (movepr2.src.value == tmp_mp6.get(n).value/2) {
                                    // System.out.println("FOUND VALUE TO COMB");
                                    movepr4 = new movePair(true, movepr2.target, movepr2.src);
                                }
                            }
                            // System.out.println("ZZFOUND VALUE TO COMB");
                            movepr4 = new movePair(true, movepr2.src, movepr2.target);

                            ArrayList<Point> tmp_mp12 = getTheirMovesForPoint(movepr4.target);
                            for (int n = 0; n < tmp_mp12.size(); n++) {
                                // System.out.println("FLIPEM");
                                if (tmp_mp12.get(n).value == movepr4.target.value*2) return new movePair(true, movepr2.target, movepr2.src);
                            }
                            return movepr4;

                        }
                    }
                    ArrayList<Point> tmp_mp9 = getOurMovesForPoint(movepr.target);
                    movepr2.src = movepr.target;
                    for (int k = 0; k < tmp_mp9.size(); k++) {
                        movepr2.target = tmp_mp9.get(k);
                        if (checkMove(movepr2, our_pair)) {
                            // We have an equal pile away from ours, so we can just compile them, but to which one?
                            ArrayList<Point> tmp_mp6 = getOurMovesForPoint(movepr2.src);
                            for (int n = 0; n < tmp_mp6.size(); n++) {
                                // System.out.printf("movepr2 src value %d - tmp_mp6 value %d\n", movepr2.src.value, tmp_mp6.get(n).value/2);
                                if (movepr2.src.value == tmp_mp6.get(n).value/2) {
                                    // System.out.println("FOUND VALUE TO COMB 2");
                                    movepr4 = new movePair(true, movepr2.target, movepr2.src);
                                }
                            }
                            // System.out.println("ZZFOUND VALUE TO COMB 2");
                            movepr4 = new movePair(true, movepr2.src, movepr2.target);

                            ArrayList<Point> tmp_mp12 = getTheirMovesForPoint(movepr4.target);
                            for (int n = 0; n < tmp_mp12.size(); n++) {
                                // System.out.println("FLIPEM");
                                if (tmp_mp12.get(n).value == movepr4.target.value*2) return new movePair(true, movepr2.target, movepr2.src);
                            }
                            return movepr4;
                        }
                    }
                    for (int k = 0; k < tmp_mp1.size(); k++) {
                        movepr2.target = tmp_mp1.get(k);
                        if (checkMove(movepr2, our_pair, 2)) {
                            // We have a half pile from that pile
                            ArrayList<Point> tmp_mp2 = getOurMovesForPoint(movepr2.target);
                            movepr3.target = movepr2.target;
                            for (int m = 0; m < tmp_mp2.size(); m++) {
                                movepr3.src = tmp_mp2.get(m);
                                if (checkMove(movepr3, our_pair)) {
                                    // We can compile that half pile with another half pile
                                    // System.out.println("WORKS");
                                    movepr3.move = true;
                                    return movepr3;
                                }
                            }
                        }
                    }
                }
            }
            ArrayList<Point> our_mp = getOurMovesForPoint(movepr.src);
            // Let's check our moves
            for (int j = 0; j < our_mp.size(); j++) {
                movepr.target = our_mp.get(j);
                if (movepr.src.value == movepr.target.value/2) {
                    // We have half value from this pile, maybe we can combine something to steal it later?
                    offensive_check++;
                    // System.out.println("WE HAVE HALF PILE");
                    ArrayList<Point> tmp_mp1 = getOurMovesForPoint(movepr.src);
                    movepr2.src = movepr.src;
                    for (int k = 0; k < tmp_mp1.size(); k++) {
                        movepr2.target = tmp_mp1.get(k);
                        // We can combine and create a pile with the same size!
                        if (movepr2.src.value == movepr2.target.value) {
                            // System.out.println("WE CAN COMBINE");
                            return new movePair(true, movepr2.target, movepr2.src);
                        } else {
                            // System.out.println("WE CAN COMBINE SOMETHING ELSE");
                            ArrayList<Point> tmp_mp2 = getOurMovesForPoint(movepr2.target);
                            movepr3.target = movepr2.target;
                            for (int m = 0; m < tmp_mp2.size(); m++) {
                                movepr3.src = tmp_mp2.get(m);
                                if (checkMove(movepr3, our_pair)) {
                                    // We can compile that half pile with another half pile
                                    // System.out.println("WORKS");
                                    movepr3.move = true;
                                    return movepr3;
                                }
                            }
                        }
                    }
                } else if (movepr.src.value == movepr.target.value) {
                    // Just do minimize moves, we can combine later?
                    // return new movePair(true, movepr.src, movepr.target);
                }
            }
        }
        boolean threatens_ours;
        for (int i = 0; i < size_total; i++) {
            if (toPoint(i).value <= 1) continue;
            movepr.src = toPoint(i);
            threatens_ours = false;
            if (toPoint(i).owner == id) continue; // in offense we're only interested in enemy piles
            // System.out.printf("Examaning point: %d %d", movepr.src.x, movepr.src.y);
            ArrayList<Point> tmp_mp4 = getTheirMovesForPoint(movepr.src);
            for (int k = 0; k < tmp_mp4.size(); k++) {
                if (tmp_mp4.get(k).owner == id) {
                    // System.out.println("THREAT");
                    threatens_ours = true;
                }
            }

            ArrayList<Point> tmp_mp1 = getOurMovesForPoint(movepr.src);
            for (int k = 0; k < tmp_mp1.size(); k++) {
                ArrayList<Point> tmp_mp2 = getOurMovesForPoint(tmp_mp1.get(k));
                movepr2.target = tmp_mp1.get(k);
                for (int m = 0; m < tmp_mp2.size(); m++) {
                    movepr2.src = tmp_mp2.get(m);
                    if (checkMove(movepr2, our_pair)) {
                        // System.out.println("SELECTING OFFENSIVE");
                        movepr3.src = movepr2.src;
                        movepr3.target = movepr2.target;
                        movepr3.move = true;
                        if (threatens_ours) {
                            // System.out.println("THREATENING OURS");
                            return movepr3;
                        }
                    }
                }
            }

        }
        if (movepr3.move) return movepr3;
        return movepr;
    }

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        init(grid, pr, pr0, history);


        movePair movepr = new movePair();

        if (history.size() <= 3 || offensive_check > 0) {
            movepr = greeedy();
        } else {
            System.out.println("No greedy");
        }

        if (movepr.move) {
            System.out.println("Found greedy move");
            return movepr;
        }
        else {
            // No moves from greedy
            List<Scored> scoredlist = minimizeMoves();
            // Rachel: pickASafeSpot(scoredlist);
            // when there is only 200? moves left, remove all the dangerous moves
            int count = 0;
        /*    if(scoredlist.size()>0)
            {
            if(scoredlist.get(0).their_current_moves<150)
            {
            for(int i=0;i<scoredlist.size();i++)
            {
            	if(validateMovePlyer0(scoredlist.get(i).movepr,pr0,grid))
            	{
            		scoredlist.remove(i);
            		count++;
            	}
            }
            System.out.println("REMOVED:    "+count);
            System.out.println("Left:      "+scoredlist.size());
            }
            }*/
            
            //pick up the top 10 moves, resort according to minimize-our moves
            List<Scored> topTenScored = new ArrayList<Scored>();
            if(scoredlist.size()>10)
            {
            	for(int i=0;i<10;i++)
            	{
            		topTenScored.add(scoredlist.get(i));
            	}
            }
            else
            {
            	topTenScored = scoredlist;
            }	
        
            Collections.sort(topTenScored, new Comparator<Scored>() {
                @Override
                // Sorts from low to high
                public int compare(Scored scored1, Scored scored2)
                {
                    if (scored1.their_future_moves-scored1.our_current_moves == scored2.their_future_moves-scored2.our_current_moves) return 0;
                    else if (scored1.their_future_moves-scored1.our_current_moves > scored2.their_future_moves-scored2.our_current_moves) return 1;
                    else return -1;
                }
            });
            
            if (scoredlist.size() == 0) {
                // Pick a random move
                movepr.move = true;
                for (int i = 0; i < size_total; i++) {
                    movepr.src = toPoint(i);
                    ArrayList<Point> tmp_mp1 = getOurMovesForPoint(movepr.src);
                    for (int k = 0; k < tmp_mp1.size(); k++) {
                        movepr.target = tmp_mp1.get(k);
                        if (checkMove(movepr, our_pair)) {
                            System.out.println("FOUND RANDOM MOVE");
                            return movepr;
                        }
                    }
                }
                movepr.move = false;
                return movepr;
            }
            System.out.printf("USING MINIMIZED SCORE, their moves: %d our moves: %d", scoredlist.get(0).their_future_moves, scoredlist.get(0).our_future_moves);
            movepr = topTenScored.get(0).movepr;
            movepr = scoredlist.get(0).movepr;
            movepr.move = true;
        }
        return movepr;
	}

	ArrayList<Scored> minimizeMoves() {
        int their_current_moves, their_future_moves, our_current_moves, our_future_moves;
        Scored scored;
        ArrayList<Scored> scoredlist = new ArrayList<Scored>();
        Point src, target;

        Point[] gridtmp = new Point[1024];
        for (int i = 0; i < size_total; i++) {
            gridtmp[i] = new Point(grid_[i].x, grid_[i].y, grid_[i].value, grid_[i].owner);
        }
        ArrayList<movePair> validMoves = getOurMoves(grid_);
        our_current_moves = validMoves.size();
        their_current_moves = countOpponentMoves(grid_);

        // Go through our valid moves and minimize opponents moves
		for (int i = 0; i < validMoves.size(); i++) {
            // Store before src/target
            src = validMoves.get(i).src;
            target = validMoves.get(i).target;
            // Simulate when we have changed points
            gridtmp[src.x*size+src.y] = new Point(src.x, src.y, 0, -1);
            gridtmp[target.x*size+target.y] = new Point(target.x, target.y, target.value*2, id);

            our_future_moves = getOurMoves(gridtmp).size();
            their_future_moves = countOpponentMoves(gridtmp);

            // Change the src/target it back
            gridtmp[src.x*size+src.y] = src;
            gridtmp[target.x*size+target.y] = target;
            // System.out.printf("min_moves: %d\n", tmpmoves);

            // Do comparison
            scored = new Scored();
            scored.movepr = validMoves.get(i);
            scored.our_current_moves = our_current_moves;
            scored.our_future_moves = our_future_moves;
            scored.their_current_moves = their_current_moves;
            scored.their_future_moves = their_future_moves;
            scoredlist.add(scored);
        }
        Collections.sort(scoredlist, new Comparator<Scored>() {
            @Override
            // Sorts from low to high
            public int compare(Scored scored1, Scored scored2)
            {
                int scored1_their = scored1.their_current_moves-scored1.their_future_moves; // 50
                int scored1_our = scored1.our_current_moves-scored1.our_future_moves; //30
                int scored2_their = scored2.their_current_moves-scored2.their_future_moves; // 50
                int scored2_our = scored2.our_current_moves-scored2.our_future_moves; //30

                if (scored1.their_future_moves == scored2.their_future_moves) return 0;
                else if (scored1.their_future_moves > scored2.their_future_moves) return 1;
                else return -1;
            }
        });
        return scoredlist;
    }

    // Get our moves for a grid
    ArrayList<movePair> getOurMoves(Point[] grid) {
        ArrayList<movePair> moveprs = new ArrayList<movePair>();
        movePair movepr = new movePair();
		for (int i = 0; i < size_total; i++) {
            movepr.src = grid[i];
            for (int j = 0; j < our_moves.get(i).size(); j++) {
                movepr.target = grid[our_moves.get(i).get(j).x*size+our_moves.get(i).get(j).y];
                if (checkMove(movepr, our_pair)) moveprs.add(new movePair(false, movepr.src, movepr.target));
            }
        }
        return moveprs;
    }

    // count their moves for a grid
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

    boolean checkMove(movePair movepr, Pair pr, int valuecheck) { // valuecheck = 0 to skip entirely, 1 to check exact, 2 for half
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
         if (valuecheck == 0) return true;
         if (valuecheck == 1 && src.value == target.value && src.value > 0) return true;
         if (valuecheck == 2 && src.value > 0 && (src.value == target.value/2 || src.value/2 == target.value)) return true;
        }
        return false;
    }

    boolean checkMove(movePair movepr, Pair pr) {
        return checkMove(movepr, pr, 1);
    }
    
    boolean validateMovePlyer0(movePair movepr, Pair pr0,Point[] grid) {
    	Point target = movepr.target;
    	movePair temp = new movePair();

    	for (int i = 0; i < size; i++) {
    		for (int j = 0; j < size; j++) {
    			{
    						movepr.move = false;
    						temp.src = target;
    						temp.target = grid[i*size+j];
    						if (checkMove(temp, pr0, 1)) {
    							return false;				
    						}
    					}
    				}
    	} 					
    	return true;
    }
}


