package offset.sim;

import offset.sim.Pair;

public class Pair {
    public int p;
    public int q;

    public Pair() { p = 0; q = 0; }

    public Pair(int pp, int qq) {
        p = pp;
        q = qq;
    }

    public Pair(Pair o) {
        this.p = o.p;
        this.q = o.q;
    }

    public boolean equals(Pair o) {
        return o.p == p && o.q == q;
    }
}