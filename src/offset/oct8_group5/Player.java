package offset.oct8_group5;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {

  private final int self = id;
  private final int SIZE = 32;//size of game board

  private final int STEAL_BOTH = 2;
  private final int STEAL_ONE  = 1;
  private final int STEAL_NONE = 0;

  private final int SEARCH_DEPTH = 4;

  private Point[][] board = new Point[SIZE][SIZE];
  private Pair selfPair = pr;
  private Pair opponentPair;
  private final int TIME_OUT = 1;//Vary this number to set time limit for subtree searching

  private double timelimit, timestart;

  private int minimaxID = id;

  List<movePair> my_legal_moves = new ArrayList<>();
  List<movePair> opponent_legal_moves = new ArrayList<>();

  public Player(Pair pair, int id) {
    super(pair, id);
  }

  public void init() {
  }

  // 1. Generate a priority set of moves, A, which depends upon if you take both, either, or neither (worst case) set of coins from the opponent
  // 2. From A, select the top N moves which cut off most of the moves of the opponent
  // 3. From the top N moves generated in step 2, choose the move which gets you the maximum number of coins
  public movePair move(Point[] grid, Pair pair0, Pair pair1, ArrayList<ArrayList> history) {
    updateBoard(grid);
    opponentPair = pair1; // pair0 is the same with selfPair

    //timelimit = ((TIME_OUT*1000))/(double)(getAllValidMoves(pr,board).length);
    Move move = bestMove(board, SEARCH_DEPTH, pair0, pair1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    if (validMove(move)){
      move.flag = true;
    }
    return move.toMovePair();
  }

  // get all valid moves
  private Move[] getAllValidMoves(Pair pair, Point[][] grid) {
    ArrayList<Move> moves = new ArrayList<Move>();
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        Move[] cellMoves = getAllValidMovesFrom(grid[i][j], pair, grid);
        for (Move move : cellMoves)
          moves.add(move);
      }
    }
    return moves.toArray(new Move[moves.size()]);
  }

  // return valid moves from one cell (either as source or as target)
  private Move[] getAllValidMovesFrom(Point p, Pair pair,Point[][] grid) {
    ArrayList<Move> moves = new ArrayList<Move>();
    final int[] cx = {-1, 1, -1, 1, -1, 1, -1, 1};
    final int[] cy = {-1, -1, 1, 1, -1, -1, 1, 1};
    for (int i = 0; i < 8; ++i) {
      int cp = pair.p, cq = pair.q;
      if (i >= 4) {
        cp = pair.q;
        cq = pair.p;
      }
      int xx = p.x + cx[i] * cp;
      int yy = p.y + cy[i] * cq;
      if (onBoard(xx, yy)) {
        Move move = new Move(grid[p.x][p.y], grid[xx][yy]);
        if (validMove(move, pair, grid))
          moves.add(move);
      }
    }
    return moves.toArray(new Move[moves.size()]);
  }

  // given a move, return how many cells from which it steals
  private int getStealScore(Move move, int id) {
    Point src = move.src;
    Point target = move.target;
    if (src.value == 1)
      return 2; //because we have a gain of 2
    if (src.owner != self && target.owner != self)
      return 2*src.value; //we gain both the piles
    else if (src.owner != self || target.owner != self)
      return src.value; //we gain one of the piles
    else
      return 0; //we gain nothing, both of them are our piles
  }

  // filter to get moves that steal from most cells
  private Move[] getBestStealMoves(Move[] allMoves, int id) {
    ArrayList<Move> moves = new ArrayList<Move>();
    int bestScore = 0;
    for (Move move : allMoves) {
      int tempScore = getStealScore(move,id);
      if (tempScore > bestScore) {
        moves.clear();
        bestScore = tempScore;
      }
      if (tempScore == bestScore)
        moves.add(move);
    }
    return moves.toArray(new Move[moves.size()]);
  }

  // filter to get limit number of moves that cutoff the most
  private Move[] getCutoffMoves(Move[] allMoves, int limit, Point[][] grid, Pair opponentPair) {
    PriorityQueue<Move> queue = new PriorityQueue<Move>(limit, new MoveComparator());
    for (Move move : allMoves) {
      Point src = move.src, target = move.target;
      int oldMoveNum = getAllValidMovesFrom(src, opponentPair, grid).length + getAllValidMovesFrom(target, opponentPair, grid).length;
      grid[src.x][src.y].value = 0;
      grid[target.x][target.y].value *= 2;
      int newMoveNum = getAllValidMovesFrom(src, opponentPair, grid).length + getAllValidMovesFrom(target, opponentPair, grid).length;
      grid[src.x][src.y].value = grid[target.x][target.y].value / 2;
      grid[target.x][target.y].value /= 2;
      move.delta = oldMoveNum - newMoveNum;
      if (queue.size() < limit || queue.peek().delta < move.delta) {
        if (queue.size() >= limit)
          queue.poll();
        queue.add(move);
      }
    }
    return queue.toArray(new Move[queue.size()]);
  }

  // get the score of a move
  private int scoreMove(Move move) {
    return move.src.value;
  }

  // get the move generating largest score
  private Move getLargestMove(Move[] moves) {
    Move bestMove = new Move();
    int bestScore = 0;
    for (Move move : moves) {
      int tempScore = scoreMove(move);
      if (tempScore > bestScore) {
        bestMove = new Move(move);
      }
    }
    return bestMove;
  }

  /////////// UTILITY METHODS ///////////

  // a better movePair class with copy constructor
  private class Move {
    public Point src;
    public Point target;
    public boolean flag;
    public int delta;
    public double score;//Used for alpha-beta

    Move() {
      src = new Point();
      target = new Point();
      flag = false;
      score = 0.0;
    }

    Move(Point src, Point target) {
      this.src = src;
      this.target = target;
      this.flag = false;
      this.score = 0.0;
    }

    Move(Move move) {
      src = move.src;
      target = move.target;
      flag = move.flag;
      score = move.score;
    }

    Move(Move move, double value) {
      if (move != null){
        src = move.src;
        target = move.target;
        flag = move.flag;
      }
      score = value;
    }

    Move(movePair move) {
      src = move.src;
      target = move.target;
      flag = move.move;
      score = 0.0;
    }

    public movePair toMovePair() {
      return new movePair(flag, src, target);
    }

    public String toString() {
      return "Source: (" + src.x + ", " + src.y + ") Target: (" + target.x + ", " + target.y + ")";
    }
  }

  // comparator for move.delta
  private class MoveComparator implements Comparator<Move> {
    @Override
      public int compare(Move a, Move b) {
        return (a.delta - b.delta);
      }
  }

  // update 2D board[][] with Point array grid[]
  private void updateBoard(Point[] grid) {
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        board[i][j] = grid[i * SIZE + j];
      }
    }
  }
  // update 2D board[][] with Point array grid[]
  private void updateBoard(Point[][] src, Point[][] target) {
    for (int i = 0; i < SIZE; ++i) {
      for (int j = 0; j < SIZE; ++j) {
        target[i][j] = new Point(src[i][j]);
      }
    }
  }

  // check if a point is on board
  private boolean onBoard(int x, int y) {
    return (x >= 0 && x < SIZE && y >= 0 && y < SIZE);
  }

  private boolean onBoard(Point p) {
    if (p != null)
      return onBoard(p.x, p.y);
    else
      return false;
  }

  // generic method of checking if a move is valid
  private boolean validMove(Point src, Point target, Pair pair, Point[][] grid) {
    if (onBoard(src) && onBoard(target) &&
        grid[src.x][src.y].value != 0 && grid[src.x][src.y].value == grid[target.x][target.y].value &&
        (Math.abs(src.x - target.x) == pair.p && Math.abs(src.y - target.y) == pair.q ||
         Math.abs(src.x - target.x) == pair.q && Math.abs(src.y - target.y) == pair.p))
      return true;
    return false;
  }

  // check if a move is valid given a Pair
  private boolean validMove(Move move, Pair pair, Point[][] grid) {
    return validMove(move.src, move.target, pair, grid);
  }

  // check if a move is valid using selfPair
  private boolean validMove(Move move) {
    return validMove(move, selfPair, board);
  }

  //Implementation of alpha-beta minimax
  Move bestMove(Point[][] grid, int depth, Pair myPair, Pair herPair, double mybest, double herbest){
    Point[][] tempGrid = new Point[SIZE][SIZE];
    double bestscore = 0.0;
    Move bestMove = new Move();
    Move tempmove = new Move();
    double tempscore = 0.0;

    double timebefore, timeafter;

    //Score for this player on current board
    int thisBoardScore = calculateScore(minimaxID,grid);

    //The id of this player
    int playerID = minimaxID;

    //End recursion at bottom of search space.
    //Just return the score since we don't choose a move at this level.
    if (depth == 0){
      return new Move(null,thisBoardScore);
    }

    //Get set of moves for this player based on our filters
    Move[] moves = getAllValidMoves(myPair, grid);
    moves = getBestStealMoves(moves,minimaxID);
    moves = getCutoffMoves(moves, 5, grid, herPair);

    //If no moves are left, just return this board's score
    if (moves.length == 0){
      return new Move(null,thisBoardScore);
    }

    //Set alpha-beta params
    bestscore = mybest;
    bestMove = null;


    for (int i = 0; i < moves.length; i++){

      //Choose the next move to try
      Move chosenMove = moves[i];

      //Make the chosen move on our copied grid, return the orginal owners of both tiles
      int[] oldOwners = makeHypMove(minimaxID,grid,chosenMove);

      //Need to set which player is moving next
      if (minimaxID == 1)
        minimaxID = 0;
      else
        minimaxID = 1;

      //Recursively call bestMove to determine what opponent will do, negating the alpha-beta params
      //so they now pertain to the opponent
      tempmove = bestMove(grid, depth-1, herPair, myPair, -1*herbest, -1*bestscore);

      //Undo chosen move on grid
      undoHypMove(oldOwners,grid,chosenMove);

      //Set minimaxID back to the current player
      minimaxID = playerID;

      //Negate opponent's best score
      tempscore = -1 * tempmove.score;


      //check if opponent's best score is lower than scores we found
      //previously (we do > because we negated their score)
      if (tempscore > bestscore){
        bestscore = tempscore;
        bestMove = chosenMove;
      }

      //Here's where the "pruning" happens -- return a move if the alpha-beta window shuts
      if (bestscore > herbest){
        return new Move(bestMove, bestscore);
      }
    }


    //We checked every possible move, so return the best one
    return new Move(bestMove, bestscore);
  }

  //Given a board and a hypothetical move, update the grid to take that move into account.
  int[] makeHypMove(int id, Point[][] grid, Move move){
    int[] oldowners = new int[2];
    Point src, target;
    src = grid[move.src.x][move.src.y];
    target = grid[move.target.x][move.target.y];
    oldowners[0] = src.owner;
    oldowners[1] = target.owner;

    //Source 
    src.value = 0;
    src.owner = -1;
    src.change = true;

    //Target
    target.value *= 2;
    target.owner = id;
    target.change = true;

    return oldowners;
  }

  void undoHypMove(int[] oldowners, Point[][] grid, Move move){
    Point src, target;
    src = grid[move.src.x][move.src.y];
    target = grid[move.target.x][move.target.y];

    //Source 
    src.value = target.value / 2;
    src.owner = oldowners[0];
    src.change = false;

    //Target
    target.value /= 2;
    target.owner = oldowners[1];
    target.change = false;

  }

  int calculateScore(int id,Point[][] grid) {
    int score =0;
    for (int i=0; i<SIZE; i++) {
      for (int j =0; j<SIZE; j++) {
        if (grid[i][j].owner == id) 
          score = score+grid[i][j].value;
        
        else if (grid[i][j].value > 1)
          score = score - grid[i][j].value;
      }
    }
    return score;
  }

}
