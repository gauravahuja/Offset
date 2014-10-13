package offset.gaurav_2;
import offset.common.GridGraph;

import java.util.*;
import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
    static final int SIZE = 32;
    static int total_moves = 0;
    

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
            advId = (id + 1) % 2;
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

        int my_moves = myGridGraph.getNumberOfEdges();
        int adv_moves = advGridGraph.getNumberOfEdges();
        int cummulative = my_moves*adv_moves;
        System.out.printf("[A2] Me:%d, Adv:%d, Cummulative:%d, MyScore: %d, AdvScore: %d\n", my_moves, adv_moves, cummulative, myGridGraph.getScore(), advGridGraph.getScore());
        
        movePair movepr = new movePair();
        
        boolean verbose = false;
        // if (my_moves < 20)
        // {
        //     verbose = true;
        // }

        minimax_depth = 1;        
        minimax_run(0, minimax_depth, true, movepr, verbose);
        if (movepr.move == true)
        {
            // update graphs with adversary last move
            advGridGraph.updateGraphWithMovePair(movepr, id);
            myGridGraph.updateGraphWithMovePair(movepr, id);
        }
        
        my_moves = myGridGraph.getNumberOfEdges();
        adv_moves = advGridGraph.getNumberOfEdges();
        cummulative = my_moves*adv_moves;
        int my_score = myGridGraph.getScore();
        int adv_score = advGridGraph.getScore();

        System.out.printf("[B2] Me:%d, Adv:%d, Cummulative:%d, MyScore: %d, AdvScore: %d\n", my_moves, adv_moves, cummulative, my_score, adv_score);

        total_moves++;
        System.out.printf("[LOG2],%d,%d,%d,%d,%d,%d,%d\n", total_moves, my_moves*2, adv_moves*2, my_score, adv_score, (my_moves - adv_moves)*2, my_score-adv_score);


        
        return movepr;
    }

    void print_minimax_summary(int current_depth, int max_depth, boolean my_turn)
    {
        System.out.printf("[Summary] current_depth = %d, max_depth = %d, my_turn = %b, my_score = %d, adv_score = %d\n", 
            current_depth,
            max_depth,
            my_turn,
            myGridGraph.getScore(),
            advGridGraph.getScore());
    }

    int minimax_run(int current_depth, int max_depth, boolean my_turn, movePair next_best_move, boolean verbose)
    {
        movePair best_movepr = new movePair();
        best_movepr.move = false;
        best_movepr.src = new Point();
        best_movepr.target = new Point();

        if((current_depth == max_depth) /*|| no moves left*/ )
        {
            next_best_move.move = best_movepr.move;
            next_best_move.src = best_movepr.src;
            next_best_move.target = best_movepr.target;
            //print_minimax_summary(current_depth, max_depth, my_turn);
            return (myGridGraph.getNumberOfEdges()-advGridGraph.getNumberOfEdges());
        }

        HashSet<Point> keys = new HashSet<Point>();
        HashSet<Point> neighbors = new HashSet<Point>();
        Iterator<Point> it;
        Iterator<Point> it_n;
        Point p;
        Point p_n;
        int best_value;
        int value;
        int current_delta = myGridGraph.getNumberOfEdges() - advGridGraph.getNumberOfEdges();
       
        movePair child_best_move = new movePair();

        movePair movepr = new movePair();
        movepr.move = false;

        if (my_turn)
        {
            best_value = -1000000; //Because I have to maximize
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

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move, verbose) - current_delta;
                    if(verbose)
                    {
                        System.out.printf("[Move] (%d, %d) -> (%d, %d) => Adv Moves=%d\n",
                            movepr.src.x,
                            movepr.src.y,
                            movepr.target.x,
                            movepr.target.y,
                            value);
                    }
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
            if(verbose)
            {
                System.out.printf("[Chosen Move] (%d, %d) -> (%d, %d) => Adv Moves=%d\n",
                        best_movepr.src.x,
                        best_movepr.src.y,
                        best_movepr.target.x,
                        best_movepr.target.y,
                        best_value);
            }
            return best_value;
        }
        else
        {
            best_value = 1000000;//Because Adv has to minimize
            //keys = advGridGraph.edgesByPoint.keySet();
            keys.addAll(advGridGraph.edgesByPoint.keySet());
            it = keys.iterator();
            
            while(it.hasNext())
            {
                p = it.next();
                //neighbors = advGridGraph.edgesByPoint.get(p);
                neighbors.clear();
                neighbors.addAll(advGridGraph.edgesByPoint.get(p));
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

                    value = minimax_run(current_depth+1, max_depth, !my_turn, child_best_move, verbose) - current_delta;
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