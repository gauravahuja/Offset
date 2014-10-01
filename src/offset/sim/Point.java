package offset.sim;

import offset.sim.Point;

public class Point {
    public int x;
    public int y;
    public int value;
    public int owner;
    public boolean change;

    public Point() { x = 0; y = 0; value =1; owner = -1;}

    public Point(int xx, int yy, int va, int ow) {
        x = xx;
        y = yy;
        this.value = va;
        this.owner = ow;
        this.change = false;
    }

  /*  public Point(Point o) {
        this.x = o.x;
        this.y = o.y;
    }*/

  /*  public boolean equals(Point o) {
        return o.x == x && o.y == y;
    }*/
}