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
public class AlphaBetaPlayer extends offset.sim.Player {
	
    private static final int size = 32;

    private static final long ONE_SECOND = 1000000000;      /* a second in nanoseconds */
    private static final long ABORT_TIME = 300000000;       /* buffer time to abort any search from to ensure we don't go over time limit */
    private static final int MAX_DEPTH = 2;                /* max depth we would want to reach for alpha beta search */
    
    public static final int MOVES_TO_CHECK = 250;
    public static final int GOOD_MOVE_LIMIT = 3;
    public static final int GOOD_MOVE_CONTROL_COUNT = 100;
    public static final double THEIR_MOVE_WEIGHT = 1.3;
    public static final double BIG_SQUARE_POWER = 1.18;
    
    private static final movePair FORFEIT_MOVE = new movePair(false, null, null);     /* return this when the agent can't move */
    private final MaxActionValueComparator maxComparator = new MaxActionValueComparator(); /* used for sorting ActionValues in descending order */
    private final MinActionValueComparator minComparator = new MinActionValueComparator(); /* used for sorting ActionValues in ascending order */

    private long moveTimeLimit;                             /* time to select a move in ns */
    private SearchNode<OffsetState> recentStates;                /* essentially a linked list of recent moves in game */
    private HashMap<Point[], Double> stateEvaluations;       /* a transition table of OffsetState to heuristic score */

    public Point[] currentGrid;
    public OffsetState currentState;

	public AlphaBetaPlayer(Pair prin, int idin) {
		super(prin, idin);

        moveTimeLimit = (long) (ONE_SECOND * 0.2); // convert 0.2 seconds to nanoseconds
        recentStates = null;

        stateEvaluations = new HashMap<Point[], Double>();
	}

	public void init() {

	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
        currentGrid = grid;

        currentState = new OffsetState(grid, pr, pr0);

        if (recentStates == null) {
            recentStates = new SearchNode<OffsetState>(currentState);
        }

        stateEvaluations.clear(); /* have to clear this or we run out of memory */

        /* perform the move search */
        OffsetState stateCopy = currentState.cloneState();
        SearchNode<OffsetState> moveSearchTree = new SearchNode<OffsetState>(stateCopy);
        movePair move = iterativeDeepeningAlphaBetaSearch(moveSearchTree);

        /* search failed us, but lets try to pick something .. */
        if (move == null) {
            return FORFEIT_MOVE;
        }

        recentStates = recentStates.addSuccessor(stateCopy, intListFromMovePair(move));

        return move;
	}

    public double score(OffsetState state) {
        if (stateEvaluations.containsKey(state.grid)) {
            return stateEvaluations.get(state.grid);
        }

        double s = 0;

        ArrayList<movePair> myValidMoves = state.validMovesAsMovePairs(true);
        ArrayList<movePair> theirValidMoves = state.validMovesAsMovePairs(false);

        for (movePair mp : myValidMoves) {
            s += Math.pow(mp.target.value, BIG_SQUARE_POWER);
        }

        for (movePair mp : theirValidMoves) {
            s -= Math.pow(mp.target.value, BIG_SQUARE_POWER) * THEIR_MOVE_WEIGHT; // their moves are worth more
        }

        s += state.gameScore(id);
        s -= state.gameScore(otherID());

        // TODO: devalue board based on moves the opponent can steal

        // TODO: do better in the early game

        // TODO: do better in the late game

        // TODO: definitely pick a steal if it is really valuable (this means having the score take into account the move)

        // TODO: also focus on moves where the source or target is currently vulnerable to a steal, but could be saved with a move

        stateEvaluations.put(state.grid, s);
        return s;
    }

    public String toString() {
        return playerName() + " // (" + pr.p + "," + pr.q + ")";
    }

    public String playerName() {
        return "Player " + id;
    }

    /*****
    *
    * ALPHA-BETA SEARCH !!!!!
    *
    */

