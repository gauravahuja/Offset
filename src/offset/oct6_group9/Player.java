package offset.oct6_group9;

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

    int historySize = history.size();

    if (historySize > 1) {
      updatePositionsAfterMove((movePair) history.get(historySize - 2).get(1));
    }

    if (historySize > 0) {
      updatePositionsAfterMove((movePair) history.get(historySize - 1).get(1));
    }
    
    board = grid;
	
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
    int escapeScore = 0;

    for (Position vulnerablePosition : ourVulnerablePositions) {
      ArrayList<Position> escapeRoutes = positionsAtOffsetFromPosition(vulnerablePosition, ourPair);
      for (Position escapeRoute : escapeRoutes) {
        int score = getHealthScore(vulnerablePosition, escapeRoute);
        
        if (score > escapeScore) {
          escapeScore = score;
          bestEscape = escapeRoute;
          escapeOrigin = vulnerablePosition;
        }          
      }
    }

    Position attackOrigin = null;
    Position bestAttack = null;
    int attackScore = 0;

    for (movePair strikeAblePosition : strikeAblePositions) {
        int score = getHealthScore(new Position(strikeAblePosition.src), new Position(strikeAblePosition.target));
        score += strikeAblePosition.target.value;

        if (score > attackScore) {
          attackScore = score;
          bestAttack = new Position(strikeAblePosition.target);
          attackOrigin = new Position (strikeAblePosition.src);
        }
      }
    
    Position layupOrigin = null;
    Position bestLayup = null;
    int layupScore = 0;

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
    System.out.println("EscapeScore: " + escapeScore);
    
    if (attackScore >= escapeScore && attackScore >= layupScore && attackScore > 0) {
      return new movePair(true, getCurrentPointForPosition(attackOrigin), getCurrentPointForPosition(bestAttack));
    } else if (layupScore > escapeScore) {
      return new movePair(true, getCurrentPointForPosition(layupOrigin), getCurrentPointForPosition(bestLayup));
    } else if (escapeScore > 0) {
      return new movePair(true, getCurrentPointForPosition(escapeOrigin), getCurrentPointForPosition(bestEscape));
    } else {
      return combineRandom();
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
    
    int score = 0;

    Point heldPoint = getCurrentPointForPosition(heldPosition);
    Point possibleRoutePoint = getCurrentPointForPosition(possibleRoute);

    
    if (possibleRoutePoint.value == heldPoint.value) {
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
          if (theirPoint.value == ourPoint.value) {
            movePair mpair1 = new movePair(true, ourPoint, theirPoint);
            movePair mpair2 = new movePair(true, theirPoint, ourPoint);
            result.add(mpair1);
            result.add(mpair2);
          }
        }
      }

      for (Position theirOtherPosition : positionsAtOffsetFromPosition(theirPos, ourPair)) {
        Point theirPoint = getCurrentPointForPosition(theirPos);
        Point theirOtherPoint = getCurrentPointForPosition(theirOtherPosition);
        
        if (theirOtherPoint.value == theirPoint.value) {
          movePair mpair = new movePair(true, theirPoint, theirOtherPoint);
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
        if (atOffsetPoint.value == theirPoint.value / 2 && atOffsetPoint.owner != theirPlayerId) {
          for (Position nextLevel : positionsAtOffsetFromPosition(atOffset, ourPair)) {
            Point nextLevelPoint = getCurrentPointForPosition(nextLevel);
            if (nextLevelPoint.value == theirPoint.value / 2 && nextLevelPoint.owner != theirPlayerId) {
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
        if (theirOffsetAway.value == ourPoint.value) {
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

  void updatePositionsAfterMove(movePair mpair) {
    if (mpair.move) {
      Position src = new Position(mpair.src.x, mpair.src.y);
      Position dst = new Position(mpair.target.x, mpair.target.y);

      if (ourPositions.get(src) != null) {
        ourPositions.remove(src);
      } else if (theirPositions.get(src) != null) {
        theirPositions.remove(src);
      }

      if (mpair.target.owner == ourPlayerId) {
        ourPositions.put(dst, mpair.target);
      } else {
        theirPositions.put(dst, mpair.target);
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
