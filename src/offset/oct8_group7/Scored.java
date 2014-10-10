package offset.oct8_group7;

import offset.sim.movePair;


public class Scored {

	public movePair movepr;
    public int score;

    public Scored() {
        score = 0;
    }

    public Scored(movePair movepr_,int score_) {
        movepr = new movePair(movepr_.move, movepr_.src, movepr_.target);
        score = score_;
    }
}

