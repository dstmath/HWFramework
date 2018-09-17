package android.filterfw.core;

import java.util.Random;
import java.util.Vector;

public class RandomScheduler extends Scheduler {
    private Random mRand = new Random();

    public RandomScheduler(FilterGraph graph) {
        super(graph);
    }

    public void reset() {
    }

    public Filter scheduleNextNode() {
        Vector<Filter> candidates = new Vector();
        for (Filter filter : getGraph().getFilters()) {
            if (filter.canProcess()) {
                candidates.add(filter);
            }
        }
        if (candidates.size() > 0) {
            return (Filter) candidates.elementAt(this.mRand.nextInt(candidates.size()));
        }
        return null;
    }
}
