package offset.oct13_group7;

import offset.sim.movePair;


public class Scored {

	public movePair movepr;
    public int score;
    public int our_current_moves;
    public int their_current_moves;
    public int our_future_moves;
    public int their_future_moves;

    public Scored() {
        score = 0;
    }

    public Scored(movePair movepr_,int score_) {
        movepr = new movePair(movepr_.move, movepr_.src, movepr_.target);
        score = score_;
    }
}

