package offset.oct6_group4;

import java.util.*;

public class Move {
    public Coord src;
    public Coord target;
    public int playerId;

    public Move(int xSrc, int ySrc, int xTarget, int yTarget, int playerId) {
        this.src = new Coord(xSrc, ySrc);
        this.target = new Coord(xTarget, yTarget);
        this.playerId = playerId;
    }
    
    public Move(Coord src, Coord target, int playerId) {
        this.src = new Coord(src);
        this.target = new Coord(target);
        this.playerId = playerId;
    }
    
	@Override public String toString() {
		return new String("(p" + this.playerId + " " + this.src + "->" + this.target + ")");
	}
}