    /**
     * Performs an iterative deeping a-b search from the given root node, searching deeper and deeper
     * layers of the tree until time to select a move has elapsed or the maximum allowed depth is reached.
     *
     * This robust method for time-sensitive move selection in a game-playing agent was given (in
     * concept) in an artificial intelligence book from last year!!
     */
    private movePair iterativeDeepeningAlphaBetaSearch(SearchNode<OffsetState> root) {
        ActionValue av = null;
        long startTime = System.nanoTime();
        int i;

        HashMap<OffsetState, List<int[]>> moveOrdering = new HashMap<OffsetState, List<int[]>>();

        /* until we reach the max depth or run out of time, do an alpha beta search to depth i */
        for (i=1; i<MAX_DEPTH; i++) {
            ActionValue possibleAv = alphaBetaMaxVal(root, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, i, startTime, moveOrdering);

            if (possibleAv != null)  { /* completed search successfully */
                av = possibleAv;
            }
            else {                      /* didn't, out of time, abort */
                break;           
            }   
        }

        /* return the last succesfully completed search's recommened move */
        if (av != null) {
            av.move.move = true;
            return av.move;
        }
        else {
            return null;
        }
    }

    /**
     * The alpha-beta search method for the 'max' player.
     *
     * Takes a stateNode with the OffsetState and book-keeping information, current alpha and beta values, the maxDepth at which to abandon the search,
     * the startTime (from System.nanoTime()) at which the search originally started, and a map of game state to an ordered list of moves to explore.
     *
     * Returns the action that led to the highest heurisitc value in the search, and the actual value in an ActionValue container.
     */
    private ActionValue alphaBetaMaxVal(SearchNode<OffsetState> stateNode, double alpha, double beta, int maxDepth, Long startTime, HashMap<OffsetState, List<int[]>> moveOrdering) {
        /* time to quit the search, return the heuristic value of the current state and action that led to it */
        if (cutoffTest(stateNode, maxDepth, true)) {
            ActionValue av = new ActionValue(score(stateNode.getData()), stateNode.getAction());
            return av;
        }

        /* ran out of time for the larger search, abort! */
        if (abortTimeTest(startTime))
            return null;

        double v = Double.NEGATIVE_INFINITY;        /* start the best value at negative infinity so *anything* else is better */
        double a = alpha;                           /* make alpha copy to change it */
        double b = beta;                            /* make beta copy to change it */
        ActionValue best = null;                    /* to contain the move with the highest value so far, and that value */

        List<int[]> actions;                        /* actions to examine from this state */
        List<int[]> orderedActions = moveOrdering.get(stateNode.getData()); /* might have a predefined ordering, if so use it */
        List<movePair> movePairActions;

        if (orderedActions == null) {
            movePairActions = stateNode.getData().validMovesAsMovePairs(true);
            actions = interestingMovesFromPairs(movePairActions, stateNode.getData());
        }
        else {
            actions = interestingMovesFromInts(orderedActions, stateNode.getData());
        }

        if (actions.size() <= 0) return best;

        PriorityQueue<ActionValue> newOrdering = new PriorityQueue<ActionValue>(actions.size(), maxComparator); /* for ordering in later searches from this state */

        /* examine every move */
        for (int index = 0; index < actions.size(); index++) {
            int[] move = actions.get(index);

            OffsetState resultState = stateNode.getData().cloneState();    /* clone state to allow manipulation of it */
            resultState.performMove(move, id);
            SearchNode<OffsetState> resultNode = stateNode.addSuccessor(resultState, move); /* enact the move and add it to the search tree */

            ActionValue minResult = alphaBetaMinVal(resultNode, a, b, maxDepth, startTime, moveOrdering); /* let 'min' choose a move based on this move */
            if (minResult == null)
                return null; /* min had to abort search, so we should too */

            newOrdering.add(new ActionValue(minResult.v, move));

            if (v < minResult.v) {                          /* min's move is better than our current best move */
                best = new ActionValue(minResult.v, move);
                v = minResult.v;
            }

            if (v >= b) {                                   /* we've reached a value that surpasses beta, so we can prune */
                ActionValue av = new ActionValue(v, move);
                setOrdering(newOrdering, actions, moveOrdering, stateNode.getData());
                return av;
            }
            a = Math.max(a, v);                             /* set alpha */

            if (abortTimeTest(startTime))                   /* need to abort */
                return null;
        }

        setOrdering(newOrdering, actions, moveOrdering, stateNode.getData());
        return best;                                        /* did not prune, return best move and value */
    }

