package offset.oct_6_group6_oct1;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	
	int size =32;

	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {

	}

	// public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
	// 	//System.out.println(history.size());
	// 	movePair movepr = new movePair();
	// 	for (int i = 0; i < size; i++) {
	// 		for (int j = 0; j < size; j++) {
	// 			for (int i_pr=0; i_pr<size; i_pr++) {
	// 			for (int j_pr=0; j_pr <size; j_pr++) {
	// 				movepr.move = false;
	// 				movepr.src = grid[i*size+j];
	// 				movepr.target = grid[size*i_pr+j_pr];
	// 				if (validateMove(movepr, pr)) {
	// 					movepr.move = true;
	// 					return movepr;
	// 				}
	// 			}
	// 			}
	// 		}
	// 	}
	// 	return movepr;
	// }


    public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) 
    {
        //System.out.println(history.size());
        movePair movepr = new movePair();
        movepr.move = false;
        for (int x1 = 0; x1 < size; x1++) 
        {
            for (int y1 = 0; y1 < size; y1++) 
            {
                for(int x2 = 0; x2 < size; x2++)
                {
                    for(int y2 = 0; y2 < size; y2++)
                    {
                        if (x2 == x2 && y2 == y1)
                            continue;
                        movePair m1 = new movePair();
                        m1.move = false;
                        m1.target = grid[x2*size+y2];
                        m1.src = grid[x1*size+y1];
                        if (rightPosition(m1, pr))
                        {
                            if(m1.src.value == m1.target.value && m1.src.value > 0)
                            {
                                //make move
                                m1.move = true;
                                return m1;
                            }
                            else
                            {
                                //Build the one which is less
                                int bx, by, tx, ty;
                                if(m1.src.value < m1.target.value)
                                {
                                    bx = m1.src.x;
                                    by = m1.src.y;
                                    tx = m1.target.x;
                                    ty = m1.target.y;
                                }
                                else
                                {
                                    bx = m1.target.x;
                                    by = m1.target.y;
                                    tx = m1.src.x;
                                    ty = m1.src.y;
                                }
                                for(int x3 = 0; x3 < size; x3++)
                                {
                                    for(int y3 = 0; y3 < size; y3++)
                                    {
                                        if(x3 == bx && y3 == by)
                                            continue;
                                        if(x3 == tx && y3 == ty)
                                            continue;
                                        movePair m2 = new movePair();
                                        m2.move = false;
                                        m2.target = grid[bx*size+by];
                                        m2.src = grid[x3*size+y3];
                                        if(validateMove(m2, pr))
                                        {
                                            m2.move = true;
                                            return m2;
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

    boolean rightPosition(movePair movepr, Pair pr) {
        
        Point src = movepr.src;
        Point target = movepr.target;
        boolean rightposition = false;
        if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
            rightposition = true;
        }
        if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
            rightposition = true;
        }
        return rightposition;
    }

    boolean validateMove(movePair movepr, Pair pr) {
        
        Point src = movepr.src;
        Point target = movepr.target;
        boolean rightposition = false;
        if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
            rightposition = true;
        }
        if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
            rightposition = true;
        }
        if (rightposition  && src.value == target.value && src.value>0) {
            return true;
        }
        else {
            return false;
        }
    }
}
