
package offset.oct8_group2;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

/**
 * Fields of base class:
 *
 * int id - the player number
 * Pair pr - pr.p, pr.q those are good paramters
 */
public class Player extends offset.sim.Player {

    static final int ALPHA_BETA_MODE = 1;
    static final int HUMAN_MODE = 2;

    int currentMode = ALPHA_BETA_MODE;

    offset.sim.Player actualPlayer;

    public Player(Pair prin, int idin) {
        super(prin, idin);

        switch (currentMode) {
            case ALPHA_BETA_MODE:
                actualPlayer = new AlphaBetaPlayer(prin, idin);
                break;

            case HUMAN_MODE:
            default:
                actualPlayer = new HumanPlayer(prin, idin);
        }
    }

    public void init() { }

    public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        return actualPlayer.move(grid, pr, pr0, history);
    }
}
