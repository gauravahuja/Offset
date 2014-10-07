package offset.group9;

import offset.sim.*;

public class Position {
    public int x;
    public int y;

    public Position(int xx, int yy) {
        x = xx;
        y = yy;
    }

    public Position(Position o) {
        this.x = o.x;
        this.y = o.y;
    }

    public Position(Point o) {
        this.x = o.x;
        this.y = o.y;
    }

    public boolean equals(Position o) {
        return (o.x == x && o.y == y);
    }

    public int hashCode() {
        return x * 31^2 + y * 31;
    }
}