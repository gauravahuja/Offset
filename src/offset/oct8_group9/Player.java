package offset.oct8_group9;

import java.util.*;

import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.sim.Player {
	
	boolean hasInitialized = false;
  
  static final int size = 32;

  HashMap<Position, Point> ourPositions;
  HashMap<Position, Point> theirPositions;
  Pair ourPair;
  Pair theirPair;
  int ourPlayerId;
  int theirPlayerId;
  Point[] board;


	public Player(Pair prin, int idin) {
		super(prin, idin);
		// TODO Auto-generated constructor stub
	}

	public void init() {

      ourPositions = new HashMap<Position, Point>();
      theirPositions = new HashMap<Position, Point>();
	}

	public movePair move(Point[] grid, Pair pr, Pair pr0, ArrayList<ArrayList> history) {
    // STAGE 1: analysis previous move and update knowledge base
    // TODO later deliverable

    if (!hasInitialized) {
      init();
      ourPair = pr;
      theirPair = pr0;

      if (history.size() % 2 == 1) {
        ourPlayerId = 1;
        theirPlayerId = 0;
      } else {
        ourPlayerId = 0;
        theirPlayerId = 1;
      }

      hasInitialized = true;
    }
   /* for(Point mp: grid){
      System.out.print("("+mp.x + ":" + mp.y + "--" + mp.owner+")"); 
    }
    */
    int historySize = history.size();
    board = grid;
    updatePositionsAfterMove();
    
    
    
	
    // STAGE 3: for each move, predict opponent player's move and choose the correct move 
    // that maximize play gain and minimize opponent gain in next turn
	
		return chessStrategy();
	}

  movePair chessStrategy() {

    movePair ourMove;

    ArrayList<Position> ourVulnerablePositions = myVulnerableNow();
    ArrayList<movePair> strikeAblePositions = strikeAbleNow();
    ArrayList<movePair> layupPositions = strikeAbleAtOne();

    Position escapeOrigin = null;
    Position bestEscape = null;
    // min is -1 for escape score
    int escapeScore = -1;

    for (Position vulnerablePosition : ourVulnerablePositions) {
      ArrayList<Position> escapeRoutes = positionsAtOffsetFromPosition(vulnerablePosition, ourPair);
      for (Position escapeRoute : escapeRoutes) {
        int score = getHealthScore(vulnerablePosition, escapeRoute);
        if(score >= 0){
          score += getCurrentPointForPosition(vulnerablePosition).value;  
        }
        if (score > escapeScore) {
          escapeScore = score;
          bestEscape = escapeRoute;
          escapeOrigin = vulnerablePosition;
        }
      }
    }

    Position attackOrigin = null;
    Position bestAttack = null;
    int attackScore = -1;

    for (movePair strikeAblePosition : strikeAblePositions) {
        int score = getHealthScore(new Position(strikeAblePosition.src), new Position(strikeAblePosition.target));
        if(score >= 0){
          score += strikeAblePosition.target.value;
        }
        if (score > attackScore) {
          attackScore = score;
          bestAttack = new Position(strikeAblePosition.target);
          attackOrigin = new Position (strikeAblePosition.src);
        }
      }
    
    Position layupOrigin = null;
    Position bestLayup = null;
    int layupScore = -1;

    for (movePair layupPosition : layupPositions) {

      int score = getHealthScore(new Position(layupPosition.src), new Position(layupPosition.target));

      if (score > layupScore) {
        layupScore = score;
        bestLayup = new Position(layupPosition.target);
        layupOrigin = new Position(layupPosition.src);
      }
    }

    System.out.println("Attack score: " + attackScore);
    System.out.println("LayupScore: " + layupScore);
    System.out.println("EscapeScore" + escapeScore);

    System.out.println("Attack: " + attackOrigin + bestAttack);
    System.out.println("Layup: " + layupOrigin + bestLayup);
    System.out.println("Escape: " + escapeOrigin + bestEscape);
    
    int maxScore = Math.max(Math.max(attackScore, layupScore), escapeScore);
    // negative score means no possible move whatsoever
    if(maxScore < 0){
      return combineRandom();
    } else {
      if(maxScore == escapeScore){
        return new movePair(true, getCurrentPointForPosition(escapeOrigin), getCurrentPointForPosition(bestEscape));
      } else if (maxScore == attackScore){
        return new movePair(true, getCurrentPointForPosition(attackOrigin), getCurrentPointForPosition(bestAttack));
      } else {
        return new movePair(true, getCurrentPointForPosition(layupOrigin), getCurrentPointForPosition(bestLayup));
      }
    }
  }

  movePair combineRandom() {
    int value = 1;
  
    while (value < 129) {
      for (int i = 0; i < board.length; i++) {
        Point p = board[i];
        if (p.value == value) {
          ArrayList<Position> surroundings = positionsAtOffsetFromPosition(new Position(p), ourPair);
          for (Position surrounding : surroundings) {
            Point surPoint = getCurrentPointForPosition(surrounding);
            if (surPoint.value == value) {
              return new movePair(true, p, surPoint);
            }
          }
        }
      }
      value *= 2;
    }

    return new movePair(false, null, null);
  }


  int getHealthScore(Position heldPosition, Position possibleRoute) {
    
    int score = -1;

    Point heldPoint = getCurrentPointForPosition(heldPosition);
    Point possibleRoutePoint = getCurrentPointForPosition(possibleRoute);

    
    if (possibleRoutePoint.value == heldPoint.value) {
      score = 0;
      int goodPoints = 0;
      int badPoints = 0;
      ArrayList<Position> aroundPossibles = positionsAtOffsetFromPosition(possibleRoute, ourPair);
      for (Position aroundPossible : aroundPossibles) {
        Point aroundPossiblePoint = getCurrentPointForPosition(aroundPossible);
        
        if (aroundPossiblePoint.value == 2 * heldPoint.value) {
          if (aroundPossiblePoint.owner == theirPlayerId) {
            goodPoints += 2 * heldPoint.value;
          } else {
            goodPoints += heldPoint.value;
          }
        }

        if (heldPoint.owner == theirPlayerId && possibleRoutePoint.owner == theirPlayerId) {
          goodPoints += heldPoint.value;
        } 
      }

      ArrayList<Position> opparoundPossibles = positionsAtOffsetFromPosition(possibleRoute, theirPair);
      for (Position opparoundPossible : opparoundPossibles) {
        Point opparoundPossiblePoint = getCurrentPointForPosition(opparoundPossible);
        if (opparoundPossiblePoint.value == 2 * heldPoint.value) {
          badPoints += 10 * heldPoint.value;
        }
      }

      score = goodPoints - badPoints;
    }

    return score;
  }


  ArrayList<movePair> strikeAbleNow() {

    ArrayList<movePair> result = new ArrayList<movePair>();
    
    for (Position theirPos : theirPositions.keySet()) {
      for (Position ourPos : ourPositions.keySet()) {
        if (isAtOffset(theirPos, ourPos, ourPair)) {
          Point theirPoint = getCurrentPointForPosition(theirPos);
          Point ourPoint = getCurrentPointForPosition(ourPos);
          if (/*ourPoint.value != 0 && */theirPoint.value == ourPoint.value) {
            movePair mpair1 = new movePair(true, ourPoint, theirPoint);
            movePair mpair2 = new movePair(true, theirPoint, ourPoint);
            result.add(mpair1);
            result.add(mpair2);
          }
        }
      }
    }

    return result;
  }

  ArrayList<movePair> strikeAbleAtOne() {
    ArrayList<movePair> result = new ArrayList<movePair>();

    for (Position theirPos : theirPositions.keySet()) {
      Point theirPoint = getCurrentPointForPosition(theirPos);
      for (Position atOffset : positionsAtOffsetFromPosition(theirPos, ourPair)) {
        Point atOffsetPoint = getCurrentPointForPosition(atOffset);
        if (/*atOffsetPoint.value != 0 && */atOffsetPoint.value * 2 == theirPoint.value) {
          for (Position nextLevel : positionsAtOffsetFromPosition(atOffset, ourPair)) {
            Point nextLevelPoint = getCurrentPointForPosition(nextLevel);
            if (/*nextLevelPoint.value != 0 && */nextLevelPoint.value * 2 == theirPoint.value) {
              result.add(new movePair(true, atOffsetPoint, nextLevelPoint));
              result.add(new movePair(true, nextLevelPoint, atOffsetPoint));
            }
          }
        } 
      }
    }

    return result;
  }

  ArrayList<Position> myVulnerableNow() {
    ArrayList<Position> result = new ArrayList<Position>();

    for (Position ourPos : ourPositions.keySet()) {
      for (Position atTheirOffset : positionsAtOffsetFromPosition(ourPos, theirPair)) {
        Point theirOffsetAway = getCurrentPointForPosition(atTheirOffset);
        Point ourPoint = getCurrentPointForPosition(ourPos);
        if (/*theirOffsetAway.value != 0 && */theirOffsetAway.value == ourPoint.value) {
          result.add(ourPos);
        }
      }
    }

    return result;
  }



// ===================
// utility methods
// ===================

  boolean validateMove(movePair movepr, Pair pr) {
   	Point src = movepr.src;
    Point target = movepr.target;
    boolean rightposition = false;
    if (Math.abs(target.x-src.x)==Math.abs(pr.p) && Math.abs(target.y-src.y)==Math.abs(pr.q)) {
    	rightposition = true;
    }
    if (Math.abs(target.x-src.x)==Math.abs(pr.q) && Math.abs(target.y-src.y)==Math.abs(pr.p)) {
    	rightposition = true;
    }
    if (rightposition && src.value == target.value && src.value >0) {
      return true;
    }
    else {
      return false;
    }
  }

  Point getCurrentPointForPosition(Position a) {
    return board[convertPositionToIndex(a)];
  }



  int convertPositionToIndex(Position a) {
    return a.x * size + a.y;
  }

  Position convertIndexToPosition(int index) {
    return new Position(index / size, index % size);
  }

  boolean isAtOffset(Position a, Position b, Pair offset) {

    return (Math.abs(b.x - a.x) == offset.p && Math.abs(b.y - a.y) == offset.q) ||
      (Math.abs(b.x - a.x) == offset.q && Math.abs(b.y - a.y) == offset.p);

  }

  movePair getTheirLastMoveFromHistory(ArrayList<ArrayList> history) {
    int size = history.size();
    return (movePair) history.get(size - 1).get(1);
  }

  void updatePositionsAfterMove() {
    ourPositions = new HashMap<Position, Point>();
    theirPositions = new HashMap<Position, Point>();
    for(int i = 0; i < board.length; i++){
      if(board[i].value == 0){
        continue;
      }
      if(board[i].owner == ourPlayerId){
        ourPositions.put(convertIndexToPosition(i), board[i]);
      }
      if(board[i].owner == theirPlayerId){
        theirPositions.put(convertIndexToPosition(i), board[i]);
      }
    }
  }

  Point[] possibleBoardAfterMove(Point[] board, movePair mpair, int playerId) {
    Point src = board[convertPositionToIndex(new Position(mpair.src))];
    Point dst = board[convertPositionToIndex(new Position(mpair.target))];

    src.owner = -1;
    src.value = 0;
    
    dst.owner = playerId;
    dst.value *= 2;

    return board;
  }

  ArrayList<Position> positionsAtOffsetFromPosition(Position a, Pair offset) {

    ArrayList<Position> list = new ArrayList<Position>();
    
    if (a.x + offset.p < size && a.y + offset.q < size)
      list.add(new Position(a.x + offset.p, a.y + offset.q));

    if (a.x - offset.p >= 0 && a.y - offset.q >= 0) 
      list.add(new Position(a.x - offset.p, a.y - offset.q));

    if (a.x + offset.q < size && a.y + offset.p < size)
      list.add(new Position(a.x + offset.q, a.y + offset.p));

    if (a.x - offset.q >= 0 && a.y - offset.p >= 0)
      list.add(new Position(a.x - offset.q, a.y - offset.p));

    if (a.x + offset.p < size && a.y - offset.q >= 0)
      list.add(new Position(a.x + offset.p, a.y - offset.q));

    if (a.x - offset.p >= 0 && a.y + offset.q < size)
      list.add(new Position(a.x - offset.p, a.y + offset.q));

    if (a.x + offset.q < size && a.y - offset.p >= 0)
      list.add(new Position(a.x + offset.q, a.y - offset.p));

    if (a.x - offset.q >= 0 && a.y + offset.p < size)
      list.add(new Position(a.x - offset.q, a.y + offset.p));

    return list;

  }
  
  Point[] deepCopyPointArray(Point[] points){
    Point[] returnArray = new Point[points.length];
    for(int i = 0; i < points.length; i++){
      returnArray[i] = new Point(points[i]);
    }
    return returnArray;
  }
  


}
