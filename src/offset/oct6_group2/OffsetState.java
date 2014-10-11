
package offset.oct6_group2;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

import java.util.*;

public class OffsetState {
    
    public static final int size = 32;

    public Point[] grid;
    public Pair pr;
    public Pair pr0;

    private ArrayList<int[]> myValidMoves;
    private ArrayList<int[]> theirValidMoves;

    public OffsetState(Point[] grid, Pair pair1, Pair pair2) {
        this(grid, pair1, pair2, null, null);
    }

    private OffsetState(Point[] grid, Pair pair1, Pair pair2, ArrayList<int[]> myValidMoves, ArrayList<int[]> theirValidMoves) {
        this.grid = cloneGrid(grid);
        this.pr = clonePair(pair1);
        this.pr0 = clonePair(pair2);

        this.myValidMoves = myValidMoves;
        this.theirValidMoves = theirValidMoves;
    }

    public void performMove(movePair movepr, int playerID) {
        if (movepr.move) {
            movePair trueMovePair = gridsMovePairFromOutsideMovePair(movepr);

            Point src = trueMovePair.src;
            Point target = trueMovePair.target;

            target.value = target.value + src.value;
            src.value = 0;
            target.owner = playerID;
            src.owner = -1;
        }
    }

    public void performMove(int[] move, int playerID) {
        performMove(movePairFromIntList(move), playerID);
    }

    public int gameScore(int playerID) {
        int score = 0;

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                Point p = grid[i * 32 + j];
                if (p.owner == playerID) {
                    score += p.value;
                }
            }
        }

        return score;
    }

    public int hashCode() {
        return grid.hashCode();
    }

    public boolean equals(Object state2) {
        OffsetState s = (OffsetState) state2;
        return s.grid.equals(grid);
    }

    public String toString() {
        return grid.toString() + pr.p + pr.q + pr0.p + pr0.q;
    }

    public int[] intListFromMovePair(movePair mp) {
        return new int[]{mp.src.x, mp.src.y, mp.target.x, mp.target.y};
    }

    public movePair movePairFromIntList(int[] move) {
        movePair movepr = new movePair();
        movepr.move = true;

        for (Point p : grid) {
            if (p.x == move[0] && p.y == move[1]) {
                movepr.src = p;
            } else if (p.x == move[2] && p.y == move[3]) {
                movepr.target = p;
            }

            if (movepr.src != null && movepr.target != null) {
                break;
            }
        }

        return movepr;
    }

    public movePair gridsMovePairFromOutsideMovePair(movePair outsideMovePair) {
        return movePairFromIntList(new int[]{outsideMovePair.src.x, outsideMovePair.src.y, outsideMovePair.target.x, outsideMovePair.target.y});
    }

    public boolean validateMove(movePair movepr, Pair pair) {
        Point src = movepr.src;
        Point target = movepr.target;
        boolean rightposition = false;

        if (Math.abs(target.x-src.x) == Math.abs(pair.p) && Math.abs(target.y-src.y) == Math.abs(pair.q)) {
            rightposition = true;
        }
        if (Math.abs(target.x-src.x) == Math.abs(pair.q) && Math.abs(target.y-src.y) == Math.abs(pair.p)) {
            rightposition = true;
        }
        if (rightposition && src.value == target.value && src.value > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public OffsetState cloneState() {
        return new OffsetState(cloneGrid(grid), clonePair(pr), clonePair(pr0), myValidMoves, theirValidMoves);
    }

    public ArrayList<int[]> validMoves(boolean mine) {
        if (mine && myValidMoves != null) return myValidMoves;

        if (!mine && theirValidMoves != null) return theirValidMoves;

        ArrayList<int[]> moves = new ArrayList<int[]>();

        Pair relevantPR = null;
        if (mine) {
            relevantPR = pr;
        } else {
            relevantPR = pr0;
        }

        movePair movepr = new movePair();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                movepr.src = grid[i * size + j];

                ArrayList<Point> potentiallyValidPoints = potentialMovesFromPoint(i, j, relevantPR);
                for (Point p : potentiallyValidPoints) {
                    if (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size) continue;

                    movepr.target = grid[p.x * size + p.y];

                    if (validateMove(movepr, relevantPR)) {
                        int[] m = new int[]{movepr.src.x, movepr.src.y, movepr.target.x, movepr.target.y};
                        moves.add(m);
                    }
                }
            }
        }

        if (mine) {
            myValidMoves = moves;
        } else {
            theirValidMoves = moves;
        }

        return moves;
    }

    public ArrayList<movePair> validMovesAsMovePairs(boolean mine) {
        ArrayList<movePair> moves = new ArrayList<movePair>();

        Pair relevantPR = null;
        if (mine) {
            relevantPR = pr;
        } else {
            relevantPR = pr0;
        }

        movePair movepr = new movePair();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                movepr.src = grid[i * size + j];

                ArrayList<Point> potentiallyValidPoints = potentialMovesFromPoint(i, j, relevantPR);
                for (Point p : potentiallyValidPoints) {
                    if (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size) continue;

                    movepr.target = grid[p.x * size + p.y];

                    if (validateMove(movepr, relevantPR)) {
                        moves.add(movepr);
                    }
                }
            }
        }

        return moves;
    }

    private ArrayList<Point> potentialMovesFromPoint(int i, int j, Pair pair) {
        Point src = grid[i * size + j];

        ArrayList<Point> list = new ArrayList<Point>(8);

        list.add(new Point(src.x + pair.p, src.y + pair.q, -1, -1));
        list.add(new Point(src.x + pair.p, src.y - pair.q, -1, -1));
        list.add(new Point(src.x - pair.p, src.y + pair.q, -1, -1));
        list.add(new Point(src.x - pair.p, src.y - pair.q, -1, -1));
        list.add(new Point(src.x + pair.q, src.y + pair.p, -1, -1));
        list.add(new Point(src.x + pair.q, src.y - pair.p, -1, -1));
        list.add(new Point(src.x - pair.q, src.y + pair.p, -1, -1));
        list.add(new Point(src.x - pair.q, src.y - pair.p, -1, -1));

        return list;
    }

    public boolean hasValidMoves(boolean mine) {
        return validMoves(mine).size() > 0;
    }

    public static Point clonePoint(Point p) {
        Point pClone = new Point(p.x, p.y, p.value, p.owner);
        pClone.change = p.change;
        return pClone;
    }

    public static Point[] cloneGrid(Point[] grid) {
        Point[] newGrid = new Point[grid.length];

        for (int i = 0; i < grid.length; i++) {
            Point p = grid[i];
            newGrid[i] = clonePoint(p);
        }

        return newGrid;
    }

    public static Pair clonePair(Pair p) {
        return new Pair(p.p, p.q);
    }

    public static String prettyGrid(Point[] grid) {
        String s = "";
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Point p = grid[j * size + i];
                s += "| " + p.value + " |";
            }
            s += "\n";
        }

        return s;
    }

}
