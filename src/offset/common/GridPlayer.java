package offset.common;
import offset.common.GridGraph;

import java.util.*;
import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public abstract class GridPlayer extends offset.sim.Player {
    static final int SIZE = 32;
    
    protected Point[] currentGrid;
    protected Pair myPair;
    protected Pair advPair;
    protected GridGraph myGridGraph;
    protected GridGraph advGridGraph;
    protected int advId;
    protected boolean playerInitialized = false;
    protected int minimax_depth;

    public GridPlayer(Pair prin, int idin) { super(prin, idin); }
    public void init() {}

    // choose move, assuming graph has already
    // been adequately set up for you.
    protected abstract movePair chooseMove();

    final public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        currentGrid = grid;
        
        if (!playerInitialized) {
            myPair = pr;
            advPair = pr0;
            advId = id + 1 % 2;
            myGridGraph = new GridGraph(pr, id);
            advGridGraph = new GridGraph(pr0, advId);
            playerInitialized = true;
        }
        
        // update graphs with adversary last move
        if (history.size() >= 1) {
            int advId = (int) history.get(history.size() - 1).get(0);
            if (advId != id) {
                movePair advLastMovePair = (movePair) history.get(history.size() - 1).get(1);
                advGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
                myGridGraph.updateGraphWithMovePair(advLastMovePair, advId);
            }
        }

        int my_moves = 2*myGridGraph.getNumberOfEdges();
        int adv_moves = 2*advGridGraph.getNumberOfEdges();
        int cummulative = my_moves*adv_moves;
        
        System.out.printf("[A] Me:%d, Adv:%d, Cummulative:%d, Depth: %d\n", my_moves, adv_moves, cummulative, minimax_depth);

        movePair movepr = chooseMove();
        if (movepr.move == true)
        {
            // update graphs with adversary last move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
        }
        return movepr;
    }

    boolean validateMove(movePair movepr, Pair pr) {

        Point src = movepr.src;
        Point target = movepr.target;
        boolean rightposition = false;
        if (Math.abs(target.x - src.x) == Math.abs(pr.p)
                && Math.abs(target.y - src.y) == Math.abs(pr.q)) {
            rightposition = true;
        }
        if (Math.abs(target.x - src.x) == Math.abs(pr.p)
                && Math.abs(target.y - src.y) == Math.abs(pr.q)) {
            rightposition = true;
        }
        if (rightposition && src.value == target.value && src.value > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    static Point[] copyPointArray(Point[] points) {
        Point[] npoints = new Point[points.length];
        for (int p = 0; p < points.length; ++p)
            npoints[p] = new Point(points[p]);

        return npoints;
    }
}
