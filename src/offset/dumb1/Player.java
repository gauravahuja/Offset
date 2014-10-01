package offset.dumb1;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	int size = 32;
	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {

	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
		movePair movepr = new movePair();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int i_pr=0; i_pr<size; i_pr++) {
				for (int j_pr=0; j_pr <size; j_pr++) {
					movepr.move = false;
					movepr.src = grid[i*size+j];
					movepr.target = grid[i_pr*size+j_pr];
					if (validateMove(movepr, pr)) {
						movepr.move = true;
						return movepr;
					}
				}
				}
			/*	if (i + pr.x >= 0 && i + pr.x < size) {
					if (j + pr.y >= 0 && j + pr.y < size) {
						
					}
					if (j - pr.y >= 0 && j - pr.y < size) {

					}
				}
				if (i - pr.x >= 0 && i - pr.x < size) {
					if (j + pr.y >= 0 && j + pr.y < size) {

					}
					if (j - pr.y >= 0 && j - pr.y < size) {

					}
				}
				if (i + pr.y >= 0 && i + pr.y < size) {
					if (j + pr.x >= 0 && j + pr.x < size) {

					}
					if (j - pr.x >= 0 && j - pr.x < size) {

					}
				}
				if (i - pr.y >= 0 && i - pr.y < size) {
					if (j + pr.x >= 0 && j + pr.x < size) {

					}
					if (j - pr.x >= 0 && j - pr.x < size) {

					}
				}
*/
			}
		}
		return movepr;
	}


boolean validateMove(movePair movepr, Pair pr) {
    	
    	Point src = movepr.src;
    	Point target = movepr.target;
    	boolean rightposition = false;
    	if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    		rightposition = true;
    	}
    	//if (Math.abs(target.x-src.x)==Math.abs(pr.y) && Math.abs(target.y-src.y)==Math.abs(pr.x)) {
    		//rightposition = true;
    	//}
        if (rightposition  && src.value == target.value && src.value >0) {
        	return true;
        }
        else {
        	return false;
        }
    }
}