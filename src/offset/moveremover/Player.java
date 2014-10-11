package offset.moveremover;
import offset.common.GridGraph;
import offset.common.GridPlayer;

import java.util.*;
import offset.sim.Pair;
import offset.sim.Point;
import offset.sim.movePair;

public class Player extends offset.common.GridPlayer {
    public Player(Pair prin, int idin) { super(prin, idin); }
    @Override
    protected movePair chooseMove() {
        movePair result = new movePair();

        int most_edges = 0;
        for (Map.Entry<Point, HashSet<Point>> advEntry : advGridGraph.edgesByPoint.entrySet()) {
            Point src = advEntry.getKey();
            HashSet<Point> advTargets = advEntry.getValue();

            Point myPoint = myGridGraph.getGraphGridPoint(src.x, src.y);
            if (advTargets.size() > most_edges && 
                myGridGraph.edgesByPoint.containsKey(myPoint) &&
                myGridGraph.edgesByPoint.get(myPoint).iterator().hasNext()) {
                most_edges = advTargets.size();

                HashSet<Point> myTargets = myGridGraph.edgesByPoint.get(myPoint);

                Point newTarget = myTargets.iterator().next();

                result.move = true;
                result.src = new Point(myPoint);
                result.target = new Point(myTargets.iterator().next());
            }
        }

        return result;
    }
}
