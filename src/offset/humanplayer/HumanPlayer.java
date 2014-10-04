package offset.humanplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

/**
 * Extension of Player meant to be controlled by a human (or anything via stdin, really).
 */
public class HumanPlayer extends offset.sim.Player {

    static final int SIZE = 32;

    private BufferedReader moveReader;

    /**
     * Constructs a human player (reading moves from stdin) with given player num and move symbol.
     */
    public HumanPlayer(Pair prin, int idin) {
        super(prin, idin);

        moveReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void init() {}

    /**
     * Getting the next move involes reading a move from stdin and ensuring it is a valid selection.
     * 
     * Move should be in the format 'row col' where row and col are integers and 0-indexed.
     */
    public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        System.out.println("Player " + id + ", please enter your next move with (p, q) (" + pr.p + "," + pr.q + ")");
        String moveString;

        movePair mp = new movePair();

        try {
            do {
                moveString = moveReader.readLine();
                String[] moves = moveString.split(" ");

                try {
                    int sx = Integer.parseInt(moves[0]);
                    int sy = Integer.parseInt(moves[1]);
                    int tx = Integer.parseInt(moves[2]);
                    int ty = Integer.parseInt(moves[3]);

                    if (sx == -1 && sy == -1 && tx == -1 && ty == -1) {
                        mp.move = false;
                        System.out.println("nice pass!!!!");
                        return mp;
                    }
                   
                    mp.src = grid[sx * SIZE + sy];
                    mp.target = grid[tx * SIZE + ty];
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Format is <src row> <src col> <target row> <target col>, both integers! Enter -1 -1 -1 -1 to pass");
                    continue;
                }

                if (!validateMove(mp, pr)) {
                    System.out.println("That's not a valid move! Try again.");
                } else {
                    mp.move = true;
                }
            } while (!validateMove(mp, pr));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return mp;
    }

    boolean validateMove(movePair movepr, Pair pr) {    
        if (movepr == null || movepr.src == null || movepr.target == null) return false;
        
        Point src = movepr.src;
        Point target = movepr.target;
        boolean rightposition = false;

        if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
            rightposition = true;
        }
        if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
            rightposition = true;
        }
        if (rightposition && src.value == target.value && src.value >0) {
            return true;
        }
        else {
            return false;
        }
    }

}