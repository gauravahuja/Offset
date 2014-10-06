package offset.minimax;
import offset.common.GridGraph;

import java.util.*;
import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
    static final int SIZE = 32;
    

    Point[] currentGrid;
    Pair myPair;
    Pair advPair;
    GridGraph myGridGraph;
    GridGraph advGridGraph;
    int advId;
    boolean playerInitialized = false;
    int minimax_depth;

    public Player(Pair prin, int idin) { super(prin, idin); }
    public void init() {}

    public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
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
        
        minimax_depth = 1;
        
        System.out.printf("[A] Me:%d, Adv:%d, Cummulative:%d, Depth: %d\n", my_moves, adv_moves, cummulative, minimax_depth);
        movePair movepr = new movePair();
        minimax_run(0, minimax_depth, true, movepr);
        if (movepr.move == true)
        {
            // update graphs with adversary last move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
        }
        return movepr;
    }

    void print_minimax_summary(int current_depth, int max_depth, boolean my_turn)
    {
        System.out.printf("[B] current_depth = %d, max_depth = %d, my_turn = %b, my_score = %d, adv_score = %d\n", 
            current_depth,
            max_depth,
            my_turn,
            myGridGraph.getScore(),
            advGridGraph.getScore());
    }

    int minimax_run(int current_depth, int max_depth, boolean my_turn, movePair next_best_move)
    {
        movePair best_movepr = new movePair();
        best_movepr.move = false;

        if((current_depth == max_depth) /*|| no moves left*/ )
        {
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            //print_minimax_summary(current_depth, max_depth, my_turn);
            return myGridGraph.getScore();
        }

        HashSet<Point> keys = new HashSet<Point>();
        HashSet<Point> neighbors = new HashSet<Point>();
        Iterator<Point> it;
        Iterator<Point> it_n;
        Point p;
        Point p_n;
        int best_value = myGridGraph.getScore();
        int value;
       
        movePair child_best_move = new movePair();

        movePair movepr = new movePair();
        movepr.move = false;

        if (my_turn)
        {
            //keys = myGridGraph.edgesByPoint.keySet();
            keys.addAll(myGridGraph.edgesByPoint.keySet());
            it = keys.iterator();

            while(it.hasNext())
            {
                p = it.next();
                //neighbors = myGridGraph.edgesByPoint.get(p);
                //best_value = -1;
                neighbors.clear();
                neighbors.addAll(myGridGraph.edgesByPoint.get(p));
                if(neighbors == null)
                    continue;
                
                it_n = neighbors.iterator();
                while(it_n.hasNext())
                {
                    p_n = it_n.next();
                    //p <- p_n
                    movepr.src = new Point(p_n);
                    movepr.target = new Point(p);

                    myGridGraph.updateGraphWithMovePair(movepr, id);
                    advGridGraph.updateGraphWithMovePair(movepr, id);
                    //print_minimax_summary(current_depth, max_depth, my_turn);

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move);

                    if (value >= best_value)//Maximize best value
                    {
                        best_movepr.src = movepr.src;
                        best_movepr.target = movepr.target;
                        best_movepr.move = true;
                        best_value = value;
                    }

                    myGridGraph.undoGraphByOneMovePair();
                    advGridGraph.undoGraphByOneMovePair();
                }
            }
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            return best_value;
        }
        else
        {
            //keys = advGridGraph.edgesByPoint.keySet();
            keys.addAll(myGridGraph.edgesByPoint.keySet());
            //best_value = 10000000;
            it = keys.iterator();
            
            while(it.hasNext())
            {
                p = it.next();
                //neighbors = advGridGraph.edgesByPoint.get(p);
                neighbors.clear();
                neighbors.addAll(myGridGraph.edgesByPoint.get(p));
                if(neighbors == null)
                    continue;
                
                it_n = neighbors.iterator();
                while(it_n.hasNext())
                {
                    p_n = it_n.next();
                    //p <- p_n
                    movepr.src = new Point(p_n);
                    movepr.target = new Point(p);

                    myGridGraph.updateGraphWithMovePair(movepr, advId);
                    advGridGraph.updateGraphWithMovePair(movepr, advId);
                    //print_minimax_summary(current_depth, max_depth, my_turn);

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move);
                    if (value <= best_value)//Minimize best value
                    {
                        best_movepr.src = movepr.src;
                        best_movepr.target = movepr.target;
                        best_movepr.move = true;
                        best_value = value;
                    }

                    myGridGraph.undoGraphByOneMovePair();
                    advGridGraph.undoGraphByOneMovePair();
                }
            }
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            return best_value;
        }
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