    /**
     * The alpha-beta search method for the 'min' player.
     *
     * Takes a stateNode with the OffsetState and book-keeping information, current alpha and beta values, the maxDepth at which to abandon the search,
     * the startTime (from System.nanoTime()) at which the search originally started, and a map of game state to an ordered list of moves to explore.
     *
     * Returns the action that led to the lowest heuristic value in the search, and the actual value in an ActionValue container.
     *
     * In-line comments here are only adressing changes from the 'max' version.
     */
    private ActionValue alphaBetaMinVal(SearchNode<OffsetState> stateNode, double alpha, double beta, int maxDepth, long startTime, HashMap<OffsetState, List<int[]>> moveOrdering) {
        if (cutoffTest(stateNode, maxDepth, false)) {
            ActionValue av = new ActionValue(score(stateNode.getData()), stateNode.getAction());
            return av;
        }

        if (abortTimeTest(startTime))
            return null;

        double v = Double.POSITIVE_INFINITY;    /* start v at positive infinity because we want move with the lowest possible value */
        double a = alpha;
        double b = beta;
        ActionValue best = null;

        List<int[]> actions;
        List<int[]> orderedActions = moveOrdering.get(stateNode.getData());
        List<movePair> movePairActions;

        if (orderedActions == null) {
            movePairActions = stateNode.getData().validMovesAsMovePairs(true);
            actions = interestingMovesFromPairs(movePairActions, stateNode.getData());
        }
        else {
            actions = interestingMovesFromInts(orderedActions, stateNode.getData());
        }

        if (actions.size() <= 0) return best;

        PriorityQueue<ActionValue> newOrdering = new PriorityQueue<ActionValue>(actions.size(), minComparator);

        /* examine every move */
        for (int index = 0; index < actions.size(); index++) {
            int[] move = actions.get(index);

            OffsetState resultState = stateNode.getData().cloneState();
            resultState.performMove(move, otherID());
            SearchNode<OffsetState> resultNode = stateNode.addSuccessor(resultState, move);

            ActionValue maxResult = alphaBetaMaxVal(resultNode, a, b, maxDepth, startTime, moveOrdering); /* let max select a move */
            if (maxResult == null)
                return null;

            newOrdering.add(new ActionValue(maxResult.v, move));

            if (v > maxResult.v) {                                  /* max's best move still lets us be better than we are currently */
                v = maxResult.v;
                best = new ActionValue(maxResult.v, move);
            }

            if (v <= a) {                                           /* we have gone lower than alpha, so we can prune */
                ActionValue av = new ActionValue(v, move);
                setOrdering(newOrdering, actions, moveOrdering, stateNode.getData());
                return av;
            }
            b = Math.min(b, v);

            if (abortTimeTest(startTime))
                return null;
        }

        setOrdering(newOrdering, actions, moveOrdering, stateNode.getData());
        return best;
    }

    public int otherID() {
        if (id == 0) return 1;
        else return 0;
    }

    public static int[] intListFromMovePair(movePair mp) {
        return new int[]{mp.src.x, mp.src.y, mp.target.x, mp.target.y};
    }

