package offset.oct8_group4;

import java.util.*;

public class Coord {
	public int x;
	public int y;
	
	public Coord(Coord c) {
		this.x = c.x;
		this.y = c.y;
	}
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Coord c) {
		return c.x == this.x && c.y == this.y;
	}
	
	@Override public String toString() {
		return new String("(" + this.x + "," + this.y + ")");
	}
}