    public movePair movePairFromIntList(int[] move, Point[] grid) {
        movePair movepr = new movePair();
        movepr.move = true;
        movepr.src = null; movepr.target = null;

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

    /**
     * Used to set best-known move orderings from a round of alpha-beta search (during 
     * iterative deeping alpha beta search) for the next round.
     *
     * Takes a partially filled PriorityQueue of moves already oredered, a list of all possible
     * actions as the given state (that may or may not be in the queue), a map of OffsetState
     * to move list (to update with ordered list), and a gomoku state to move from.
     *
     * Puts the ordered list of moves to select into the ordering map for the given state
     * so that the agent selecting moves for that state can order moves appropriately.
     */
    private void setOrdering(PriorityQueue<ActionValue> ordering, List<int[]> allActions, HashMap<OffsetState, List<int[]>> orderingMap, OffsetState state) {
        List<int[]> orderedList = new ArrayList<int[]>();
        Set<int[]> movesInList = new HashSet<int[]>();

        while (ordering.peek() != null) {                   /* add everything in the ordering queue to the orderedList */
            ActionValue av = ordering.poll();
            orderedList.add(av.intList);
            movesInList.add(av.intList);
        }

        for (int[] action : allActions) {                   /* add anything not ordered to the ordered list in arbitrary fashion */
            if (!movesInList.contains(action))
                orderedList.add(action);
        }

        orderingMap.put(state, orderedList);                /* update the ordering map */
    }

    /**
     * Determines if alpha-beta search should stop at the given node.
     *
     * Will return true in two cases:
     * 1. The search has reached the maximum depth for the current iteration.
     * 2. The search has reached a state that is end-game for gomoku.
     */
    private boolean cutoffTest(SearchNode<OffsetState> stateNode, int maxDepth, boolean mine) {
        if (stateNode.getDepth() >= maxDepth) {
            return true;
        }

        OffsetState state = stateNode.getData();
        int[] lastMove = stateNode.getAction();

        if (lastMove != null && !state.hasValidMoves(mine)) {
            return true;
        }

        return false;
    }

    /**
     * Determines if a search should be "ABANDONED AT ALL COST"; if time is running
     * dangerously low, this method will return true and inform alpha-beta search to
     * stop! 
     */
    private boolean abortTimeTest(long startTime) {
        // long elapsed = System.nanoTime() - startTime;

        // if (moveTimeLimit - elapsed < ABORT_TIME)
        //     return true;
        // else
        //     return false;

        return false;
    }

    private List<int[]> interestingMovesFromPairs(List<movePair> legalMoves, OffsetState state) {
        ArrayList<int[]> goodMoves = new ArrayList<int[]>(MOVES_TO_CHECK);
        HashSet<int[]> moveSet = new HashSet<int[]>();

        // add any move that we could take away from an opponent
        for (movePair mp : legalMoves) {
            if (mp.src.owner == otherID() || mp.target.owner == otherID()) {
                // we can steal from them
                if (sourcesTargetReachableFromNextTurn(mp.target, id, state.pr0, state) <= 0) {
                    // and they can't steal it back
                    int[] move = intListFromMovePair(mp);
                    goodMoves.add(move);
                    moveSet.add(move);
                }
            }
        }

        int moveListSize = MOVES_TO_CHECK;
        if (goodMoves.size() >= GOOD_MOVE_LIMIT) {
            moveListSize = GOOD_MOVE_CONTROL_COUNT;
        }

        // TODO: add moves that prevent a steal from us
        // for (movePair mp : legalMoves) {
        //     int[] move = intListFromMovePair(mp);

        //     // if making this move won't lead to a steal
        //     if (!moveSet.contains(move) && sourcesTargetReachableFromNextTurn(mp.target, id, state.pr0, state) <= 0) {
        //         // and if either the source or target is currently vulnerable to a steal
        //         if ((mp.src.owner == id && sourcesTargetReachableFromWithValue(mp.src, mp.src.value, id, state.pr0, state) > 0) ||
        //             (mp.target.owner == id && sourcesTargetReachableFromWithValue(mp.target, mp.target.value, id, state.pr0, state) > 0)) {
        //                 goodMoves.add(move);
        //                 moveSet.add(move);
        //         }
        //     }
        // }

        // then add random legal moves to fill
        Collections.shuffle(legalMoves, new Random());
        for (int i = 0; i < legalMoves.size() && goodMoves.size() <= moveListSize; i++) {
            movePair mp = legalMoves.get(i);
            int[] move = intListFromMovePair(mp);
            // we don't want to add stealable moves
            if (!moveSet.contains(move) && sourcesTargetReachableFromNextTurn(mp.target, id, state.pr0, state) <= 0) {
                goodMoves.add(move);
                moveSet.add(move);
            }
        }

        // have to add a bad move if we didn't find a good one
        if (goodMoves.size() < 1 && legalMoves.size() > 0) {
            int[] badMove = intListFromMovePair(legalMoves.get(0));
            goodMoves.add(badMove);
        }

        return goodMoves;
    }

    private List<int[]> interestingMovesFromInts(List<int[]> legalMoves, OffsetState state) {
        ArrayList<movePair> legalMovePairs = new ArrayList<movePair>(legalMoves.size());
        
        for (int[] move : legalMoves) {
            legalMovePairs.add(movePairFromIntList(move, state.grid));
        }

        return interestingMovesFromPairs(legalMovePairs, state);
    }

    private int sourcesTargetReachableFromNextTurn(Point target, int ownerID, Pair pair, OffsetState state) {
        return sourcesTargetReachableFromWithValue(target, target.value * 2, ownerID, pair, state);
    }

    private int sourcesTargetReachableFromWithValue(Point target, int value, int ownerID, Pair pair, OffsetState state) {
        if (target.owner != ownerID) return 0;

        Point nextTarget = new Point(target.x, target.y, value, target.owner);

        int count = 0;

        ArrayList<Point> potentialSources = state.potentialMovesFromPoint(nextTarget, pair);
        for (Point p : potentialSources) {
            if (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size) continue;

            movePair mp = new movePair(true, state.grid[p.x * size + p.y], nextTarget);
            if (state.validateMove(mp, pair)) {
                count++;
            }
        }

        return count;
    }

    /*****
     *
     * HELPFUL PRIVATE CLASSES !!!!!
     *
     */

    /**
     * Helper class used in A-B search to keep track of the value of
     * various moves across various search levels / depths
     */
    private class ActionValue implements Comparable<ActionValue> {
        public double v;
        public movePair move;
        int[] intList;

        /**
         * Constructs an ActionValue with given value and move
         */
        public ActionValue(double v, movePair move) {
            this.v = v;
            this.move = move;
            this.intList = intListFromMovePair(move);
        }

        public ActionValue(double v, int[] move) {
            this.v = v;
            this.intList = move;
            this.move = movePairFromIntList(move, currentGrid);
        }

        /**
         * To string just dumps the value, row, and col.
         */
        public String toString() {
            return v + " --- " + move;
        }

        /**
         * Compares the value portions of the two ActionValues
         */
        public int compareTo(ActionValue other) {
            Double V = new Double(v);
            Double otherV = new Double(other.v);
            return V.compareTo(otherV);
        }
    }

    /**
     * Little helper class for sorting that compares two ActionValues by value
     * in descending order.
     */
    private class MaxActionValueComparator implements Comparator<ActionValue> {
        public int compare(ActionValue o1, ActionValue o2) {
            return -1 * o1.compareTo(o2);
        }

        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    /**
     * Little helper class for sorting that compares two ActionValues by value
     * in the traditional (ascending) order.
     */
    private class MinActionValueComparator implements Comparator<ActionValue> {
        public int compare(ActionValue o1, ActionValue o2) {
            return o1.compareTo(o2);
        }

        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}
